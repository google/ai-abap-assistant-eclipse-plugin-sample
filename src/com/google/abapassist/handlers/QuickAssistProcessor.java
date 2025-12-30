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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;

import com.google.abapassist.services.QuickAssistDataSource;
import com.google.abapassist.views.AbapSourceCodeEditor;
import com.google.abapassist.services.QuickFixProposal;
import com.google.abapassist.preference.Activator;
import com.google.abapassist.preference.PreferenceConstants;

public class QuickAssistProcessor implements IQuickAssistProcessor {

	private QuickAssistDataSource quickAssistDataSource;
	private AbapSourceCodeEditor sourceEditor;
	private IDocument document;
	public int offset;
	private String selectedCode;
	public String model;
	private QuickFixProposal completionProposal;

	@Override
	public boolean canAssist(IQuickAssistInvocationContext arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canFix(Annotation arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ICompletionProposal[] computeQuickAssistProposals(
			IQuickAssistInvocationContext context) {
		QuickFixProposal completionProposal = null;
		String option;
		String prompt;

		model = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_AI_MODEL);
		Boolean isQuickAssistEnabled = Activator.getDefault()
				.getPreferenceStore()
				.getBoolean(PreferenceConstants.P_ENABLE_QUICK_ASSIST);

		if (isQuickAssistEnabled) {

			try {
				sourceEditor = new AbapSourceCodeEditor()
						.loadEditorAttributes();
				document = sourceEditor.getDocument();
				selectedCode = sourceEditor.getSelectedCode();
				offset = sourceEditor.getSelection().getOffset();
				String allCode = document.get();
				int cursor = context.getOffset();
				int selectedCodelength = sourceEditor.getSelection()
						.getLength();
				if (selectedCodelength == 0) {
					option = "content";
					prompt = "Suggest what lines of code should come next based on the given context";

					selectedCode = allCode.substring(0, cursor)
							.concat(" {suggest} ").concat(allCode
									.substring(cursor, allCode.length()));

				} else {
					option = "eclipseRefactor";
					prompt = "Refactor this code within the given context";
					selectedCode = allCode.substring(0, cursor)
							.concat(" {refactor} ").concat(selectedCode)
							.concat(" {refactor} ")
							.concat(allCode.substring(
									cursor + selectedCodelength,
									allCode.length()));
				}

				quickAssistDataSource = new QuickAssistDataSource();
				String proposal = quickAssistDataSource.processResponse(
						quickAssistDataSource.getProposals(selectedCode,
								option, prompt, model));

				completionProposal = new QuickFixProposal(proposal, offset,
						0, proposal.length(), null, "ABAP Assist", null,
						proposal, false);

			} catch (Exception e) {
				LogUtil.writeToLog(e.toString(), "AbapAssist.log");
				e.printStackTrace();
			}
		}
		return completionProposal != null
				? new ICompletionProposal[]{completionProposal}
				: new ICompletionProposal[0];
	}

	@Override
	public String getErrorMessage() {
		return "Error during quick assist proposal generation.";
	}

}
