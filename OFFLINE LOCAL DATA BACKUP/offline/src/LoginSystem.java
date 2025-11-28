import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;

public class LoginSystem extends JFrame {
    // DB-backed; legacy file-based storage removed

    public LoginSystem() {
        setupLoginUI();
    }

    private void setupLoginUI() {
        setTitle("RP Karongi - Backup System Login");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("LOGIN TO BACKUP SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLUE);

        JLabel userLabel = new JLabel("Username or Email:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JLabel roleLabel = new JLabel("Select Role:");
        JComboBox<String> roleCombo = new JComboBox<>();

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Create New Account");

        panel.add(titleLabel);
        panel.add(new JLabel());
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(roleLabel);
        panel.add(roleCombo);
        panel.add(loginBtn);
        panel.add(signupBtn);

        // Populate roles from roles table initially and whenever email changes
        final Runnable reloadRoles = () -> {
            roleCombo.removeAllItems();
            List<String> roles = RoleDAO.getAllRoleNames();
            for (String r : roles) {
                roleCombo.addItem(r);
            }
        };
        reloadRoles.run();

        userField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { reloadRoles.run(); }
            public void removeUpdate(DocumentEvent e) { reloadRoles.run(); }
            public void changedUpdate(DocumentEvent e) { reloadRoles.run(); }
        });

        loginBtn.addActionListener(e -> {
            String input = userField.getText();
            String password = new String(passField.getPassword());
            if (input.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields!");
                return;
            }

            // Try login by Username first
            UserDAO.AuthResult res = UserDAO.authenticate(input, password);
            
            // If failed, try by Email
            if (!res.ok) {
                res = UserDAO.authenticateByEmail(input, password);
            }

            if (res.ok) {
                UserDAO.UserRecord user = res.user;
                String selectedRole = (String) roleCombo.getSelectedItem();
                if (selectedRole == null || selectedRole.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please select a role");
                    return;
                }

                // Validate selected role matches user's role from DB
                String userRole = user.role != null ? user.role : "Staff";
                if (!userRole.equalsIgnoreCase(selectedRole)) {
                    JOptionPane.showMessageDialog(null, "Selected role does not match your assigned role: " + userRole);
                    return;
                }

                String roleUpper = userRole.toUpperCase();
                if ("ADMIN".equals(roleUpper)) {
                    JOptionPane.showMessageDialog(null,
                            " WELCOME SYSTEM ADMINISTRATOR!\n\n" +
                                    "Name: " + user.fullName + "\n" +
                                    "Role: " + roleUpper + "\n" +
                                    "Authority: ALL DEPARTMENTS\n\n" +
                                    "You have FULL SYSTEM ACCESS across all departments\nand complete user management privileges.",
                            "Admin Access Granted",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Welcome " + user.fullName + "!\n" +
                                    "Role: " + roleUpper + "\n" +
                                    "Department: " + user.department);
                }

                dispose();
                if ("ADMIN".equals(roleUpper)) {
                    new MainDashboard(roleUpper, user.fullName, user.department, user.username);
                } else {
                    new RoleDashboard(roleUpper, user.fullName, user.department, user.username);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Login failed! Invalid username/email or password.");
            }
        });

        signupBtn.addActionListener(e -> {
            dispose();
            new SignupSystem();
        });

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginSystem();
    }
}