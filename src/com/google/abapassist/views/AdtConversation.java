
package com.google.abapassist.views;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/** Java class representing the ADT_CONVERSATION XML element. */
@XmlRootElement(name = "ADT_CONVERSATION")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdtConversation {

  @XmlElement(name = "MODEL")
  private String model;

  @XmlElement(name = "CONVO_ID")
  private String conversationId;

  @XmlElement(name = "PROMPT")
  private String prompt;

  @XmlElement(name = "RESPONSE")
  private String response;

  @XmlElement(name = "ADDITIONAL_INFO")
  private String additionalInfo;

  @XmlElement(name = "USERNAME")
  private String username;

  @XmlElementWrapper(name = "TEMPLATES")
  @XmlElement(name = "TEMPLATE")
  private List<Template> templates;

  @XmlElementWrapper(name = "MODELS")
  @XmlElement(name = "MODEL")
  private List<Model> models;

  @XmlElementWrapper(name = "HISTORY")
  @XmlElement(name = "ITEM")
  private List<Item> history;

  public List<Item> getHistory() {
    return history;
  }

  public List<Chat> getConversation(String convoId) {
    List<Chat> chats = null;
    setConvoId(convoId);
    for (Item historyItem : history) {
      if (historyItem.conversationId != null && historyItem.conversationId.equals(convoId)) {
        chats = historyItem.getChats();
        break;
      }
    }
    return chats;
  }

  public void setHistory(List<Item> item) {
    this.history = item;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getConvoId() {
    return conversationId;
  }

  public void setConvoId(String convoId) {
    this.conversationId = convoId;
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<Template> getTemplates() {
    return templates;
  }

  public void setTemplates(List<Template> templates) {
    this.templates = templates;
  }

  public List<Model> getModels() {
    return models;
  }

  public void setModels(List<Model> models) {
    this.models = models;
  }

  /** Java class representing the TEMPLATE XML element. */
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Template {
    @XmlElement(name = "TEMPLATE_ID")
    private String templateId;

    @XmlElement(name = "DESCRIPTION")
    private String description;

    @XmlElement(name = "TEMPLATE")
    private String template;

    public String getTemplateId() {
      return templateId;
    }

    public void setTemplateId(String templateId) {
      this.templateId = templateId;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getTemplate() {
      return template;
    }

    public void setTemplate(String template) {
      this.template = template;
    }
  }

  /** Java class representing the MODEL XML element. */
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Model {
    @XmlElement(name = "MODEL_KEY")
    private String modelKey;

    @XmlElement(name = "MODEL_NAME")
    private String modelName;

    public String getModelKey() {
      return modelKey;
    }

    public void setModelKey(String modelKey) {
      this.modelKey = modelKey;
    }

    public String getModelName() {
      return modelName;
    }

    public void setModelName(String modelName) {
      this.modelName = modelName;
    }
  }

  /** Java class representing the CHAT XML element. */
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Chat {
    @XmlElement(name = "REQ_ID")
    private String requestId;

    @XmlElement(name = "REQUEST_STRING")
    private String requestString;

    @XmlElement(name = "REQUEST_AFTER_PREPROCESS")
    private String requestAfterPreprocess;

    @XmlElement(name = "REQ_TIMESTAMP")
    private String reqTimestamp;

    @XmlElement(name = "RESP_ID")
    private String respId;

    @XmlElement(name = "RESPONSE_STRING")
    private String responseString;

    @XmlElement(name = "RET_CODE")
    private String retCode;

    @XmlElement(name = "RET_TEXT")
    private String retText;

    @XmlElement(name = "RESP_TIMESTAMP")
    private String respTimestamp;

    public String getRequestText() {
      return requestString;
    }

    public String getResponseText() {
      return responseString;
    }

    public String getRespId() {
      return respId;
    }

    public void setResponseText(String text) {
      responseString = text;
    }

    public void setRequestString(String requestString) {
      this.requestString = requestString;
    }
  }

  /** Java class representing the ITEM XML element. */
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Item {
    @XmlElement(name = "CONVO_ID")
    private String conversationId;

    @XmlElement(name = "UNAME")
    private String userName;

    @XmlElement(name = "FIRST_RUN")
    private String firstRun;

    @XmlElementWrapper(name = "CHATS")
    @XmlElement(name = "CHAT")
    private List<Chat> chats;

    public List<Chat> getChats() {
      return chats;
    }

    public String getConvoId() {
      return conversationId;
    }
  }
}
