package com.google.abapassist.services;

import com.google.abapassist.views.AdtConversation;
import com.sap.adt.communication.message.IResponse;
import com.sap.adt.communication.resources.AdtRestResourceFactory;
import com.sap.adt.communication.resources.IRestResource;
import com.sap.adt.communication.resources.IRestResourceFactory;
import com.sap.adt.communication.resources.ResourceNotFoundException;
import com.sap.adt.destinations.ui.logon.AdtLogonServiceUIFactory;
import com.sap.adt.tools.core.project.AdtProjectServiceFactory;
import com.sap.adt.tools.core.project.IAbapProject;
import com.sap.adt.tools.core.project.IAbapProjectService;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.StringReader;

public class QuickAssistDataSource {
	
	public IRestResourceFactory restResourceFactory;
	private String destination;
	public String resourceUri;
	public IResponse restResponse;
	public String message;
	Map<String, String> codeBlocks;
	
	public QuickAssistDataSource() {

	    // rest call to sap
	    // Create resource factory
	    restResourceFactory = AdtRestResourceFactory.createRestResourceFactory();

	    // Get available projects in the workspace
	    IAbapProjectService psf = AdtProjectServiceFactory.createProjectService();

	    IAbapProject abapProject = psf.getAvailableAbapProjects()[0].getAdapter(IAbapProject.class);

	    // Trigger logon dialog if necessary
	    AdtLogonServiceUIFactory.createLogonServiceUI()
	        .ensureLoggedOn(
	            abapProject.getDestinationData(), PlatformUI.getWorkbench().getProgressService());
	    this.destination = abapProject.getDestinationId();
	}
	
	public String getProposals(String context) {
		
		String response = null;
		String prompt = "Suggest what lines of code should come next based on the given context";
		String option = "content";
		String convoId = null;

	    resourceUri = "/sap/bc/adt/yabapassist/adt_resource/contentAssistProposals?";

	    resourceUri =
	        resourceUri
	            + "model="
	            + URLEncoder.encode("gemini 1.5", StandardCharsets.UTF_8)
	            + '&'
	            + "prompt="
	            + URLEncoder.encode(prompt, StandardCharsets.UTF_8)
	            + '&'
	            + "context="
	            + URLEncoder.encode(context, StandardCharsets.UTF_8)
	            + '&'
	            + "option="
	            + option
	            + '&'
	            + "convo_id="
	            + convoId;
	    URI abapAssistUri = URI.create(resourceUri);
	    IRestResource abapAssistResource =
	        restResourceFactory.createResourceWithStatelessSession(abapAssistUri, destination);

	    try {
	      // Trigger GET request on resource data
	      restResponse = abapAssistResource.get(null, IResponse.class);
	      message = restResponse.getBody().toString();
	      
	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = factory.newDocumentBuilder();
          InputSource inputSource = new InputSource(new StringReader(message));
          Document document = builder.parse(inputSource);
          response = document.getElementsByTagName("RESPONSE").item(0).getTextContent();

	      System.out.println("HTTP-status:" + String.valueOf(restResponse.getStatus()));
	    } catch (ResourceNotFoundException e) {
	      System.out.println("No  data found");
	    } catch (RuntimeException e) {
	      // Display any kind of other errors
	      System.out.println(e.getMessage());
	    } catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    return response;
		
	}
	
	public String processResponse(String result) {
		String codeType = null;
		StringBuilder processed = new StringBuilder();
		Map<String, String> extractedcode = new HashMap<>();
		int id = 1;
		while (result != null && !result.isEmpty()) {
			int lvCodeEnd;
			boolean sqlFlag = false;
			int lvCodeStart = result.indexOf("```abap");

			codeType = "ABAP";
			if (lvCodeStart == -1) {
				lvCodeStart = result.indexOf("```sql");
				codeType = "SQL";
				sqlFlag = true;
			}

			if (lvCodeStart >= 0) {
				if (lvCodeStart > 0) {
					String lvExplanation = result.substring(0, lvCodeStart);
					if (lvExplanation != null && !lvExplanation.isEmpty()) {
						processed.append(lvExplanation);
					}
				}

				if (sqlFlag == true) {
					lvCodeEnd = result.indexOf("```", lvCodeStart + "```sql".length());
				} else {
					lvCodeEnd = result.indexOf("```", lvCodeStart + "```abap".length());
				}

				if (lvCodeEnd > 0) {
					int lvOffset = lvCodeStart + 11;
					int lvLength = lvCodeEnd - lvCodeStart - 11;
					String lvCode = result.substring(lvOffset, lvOffset + lvLength);
					extractedcode.put(String.valueOf(id), lvCode);
					processed.append(lvCode);

					result = result.substring(lvCodeEnd + 3);
				} else {
					processed.append("<p>Error: Unclosed code block found.</p>");
					break;
				}
			} else {
				processed.append(result);
				result = null;
				break;
			}
			id++;
		}
		this.codeBlocks = extractedcode;
		return processed.toString();
	}
	
}
