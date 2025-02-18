# minimi....

## Objective

Minimi is a Chrome extension built to help students quickly minimize long texts by summarizing content, generating study questions, and more. Its goal is to make studying more efficient and less overwhelming.

## Workflow Overview

1. **User Interaction:**  
   Students select text on a web page and click the "Summarize" button in the extension's side panel.

2. **Extension Processing:**  
   The extension extracts the selected text and sends it, along with the chosen operation (e.g., "summarize"), to the backend.

3. **Backend Processing:**  
   A Spring Boot backend receives the request, builds an appropriate prompt for the Gemini API, and returns a concise summary (or other processed output).

4. **Display & Notes:**  
   The extension displays the formatted output and offers a simple note-taking system for additional study aids.

## To run locally

### Spring Boot Backend

1. **Configure** your Gemini API settings in `application.properties`:
   ```properties
   gemini.api.url=https://your-gemini-api-url/
   gemini.api.key=your-api-key

2. Build and Run the backend:
  mvn clean install
  mvn spring-boot:run

Navigate to Chrome Extension, enable dev mode, and load unpacked. 

Demo video for those who are lazy: 
https://youtu.be/O8-1ILm75yM 
