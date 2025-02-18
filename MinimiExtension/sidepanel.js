document.addEventListener('DOMContentLoaded', () => {
    // Load the saved note text for the textarea (if you still need it)
    chrome.storage.local.get(['researchNotes'], function(result) {
      if (result.researchNotes) {
        document.getElementById('notesInput').value = result.researchNotes;
      }
    });
    
    // Attach event listeners for buttons.
    document.getElementById('summarize').addEventListener('click', summarizeText);
    document.getElementById('saveNote').addEventListener('click', saveNote);
    
    // Load the list of saved notes.
    loadNotes();
  });
  
  // Summarize function (unchanged)
  async function summarizeText() {
    try {
      // Get the active tab.
      const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
      
      // Execute a script in the active tab to get the selected text.
      const [{ result }] = await chrome.scripting.executeScript({
        target: { tabId: tab.id },
        function: () => window.getSelection().toString()
      });
      
      if (!result || result.trim() === '') {
        showResult('Please select some text first');
        return;
      }
      
      // Call the backend API with the selected text and 'summarize' operation.
      const response = await fetch('http://localhost:8080/api/research/process', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: result, operation: 'summarize' })
      });
      
      if (!response.ok) {
        throw new Error(`API ERROR: ${response.status}`);
      }
      
      const text = await response.text();
      showResult(text.replace(/\n/g, '<br>'));
    } catch (error) {
      showResult('Error: ' + error.message);
    }
    
    console.log("Summarize button clicked");
  }
  
  // Save a new note
  function saveNote() {
    const noteText = document.getElementById('notesInput').value;
    if (noteText.trim() === '') {
      alert('Please enter a note.');
      return;
    }
    
    chrome.storage.local.get(['notesList'], function(result) {
      // Get the current notes list or create an empty array if not available.
      let notes = result.notesList || [];
      notes.push(noteText);
      
      chrome.storage.local.set({ 'notesList': notes }, function() {
        document.getElementById('notesInput').value = '';
        loadNotes();
        alert("Note saved!");
      });
    });
  }
  
  // Load and render the list of saved notes
  function loadNotes() {
    chrome.storage.local.get(['notesList'], function(result) {
      const notes = result.notesList || [];
      const notesListContainer = document.getElementById('notesList');
      notesListContainer.innerHTML = ''; // Clear previous content
      
      notes.forEach((note, index) => {
        // Create a container for each note.
        const noteItem = document.createElement('div');
        noteItem.className = 'note-item';
        noteItem.innerHTML = `
          <div class="note-content">${note}</div>
          <button class="delete-note" data-index="${index}">Delete</button>
        `;
        notesListContainer.appendChild(noteItem);
      });
      
      // Attach event listeners to each delete button.
      document.querySelectorAll('.delete-note').forEach(button => {
        button.addEventListener('click', (e) => {
          const idx = e.target.getAttribute('data-index');
          deleteNote(idx);
        });
      });
    });
  }
  
  // Delete a note by its index
  function deleteNote(index) {
    chrome.storage.local.get(['notesList'], function(result) {
      let notes = result.notesList || [];
      notes.splice(index, 1);
      chrome.storage.local.set({ 'notesList': notes }, function() {
        loadNotes();
      });
    });
  }
  
  // Display result in the UI (for output from summarization)
  function showResult(content) {
    document.getElementById('results').innerHTML =
      `<div class="result-item"><div class="result-content">${content}</div></div>`;
  }
  