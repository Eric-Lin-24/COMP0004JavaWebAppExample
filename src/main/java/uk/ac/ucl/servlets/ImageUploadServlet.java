package uk.ac.ucl.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/uploadImage")
@MultipartConfig
public class ImageUploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart = request.getPart("file");
        String noteTitle = request.getParameter("noteTitle");

        if (filePart == null || filePart.getSubmittedFileName().isEmpty() || noteTitle == null || noteTitle.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: No file uploaded or note title missing.");
            return;
        }

        String fileName = filePart.getSubmittedFileName();
        String fileDirectory = getServletContext().getRealPath("/NoteData/" + noteTitle);

        // Create the directory if it doesn't exist
        File directory = new File(fileDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the uploaded file to the directory
        File file = new File(directory, fileName);
        Files.copy(filePart.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Image uploaded successfully.");
    }
}