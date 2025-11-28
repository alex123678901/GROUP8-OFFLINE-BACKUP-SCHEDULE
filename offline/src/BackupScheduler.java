import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BackupScheduler extends JFrame {
    public BackupScheduler() {
        // Setup main window
        setTitle("RP Karongi College - Backup System");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window

        // Create main panel
        createMainPanel();

        setVisible(true);
    }

    private void createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title
        JLabel titleLabel = new JLabel("OFFLINE BACKUP SCHEDULER", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLUE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Add center content
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        // Add bottom buttons
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(4, 1, 10, 10));

        // Source folder selection
        JPanel sourcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourcePanel.add(new JLabel("Source Folder:"));
        JTextField sourceField = new JTextField(25);
        sourceField.setText("C:/CollegeData");
        sourcePanel.add(sourceField);
        JButton browseSource = new JButton("Browse");
        sourcePanel.add(browseSource);

        // Backup destination
        JPanel destPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destPanel.add(new JLabel("Backup To:"));
        JTextField destField = new JTextField(25);
        destField.setText("//Server/Backups");
        destPanel.add(destField);
        JButton browseDest = new JButton("Browse");
        destPanel.add(browseDest);

        // Schedule options
        JPanel schedulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        schedulePanel.add(new JLabel("Schedule:"));
        String[] schedules = {"Daily", "Weekly", "Monthly", "Manual"};
        JComboBox<String> scheduleCombo = new JComboBox<>(schedules);
        schedulePanel.add(scheduleCombo);

        // Backup type
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("Backup Type:"));
        JCheckBox compressCheck = new JCheckBox("Compress (ZIP)");
        JCheckBox incrementalCheck = new JCheckBox("Incremental Backup");
        typePanel.add(compressCheck);
        typePanel.add(incrementalCheck);

        centerPanel.add(sourcePanel);
        centerPanel.add(destPanel);
        centerPanel.add(schedulePanel);
        centerPanel.add(typePanel);

        return centerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton backupNowBtn = new JButton("Backup Now");
        backupNowBtn.setBackground(Color.GREEN);
        backupNowBtn.setForeground(Color.BLACK);

        JButton scheduleBtn = new JButton("Schedule Backup");
        scheduleBtn.setBackground(Color.ORANGE);

        JButton viewLogsBtn = new JButton("View Logs");

        JButton exitBtn = new JButton("Exit");
        exitBtn.setBackground(Color.RED);
        exitBtn.setForeground(Color.WHITE);

        // Add action listeners
        backupNowBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Backup started successfully!\nThis is a demo version.");
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(backupNowBtn);
        buttonPanel.add(scheduleBtn);
        buttonPanel.add(viewLogsBtn);
        buttonPanel.add(exitBtn);

        return buttonPanel;
    }

    public static void main(String[] args) {
        // SIMPLE VERSION - JUST CREATE THE WINDOW
        new BackupScheduler();
    }
}