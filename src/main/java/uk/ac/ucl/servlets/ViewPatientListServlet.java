package uk.ac.ucl.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/patientList.html")
public class ViewPatientListServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // Read the JSON file
    String jsonFilePath = getServletContext().getRealPath("/NoteData/NoteNames.json");
    File jsonFile = new File(jsonFilePath);
    List<JsonNode> notes = new ArrayList<>();

    if (jsonFile.exists()) {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(jsonFile);
      JsonNode notesNode = rootNode.path("notes");

      if (notesNode.isArray()) {
        for (JsonNode noteNode : notesNode) {
          notes.add(noteNode);
        }
      }
    }

    // Sort the notes based on the query parameter
    String sort = request.getParameter("sort");
    if ("alphabetical".equals(sort)) {
      notes.sort(Comparator.comparing(note -> note.path("name").asText()));
    } else if ("timeMade".equals(sort)) {
      notes.sort(Comparator.comparing(note -> note.path("timeMade").asText()));
    }

    // Extract note names after sorting
    List<String> noteNames = new ArrayList<>();
    for (JsonNode note : notes) {
      noteNames.add(note.path("name").asText());
    }

    // Add the data to the request object that will be sent to the Java Server Page, so that
    // the JSP can access the data (a Java data structure).
    request.setAttribute("noteNames", noteNames);

    // Invoke the JSP.
    // A JSP page is actually converted into a Java class, so behind the scenes everything is Java.
    ServletContext context = getServletContext();
    RequestDispatcher dispatch = context.getRequestDispatcher("/patientList.jsp");
    dispatch.forward(request, response);
  }
}