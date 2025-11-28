import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupServer {
    public static final int PORT = 5050; // LAN port
    public static final String CENTRAL_BACKUP_ROOT = "C:/xampp/htdocs/OFFLINE BACKED UP DATA"; // central storage

    private final ExecutorService clientPool = Executors.newCachedThreadPool();

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("BackupServer listening on port " + PORT);
            while (true) {
                Socket client = serverSocket.accept();
                clientPool.submit(new ClientHandler(client));
            }
        }
    }

    public static void main(String[] args) {
        try {
            new BackupServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
