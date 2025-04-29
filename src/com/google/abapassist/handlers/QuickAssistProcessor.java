package com.google.abapassist.handlers;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;

import com.google.abapassist.services.QuickAssistDataSource;
import com.google.abapassist.views.AbapSourceCodeEditor;

public class QuickAssistProcessor implements IQuickAssistProcessor {

	private QuickAssistDataSource quickAssistDataSource;
	private AbapSourceCodeEditor sourceEditor;
	private IDocument document;
	public int offset;
	private String selectedCode;

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
		CompletionProposal completionProposal = null;
		String option;
		String prompt;

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
			String proposal = quickAssistDataSource
					.processResponse(quickAssistDataSource
							.getProposals(selectedCode, option, prompt));

			completionProposal = new CompletionProposal(proposal, offset,
					0, proposal.length());

		} catch (Exception e) {
			LogUtil.writeToLog(e.toString(), "AbapAssist.log");
			e.printStackTrace();
		}

		return new ICompletionProposal[]{completionProposal};
	}

	@Override
	public String getErrorMessage() {
		return "Error during quick assist proposal generation.";
	}

}
