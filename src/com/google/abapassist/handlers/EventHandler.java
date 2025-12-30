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

		LogUtil.writeToLog("Entering EventHandler->execute method",
				"AbapAssist.log");

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart activeEditor = page.getActiveEditor();
		System.out.println(activeEditor.toString());
		try {
			LogUtil.writeToLog("Trying to open Abapassist view",
					"AbapAssist.log");
			page.showView("com.google.abapassist.views.Chatbot");
		} catch (PartInitException e) {
			LogUtil.writeToLog(e.toString(), "AbapAssist.log");
			e.printStackTrace();
		}

		LogUtil.writeToLog("Leaving EventHandler->execute method",
				"AbapAssist.log");
		return null;
	}
}
