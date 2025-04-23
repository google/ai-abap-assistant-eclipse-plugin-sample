package com.google.abapassist.handlers;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.abapassist.services.QuickAssistDataSource;
import com.google.abapassist.views.AbapSourceCodeEditor;
import com.google.abapassist.views.AdtConversation;

public class QuickAssistProcessor implements IQuickAssistProcessor {
	
	public QuickAssistDataSource dataApi = null;
	public AbapSourceCodeEditor sourceEditor;
	public IDocument document;
	public int offset;
	public String selectedCode = null;
	
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
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext context) {
		
		sourceEditor = new AbapSourceCodeEditor().loadEditorAttributes();
		document = sourceEditor.getDocument();
		selectedCode = sourceEditor.getSelectedCode();
		offset = sourceEditor.getSelection().getOffset();
		int selectedCodelength = sourceEditor.getSelection().getLength();
	    if (selectedCodelength == 0) {
	    	String allCode = document.get();
	    	int cursor = context.getOffset();
	    	selectedCode = allCode.substring(0, cursor).concat(" {suggest} ").concat(allCode.substring(cursor, allCode.length() - cursor));
	    } else {
	    	
	    }
		
		dataApi = new QuickAssistDataSource();
		String proposal = dataApi.processResponse(dataApi.getProposals(selectedCode));
		
		CompletionProposal completionProposal = new CompletionProposal(proposal, offset, 0, proposal.length());

        return new ICompletionProposal[] { completionProposal };
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
