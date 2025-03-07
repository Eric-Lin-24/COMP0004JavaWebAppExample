package uk.ac.ucl.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/changeNoteTitle")
public class ChangeNoteTitleServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestBody = new String(request.getInputStream().readAllBytes());
        String oldNoteTitle = requestBody.split("\"oldNoteTitle\":\"")[1].split("\"")[0];
        String newNoteTitle = requestBody.split("\"newNoteTitle\":\"")[1].split("\"")[0];

        if (oldNoteTitle == null || oldNoteTitle.trim().isEmpty() || newNoteTitle == null || newNoteTitle.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: Note titles missing.");
            return;
        }

        // Update the directory name
        String oldDirectoryPath = getServletContext().getRealPath("/NoteData/" + oldNoteTitle);
        String newDirectoryPath = getServletContext().getRealPath("/NoteData/" + newNoteTitle);
        File oldDirectory = new File(oldDirectoryPath);
        File newDirectory = new File(newDirectoryPath);

        if (!oldDirectory.exists() || !oldDirectory.isDirectory()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Error: Old directory not found.");
            return;
        }

        if (newDirectory.exists()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("Error: New directory already exists.");
            return;
        }

        Files.move(oldDirectory.toPath(), newDirectory.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Rename the text file inside the directory
        File oldTextFile = new File(newDirectory, oldNoteTitle + ".txt");
        File newTextFile = new File(newDirectory, newNoteTitle + ".txt");
        if (oldTextFile.exists()) {
            Files.move(oldTextFile.toPath(), newTextFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Update NoteNames.json
        String jsonFilePath = getServletContext().getRealPath("/NoteData/NoteNames.json");
        File jsonFile = new File(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);
        ArrayNode notesNode = (ArrayNode) rootNode.path("notes");

        for (JsonNode noteNode : notesNode) {
            if (noteNode.path("name").asText().equals(oldNoteTitle)) {
                ((ObjectNode) noteNode).put("name", newNoteTitle);
                break;
            }
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Note title changed successfully.");
    }
}