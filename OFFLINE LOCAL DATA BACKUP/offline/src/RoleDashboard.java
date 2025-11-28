import javax.swing.*;
import java.awt.*;

public class RoleDashboard extends JFrame {

    private final String roleUpper;
    private final String fullName;
    private final String department;
    private final String username;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private java.util.Timer backupTimer;
    private java.util.List<BackupSchedule> activeSchedules = new java.util.ArrayList<>();
    private javax.swing.table.DefaultTableModel schedulesTableModel;
    private javax.swing.table.DefaultTableModel logsTableModel;

    public RoleDashboard(String role, String fullName, String department, String username) {
        this.roleUpper = role != null ? role.toUpperCase() : "STAFF";
        this.fullName = fullName;
        this.department = department;
        this.username = username;
        this.backupTimer = new java.util.Timer("RoleBackupTimer", true);

        setTitle("RP Karongi - Backup Scheduler");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildUI();
        loadSchedules(); // Load from DB
        setVisible(true);
    }

    private void loadSchedules() {
        java.util.List<BackupSchedule> dbSchedules = ScheduleDAO.loadSchedulesForUser(username, roleUpper, department);
        for (BackupSchedule s : dbSchedules) {
            // Re-attach timer to loaded schedules
            BackupSchedule withTimer = new BackupSchedule(s.getName(), s.getTime(), s.getSource(), s.getDestination(), 
                                                        s.isOnlineBackup(), s.getCreatedBy(), s.getDepartment(), backupTimer);
            activeSchedules.add(withTimer);
            withTimer.schedule();
        }
        refreshSchedules();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(44, 62, 80));
        p.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("RP KARONGI – BACKUP SCHEDULER", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        p.add(title, BorderLayout.CENTER);

        String displayDept = roleUpper.equals("ADMIN") ? "ALL DEPARTMENTS" : department;
        JLabel userLbl = new JLabel("Welcome, " + fullName + " (" + roleUpper + ", " + displayDept + ")");
        userLbl.setForeground(roleUpper.equals("ADMIN") ? Color.CYAN : (roleUpper.equals("HOD") ? new Color(144,238,144) : Color.YELLOW));
        p.add(userLbl, BorderLayout.EAST);
        return p;
    }

    private JComponent buildContent() {
        JPanel container = new JPanel(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(6, 1, 8, 8));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(52, 73, 94));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JButton btnDashboard = menuButton("📊 Dashboard", new Color(41,128,185));
        JButton btnSchedule  = menuButton("⏰ Schedule", new Color(230,126,34));
        JButton btnBackup    = menuButton("💾 Backup", new Color(39,174,96));
        JButton btnLogs      = menuButton("📋 Logs", new Color(155,89,182));
        JButton btnLogout    = menuButton("🚪 Logout", new Color(231, 76, 60));

        sidebar.add(btnDashboard);
        sidebar.add(btnSchedule);
        sidebar.add(btnBackup);
        sidebar.add(btnLogs);
        sidebar.add(new JPanel()); // Spacer
        sidebar.add(btnLogout);

        // Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(buildDashboardPanel(), "Dashboard");
        contentPanel.add(buildSchedulePanel(), "Schedule");
        contentPanel.add(buildBackupPanel(), "Backup");
        contentPanel.add(buildLogsPanel(), "Logs");

        container.add(sidebar, BorderLayout.WEST);
        container.add(contentPanel, BorderLayout.CENTER);

        // Action Listeners
        btnDashboard.addActionListener(e -> cardLayout.show(contentPanel, "Dashboard"));
        btnSchedule.addActionListener(e -> {
            refreshSchedules();
            cardLayout.show(contentPanel, "Schedule");
        });
        btnBackup.addActionListener(e -> cardLayout.show(contentPanel, "Backup"));
        btnLogs.addActionListener(e -> {
            refreshLogs();
            cardLayout.show(contentPanel, "Logs");
        });
        btnLogout.addActionListener(e -> {
            if (backupTimer != null) backupTimer.cancel();
            dispose();
            new LoginSystem();
        });

