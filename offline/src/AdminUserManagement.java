import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class AdminUserManagement extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    public AdminUserManagement() {
        setTitle("User Management - Admin");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        loadUsers();
        setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout(8,8));

        // Header with title and search
        JPanel header = new JPanel(new BorderLayout(8,8));
        JLabel title = new JLabel("👥 Users & Roles Management", JLabel.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        searchField = new JTextField(24);
        searchField.putClientProperty("JTextField.placeholderText", "Search (username, name, department, role, email)");
        JButton btnSearchClear = new JButton("Clear");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(btnSearchClear);
        header.add(searchPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"User ID", "Username", "Full Name", "Department", "Role", "Email", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(4,8,8,8));
        add(sp, BorderLayout.CENTER);

        // Toolbar actions
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton btnRefresh = new JButton("🔄 Refresh");
        JButton btnApprove = new JButton("✅ Approve");
        JButton btnAssignRole = new JButton("🎭 Assign Role");
        JButton btnEdit = new JButton("✏️ Edit");
        JButton btnDelete = new JButton("🗑️ Delete");
        JButton btnResetPwd = new JButton("🔑 Reset Password");
        toolbar.add(btnRefresh);
        toolbar.addSeparator();
        toolbar.add(btnApprove);
        toolbar.add(btnAssignRole);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.addSeparator();
        toolbar.add(btnResetPwd);
        add(toolbar, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadUsers());
        btnApprove.addActionListener(e -> approveSelected());
        btnAssignRole.addActionListener(e -> assignRoleSelected());
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnResetPwd.addActionListener(e -> resetPasswordSelected());

        // Search listeners
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String text = searchField.getText();
                if (text == null || text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });
        btnSearchClear.addActionListener(e -> searchField.setText(""));
    }

    private void loadUsers() {
        model.setRowCount(0);
        List<AdminUsersDAO.UserRow> rows = AdminUsersDAO.listAllUsers();
        for (AdminUsersDAO.UserRow r : rows) {
            model.addRow(new Object[]{r.userId, r.username, r.fullName, r.department, r.role, r.email, r.status});
        }
        // Column sizing
        int[] widths = {90, 120, 180, 150, 100, 220, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private String getSelectedUsernameOrWarn() {
        int idx = table.getSelectedRow();
        if (idx == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user row first.");
            return null;
        }
        return String.valueOf(model.getValueAt(idx, 1));
    }

    private void approveSelected() {
        String username = getSelectedUsernameOrWarn();
        if (username == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Approve user '" + username + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = AdminUsersDAO.approveUser(username);
        JOptionPane.showMessageDialog(this, ok ? "Approved." : "Failed to approve.");
        loadUsers();
    }

    private void assignRoleSelected() {
        String username = getSelectedUsernameOrWarn();
        if (username == null) return;
        java.util.List<String> roles = RoleDAO.getAllRoleNames();
        String role = (String) JOptionPane.showInputDialog(this, "Select role", "Assign Role",
                JOptionPane.PLAIN_MESSAGE, null, roles.toArray(), roles.isEmpty() ? null : roles.get(0));
        if (role == null || role.trim().isEmpty()) return;
        boolean ok = AdminUsersDAO.assignRole(username, role);
        JOptionPane.showMessageDialog(this, ok ? "Role assigned." : "Failed to assign role.");
        loadUsers();
    }

    private void editSelected() {
        int idx = table.getSelectedRow();
        if (idx == -1) { JOptionPane.showMessageDialog(this, "Please select a user row first."); return; }
        idx = table.convertRowIndexToModel(idx);
        String oldUsername = String.valueOf(model.getValueAt(idx, 1));

        // Current values
        String curUsername = oldUsername;
        String curFullName = String.valueOf(model.getValueAt(idx, 2));
        String curDepartment = String.valueOf(model.getValueAt(idx, 3));
        String curRole = String.valueOf(model.getValueAt(idx, 4));
        String curEmail = String.valueOf(model.getValueAt(idx, 5));
        String curStatus = String.valueOf(model.getValueAt(idx, 6));

        // Build form
        JTextField tfUsername = new JTextField(curUsername, 18);
        JTextField tfFullName = new JTextField(curFullName, 24);
        JTextField tfDepartment = new JTextField(curDepartment, 18);
        java.util.List<String> roles = RoleDAO.getAllRoleNames();
        JComboBox<String> cbRole = new JComboBox<>(roles.toArray(new String[0]));
        cbRole.setSelectedItem(curRole);
        JTextField tfEmail = new JTextField(curEmail, 24);
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        cbStatus.setSelectedItem(curStatus);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,6,4,6);
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.EAST; form.add(new JLabel("Username:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST; form.add(tfUsername, gc);
        gc.gridx = 0; gc.gridy++; gc.anchor = GridBagConstraints.EAST; form.add(new JLabel("Full Name:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST; form.add(tfFullName, gc);
        gc.gridx = 0; gc.gridy++; gc.anchor = GridBagConstraints.EAST; form.add(new JLabel("Department:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST; form.add(tfDepartment, gc);
        gc.gridx = 0; gc.gridy++; gc.anchor = GridBagConstraints.EAST; form.add(new JLabel("Role:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST; form.add(cbRole, gc);
        gc.gridx = 0; gc.gridy++; gc.anchor = GridBagConstraints.EAST; form.add(new JLabel("Email:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST; form.add(tfEmail, gc);
        gc.gridx = 0; gc.gridy++; gc.anchor = GridBagConstraints.EAST; form.add(new JLabel("Status:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST; form.add(cbStatus, gc);

        int res = JOptionPane.showConfirmDialog(this, form, "Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String newUsername = tfUsername.getText().trim();
        String fullName = tfFullName.getText().trim();
        String department = tfDepartment.getText().trim();
        String role = (String) cbRole.getSelectedItem();
        String email = tfEmail.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        if (newUsername.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Full Name are required.");
            return;
        }

        boolean ok = AdminUsersDAO.updateUserAll(oldUsername, newUsername, fullName, department, role, email, status);
        JOptionPane.showMessageDialog(this, ok ? "User updated." : "Failed to update user.");
        loadUsers();
    }

    private void deleteSelected() {
        String username = getSelectedUsernameOrWarn();
        if (username == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user '" + username + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = AdminUsersDAO.deleteUser(username);
        JOptionPane.showMessageDialog(this, ok ? "User deleted." : "Failed to delete user.");
        loadUsers();
    }

    private void resetPasswordSelected() {
        String username = getSelectedUsernameOrWarn();
        if (username == null) return;
        JPasswordField pf = new JPasswordField();
        JPasswordField pf2 = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(2,2,8,8));
        p.add(new JLabel("New Password:")); p.add(pf);
        p.add(new JLabel("Confirm Password:")); p.add(pf2);
        int res = JOptionPane.showConfirmDialog(this, p, "Reset Password", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        String pwd = new String(pf.getPassword());
        String pwd2 = new String(pf2.getPassword());
        if (!pwd.equals(pwd2)) { JOptionPane.showMessageDialog(this, "Passwords do not match."); return; }
        String err = PasswordUtil.validatePasswordStrength(pwd);
        if (err != null) { JOptionPane.showMessageDialog(this, err); return; }
        boolean ok = AdminUsersDAO.resetPassword(username, pwd);
        JOptionPane.showMessageDialog(this, ok ? "Password reset." : "Failed to reset password.");
    }
}
