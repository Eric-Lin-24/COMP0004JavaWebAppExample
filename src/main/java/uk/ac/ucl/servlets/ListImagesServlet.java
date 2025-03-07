package uk.ac.ucl.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/listImages")
public class ListImagesServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String noteTitle = request.getParameter("noteTitle");

        if (noteTitle == null || noteTitle.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: Note title missing.");
            return;
        }

        String fileDirectory = getServletContext().getRealPath("/NoteData/" + noteTitle);
        File directory = new File(fileDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Error: Directory not found.");
            return;
        }

        File[] files = directory.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") || lowerName.endsWith(".gif");
        });

        List<String> imageFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                imageFiles.add("NoteData/" + noteTitle + "/" + file.getName());
            }
        }

        // Manually build the JSON response
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < imageFiles.size(); i++) {
            jsonBuilder.append("\"").append(imageFiles.get(i)).append("\"");
            if (i < imageFiles.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");

        response.setContentType("application/json");
        response.getWriter().write(jsonBuilder.toString());
    }
}