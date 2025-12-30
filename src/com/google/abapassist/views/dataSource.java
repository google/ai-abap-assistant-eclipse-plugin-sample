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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

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
		String username = null;

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

			JAXBContext jaxbContext = JAXBContext
					.newInstance(AdtConversation.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			data = (AdtConversation) unmarshaller
					.unmarshal(new StringReader(message));

			System.out.println("HTTP-status:"
					+ String.valueOf(restResponse.getStatus()));
			/*
			 * username = data.getUsername(); convoId = data.getConvoId();
			 * templates = data.getTemplates(); models = data.getmodels();
			 */
		} catch (ResourceNotFoundException e) {
			System.out.println("No  data found");
		} catch (RuntimeException e) {
			// Display any kind of other error
			System.out.println(e.getMessage());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

			JAXBContext jaxbContext = JAXBContext
					.newInstance(AdtConversation.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			data = (AdtConversation) unmarshaller
					.unmarshal(new StringReader(message));

			System.out.println("HTTP-status:"
					+ String.valueOf(restResponse.getStatus()));
		} catch (ResourceNotFoundException e) {
			System.out.println("No  data found");
		} catch (RuntimeException e) {
			// Display any kind of other errors
			System.out.println(e.getMessage());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

			JAXBContext jaxbContext = JAXBContext
					.newInstance(AdtConversation.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			data = (AdtConversation) unmarshaller
					.unmarshal(new StringReader(message));

			System.out.println("HTTP-status:"
					+ String.valueOf(restResponse.getStatus()));
		} catch (ResourceNotFoundException e) {
			System.out.println("No  data found");
		} catch (RuntimeException e) {
			// Display any kind of other errors
			System.out.println(e.getMessage());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return data;
	}
}
