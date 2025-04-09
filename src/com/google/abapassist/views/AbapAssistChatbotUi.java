package com.google.abapassist.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * The AbapAssistChatbotUi class provides a user interface for interacting with an ABAP chatbot. It
 * allows users to input prompts, view responses, and interact with code suggestions.
 */
public class AbapAssistChatbotUi extends ViewPart implements ISelectionListener {

  private static final String CSS_STYLE_PLACEHOLDER = "CSSSTYLE";
  private static final String JAVASCRIPT_PLACEHOLDER = "JAVASCRIPT";
  private static final String MODELS_PLACEHOLDER = "MODELS";
  private static final String TILES_PLACEHOLDER = "TILES";
  private static final String LINKS_PLACEHOLDER = "LINKS";
  private static final String WELCOME_MESSAGE = "Good morning";
  private static final String DELIMITER = "\r\n";
  private static final String COPY_PROMPT = "COPY";
  private static final String ACCEPT_PROMPT = "ACCEPT";
  private static final String LOADHISTORY_PROMPT = "LOADHISTORY";
  private static final String LIKE_PROMPT = "LIKE";
  private static final String DISLIKE_PROMPT = "DISLIKE";

  private static final String JS_FILE = "AbapAssistChatbotUi.js";
  private static final String CSS_FILE = "AbapAssistChatbotUi.css";
  private static final String UI_PAGE_FILE = "uipage.html";

  public dataSource dataapi = null;
  public String selectedCode = null;
  public ITextSelection selection;
  public ITextEditor textEditor;
  public IEditorPart activeEditor;
  public AbapSourceCodeEditor sourceEditor;
  public IDocument document;
  public int offset;
  public String convoId = "";
  public AdtConversation initInfo;

  @Inject Shell shell;

  private Browser uiBrowser;
  String blockid = null;

  @Override
  public void createPartControl(Composite parent) {
    sourceEditor = new AbapSourceCodeEditor().loadEditorAttributes();
    activeEditor = sourceEditor.getActiveEditor();

    uiBrowser = new Browser(parent, SWT.NONE);
    uiBrowser.setText(getContent());

    new ModelApi(uiBrowser, "callJavafunction", parent.getDisplay());

    getSite().getPage().addSelectionListener(this);
  }

  private class ModelApi extends BrowserFunction {
    // private Runnable function;
    Display display;
    Map<String, String> codeBlocks;
    public Object[] arguments;

    ModelApi(Browser browser, String name, Display display) {
      super(browser, name);
      this.display = display;
    }

    @Override
    public Object function(Object[] arguments) {

      this.arguments = arguments;

      sourceEditor = new AbapSourceCodeEditor().loadEditorAttributes();
      document = sourceEditor.getDocument();
      selectedCode = sourceEditor.getSelectedCode();
      offset = sourceEditor.getSelection().getOffset();
      int selectedCodelength = sourceEditor.getSelection().getLength();
      if (selectedCodelength == 0) {
        selectedCode = document.get();
      }

      dataapi = new dataSource();

      new Thread(
              () -> {
                // Get the callback function from the JavaScript arguments
                String resolveFunctionName = (String) this.arguments[0];
                String prompt = (String) this.arguments[1];

                switch (prompt) {
                  case COPY_PROMPT:
                    {
                      copyToClipboard(this.arguments);
                    }
                    break;
                  case ACCEPT_PROMPT:
                    {
                      String id = String.valueOf(this.arguments[2]);
                      String code = codeBlocks.get(id);
                      dataapi.insertCode(document, offset, code, DELIMITER);
                    }
                    break;
                  case LOADHISTORY_PROMPT:
                    {
                      loadHistory(this.arguments);
                    }
                  case LIKE_PROMPT:
                    {
                      String id = String.valueOf(this.arguments[2]);
                      dataapi.updateFeedback(id, "LIKE");
                    }
                  case DISLIKE_PROMPT:
                    {
                      String id = String.valueOf(this.arguments[2]);
                      dataapi.updateFeedback(id, "DISLIKE");
                    }
                    break;
                  default:
                    {
                      callModel(this.arguments);
                    }
                }
              })
          .start();
      return null;
    }

    private void copyToClipboard(Object[] arguments) {
      String id = String.valueOf(this.arguments[2]);
      System.out.println("Copy the code block with id" + id);
      String code = codeBlocks.get(id);
      StringSelection stringSelection = new StringSelection(code);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, null);
    }

    private void loadHistory(Object[] arguments) {
        String resolveFunctionName = (String) this.arguments[0];
      String convoId = String.valueOf(this.arguments[2]);
      List<AdtConversation.Chat> chats = initInfo.getConversation(convoId);

      chats.forEach(
          item -> {
            item.setResponseText(processResponse(item.getResponseText()));
          });

      String encodedResult = null;
      ObjectMapper om = new ObjectMapper();
      try {
        encodedResult = om.writeValueAsString(chats);
      } catch (Exception e) {
        e.printStackTrace();
      }

      final String finalresult = encodedResult;
      // Invoke the callback on the UI thread
      display.asyncExec(
          () -> {
            String jsCallback = resolveFunctionName + "(" + finalresult + ");";
            System.out.println("jscallback ==" + jsCallback);
            uiBrowser.execute(jsCallback);
          });
    }

