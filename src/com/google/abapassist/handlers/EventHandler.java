package com.google.abapassist.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class EventHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		LogUtil.writeToLog("Entering EventHandler->execute method", "AbapAssist.log");

		
		IWorkbenchPage page =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		 IEditorPart activeEditor = page.getActiveEditor();
		 System.out.println(activeEditor.toString());
				try {
					LogUtil.writeToLog("Trying to open Abapassist view", "AbapAssist.log");
					page.showView("com.abapassist.views.Chatbot");
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					LogUtil.writeToLog(e.toString(), "AbapAssist.log");
					e.printStackTrace();
				}

		 LogUtil.writeToLog("Leaving EventHandler->execute method", "AbapAssist.log");	
		return null;
	}
}
