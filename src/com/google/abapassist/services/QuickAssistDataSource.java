package com.google.abapassist.services;

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

import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class QuickAssistDataSource {

	private IRestResourceFactory restResourceFactory;
	private String destination;
	private String resourceUri;
	private IResponse restResponse;
	private String message;

	public QuickAssistDataSource() {

		// rest call to sap
		// Create resource factory
		restResourceFactory = AdtRestResourceFactory
				.createRestResourceFactory();

		// Get available projects in the workspace
		IAbapProjectService psf = AdtProjectServiceFactory
				.createProjectService();

		IAbapProject abapProject = psf.getAvailableAbapProjects()[0]
				.getAdapter(IAbapProject.class);

		// Trigger logon dialog if necessary
		AdtLogonServiceUIFactory.createLogonServiceUI().ensureLoggedOn(
				abapProject.getDestinationData(),
				PlatformUI.getWorkbench().getProgressService());
		this.destination = abapProject.getDestinationId();
	}

	public String getProposals(String context, String option, String prompt, String model) {

		String response = "";
		String convoId = "";

		resourceUri = "/sap/bc/adt/yabapassist/adt_resource/contentAssistProposals?";

		resourceUri = resourceUri + "model="
				+ model
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

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource inputSource = new InputSource(
					new StringReader(message));
			Document document = builder.parse(inputSource);
			response = document.getElementsByTagName("RESPONSE").item(0)
					.getTextContent();

			System.out.println("HTTP-status:"
					+ String.valueOf(restResponse.getStatus()));
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
		StringBuilder processed = new StringBuilder();
		Map<String, String> extractedcode = new HashMap<>();
		int id = 1;
		while (result != null && !result.isEmpty()) {
			int lvCodeEnd;
			boolean sqlFlag = false;
			int lvCodeStart = result.indexOf("```abap");
			
			if (lvCodeStart == -1) {
				lvCodeStart = result.indexOf("```sql");
				sqlFlag = true;
			}

			if (lvCodeStart >= 0) {
				if (lvCodeStart > 0) {
					String lvExplanation = result.substring(0,
							lvCodeStart);
					if (lvExplanation != null
							&& !lvExplanation.isEmpty()) {
						processed.append(lvExplanation);
					}
				}

				if (sqlFlag == true) {
					lvCodeEnd = result.indexOf("```",
							lvCodeStart + "```sql".length());
				} else {
					lvCodeEnd = result.indexOf("```",
							lvCodeStart + "```abap".length());
				}

				if (lvCodeEnd > 0) {
					int lvOffset = lvCodeStart + 8;
					int lvLength = lvCodeEnd - lvCodeStart - 9;
					String lvCode = result.substring(lvOffset,
							lvOffset + lvLength);
					extractedcode.put(String.valueOf(id), lvCode);
					processed.append(lvCode);

					result = result.substring(lvCodeEnd + 3);
				} else {
					processed.append(
							"<p>Error: Unclosed code block found.</p>");
					break;
				}
			} else {
				processed.append(result);
				result = null;
				break;
			}
			id++;
		}
		return processed.toString();
	}

}
