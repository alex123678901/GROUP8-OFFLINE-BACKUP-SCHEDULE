import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BackupTaskDAO {

    public static int createTask(long userId, int scheduleId, String taskType, String status) {
        String sql = "INSERT INTO backup_tasks (user_id, schedule_id, task_type, status, start_time) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, userId);
            if (scheduleId > 0) {
                pstmt.setInt(2, scheduleId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, taskType);
            pstmt.setString(4, status);
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void updateTaskStatus(int taskId, String status, String remarks) {
        String sql = "UPDATE backup_tasks SET status = ?, remarks = ?, end_time = CASE WHEN ? = 'Completed' OR ? = 'Failed' THEN NOW() ELSE end_time END WHERE task_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, remarks);
            pstmt.setString(3, status);
            pstmt.setString(4, status);
            pstmt.setInt(5, taskId);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addBackupFile(int taskId, String fileName, String filePath, long fileSize, String checksum) {
        String sql = "INSERT INTO backup_files (task_id, file_name, file_path, file_size, checksum) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            pstmt.setString(2, fileName);
            pstmt.setString(3, filePath);
            pstmt.setLong(4, fileSize);
            pstmt.setString(5, checksum);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static long getUserIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Log a cloud upload to the database
     * @param taskId The backup task ID
     * @param cloudProvider The cloud provider name (e.g., "Google Drive")
     * @param cloudFileId The file ID in the cloud storage
     * @param cloudUrl The URL to access the file
     * @param uploadSize The size of the uploaded file in bytes
     */
    public static void logCloudUpload(int taskId, String cloudProvider, String cloudFileId, String cloudUrl, long uploadSize) {
        String sql = "INSERT INTO cloud_uploads (task_id, cloud_provider, cloud_file_id, cloud_url, upload_size, upload_time) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            pstmt.setString(2, cloudProvider);
            pstmt.setString(3, cloudFileId);
            pstmt.setString(4, cloudUrl);
            pstmt.setLong(5, uploadSize);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
