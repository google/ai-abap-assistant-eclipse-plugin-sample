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

import com.sap.adt.tools.abapsource.ui.sources.editors.IAbapSourcePage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/** Utility class to get information about the active ABAP source code editor. */
public class AbapSourceCodeEditor {

  private IDocument document;
  private ITextSelection selection;
  private IEditorPart activeEditor;

  /**
   * Loads the active editor attributes.
   *
   * @return this instance of {@link AbapSourceCodeEditor}
   */
  public AbapSourceCodeEditor loadEditorAttributes() {
    this.activeEditor =
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    return this;
  }

  /**
   * Gets the document of the active editor.
   *
   * @return the {@link IDocument} of the active editor
   */
  public IDocument getDocument() {
    IAdaptable adaptable = (IAdaptable) activeEditor;
    IAbapSourcePage abapSourcepage = (IAbapSourcePage) adaptable.getAdapter(IAbapSourcePage.class);
    document = abapSourcepage.getDocument();
    return document;
  }

  /**
   * Gets the selection of the active editor.
   *
   * @return the {@link ITextSelection} of the active editor
   */
  public ITextSelection getSelection() {
    selection = (ITextSelection) this.activeEditor.getSite().getSelectionProvider().getSelection();
    return selection;
  }

  /**
   * Gets the selected code from the active editor.
   *
   * @return the selected code as a {@link String}
   */
  public String getSelectedCode() {
    return getSelection().getText();
  }

  /**
   * Gets the active editor.
   *
   * @return the active {@link IEditorPart}
   */
  public IEditorPart getActiveEditor() {
    return this.activeEditor;
  }
}
