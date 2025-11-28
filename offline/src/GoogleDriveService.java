import com.rpkarongi.backup.GoogleDriveManager;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Service for uploading backup files to Google Drive
 */
public class GoogleDriveService {
    
    private static final String ROOT_FOLDER_NAME = "RP_Karongi_Backups";
    private GoogleDriveManager driveManager;
    private String rootFolderId;
    
    /**
     * Progress callback interface for upload operations
     */
    public interface Progress {
        void onProgress(int percent, String message);
        void onComplete(boolean success, String fileId, String message);
    }
    
    /**
     * Initialize the Google Drive service
     * @throws GeneralSecurityException if there's a security issue
     * @throws IOException if credentials file is missing or invalid
     */
    public GoogleDriveService() throws GeneralSecurityException, IOException {
        this.driveManager = new GoogleDriveManager();
        // Find or create the root backup folder
        this.rootFolderId = driveManager.findOrCreateFolder(ROOT_FOLDER_NAME, null);
    }
    
    /**
     * Upload a backup file to Google Drive
     * @param backupFile The local backup file to upload
     * @param department The department name (used for folder organization)
     * @param callback Progress callback (optional, can be null)
     * @return The Google Drive file ID of the uploaded file
     * @throws IOException if upload fails
     */
    public String uploadBackup(File backupFile, String department, Progress callback) throws IOException {
        if (!backupFile.exists()) {
            throw new IOException("Backup file does not exist: " + backupFile.getAbsolutePath());
        }
        
        try {
            // Report progress: Finding/creating department folder
            if (callback != null) {
                callback.onProgress(10, "Creating department folder in Google Drive...");
            }
            
            // Find or create department folder under root
            String departmentFolderId = driveManager.findOrCreateFolder(department, rootFolderId);
            
            // Report progress: Starting upload
            if (callback != null) {
                callback.onProgress(30, "Uploading " + backupFile.getName() + " to Google Drive...");
            }
            
            // Upload the file
            String fileId = driveManager.uploadFile(backupFile, departmentFolderId);
            
            // Report progress: Upload complete
            if (callback != null) {
                callback.onProgress(100, "Upload complete!");
                callback.onComplete(true, fileId, "Successfully uploaded to Google Drive");
            }
            
            return fileId;
            
        } catch (IOException e) {
            if (callback != null) {
                callback.onComplete(false, null, "Upload failed: " + e.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Test the Google Drive connection
     * @throws IOException if connection fails
     */
    public void testConnection() throws IOException {
        driveManager.testConnection();
    }
    
    /**
     * Get the web URL for viewing the backup folder
     * @param department The department name
     * @return URL to view the department's backup folder in Google Drive
     */
    public String getBackupFolderUrl(String department) {
        try {
            String departmentFolderId = driveManager.findOrCreateFolder(department, rootFolderId);
            return "https://drive.google.com/drive/folders/" + departmentFolderId;
        } catch (IOException e) {
            return "https://drive.google.com/drive/folders/" + rootFolderId;
        }
    }
}
