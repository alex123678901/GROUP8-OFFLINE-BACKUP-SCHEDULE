import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupExecutor {

    public static void performBackup(BackupSchedule schedule, String department, String performedBy, LocalBackupService.Progress uiCallback) {
        performBackupInternal(schedule, department, performedBy, uiCallback, "SCHEDULED", "Scheduled");
    }

    public static void performManualBackup(BackupSchedule schedule, String department, String performedBy, LocalBackupService.Progress uiCallback) {
        performBackupInternal(schedule, department, performedBy, uiCallback, "MANUAL", "Manual");
    }

    private static void performBackupInternal(BackupSchedule schedule, String department, String performedBy, LocalBackupService.Progress uiCallback, String backupType, String taskType) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupName = schedule.getName() + "_" + timestamp;

        // 1. Log start in backup_logs
        BackupLog startLog = new BackupLog(
            schedule.getName(),
            backupType,
            "IN PROGRESS",
            "Starting backup...",
            performedBy,
            department
        );
        int startLogId = BackupLogDAO.addLog(startLog);

        // 2. Create task in backup_tasks
        long userId = BackupTaskDAO.getUserIdByUsername(performedBy);
        int taskId = -1;
        
        if (userId > 0) {
            taskId = BackupTaskDAO.createTask(userId, 0, taskType, "Running");
        } else {
            System.err.println("ERROR: User '" + performedBy + "' not found in database. Cannot create backup task.");
            // We can still proceed with backup, but we won't be able to log to backup_tasks/files
        }

        try {
            // Perform Local Backup
            int finalTaskId = taskId; // For use in lambda
            LocalBackupService.runBackup(
                backupName,
                schedule.getSource(),
                schedule.getDestination(),
                department,
                new LocalBackupService.Progress() {
                    @Override
                    public void onProgress(int percent, String message) {
                        if (uiCallback != null) uiCallback.onProgress(percent, message);
                    }

                    @Override
                    public void onDone(boolean ok, String details) {
                        if (ok) {
                            // Local backup success
                            handleLocalSuccess(schedule, department, performedBy, backupName, finalTaskId, backupType, startLogId);
                            // Log file details (simplified)
                            if (finalTaskId > 0) {
                                File src = new File(schedule.getSource());
                                BackupTaskDAO.addBackupFile(finalTaskId, src.getName(), schedule.getDestination(), src.length(), "N/A");
                            }
                        } else {
                            // Local backup failed
                            logFailure(schedule, department, performedBy, "Local backup failed: " + details, finalTaskId, backupType, startLogId);
                        }
                        if (uiCallback != null) uiCallback.onDone(ok, details);
                    }
                }
            );

        } catch (Exception e) {
            e.printStackTrace();
            logFailure(schedule, department, performedBy, "Backup execution error: " + e.getMessage(), taskId, backupType, startLogId);
        }
    }


    private static void handleLocalSuccess(BackupSchedule schedule, String department, String performedBy, String backupName, int taskId, String backupType, int startLogId) {
        // Update task status if valid
        if (taskId > 0) {
            BackupTaskDAO.updateTaskStatus(taskId, "Completed", "Backup completed successfully");
        }

        // Delete the IN PROGRESS log
        BackupLogDAO.deleteLog(startLogId);

        // If online backup is enabled, upload to Google Drive
        if (schedule.isOnlineBackup()) {
            try {
                // Log cloud backup start
                int cloudLogId = BackupLogDAO.addLog(new BackupLog(
                    schedule.getName(),
                    "CLOUD",
                    "IN PROGRESS",
                    "Uploading to Google Drive...",
                    performedBy,
                    department
                ));
                
                // Get the backup file
                File backupFile = new File(schedule.getDestination(), backupName + ".zip");
                if (!backupFile.exists()) {
                    // Try without .zip extension
                    backupFile = new File(schedule.getDestination());
                }
                
                if (backupFile.exists()) {
                    // Initialize Google Drive service
                    GoogleDriveService driveService = new GoogleDriveService();
                    
                    // Upload the file
                    String fileId = driveService.uploadBackup(backupFile, department, null);
                    String fileUrl = "https://drive.google.com/file/d/" + fileId + "/view";
                    
                    // Log to cloud_uploads table
                    if (taskId > 0) {
                        BackupTaskDAO.logCloudUpload(taskId, "Google Drive", fileId, fileUrl, backupFile.length());
                    }
                    
                    // Delete the IN PROGRESS cloud log
                    BackupLogDAO.deleteLog(cloudLogId);
                    
                    // Log cloud backup success
                    BackupLogDAO.addLog(new BackupLog(
                        schedule.getName(),
                        "CLOUD",
                        "COMPLETED",
                        "Successfully uploaded to Google Drive (File ID: " + fileId + ")",
                        performedBy,
                        department
                    ));
                } else {
                    // Delete the IN PROGRESS cloud log
                    BackupLogDAO.deleteLog(cloudLogId);
                    
                    // Log cloud backup failure
                    BackupLogDAO.addLog(new BackupLog(
                        schedule.getName(),
                        "CLOUD",
                        "FAILED",
                        "Backup file not found for cloud upload",
                        performedBy,
                        department
                    ));
                }
            } catch (Exception e) {
                // Log cloud backup failure
                BackupLogDAO.addLog(new BackupLog(
                    schedule.getName(),
                    "CLOUD",
                    "FAILED",
                    "Cloud upload failed: " + e.getMessage(),
                    performedBy,
                    department
                ));
                e.printStackTrace();
            }
        }

        // Log final local backup success
        BackupLogDAO.addLog(new BackupLog(
            schedule.getName(),
            backupType,
            "COMPLETED",
            "Backup completed successfully",
            performedBy,
            department
        ));
    }

    private static void logFailure(BackupSchedule schedule, String department, String performedBy, String error, int taskId, String backupType, int startLogId) {
        // Update task status if valid
        if (taskId > 0) {
            BackupTaskDAO.updateTaskStatus(taskId, "Failed", error);
        }
        
        // Delete the IN PROGRESS log
        BackupLogDAO.deleteLog(startLogId);
        
        BackupLogDAO.addLog(new BackupLog(
            schedule.getName(),
            backupType,
            "FAILED",
            error,
            performedBy,
            department
        ));
    }


    // Utility for zipping (moved from MainDashboard)
    public static void zipDirectory(File sourceDir, File outputFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
            Path sourcePath = sourceDir.toPath();
            Files.walk(sourcePath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString().replace('\\', '/'));
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }
}
