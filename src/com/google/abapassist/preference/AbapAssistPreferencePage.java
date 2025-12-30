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
package com.google.abapassist.preference;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.abapassist.views.AdtConversation;
import com.google.abapassist.views.dataSource;

public class AbapAssistPreferencePage extends PreferencePage
		implements
			IWorkbenchPreferencePage {

	private Button enableQuickAssistCheckbox;
	private Combo aiModelDropdown;
	private String[] AI_MODELS; // Example models
	private Boolean noAIModelsFlag = false;
	public AbapAssistPreferencePage() {
		
		// No-arg constructor
		try {
			AdtConversation initInfo = new dataSource().init();
			if (initInfo != null) {
				List<AdtConversation.Model> models = initInfo.getModels();
				List<String> aiModelsList = new ArrayList<>();
				models.forEach(model -> {
					aiModelsList.add(model.getModelKey());
				});
				AI_MODELS = aiModelsList.toArray(new String[0]);
			} else {
				noAIModelsFlag = true;
			}
		} catch (Exception e) { 
			noAIModelsFlag = true;
		}
	}

	public AbapAssistPreferencePage(String title) {
		super(title);
	}

	public AbapAssistPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Configure ABAP Assistant settings.");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		// Quick Assist Checkbox
		enableQuickAssistCheckbox = new Button(container, SWT.CHECK);
		enableQuickAssistCheckbox.setText("Enable Quick Assist");
		enableQuickAssistCheckbox.setSelection(getPreferenceStore()
				.getBoolean(PreferenceConstants.P_ENABLE_QUICK_ASSIST));
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		enableQuickAssistCheckbox.setLayoutData(gd);

		// AI Model Dropdown
		if (!noAIModelsFlag) {
			Label aiModelLabel = new Label(container, SWT.NONE);
			aiModelLabel.setText("AI Model:");
			aiModelDropdown = new Combo(container, SWT.READ_ONLY);
			aiModelDropdown.setItems(AI_MODELS);
			String selectedModel = getPreferenceStore()
					.getString(PreferenceConstants.P_AI_MODEL);
			int selectionIndex = -1;
			for (int i = 0; i < AI_MODELS.length; i++) {
				if (AI_MODELS[i].equals(selectedModel)) {
					selectionIndex = i;
					break;
				}
			}
			if (selectionIndex != -1) {
				aiModelDropdown.select(selectionIndex);
			} else if (AI_MODELS.length > 0) {
				aiModelDropdown.select(0);
			}
			aiModelDropdown
					.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		return container;
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(
				PreferenceConstants.P_ENABLE_QUICK_ASSIST,
				enableQuickAssistCheckbox.getSelection());
		if (!noAIModelsFlag) {
			if (aiModelDropdown.getSelectionIndex() != -1) {
				getPreferenceStore().setValue(
						PreferenceConstants.P_AI_MODEL,
						aiModelDropdown.getItem(
								aiModelDropdown.getSelectionIndex()));
			}
		}
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		enableQuickAssistCheckbox
				.setSelection(getPreferenceStore().getDefaultBoolean(
						PreferenceConstants.P_ENABLE_QUICK_ASSIST));

		if (!noAIModelsFlag) {
			aiModelDropdown.setText(getPreferenceStore()
					.getDefaultString(PreferenceConstants.P_AI_MODEL));
		}
		super.performDefaults();
	}
	
	@Override
	protected void performApply() {
		super.performApply();
	}
}