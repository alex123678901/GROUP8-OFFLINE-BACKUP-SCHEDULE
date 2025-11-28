import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class SignupSystem extends JFrame {
    public SignupSystem() {
        setupSignupUI();
    }

    private void setupSignupUI() {
        setTitle("RP Karongi - Create Account");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(10, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("CREATE NEW ACCOUNT", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLUE);

        JLabel userIdLabel = new JLabel("User ID (16+ digits):");
        JTextField userIdField = new JTextField();

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel fullNameLabel = new JLabel("Full Name:");
        JTextField fullNameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPassField = new JPasswordField();

        JLabel deptLabel = new JLabel("Department:");
        String[] departments = {"Academics", "Finance", "ICT Department", "Administration", "Horticulture", "Hospitality", "Electrical", "Manufacturing Technology"};
        JComboBox<String> deptCombo = new JComboBox<>(departments);

        JLabel roleLabel = new JLabel("Role:");
        String[] roles = {"Staff", "HOD", "Admin"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);

        JButton signupBtn = new JButton("Sign Up");
        JButton loginBtn = new JButton("Already have account? Login");

        panel.add(titleLabel);
        panel.add(new JLabel());
        panel.add(userIdLabel);
        panel.add(userIdField);
        panel.add(userLabel);
        panel.add(userField);
        panel.add(fullNameLabel);
        panel.add(fullNameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(confirmPassLabel);
        panel.add(confirmPassField);
        panel.add(deptLabel);
        panel.add(deptCombo);
        panel.add(roleLabel);
        panel.add(roleCombo);
        panel.add(signupBtn);
        panel.add(loginBtn);

        signupBtn.addActionListener(e -> {
            String userIdStr = userIdField.getText().trim();
            String username = userField.getText();
            String fullName = fullNameField.getText();
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword());
            String confirmPassword = new String(confirmPassField.getPassword());
            String department = (String) deptCombo.getSelectedItem();
            String role = (String) roleCombo.getSelectedItem();

            // Required validations
            if (userIdStr.isEmpty() || username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill required fields: User ID, Username, Full Name, Password");
                return;
            }

            // User ID numeric and 16+ digits
            if (!userIdStr.matches("^\\d{16,}$")) {
                JOptionPane.showMessageDialog(null, "User ID must be numeric and at least 16 digits long");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(null, "Passwords do not match!");
                return;
            }

            // Strong password
            String pwdErr = PasswordUtil.validatePasswordStrength(password);
            if (pwdErr != null) {
                JOptionPane.showMessageDialog(null, pwdErr);
                return;
            }

            // Email validation (optional if empty)
            if (!email.isEmpty() && !PasswordUtil.isValidEmail(email)) {
                JOptionPane.showMessageDialog(null, "Please enter a valid email address");
                return;
            }

            // Check if username exists
            UserDAO.UserRecord existing = UserDAO.findByUsername(username);
            if (existing != null) {
                JOptionPane.showMessageDialog(null, "Username already exists!");
                return;
            }

            try {
                long userId = Long.parseLong(userIdStr);
                boolean ok = UserDAO.createUser(userId, username, fullName, department, role, email, password);
                if (!ok) {
                    JOptionPane.showMessageDialog(null, "Failed to create user. Try again.");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error creating user: " + ex.getMessage());
                return;
            }

            JOptionPane.showMessageDialog(null,
                    " ACCOUNT CREATED SUCCESSFULLY!\n\n" +
                            "Username: " + username + "\n" +
                            "Full Name: " + fullName + "\n" +
                            "Department: " + department + "\n\n" +
                            " STATUS: PENDING APPROVAL\n\n" +
                            "Your account is waiting for administrator approval.\n" +
                            "You will be notified once your account is approved.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginSystem();
        });

        loginBtn.addActionListener(e -> {
            dispose();
            new LoginSystem();
        });

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        new SignupSystem();
    }
}