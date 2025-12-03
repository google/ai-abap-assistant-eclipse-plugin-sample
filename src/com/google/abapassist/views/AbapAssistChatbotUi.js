/**
 * @fileoverview This file contains the JavaScript code for the ABAP Assist
 * Chatbot UI. It handles user input, generating responses, and displaying
 * feedback.
 */

/**
 * Sends a message to the chatbot.
 */
function sendMessage() {
  const messageInput = document.getElementById('message-input');
  if (messageInput.value) {
    processInput(messageInput.value, 'chatbot');
    messageInput.value = '';
  }
}

/**
 * Adds a message to the chat container.
 * @param {string} sender The sender of the message ('You' or 'ABAP Assist').
 */
function addMessage(sender) {
  const chatContainer = document.getElementById('chat-container');
  const abapAssist = document.createElement('div');
  abapAssist.className = 'left-box';
  abapAssist.innerHTML = '<h4 class="gradient-text">' + sender + ':</h4>';
  abapAssist.style.display = 'block';
  chatContainer.appendChild(abapAssist);
}

/**
 * Processes the input from the user.
 * @param {string} propmt The user's prompt.
 * @param {string} option The selected option.
 */
function processInput(propmt, option) {
  const model = document.getElementById('modelDropdown');
  const startupTiles = document.getElementById('startupTiles');
  startupTiles.style.display = 'none';
  addMessage('You');
  appendMessage('user', propmt);
  showLoadingdots();
  generateBotResponse(propmt, model.value, option);
}

/**
 * Removes all children from the chat container except the startup tiles.
 */
function newMessage() {
  removeAllChildrenExceptStartupTiles('chat-container', 'startupTiles');
}

function newConversation() {
  removeAllChildrenExceptStartupTiles('chat-container', 'startupTiles');
  callJavafunction('clearConvoId', 'CLEARCONVOID');
}

/**
 * Toggles the history.
 */
function toggleHistory() {
  document.getElementById('title').innerText = 'Modified in toggle';
}

/**
 * Appends a message to the chat container.
 * @param {string} sender The sender of the message ('user', 'bot', 'feedback').
 * @param {string} message The message content.
 */
function appendMessage(sender, message) {
  const chatContainer = document.getElementById('chat-container');
  const messageElement = document.createElement('div');
  if (sender != 'feedback') {
    messageElement.className = sender + '-message';
  }
  messageElement.innerHTML = message;
  chatContainer.appendChild(messageElement);
  chatContainer.scrollTop = chatContainer.scrollHeight;
}

/**
 * Generates a response from the chatbot.
 * @param {string} prompt The user's prompt.
 * @param {string} model The selected model.
 * @param {string} option The selected option.
 */
function generateBotResponse(prompt, model, option) {
  // call java and invoke service
  callJavafunction('callbackModelResponse', prompt, model, option);
}

/**
 * Callback function for the model response.
 * @param {string} Result The result from the model.
 */
function callbackModelResponse(Result) {
  console.log('entering call back response');
  console.log('decodedURICOMResult result:', Result);
  hideLoadingdots();
  appendMessage('bot', Result);
  addFeedbackbar('');
}

/**
 * Shows the loading dots.
 */
function showLoadingdots() {
  console.log('entering showloaddots');
  const chatContainer = document.getElementById('chat-container');

  addMessage('ABAP Assist');

  const loadingdots = document.createElement('div');
  loadingdots.setAttribute('id', 'loadingDots');
  loadingdots.className = 'loading-dots';
  loadingdots.innerHTML =
      '<div class ="row-box-normal"><span></span><span></span><span></span></div>';
  loadingdots.style.display = 'block';
  console.log('before append child');
  chatContainer.appendChild(loadingdots);
  chatContainer.scrollTop = chatContainer.scrollHeight;
  console.log('after append child');
}

/**
 * Hides the loading dots.
 */
function hideLoadingdots() {
  console.log('entering hideloaddots');
  const chatContainer = document.getElementById('chat-container');
  const loadingdots = document.getElementById('loadingDots');
  chatContainer.removeChild(loadingdots);
}

/**
 * Copies the code block with the given id.
 * @param {string} id The id of the code block to copy.
 */
function copy(id) {
  console.log('copied ' + id);

  callJavafunction('', 'COPY', String(parseInt(id)));
}

/**
 * Accepts the code block with the given id.
 * @param {string} id The id of the code block to accept.
 */
function accept(id) {
  console.log('accepted ' + id);
  callJavafunction('', 'ACCEPT', String(parseInt(id)));
}

/**
 * Performs a quick action based on the given code.
 * @param {string} qc The quick action code.
 */
