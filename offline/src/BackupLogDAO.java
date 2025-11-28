import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BackupLogDAO {
    
    // SQL statements
    private static final String CREATE_TABLE = 
        "CREATE TABLE IF NOT EXISTS backup_logs (" +
        "id INT AUTO_INCREMENT PRIMARY KEY," +
        "timestamp DATETIME NOT NULL," +
        "backup_name VARCHAR(255) NOT NULL," +
        "backup_type VARCHAR(50) NOT NULL," +
        "status VARCHAR(20) NOT NULL," +
        "details TEXT," +
        "performed_by VARCHAR(100) NOT NULL," +
        "department VARCHAR(100) NOT NULL" +
        ")";
    
    private static final String INSERT_LOG = 
        "INSERT INTO backup_logs (timestamp, backup_name, backup_type, status, details, performed_by, department) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String GET_ALL_LOGS = 
        "SELECT * FROM backup_logs ORDER BY timestamp DESC";
    
    private static final String GET_LOGS_BY_USER = 
        "SELECT * FROM backup_logs WHERE performed_by = ? ORDER BY timestamp DESC";
    
    static {
        initializeDatabase();
    }
    
    private static void initializeDatabase() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static int addLog(BackupLog log) {
        String sql = "INSERT INTO backup_logs (timestamp, backup_name, backup_type, status, details, performed_by, department) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(log.getTimestamp()));
            pstmt.setString(2, log.getScheduleName());
            pstmt.setString(3, log.getType());
            pstmt.setString(4, log.getStatus());
            pstmt.setString(5, log.getDetails());
            pstmt.setString(6, log.getPerformedBy());
            pstmt.setString(7, log.getDepartment());
            
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

    public static void deleteLog(int logId) {
        String sql = "DELETE FROM backup_logs WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, logId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static List<BackupLog> getAllLogs() {
        List<BackupLog> logs = new ArrayList<>();
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(GET_ALL_LOGS)) {
            
            while (rs.next()) {
                logs.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    public static List<BackupLog> getLogsByUser(String username) {
        List<BackupLog> logs = new ArrayList<>();
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_LOGS_BY_USER)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLogFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    private static BackupLog extractLogFromResultSet(ResultSet rs) throws SQLException {
        // Create a new BackupLog with the data from the result set
        String backupName = rs.getString("backup_name");
        String backupType = rs.getString("backup_type");
        String status = rs.getString("status");
        String details = rs.getString("details");
        String performedBy = rs.getString("performed_by");
        String department = rs.getString("department");
        
        // Create a new BackupLog - the constructor will set the current timestamp
        BackupLog log = new BackupLog(backupName, backupType, status, details, performedBy, department);
        
        // If you need to preserve the original timestamp from the database, you would need to add a setter for timestamp in BackupLog
        // For now, we'll keep the current timestamp set by the constructor
        
        return log;
    }
}
