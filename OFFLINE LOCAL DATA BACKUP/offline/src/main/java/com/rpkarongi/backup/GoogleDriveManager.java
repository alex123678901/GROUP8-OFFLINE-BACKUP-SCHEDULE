package com.rpkarongi.backup;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.nio.file.Files;
import com.google.api.services.drive.model.FileList;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class GoogleDriveManager {
    private static final String APPLICATION_NAME = "RP-Karongi-Backup";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String TOKENS_DIRECTORY = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    private final Drive service;

    public GoogleDriveManager() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets from file
        java.io.File credentialsFile = new java.io.File(CREDENTIALS_FILE_PATH);
        if (!credentialsFile.exists()) {
            throw new FileNotFoundException("Credentials file not found at: " + credentialsFile.getAbsolutePath());
        }
        
        GoogleClientSecrets clientSecrets;
        try (FileReader reader = new FileReader(credentialsFile)) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        }

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY)))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public String uploadFile(java.io.File file, String folderId) throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(file.getName());
        
        // Set parent folder if specified
        if (folderId != null && !folderId.isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(folderId));
        }

        // Get the MIME type of the file
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        FileContent mediaContent = new FileContent(mimeType, file);
        
        com.google.api.services.drive.model.File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id, name, webViewLink")
                .execute();
                
        return uploadedFile.getId();
    }

    public String createFolder(String folderName, String parentFolderId) throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        
        if (parentFolderId != null && !parentFolderId.isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        com.google.api.services.drive.model.File folder = service.files().create(fileMetadata)
                .setFields("id, name, webViewLink")
                .execute();
                
        return folder.getId();
    }

    public String findOrCreateFolder(String folderName, String parentFolderId) throws IOException {
        String query = "mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and trashed=false";
        if (parentFolderId != null && !parentFolderId.isEmpty()) {
            query += " and '" + parentFolderId + "' in parents";
        }
        
        com.google.api.services.drive.model.FileList result = service.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();
                
        if (!result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }
        
        // Folder doesn't exist, create it
        return createFolder(folderName, parentFolderId);
    }

    public void testConnection() throws IOException {
        // This will list the first 10 files in the user's Google Drive
        com.google.api.services.drive.model.FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<com.google.api.services.drive.model.File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Connected to Google Drive. Found " + files.size() + " files:");
            for (com.google.api.services.drive.model.File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }
}
