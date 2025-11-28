import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String remote = socket.getRemoteSocketAddress().toString();
        System.out.println("Client connected: " + remote);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"))) {

            // Simple line-oriented protocol (JSON payloads for responses)
            // Commands:
            // PING -> {"ok":true,"ts":...}
            // AUTH <username> <password_hash> -> {ok, role, department}
            // LIST_LOGS -> {logs:[...]}

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("PING")) {
                    writeJson(out, "{\"ok\":true,\"ts\":" + System.currentTimeMillis() + "}");
                } else if (line.startsWith("AUTH ")) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length < 3) { writeJson(out, "{\"ok\":false,\"error\":\"bad_auth_format\"}"); continue; }
                    String username = parts[1];
                    String pwdHash = parts[2];
                    handleAuth(username, pwdHash, out);
                } else if (line.equals("LIST_LOGS")) {
                    handleListLogs(out);
                } else {
                    writeJson(out, "{\"ok\":false,\"error\":\"unknown_command\"}");
                }
            }
        } catch (IOException e) {
            System.err.println("Client IO error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("Client disconnected: " + remote);
        }
    }

    private void handleAuth(String username, String pwdHash, BufferedWriter out) throws IOException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT role, department, password_hash, status FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    writeJson(out, "{\"ok\":false,\"error\":\"user_not_found\"}");
                    return;
                }
                String dbHash = rs.getString("password_hash");
                String role = rs.getString("role");
                String department = rs.getString("department");
                String status = rs.getString("status");
                if (!dbHash.equalsIgnoreCase(pwdHash)) {
                    writeJson(out, "{\"ok\":false,\"error\":\"invalid_password\"}");
                    return;
                }
                if (!"Active".equalsIgnoreCase(status)) {
                    writeJson(out, "{\"ok\":false,\"error\":\"inactive_user\"}");
                    return;
                }
                writeJson(out, String.format("{\"ok\":true,\"role\":\"%s\",\"department\":\"%s\"}", escape(role), escape(department)));
            }
        } catch (SQLException e) {
            writeJson(out, String.format("{\"ok\":false,\"error\":\"db_error:%s\"}", escape(e.getMessage())));
        }
    }

    private void handleListLogs(BufferedWriter out) throws IOException {
        List<String> items = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT timestamp, user_id, task_id, action, status, LEFT(message, 120) AS msg FROM backup_logs ORDER BY timestamp DESC LIMIT 50")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String obj = String.format("{\"ts\":\"%s\",\"userId\":%d,\"taskId\":%s,\"action\":\"%s\",\"status\":\"%s\",\"message\":\"%s\"}",
                            rs.getTimestamp("timestamp"),
                            rs.getLong("user_id"),
                            rs.getObject("task_id") == null ? "null" : String.valueOf(rs.getInt("task_id")),
                            escape(rs.getString("action")),
                            escape(rs.getString("status")),
                            escape(rs.getString("msg")));
                    items.add(obj);
                }
            }
            String json = "{\"ok\":true,\"logs\":[" + String.join(",", items) + "]}";
            writeJson(out, json);
        } catch (SQLException e) {
            writeJson(out, String.format("{\"ok\":false,\"error\":\"db_error:%s\"}", escape(e.getMessage())));
        }
    }

    private static void writeJson(BufferedWriter out, String json) throws IOException {
        out.write(json);
        out.write("\n");
        out.flush();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
