/*********************************************************************
*  Copyright 2025 Google LLC                                         *
*                                                                    *
*  Licensed under the Apache License, Version 2.0 (the "License");   *
*  you may not use this file except in compliance with the License.  *
*  You may obtain a copy of the License at                           *
*      https://www.apache.org/licenses/LICENSE-2.0                   *
*  Unless required by applicable law or agreed to in writing,        *
*  software distributed under the License is distributed on an       *
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,      *
*  either express or implied.                                        *
*  See the License for the specific language governing permissions   *
*  and limitations under the License.                                *
*********************************************************************/
package com.google.abapassist.views;

import com.sap.adt.communication.message.IResponse;
import com.sap.adt.communication.resources.AdtRestResourceFactory;
import com.sap.adt.communication.resources.IRestResource;
import com.sap.adt.communication.resources.IRestResourceFactory;
import com.sap.adt.communication.resources.ResourceNotFoundException;
import com.sap.adt.destinations.ui.logon.AdtLogonServiceUIFactory;
import com.sap.adt.tools.core.project.AdtProjectServiceFactory;
import com.sap.adt.tools.core.project.IAbapProject;
import com.sap.adt.tools.core.project.IAbapProjectService;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.IFileEditorInput;
import com.google.abapassist.handlers.LogUtil;
/**
 * Handles the data source for the ABAP assist plugin. It provides methods to
 * interact with the SAP backend and retrieve data.
 */
public class dataSource {

	public IEditorPart activeEditor;
	public String resourceUri;
	public String selectedContext;
	int lvLength, lvOffset, delimitLength;
	public IResponse restResponse;
	public String message, responsefrommodel;
	private String destination;
	public IRestResourceFactory restResourceFactory;
	public IDocument document;
	public ITextSelection selection;
	public ITextEditor textEditor;
	public IEditorInput einput;
	public ISelectionProvider selectionprovider;
	String eid;
	public String viewId;
	public String convoId;

	public AdtConversation AdtConversation;
	public AdtConversation.Template[] templates;
	public AdtConversation.Model[] models;
	private IAbapProject abapProject;

	public dataSource() {

		// rest call to sap
		// Create resource factory
		restResourceFactory = AdtRestResourceFactory
				.createRestResourceFactory();

		// Get available projects in the workspace
		// IAbapProjectService psf =
		// AdtProjectServiceFactory.createProjectService();

		// IAbapProject abapProject =
		// psf.getAvailableAbapProjects()[0].getAdapter(IAbapProject.class);
		IEditorPart activeEditor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();

		abapProject = null;
		if (activeEditor != null) {
			IEditorInput editorInput = activeEditor.getEditorInput();
			IProject project = null;

			// Check if the input is a standard workspace file
			if (editorInput instanceof IFileEditorInput) {
				project = ((IFileEditorInput) editorInput).getFile()
						.getProject();
			}

			IAbapProjectService psf = AdtProjectServiceFactory
					.createProjectService();
			IProject[] availableProjects = psf.getAvailableAbapProjects();
			if (availableProjects.length > 0) {
				abapProject = availableProjects[0]
						.getAdapter(IAbapProject.class);
				for (IProject proj : availableProjects) {
					if (proj.getName().equals(project.getName())) {
						abapProject = proj.getAdapter(IAbapProject.class);
						break;
					}
				}
			} else {
				throw new IllegalStateException(
						"Could not find an active ABAP project.");
			}

		}

		// Trigger logon dialog if necessary
		AdtLogonServiceUIFactory.createLogonServiceUI().ensureLoggedOn(
				abapProject.getDestinationData(),
				PlatformUI.getWorkbench().getProgressService());
		this.destination = abapProject.getDestinationId();
	}