function quickAction(qc) {
  switch (qc) {
    case 'EXPLAIN':
      processInput(
          'Explain the ABAP code for the code in the given context', 'explain');
      break;
    case 'REVIEW':
      processInput(
          'Perform ABAP code review for the code in the given context',
          'review');
      break;
    case 'SUGGEST':
      processInput(
          'Suggest improvement for the code in the given context', 'suggest');
      break;
    case 'AUT':
      processInput(
          'Generate ABAP unit test for the code in the given context', 'aut');
      break;
  }
}

/**
 * Removes all children from a parent element except for a specified child.
 * @param {string} parentElementId The ID of the parent element.
 * @param {string} exceptionChildId The ID of the child element to keep.
 */
function removeAllChildrenExceptStartupTiles(
    parentElementId, exceptionChildId) {
  const parentElement = document.getElementById(parentElementId);
  const exceptionChild = document.getElementById(exceptionChildId);

  if (!parentElement) {
    console.error(`Parent element with ID '${parentElementId}' not found.`);
    return;
  }

  if (!exceptionChild) {
    console.error(
        `Exception child element with ID '${exceptionChildId}' not found.`);
    return;
  }

  const children = parentElement.children;
  const childrenArray =
      Array.from(children);  // Convert HTMLCollection to array

  childrenArray.forEach(child => {
    if (child !== exceptionChild) {
      parentElement.removeChild(child);
    }
  });

  exceptionChild.style.display = 'block';
}

/**
 * Sets the value of the message input field.
 * @param {string} template The template to set as the input value.
 */
function setMessageInput(template) {
  document.getElementById('message-input').value = template;
}

/**
 * Loads a conversation from history.
 * @param {string} convoId The ID of the conversation to load.
 */
function loadHistoryitem(convoId) {
  callJavafunction('loadConversation', 'LOADHISTORY', convoId);
}

/**
 * Loads a conversation into the chat UI.
 * @param {!Array} chats An array of chat messages.
 */
function loadConversation(chats) {
  newMessage();
  const startupTiles = document.getElementById('startupTiles');
  startupTiles.style.display = 'none';
  console.log(chats);
  chats.forEach(item => {
    addMessage('You');
    appendMessage('user', item.requestText);
    addMessage('ABAP Assist');
    appendMessage('bot', item.responseText);
    addFeedbackbar(item.respId);
  });
}

/**
 * Adds a feedback bar to the chat UI.
 * @param {string} respId The ID of the response to provide feedback for.
 */
