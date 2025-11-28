import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackupSchedule {
    private String name;
    private String time;  // "HH:mm" format
    private String status; // "ACTIVE", "PAUSED", "COMPLETED"
    private Date nextRun;
    private String source;
    private String destination;
    private boolean onlineBackup;
    private String createdBy;
    private String department;
    private TimerTask task;
    private Timer timer; // Reference to a timer

    public BackupSchedule(String name, String time, String source, String destination,
                          boolean onlineBackup, String createdBy, String department, Timer timer) {
        this.name = name;
        this.time = time;
        this.source = source;
        this.destination = destination;
        this.onlineBackup = onlineBackup;
        this.createdBy = createdBy;
        this.department = department;
        this.status = "ACTIVE";
        this.timer = timer;
        this.nextRun = calculateNextRun(time);
    }

    private Date calculateNextRun(String time) {
        Calendar cal = Calendar.getInstance();
        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        
        if (cal.getTime().before(new Date())) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cal.getTime();
    }

    public void schedule() {
        if (task != null) {
            task.cancel();
        }
        
        task = new TimerTask() {
            @Override
            public void run() {
                // Create a callback to show notification dialog
                LocalBackupService.Progress notificationCallback = new LocalBackupService.Progress() {
                    @Override
                    public void onProgress(int percent, String message) {
                        // No UI update during scheduled backup
                    }

                    @Override
                    public void onDone(boolean ok, String details) {
                        // Show notification dialog on completion
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            if (ok) {
                                javax.swing.JOptionPane.showMessageDialog(null,
                                    "Scheduled backup '" + name + "' completed successfully!\n" +
                                    "Department: " + department + "\n" +
                                    "Time: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
                                    "Backup Success",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                javax.swing.JOptionPane.showMessageDialog(null,
                                    "Scheduled backup '" + name + "' failed!\n" +
                                    "Department: " + department + "\n" +
                                    "Error: " + details,
                                    "Backup Failed",
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                };
                
                BackupExecutor.performBackup(BackupSchedule.this, department, createdBy, notificationCallback);
                nextRun = calculateNextRun(time);
            }
        };
        
        long delay = nextRun.getTime() - System.currentTimeMillis();
        if (delay > 0 && timer != null) {
            timer.schedule(task, delay);
        }
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        this.status = "CANCELLED";
    }

    // Getters
    public String getName() { return name; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public Date getNextRun() { return nextRun; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public boolean isOnlineBackup() { return onlineBackup; }
    public String getCreatedBy() { return createdBy; }
    public String getDepartment() { return department; }
}
