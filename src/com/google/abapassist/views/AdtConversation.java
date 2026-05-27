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

import java.util.List;

/** Java class representing the ADT_CONVERSATION XML element. */
public class AdtConversation {

  private String model;
  private String conversationId;
  private String prompt;
  private String response;
  private String additionalInfo;
  private String username;
  private List<Template> templates;
  private List<Model> models;
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
  public static class Template {
    private String templateId;
    private String description;
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
  public static class Model {
    private String modelKey;
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
  public static class Chat {
    private String requestId;
    private String requestString;
    private String requestAfterPreprocess;
    private String reqTimestamp;
    private String respId;
    private String responseString;
    private String retCode;
    private String retText;
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

    public void setRequestId(String requestId) {
      this.requestId = requestId;
    }
    public void setRequestAfterPreprocess(String requestAfterPreprocess) {
      this.requestAfterPreprocess = requestAfterPreprocess;
    }
    public void setReqTimestamp(String reqTimestamp) {
      this.reqTimestamp = reqTimestamp;
    }
    public void setRespId(String respId) {
      this.respId = respId;
    }
    public void setResponseString(String responseString) {
      this.responseString = responseString;
    }
    public void setRetCode(String retCode) {
      this.retCode = retCode;
    }
    public void setRetText(String retText) {
      this.retText = retText;
    }
    public void setRespTimestamp(String respTimestamp) {
      this.respTimestamp = respTimestamp;
    }
  }

  /** Java class representing the ITEM XML element. */
  public static class Item {
    private String conversationId;
    private String userName;
    private String firstRun;
    private List<Chat> chats;

    public List<Chat> getChats() {
      return chats;
    }

    public String getConvoId() {
      return conversationId;
    }

    public void setConversationId(String conversationId) {
      this.conversationId = conversationId;
    }
    public void setUserName(String userName) {
      this.userName = userName;
    }
    public void setFirstRun(String firstRun) {
      this.firstRun = firstRun;
    }
    public void setChats(List<Chat> chats) {
      this.chats = chats;
    }
  }
}
