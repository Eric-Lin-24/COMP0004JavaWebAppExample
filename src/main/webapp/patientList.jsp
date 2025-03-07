<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/meta.jsp"/>
  <title>NOTED</title>
</head>
<body>
<jsp:include page="/header.jsp"/>
<div class="main">
  <h2>Notes:</h2>
  <div>
    <label for="sortOptions">Sort by:</label>
    <select id="sortOptions" onchange="sortNotes()">
      <option value="alphabetical">Alphabetical</option>
      <option value="timeMade">Time Made</option>
    </select>
  </div>
  <ul id="noteList">
    <%
      List<String> names = (List<String>) request.getAttribute("noteNames");
      for (String name : names) {
        String href = "dummypage.html?file=" + name + ".txt";
    %>
    <li id="<%=name%>">
      <a href="<%=href%>"><%=name%></a>
      <button onclick="removeNote('<%=name%>')">Remove</button>
    </li>
    <% } %>
  </ul>

  <h3>Create a New Note</h3>
  <form id="createNoteForm">
    <label for="noteName">Note Name:</label>
    <input type="text" id="noteName" name="noteName" required>
    <button type="submit">Create Note</button>
  </form>
</div>
<jsp:include page="/footer.jsp"/>

<script>
  document.getElementById('createNoteForm').addEventListener('submit', function(event) {
    event.preventDefault();
    var noteName = document.getElementById('noteName').value.trim();
    if (noteName) {
      fetch('/createNote', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ noteName: noteName })
      }).then(response => response.text())
        .then(result => {
          alert(result);
          if (result === "Note created successfully.") {
            var noteList = document.getElementById('noteList');
            var listItem = document.createElement('li');
            listItem.id = noteName;
            var link = document.createElement('a');
            link.href = 'dummypage.html?file=' + noteName + '.txt';
            link.textContent = noteName;
            var removeButton = document.createElement('button');
            removeButton.textContent = 'Remove';
            removeButton.onclick = function() { removeNote(noteName); };
            listItem.appendChild(link);
            listItem.appendChild(removeButton);
            noteList.appendChild(listItem);
            document.getElementById('noteName').value = ''; // Clear the input field
          }
        })
        .catch(error => {
          console.error('Error creating note:', error);
          alert('Error creating note: ' + error);
        });
    } else {
      alert('Please enter a note name.');
    }
  });

  function removeNote(noteName) {
    fetch('/removeNote', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ noteName: noteName })
    }).then(response => response.text())
      .then(result => {
        alert(result);
        if (result === "Note removed successfully.") {
          var noteList = document.getElementById('noteList');
          var listItem = document.getElementById(noteName);
          noteList.removeChild(listItem);
        }
      })
      .catch(error => {
        console.error('Error removing note:', error);
        alert('Error removing note: ' + error);
      });
  }

  function sortNotes() {
    var sortOption = document.getElementById('sortOptions').value;
    window.location.href = '/patientList.html?sort=' + sortOption;
  }
</script>

</body>
</html>