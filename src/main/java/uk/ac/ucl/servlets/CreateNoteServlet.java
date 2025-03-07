package uk.ac.ucl.servlets;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/createNote")
public class CreateNoteServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String requestBody = new String(request.getInputStream().readAllBytes());
    String noteName = requestBody.split("\"noteName\":\"")[1].split("\"")[0];

    if (noteName == null || noteName.trim().isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Error: Note name is missing.");
      return;
    }

    // Read the JSON file
    String jsonFilePath = getServletContext().getRealPath("/NoteData/NoteNames.json");
    File jsonFile = new File(jsonFilePath);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode;
    ArrayNode notesNode;

    if (jsonFile.exists()) {
      rootNode = mapper.readTree(jsonFile);
      notesNode = (ArrayNode) rootNode.path("notes");
    } else {
      rootNode = mapper.createObjectNode();
      notesNode = mapper.createArrayNode();
      ((ObjectNode) rootNode).set("notes", notesNode);
    }

    // Check if the note already exists
    for (JsonNode noteNode : notesNode) {
      if (noteNode.path("name").asText().equals(noteName)) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        response.getWriter().write("Error: Note already exists.");
        return;
      }
    }

    // Add the new note
    ObjectNode newNoteNode = mapper.createObjectNode();
    newNoteNode.put("name", noteName);
    newNoteNode.put("timeMade", java.time.Instant.now().toString());
    notesNode.add(newNoteNode);

    // Write the updated JSON back to the file
    mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);

    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write("Note created successfully.");
  }
}