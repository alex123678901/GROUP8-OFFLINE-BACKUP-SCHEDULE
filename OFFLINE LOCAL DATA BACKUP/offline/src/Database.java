import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    // Adjust these if your MySQL credentials differ
    private static final String URL = "jdbc:mysql://localhost:3306/offline_backup_scheduler?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // XAMPP default
    private static final String PASSWORD = "";  // XAMPP default (empty)

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
