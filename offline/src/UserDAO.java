import java.sql.*;

public class UserDAO {

    public static void ensureDefaultAdmin() {
        try (Connection conn = Database.getConnection()) {
            String checkSql = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, "admin");
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        // Try to insert with explicit user_id (for schemas without AUTO_INCREMENT)
                        String insertSqlWithId = "INSERT INTO users (user_id, username, password_hash, full_name, department, role, email, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement ins = conn.prepareStatement(insertSqlWithId)) {
                            ins.setLong(1, 1000000000000000L);
                            ins.setString(2, "admin");
                            ins.setString(3, PasswordUtil.sha256("admin123"));
                            ins.setString(4, "System Administrator");
                            ins.setString(5, "ALL DEPARTMENTS");
                            ins.setString(6, "Admin");
                            ins.setString(7, null);
                            ins.setString(8, "Active");
                            ins.executeUpdate();
                        } catch (SQLException ex) {
                            // Fallback to schema with AUTO_INCREMENT
                            String insertSql = "INSERT INTO users (username, password_hash, full_name, department, role, email, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement ins2 = conn.prepareStatement(insertSql)) {
                                ins2.setString(1, "admin");
                                ins2.setString(2, PasswordUtil.sha256("admin123"));
                                ins2.setString(3, "System Administrator");
                                ins2.setString(4, "ALL DEPARTMENTS");
                                ins2.setString(5, "Admin");
                                ins2.setString(6, null);
                                ins2.setString(7, "Active");
                                ins2.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static UserRecord findByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, full_name, department, role, email, status FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UserRecord findByEmail(String email) {
        String sql = "SELECT user_id, username, password_hash, full_name, department, role, email, status FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UserRecord findByUserId(long userId) {
        String sql = "SELECT user_id, username, password_hash, full_name, department, role, email, status FROM users WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean createUser(long userId, String username, String fullName, String department, String role, String email, String rawPassword) throws SQLException {
        String sql = "INSERT INTO users (user_id, username, password_hash, full_name, department, role, email, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, username);
            ps.setString(3, PasswordUtil.sha256(rawPassword));
            ps.setString(4, fullName);
            ps.setString(5, department);
            ps.setString(6, role);
            ps.setString(7, email);
            String status = (role != null && role.equalsIgnoreCase("Admin")) ? "Active" : "Inactive";
            ps.setString(8, status);
            return ps.executeUpdate() == 1;
        }
    }

    public static AuthResult authenticate(String username, String rawPassword) {
        UserRecord u = findByUsername(username);
        if (u == null) return new AuthResult(false, "Username not found", null);
        String inputHash = PasswordUtil.sha256(rawPassword);
        if (!inputHash.equalsIgnoreCase(u.passwordHash)) {
            return new AuthResult(false, "Invalid password", null);
        }
        if (!"Active".equalsIgnoreCase(u.status)) {
            return new AuthResult(false, "Account pending approval", null);
        }
        return new AuthResult(true, null, u);
    }

    public static AuthResult authenticateByEmail(String email, String rawPassword) {
        UserRecord u = findByEmail(email);
        if (u == null) return new AuthResult(false, "Email not found", null);
        String inputHash = PasswordUtil.sha256(rawPassword);
        if (!inputHash.equalsIgnoreCase(u.passwordHash)) {
            return new AuthResult(false, "Invalid password", null);
        }
        if (!"Active".equalsIgnoreCase(u.status)) {
            return new AuthResult(false, "Account pending approval", null);
        }
        return new AuthResult(true, null, u);
    }

    private static UserRecord map(ResultSet rs) throws SQLException {
        UserRecord u = new UserRecord();
        u.userId = rs.getLong("user_id");
        u.username = rs.getString("username");
        u.passwordHash = rs.getString("password_hash");
        u.fullName = rs.getString("full_name");
        u.department = rs.getString("department");
        u.role = rs.getString("role");
        u.email = rs.getString("email");
        u.status = rs.getString("status");
        return u;
    }

    public static java.util.List<UserRecord> getAllUsers() {
        java.util.List<UserRecord> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static class UserRecord {
        public long userId;
        public String username;
        public String passwordHash;
        public String fullName;
        public String department;
        public String role;
        public String email;
        public String status;
    }

    public static class AuthResult {
        public final boolean ok;
        public final String error;
        public final UserRecord user;
        public AuthResult(boolean ok, String error, UserRecord user) {
            this.ok = ok;
            this.error = error;
            this.user = user;
        }
    }
}
