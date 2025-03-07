package uk.ac.ucl.servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/saveFile")
public class FileSaveServlet extends HttpServlet {
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileName = request.getParameter("file");

        if (fileName == null || fileName.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: No file name provided.");
            System.out.println("Error: No file name provided.");
            return;
        }

        // Determine the directory based on the note's name
        String noteName = fileName.split("\\.")[0]; // Assuming the note name is the part before the file extension
        System.out.println(noteName);
        String fileDirectory = getServletContext().getRealPath("/NoteData/" + noteName);

        // Create the directory if it doesn't exist
        File directory = new File(fileDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create the file within the directory
        File file = new File("src/main/webapp/NoteData/"+(String)noteName, fileName);

        System.out.println("Saving file to: " + file.getAbsolutePath());

        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(fileContent.toString());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("File saved successfully.");
            System.out.println("File saved successfully: " + file.getPath());
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to save file.");
            System.out.println("Failed to save file: " + e.getMessage());
        }
    }
}