        return container;
    }

    private JPanel buildDashboardPanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel hero = new JLabel(roleSummaryHtml(), JLabel.LEFT);
        hero.setFont(new Font("Arial", Font.PLAIN, 14));
        content.add(hero, BorderLayout.NORTH);

        JTextArea info = new JTextArea(featuresText());
        info.setEditable(false);
        info.setFont(new Font("Monospaced", Font.PLAIN, 12));
        info.setBorder(BorderFactory.createTitledBorder("Features"));
        content.add(new JScrollPane(info), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Create New Schedule"));
        
        JTextField nameField = new JTextField();
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        
        JTextField sourceField = new JTextField("C:/CollegeData");
        JButton browseSourceBtn = new JButton("Browse");
        JPanel sourcePanel = new JPanel(new BorderLayout(5, 0));
        sourcePanel.add(sourceField, BorderLayout.CENTER);
        sourcePanel.add(browseSourceBtn, BorderLayout.EAST);

        JTextField destField = new JTextField("C:/Backups");
        JButton browseDestBtn = new JButton("Browse");
        JPanel destPanel = new JPanel(new BorderLayout(5, 0));
        destPanel.add(destField, BorderLayout.CENTER);
        destPanel.add(browseDestBtn, BorderLayout.EAST);
        
        form.add(new JLabel("Schedule Name:")); form.add(nameField);
        form.add(new JLabel("Time (HH:mm):")); form.add(timeSpinner);
        form.add(new JLabel("Source Path:")); form.add(sourcePanel);
        form.add(new JLabel("Destination Path:")); form.add(destPanel);
        
        JButton addBtn = new JButton("Add Schedule");
        addBtn.setBackground(new Color(46, 204, 113));
        addBtn.setForeground(Color.WHITE);
        form.add(new JLabel("")); form.add(addBtn);

        // Browse Actions
        browseSourceBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                sourceField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        browseDestBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                destField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Table
        String[] cols = {"Name", "Time", "Source", "Dest", "Status", "Next Run"};
        schedulesTableModel = new javax.swing.table.DefaultTableModel(cols, 0);
        JTable table = new JTable(schedulesTableModel);
        
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            String name = nameField.getText();
            String time = new java.text.SimpleDateFormat("HH:mm").format(timeSpinner.getValue());
            String src = sourceField.getText();
            String dst = destField.getText();
            
            if (name.isEmpty() || src.isEmpty() || dst.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields");
                return;
            }

            BackupSchedule sched = new BackupSchedule(name, time, src, dst, false, username, department, backupTimer);
            
            // Save to database
            long userId = BackupTaskDAO.getUserIdByUsername(username);
            if (userId > 0) {
                int scheduleId = ScheduleDAO.createSchedule(userId, sched);
                if (scheduleId > 0) {
                    System.out.println("Schedule saved with ID: " + scheduleId);
                    activeSchedules.add(sched);
                    sched.schedule();
                    refreshSchedules();
                    // Show success dialog
                    JOptionPane.showMessageDialog(this, 
                        "Schedule '" + name + "' created successfully!\nNext run: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(sched.getNextRun()),
                        "Schedule Created",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to save schedule to database.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "User not found in database. Cannot save schedule.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

            // Clear fields
            nameField.setText("");
            sourceField.setText("C:/CollegeData");
            destField.setText("C:/Backups");
            timeSpinner.setValue(new java.util.Date()); // Reset time spinner to current time
        });

        return panel;
    }

    private void refreshSchedules() {
        schedulesTableModel.setRowCount(0);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (BackupSchedule s : activeSchedules) {
            schedulesTableModel.addRow(new Object[]{
                s.getName(), s.getTime(), s.getSource(), s.getDestination(), s.getStatus(), sdf.format(s.getNextRun())
            });
        }
    }

    private JPanel buildBackupPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel controls = new JPanel(new GridLayout(4, 2, 10, 10));
        controls.setBorder(BorderFactory.createTitledBorder("Manual Backup"));

        JTextField srcField = new JTextField("C:/CollegeData");
        JButton browseSrcBtn = new JButton("Browse");
        JPanel srcPanel = new JPanel(new BorderLayout(5, 0));
        srcPanel.add(srcField, BorderLayout.CENTER);
        srcPanel.add(browseSrcBtn, BorderLayout.EAST);

        JTextField dstField = new JTextField("C:/Backups");
        JButton browseDstBtn = new JButton("Browse");
        JPanel dstPanel = new JPanel(new BorderLayout(5, 0));
        dstPanel.add(dstField, BorderLayout.CENTER);
        dstPanel.add(browseDstBtn, BorderLayout.EAST);
        
        controls.add(new JLabel("Source:")); controls.add(srcPanel);
        controls.add(new JLabel("Destination:")); controls.add(dstPanel);
        
        JButton backupBtn = new JButton("Start Backup Now");
        backupBtn.setBackground(new Color(39, 174, 96));
        backupBtn.setForeground(Color.WHITE);
        
        controls.add(new JLabel("")); controls.add(backupBtn);
        
        panel.add(controls, BorderLayout.NORTH);

        // Browse Actions
        browseSrcBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                srcField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        browseDstBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                dstField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        backupBtn.addActionListener(e -> {
            String src = srcField.getText();
            String dst = dstField.getText();
            if (src.isEmpty() || dst.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select source and destination");
                return;
            }
            
            // Create manual backup schedule
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            BackupSchedule temp = new BackupSchedule("Manual_" + timestamp, "00:00", src, dst, false, username, department, null);
            
            new Thread(() -> {
                BackupExecutor.performManualBackup(temp, department, username, null);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Backup completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshLogs(); // Refresh logs to show updated status
                });
            }).start();
        });

        return panel;
    }

    private JPanel buildLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] cols = {"Time", "Name", "Type", "Status", "Details", "User"};
        logsTableModel = new javax.swing.table.DefaultTableModel(cols, 0);
        JTable table = new JTable(logsTableModel);
        
        JButton refreshBtn = new JButton("Refresh Logs");
        refreshBtn.addActionListener(e -> refreshLogs());
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshLogs() {
        logsTableModel.setRowCount(0);
        java.util.List<BackupLog> logs;
        if (roleUpper.equals("HOD")) {
            // HOD sees all logs (simplified for now, ideally filtered by department)
            logs = BackupLogDAO.getAllLogs(); 
        } else {
            // Staff sees only their own
            logs = BackupLogDAO.getLogsByUser(username);
        }
        
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (BackupLog l : logs) {
            // Filter for HOD by department if needed
            if (roleUpper.equals("HOD") && !l.getDepartment().equalsIgnoreCase(department)) continue;
            
            logsTableModel.addRow(new Object[]{
                l.getTimestamp().format(dtf), l.getScheduleName(), l.getType(), l.getStatus(), l.getDetails(), l.getPerformedBy()
            });
        }
    }

    private String roleSummaryHtml() {
        String displayDept = roleUpper.equals("ADMIN") ? "ALL DEPARTMENTS" : department;
        String perms = roleUpper.equals("ADMIN") ?
                "FULL SYSTEM ACCESS (All Departments)" :
                (roleUpper.equals("HOD") ? "DEPARTMENT-LEVEL MANAGEMENT (" + displayDept + ")" : "LIMITED ACCESS (" + displayDept + ")");
        return "<html><b>User:</b> " + fullName + " (" + username + ")" +
                "<br/><b>Role:</b> " + roleUpper +
                "<br/><b>Department:</b> " + displayDept +
                "<br/><b>Permissions:</b> " + perms + "</html>";
    }

    private String featuresText() {
        if (roleUpper.equals("ADMIN")) {
            return String.join("\n",
                    "- Full system access across ALL departments",
                    "- Automatic Google Drive cloud backup",
                    "- Complete user management",
                    "- Advanced system settings",
                    "- Monitoring & reports",
                    "- Security & permission management");
        } else if (roleUpper.equals("HOD")) {
            return String.join("\n",
                    "- Department-level user oversight",
                    "- Department scheduling",
                    "- Department logs",
                    "- Department file operations",
                    "- Notifications",
                    "- Usage monitoring");
        } else {
            return String.join("\n",
                    "- Local backup operations",
                    "- Personal file management",
                    "- Backup scheduling",
                    "- View personal logs",
                    "- Notifications",
                    "- Storage usage monitoring");
        }
    }

    private JButton menuButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        return b;
    }
}
