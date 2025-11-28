import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersDAO {
    public static class UserRow {
        public long userId;
        public String username;
        public String fullName;
        public String department;
        public String role;
        public String email;
        public String status;
    }

    public static List<UserRow> listAllUsers() {
        String sql = "SELECT user_id, username, full_name, department, role, email, status FROM users ORDER BY created_at DESC";
        List<UserRow> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UserRow r = new UserRow();
                r.userId = rs.getLong("user_id");
                r.username = rs.getString("username");
                r.fullName = rs.getString("full_name");
                r.department = rs.getString("department");
                r.role = rs.getString("role");
                r.email = rs.getString("email");
                r.status = rs.getString("status");
                rows.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public static boolean approveUser(String username) {
        String sql = "UPDATE users SET status='Active' WHERE username=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean assignRole(String username, String role) {
        String sql = "UPDATE users SET role=? WHERE username=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean resetPassword(String username, String newRawPassword) {
        String sql = "UPDATE users SET password_hash=? WHERE username=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.sha256(newRawPassword));
            ps.setString(2, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean updateUser(String username, String fullName, String department, String email) {
        String sql = "UPDATE users SET full_name=?, department=?, email=? WHERE username=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, department);
            ps.setString(3, email);
            ps.setString(4, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Update all editable fields; identify row by old username
    public static boolean updateUserAll(String oldUsername,
                                        String newUsername,
                                        String fullName,
                                        String department,
                                        String role,
                                        String email,
                                        String status) {
        String sql = "UPDATE users SET username=?, full_name=?, department=?, role=?, email=?, status=? WHERE username=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setString(2, fullName);
            ps.setString(3, department);
            ps.setString(4, role);
            ps.setString(5, email);
            ps.setString(6, status);
            ps.setString(7, oldUsername);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