function addFeedbackbar(respId) {
	let feedback = "<br/><div class=\"like-dislike-container\"><button class=\"like-dislike-button\" id=\"likeButton\" onclick =\"like('" + respId + "');\">\n"
		+ "	<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"256\" height=\"256\" viewBox=\"0 0 256 256\" xml:space=\"preserve\">\n"
		+ "	<defs></defs><g style=\"stroke: none; stroke-width: 0; stroke-dasharray: none; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: none;\n"
		+ "	 fill-rule: nonzero; opacity: 1;\" transform=\"translate(1.4065934065934016 1.4065934065934016) scale(2.81 2.81)\" >\n"
		+ "	<circle cx=\"45\" cy=\"45\" r=\"45\" style=\"stroke: none; stroke-width: 1; stroke-dasharray: none; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; \n"
		+ "	fill: rgb(26,198,26); fill-rule: nonzero; opacity: 1;\" transform=\"  matrix(1 0 0 1 0 0) \"/>\n"
		+ "	<path d=\"M 20.142 66.312 h 10.208 c 0.795 0 1.44 -0.645 1.44 -1.44 V 37.665 c 0 -0.795 -0.645 -1.44 -1.44 -1.44 H 20.142 V 66.312 z\" style=\"stroke: none; \n"
		+ "	stroke-width: 1; stroke-dasharray: none; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: rgb(255,255,255); fill-rule: nonzero; opacity: 1;\"\n"
		+ "	transform=\" matrix(1 0 0 1 0 0) \" stroke-linecap=\"round\" />\n"
		+ "	<path d=\"M 66.094 43.729 c 2.079 0 3.764 -1.685 3.764 -3.764 c 0 -2.079 -1.685 -3.764 -3.764 -3.764 h -11.93 c 2.076 -3.739 2.139 -15.096 -2.787 -16.46 c -0.933\n"
		+ "	 -0.258 -1.859 0.454 -1.963 1.417 c -0.866 7.97 -5.742 17.877 -10.7 18.164 h -3.862 v 23.755 h 2.003 c 1.078 0 2.108 0.368 3.008 0.963 c 2.245 1.486 6.025 2.356 9.648 2.265\n"
		+ "	 h 1.752 v 0.006 h 12.036 c 2.079 0 3.764 -1.685 3.764 -3.764 s -1.685 -3.764 -3.764 -3.764 h 1.678 c 2.079 0 3.764 -1.685 3.764 -3.764 c 0 -2.079 -1.685 -3.764 -3.764 -3.764 h 1.118\n"
		+ "	 c 2.079 0 3.764 -1.685 3.764 -3.764 S 68.173 43.729 66.094 43.729 z\" style=\"stroke: none; stroke-width: 1; stroke-dasharray: none; stroke-linecap: butt; stroke-linejoin: \n"
		+ "	miter; stroke-miterlimit: 10; fill: rgb(255,255,255); fill-rule: nonzero; opacity: 1;\" transform=\" matrix(1 0 0 1 0 0) \" stroke-linecap=\"round\" />\n"
		+ "	</g></svg></button>\n"
		+ "	<button class=\"like-dislike-button\" id=\"dislikeButton\" onclick =\"dislike('" + respId + "');\" >\n"
		+ "	<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"256\" height=\"256\" viewBox=\"0 0 256 256\" xml:space=\"preserve\">\n"
		+ "	<defs></defs><g style=\"stroke: none; stroke-width: 0; stroke-dasharray: none; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: none; fill-rule:\n"
		+ "	nonzero; opacity: 1;\" transform=\"translate(1.4065934065934016 1.4065934065934016) scale(2.81 2.81)\" >\n"
		+ "	<circle cx=\"45\" cy=\"45\" r=\"45\" style=\"stroke: none; stroke-width: 1; stroke-dasharray: none; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill:\n"
		+ "	rgb(178,22,22); fill-rule: nonzero; opacity: 1;\" transform=\"  matrix(1 0 0 1 0 0) \"/><path d=\"M 20.1 27.7 h 10.2 c 0.8 0 1.4 0.6 1.4 1.4 v 27.2 c 0 0.8 -0.6 1.4 -1.4 1.4 H 20.1\n"
		+ "	V 27.7 z\" style=\"stroke: none; stroke-width: 1; stroke-dasharray: none; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: rgb(255,255,255); fill-rule: \n"
		+ "	nonzero; opacity: 1;\" transform=\" matrix(1 0 0 1 0 0) \" stroke-linecap=\"round\" />	<path d=\"M 66.1 50.3 c 2.1 0 3.8 1.7 3.8 3.8 s -1.7 3.8 -3.8 3.8 H 54.2 c 2.1 3.7 2.1 15.1\n"
		+ "	 -2.8 16.5 c -0.9 0.3 -1.9 -0.5 -2 -1.4 c -0.9 -8 -5.7 -17.9 -10.7 -18.2 h -3.9 V 30.9 h 2 c 1.1 0 2.1 -0.4 3 -1 c 2.2 -1.5 6 -2.4 9.6 -2.3 h 1.8 v 0 h 12\n"
		+ "	  c 2.1 0 3.8 1.7 3.8 3.8 s -1.7 3.8 -3.8 3.8 H 65 c 2.1 0 3.8 1.7 3.8 3.8 s -1.7 3.8 -3.8 3.8 h 1.1 c 2.1 0 3.8 1.7 3.8 3.8 S 68.2 50.3 66.1 50.3 z\" \n"
		+ "	 style=\"stroke: none; stroke-width: 1; stroke-dasharray: none; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: rgb(255,255,255); \n"
		+ "	 fill-rule: nonzero; opacity: 1;\" transform=\" matrix(1 0 0 1 0 0) \" stroke-linecap=\"round\" /></g></svg>\n"
		+ "	</button></div>";
	appendMessage('feedback', feedback);
}

/**
 * Sends a "like" feedback for a given response ID.
 * @param {string} Id The ID of the response to like.
 */
function like(Id){
	callJavafunction('', 'LIKE', Id);
}

/**
 * Sends a "dislike" feedback for a given response ID.
 * @param {string} Id The ID of the response to dislike.
 */
function dislike(Id){
	callJavafunction('', 'DISLIKE', Id);
}

document.addEventListener('DOMContentLoaded', function() {
  const navbar = document.querySelector('.navbar');
  const collapseBtn = document.querySelector('.collapse-btn');
  const expandBtn = document.querySelector('.expand-btn');

  collapseBtn.addEventListener('click', () => {
    navbar.classList.add('collapsed');
  });

  expandBtn.addEventListener('click', () => {
    navbar.classList.remove('collapsed');
  });
  const recentListItems = document.querySelectorAll('.recent-list-item');
  recentListItems.forEach(item => {
    item.addEventListener('click', function() {
      recentListItems.forEach(i => i.classList.remove('selected'));
      this.classList.add('selected');
    });
  });
});
