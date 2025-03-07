package uk.ac.ucl.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/removeNote")
public class RemoveNoteServlet extends HttpServlet {

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
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Error: Note file not found.");
      return;
    }

    // Remove the note
    Iterator<JsonNode> iterator = notesNode.iterator();
    boolean noteFound = false;
    while (iterator.hasNext()) {
      JsonNode noteNode = iterator.next();
      if (noteNode.path("name").asText().equals(noteName)) {
        iterator.remove();
        noteFound = true;
        break;
      }
    }

    if (!noteFound) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Error: Note not found.");
      return;
    }

    // Write the updated JSON back to the file
    mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);

    // Delete the directory associated with the note
    String noteDirectoryPath = getServletContext().getRealPath("/NoteData/" + noteName);
    Path noteDirectory = Paths.get(noteDirectoryPath);
    if (Files.exists(noteDirectory)) {
      deleteDirectory(noteDirectory);
    }

    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write("Note removed successfully.");
  }

  private void deleteDirectory(Path directory) throws IOException {
    Files.walk(directory)
      .sorted((path1, path2) -> path2.compareTo(path1)) // Sort in reverse order to delete files before directories
      .forEach(path -> {
        try {
          Files.delete(path);
        } catch (IOException e) {
          throw new RuntimeException("Failed to delete " + path, e);
        }
      });
  }
}