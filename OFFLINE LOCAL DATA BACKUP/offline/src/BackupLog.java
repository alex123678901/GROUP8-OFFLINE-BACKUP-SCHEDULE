import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class BackupLog {
    private LocalDateTime timestamp;
    private String scheduleName;
    private String type;
    private String status;  // Will be one of: "IN PROGRESS", "SUCCESS", "FAILED"
    private String details;
    private String performedBy;
    private String department;
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public BackupLog(String scheduleName, String type, String status, String details, String performedBy, String department) {
        this.timestamp = LocalDateTime.now();
        this.scheduleName = scheduleName != null ? scheduleName : "";
        this.type = type != null ? type : "";
        this.status = status != null ? status.trim().toUpperCase() : "";
        this.details = details != null ? details : "";
        this.performedBy = performedBy != null ? performedBy : "";
        this.department = department != null ? department : "";
    }
    
    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getScheduleName() { return scheduleName; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getDetails() { return details; }
    public String getPerformedBy() { return performedBy; }
    public String getDepartment() { return department; }
    
    public String getFormattedTimestamp() {
        return timestamp.format(TIMESTAMP_FORMAT);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackupLog backupLog = (BackupLog) o;
        return Objects.equals(timestamp, backupLog.timestamp) &&
               Objects.equals(scheduleName, backupLog.scheduleName) &&
               Objects.equals(type, backupLog.type) &&
               Objects.equals(status, backupLog.status) &&
               Objects.equals(details, backupLog.details) &&
               Objects.equals(performedBy, backupLog.performedBy) &&
               Objects.equals(department, backupLog.department);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(timestamp, scheduleName, type, status, details, performedBy, department);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s (%s)", 
            getFormattedTimestamp(), 
            type, 
            scheduleName, 
            status,
            performedBy);
    }
}