	public void insertCode(IDocument editor, int offset, String code,
			String delimiter) {

		try {
			code = delimiter + code + delimiter;
			editor.replace(offset, 0, code);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getContext() {
		return selectedContext;
	}

	public AdtConversation init() {
		AdtConversation data = null;

		resourceUri = "/sap/bc/adt/zabapassist/adt_resource/conversations?";
		resourceUri = resourceUri + "userinfo=x";
		URI abapAssistUri = URI.create(resourceUri);
		IRestResource abapAssistResource = restResourceFactory
				.createResourceWithStatelessSession(abapAssistUri,
						destination);

		try {
			// Trigger GET request on resource data
			restResponse = abapAssistResource.get(null, IResponse.class);
			message = restResponse.getBody().toString();

			data = parseXml(message);

			System.out.println("HTTP-status:"
					+ String.valueOf(restResponse.getStatus()));
		} catch (ResourceNotFoundException e) {
			LogUtil.writeToLog(" ResourceNotFoundException in init: " + e.getMessage(), "abapassist_error.log");
			e.printStackTrace();
			throw new RuntimeException("Resource not found", e);
		} catch (Throwable t) {
			LogUtil.writeToLog(" Throwable in init: " + t.getMessage(), "abapassist_error.log");
			java.io.StringWriter sw = new java.io.StringWriter();
			java.io.PrintWriter pw = new java.io.PrintWriter(sw);
			t.printStackTrace(pw);
			LogUtil.writeToLog(sw.toString(), "abapassist_error.log");
			t.printStackTrace();
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			throw new RuntimeException(t);
		}
		return data;
	}

	public AdtConversation getModelResponse(String prompt, String context,
			String model, String option, String convoId) {

		AdtConversation data = null;

		resourceUri = "/sap/bc/adt/zabapassist/adt_resource/conversations?";

		resourceUri = resourceUri + "model="
				+ URLEncoder.encode(model.substring(7),
						StandardCharsets.UTF_8)
				+ '&' + "prompt="
				+ URLEncoder.encode(prompt, StandardCharsets.UTF_8) + '&'
				+ "context="
				+ URLEncoder.encode(context, StandardCharsets.UTF_8) + '&'
				+ "option=" + option + '&' + "convo_id=" + convoId;
		URI abapAssistUri = URI.create(resourceUri);
		IRestResource abapAssistResource = restResourceFactory
				.createResourceWithStatelessSession(abapAssistUri,
						destination);

		try {
			// Trigger GET request on resource data
			restResponse = abapAssistResource.get(null, IResponse.class);
			message = restResponse.getBody().toString();

			data = parseXml(message);

			System.out.println("HTTP-status:"
					+ String.valueOf(restResponse.getStatus()));
		} catch (ResourceNotFoundException e) {
			System.out.println("No  data found");
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			t.printStackTrace();
		}

		return data;
	}

	public AdtConversation updateFeedback(String respId,
			String feedBackType) {

		AdtConversation data = null;

		String resourceUri = "";

		resourceUri = "/sap/bc/adt/zabapassist/adt_resource/conversations?";

		if (feedBackType == "LIKE") {
			resourceUri = resourceUri + "like=" + respId;
		} else if (feedBackType == "DISLIKE") {
			resourceUri = resourceUri + "dislike=" + respId;
		}

		URI abapAssistUri = URI.create(resourceUri);
		IRestResource abapAssistResource = restResourceFactory
				.createResourceWithStatelessSession(abapAssistUri,
						destination);

		try {
			// Trigger GET request on resource data
			restResponse = abapAssistResource.get(null, IResponse.class);
			message = restResponse.getBody().toString();

			data = parseXml(message);

			System.out.println("HTTP-status:"
					+ String.valueOf(restResponse.getStatus()));
		} catch (ResourceNotFoundException e) {
			System.out.println("No  data found");
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			t.printStackTrace();
		}

		return data;
	}

	private AdtConversation parseXml(String xmlString) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
		doc.getDocumentElement().normalize();

		Element root = doc.getDocumentElement();
		if (!root.getNodeName().equals("ADT_CONVERSATION")) {
			throw new IllegalArgumentException("Root element is not ADT_CONVERSATION");
		}

		AdtConversation convo = new AdtConversation();
		convo.setModel(getDirectChildValue("MODEL", root));
		convo.setConvoId(getDirectChildValue("CONVO_ID", root));
		convo.setPrompt(getDirectChildValue("PROMPT", root));
		convo.setResponse(getDirectChildValue("RESPONSE", root));
		convo.setAdditionalInfo(getDirectChildValue("ADDITIONAL_INFO", root));
		convo.setUsername(getDirectChildValue("USERNAME", root));

		// Parse TEMPLATES
		Element templatesElem = getDirectChildElement("TEMPLATES", root);
		if (templatesElem != null) {
			List<Element> templateNodes = getDirectChildElements("TEMPLATE", templatesElem);
			List<AdtConversation.Template> templates = new java.util.ArrayList<>();
			for (Element tElem : templateNodes) {
				AdtConversation.Template t = new AdtConversation.Template();
				t.setTemplateId(getDirectChildValue("TEMPLATE_ID", tElem));
				t.setDescription(getDirectChildValue("DESCRIPTION", tElem));
				t.setTemplate(getDirectChildValue("TEMPLATE", tElem));
				templates.add(t);
			}
			convo.setTemplates(templates);
		}

		// Parse MODELS
		Element modelsElem = getDirectChildElement("MODELS", root);
		if (modelsElem != null) {
			List<Element> modelNodes = getDirectChildElements("MODEL", modelsElem);
			List<AdtConversation.Model> models = new java.util.ArrayList<>();
			for (Element mElem : modelNodes) {
				AdtConversation.Model m = new AdtConversation.Model();
				m.setModelKey(getDirectChildValue("MODEL_KEY", mElem));
				m.setModelName(getDirectChildValue("MODEL_NAME", mElem));
				models.add(m);
			}
			convo.setModels(models);
		}

		// Parse HISTORY
		Element historyElem = getDirectChildElement("HISTORY", root);
		if (historyElem != null) {
			List<Element> itemNodes = getDirectChildElements("ITEM", historyElem);
			List<AdtConversation.Item> history = new java.util.ArrayList<>();
			for (Element iElem : itemNodes) {
				AdtConversation.Item item = new AdtConversation.Item();
				item.setConversationId(getDirectChildValue("CONVO_ID", iElem));
				item.setUserName(getDirectChildValue("UNAME", iElem));
				item.setFirstRun(getDirectChildValue("FIRST_RUN", iElem));

				// Parse CHATS inside ITEM
				Element chatsElem = getDirectChildElement("CHATS", iElem);
				if (chatsElem != null) {
					List<Element> chatNodes = getDirectChildElements("CHAT", chatsElem);
					List<AdtConversation.Chat> chats = new java.util.ArrayList<>();
					for (Element cElem : chatNodes) {
						AdtConversation.Chat chat = new AdtConversation.Chat();
						chat.setRequestId(getDirectChildValue("REQ_ID", cElem));
						chat.setRequestString(getDirectChildValue("REQUEST_STRING", cElem));
						chat.setRequestAfterPreprocess(getDirectChildValue("REQUEST_AFTER_PREPROCESS", cElem));
						chat.setReqTimestamp(getDirectChildValue("REQ_TIMESTAMP", cElem));
						chat.setRespId(getDirectChildValue("RESP_ID", cElem));
						chat.setResponseString(getDirectChildValue("RESPONSE_STRING", cElem));
						chat.setRetCode(getDirectChildValue("RET_CODE", cElem));
						chat.setRetText(getDirectChildValue("RET_TEXT", cElem));
						chat.setRespTimestamp(getDirectChildValue("RESP_TIMESTAMP", cElem));
						chats.add(chat);
					}
					item.setChats(chats);
				}
				history.add(item);
			}
			convo.setHistory(history);
		}

		return convo;
	}

	private String getDirectChildValue(String tag, Element element) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tag)) {
				return child.getTextContent();
			}
		}
		return null;
	}

	private Element getDirectChildElement(String tag, Element element) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tag)) {
				return (Element) child;
			}
		}
		return null;
	}

	private List<Element> getDirectChildElements(String tag, Element element) {
		List<Element> list = new java.util.ArrayList<>();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tag)) {
				list.add((Element) child);
			}
		}
		return list;
	}
}