    private void callModel(Object[] arguments) {
        String resolveFunctionName = (String) this.arguments[0];
        String prompt = (String) this.arguments[1];
      String model = this.arguments[2].toString();
      String option = this.arguments[3].toString();
      AdtConversation response =
          dataapi.getModelResponse(prompt, selectedCode, model, option, convoId);
      String result = processResponse(response.getResponse());
      String encodedResult = null;
      if (convoId != response.getConvoId()) {
    	  convoId = response.getConvoId();
      }

      String respId =
          '\'' + response.getHistory().getFirst().getChats().getFirst().getRespId() + '\'';

      ObjectMapper om = new ObjectMapper();
      try {
        encodedResult = om.writeValueAsString(result);
      } catch (Exception e) {
        e.printStackTrace();
      }

      final String finalresult = encodedResult;
      final String id = respId;
      // Invoke the callback on the UI thread
      display.asyncExec(
          () -> {
            String jsCallback = resolveFunctionName + "(" + finalresult + "," + id + ");";
            System.out.println("jscallback ==" + jsCallback);
            uiBrowser.execute(jsCallback);
          });
    }

    private String processResponse(String result) {
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
            int lvOffset = lvCodeStart + 7;
            int lvLength = lvCodeEnd - lvCodeStart - 7;
            String lvCode = result.substring(lvOffset, lvOffset + lvLength);
            extractedcode.put(String.valueOf(id), lvCode);
            processed.append(addCodebar(lvCode, String.valueOf(id)));

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

    private Object addCodebar(String lvCode, String id) {
      String codeToolbar =
          "<pre><code class=\"coding-block\"><div class=\"code-bar\"><span>ABAP</span>"
              + "<button class=\"rounded-button\" onclick =\"copy("
              + id
              + ");\">Copy</button>"
              + "<button class=\"rounded-button\" onclick =\"accept("
              + id
              + ");\">Accept</button>"
              + "</div>";

      lvCode = codeToolbar + lvCode + "</code></pre>";
      return lvCode;
    }
  }

  public String getContent() {
    String js = null, css = null, uiPage = null, templateTags = null;

    try {
      InputStream inputStream = getClass().getResourceAsStream(JS_FILE);
      js = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

      InputStream cssStream = getClass().getResourceAsStream(CSS_FILE);
      css = new String(cssStream.readAllBytes(), StandardCharsets.UTF_8);

      InputStream uiPageStream = getClass().getResourceAsStream(UI_PAGE_FILE);
      uiPage = new String(uiPageStream.readAllBytes(), StandardCharsets.UTF_8);

    } catch (IOException e) {
      e.printStackTrace();
    }

    initInfo = new dataSource().init();
    String username = initInfo.getUsername();
    List<AdtConversation.Model> models = initInfo.getModels();
    List<AdtConversation.Template> templates = initInfo.getTemplates();

    StringBuilder buffer = new StringBuilder();

    String updateUiPage = uiPage.replace(CSS_STYLE_PLACEHOLDER, "<style> " + css + "</style>");
    updateUiPage = updateUiPage.replace(JAVASCRIPT_PLACEHOLDER, "<script>" + js + "</script>");
    updateUiPage = updateUiPage.replace(WELCOME_MESSAGE, username);

    updateUiPage = replaceModelsPlaceholder(updateUiPage);
    updateUiPage = replaceTemplatesPlaceholder(updateUiPage);
    updateUiPage = replaceHistoryPlaceholder(updateUiPage);

    buffer.append(updateUiPage);
    System.out.println(updateUiPage);

    return buffer.toString();
  }

  private String replaceModelsPlaceholder(String uiPage) {
    List<AdtConversation.Model> models = initInfo.getModels();
    StringBuilder modelOptionsBuilder = new StringBuilder();
    models.forEach(
        model -> {
          modelOptionsBuilder
              .append("<option value=\"")
              .append(model.getModelKey())
              .append("\">")
              .append(model.getModelName())
              .append("</option>");
        });
    String modelOptions = modelOptionsBuilder.toString();
    return uiPage.replace(MODELS_PLACEHOLDER, modelOptions.toString());
  }

  private String replaceTemplatesPlaceholder(String uiPage) {
    List<AdtConversation.Template> templates = initInfo.getTemplates();
    StringBuilder templatesBuilder = new StringBuilder();
    templates.forEach(
        template -> {
          String templateText = template.getTemplate();
          String desc = template.getDescription();
          templatesBuilder
              .append("<button class=\"welcome-box\" onclick=\"setMessageInput('")
              .append(templateText)
              .append("\');\">")
              .append(desc)
              .append("</button>");
        });
    return uiPage.replace(TILES_PLACEHOLDER, templatesBuilder.toString());
  }

  private String replaceHistoryPlaceholder(String uiPage) {
    StringBuilder history = new StringBuilder();
    initInfo
        .getHistory()
        .forEach(
            record -> {
              record
                  .getChats()
                  .forEach(
                      item -> {
                        history
                            .append(
                                "<li><button class=\"recent-list-item\""
                                    + " onclick=\"loadHistoryitem('")
                            .append(record.getConvoId())
                            .append("\');\">")
                            .append(
                                item.getRequestText().length() <= 50
                                    ? item.getRequestText()
                                    : item.getRequestText().substring(0, 50))
                            .append("</button></li><br/>");
                        return;
                      });
            });
    return uiPage.replace(LINKS_PLACEHOLDER, history.toString());
  }

  @Override
  public void setFocus() {
    uiBrowser.setFocus();
  }

  @Override
  public void dispose() {
    getSite().getPage().removeSelectionListener(this);
    super.dispose();
  }

@Override
public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	// TODO Auto-generated method stub
	
}
}
