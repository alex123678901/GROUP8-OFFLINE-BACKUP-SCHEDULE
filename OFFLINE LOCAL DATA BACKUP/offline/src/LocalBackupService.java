import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalBackupService {
    public interface Progress {
        void onProgress(int percent, String message);
        void onDone(boolean ok, String details);
    }

    private static final ExecutorService pool = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

    public static void runBackup(String backupName,
                                 String sourcePathsSemicolon,
                                 String destinationRoot,
                                 String department,
                                 Progress cb) {
        pool.submit(() -> {
            try {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                File destFolder = new File(destinationRoot, department + "_Backup_" + timestamp);
                if (!destFolder.mkdirs() && !destFolder.exists()) {
                    cb.onDone(false, "Cannot create destination folder: " + destFolder);
                    return;
                }

                String[] sources = sourcePathsSemicolon.split(";");
                int total = countFiles(sources);
                if (total == 0) total = 1;
                final int totalFiles = total;
                int processed = 0;

                for (String s : sources) {
                    File f = new File(s.trim());
                    if (!f.exists()) continue;
                    if (f.isDirectory()) {
                        processed = copyDirectory(f, destFolder, processed, totalFiles, cb);
                    } else {
                        Files.copy(f.toPath(), new File(destFolder, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        processed++;
                        int p = (int) Math.min(100, (processed * 100L) / totalFiles);
                        cb.onProgress(p, "Copying files...");
                    }
                }
                cb.onProgress(100, "Completed");
                cb.onDone(true, destFolder.getAbsolutePath());
            } catch (Exception ex) {
                cb.onDone(false, ex.getMessage());
            }
        });
    }

    private static int copyDirectory(File sourceDir, File destRoot, int processed, int total, Progress cb) throws IOException {
        Path src = sourceDir.toPath();
        Path dst = new File(destRoot, sourceDir.getName()).toPath();
        if (!Files.exists(dst)) Files.createDirectories(dst);
        try (java.util.stream.Stream<Path> stream = Files.walk(src)) {
            for (Path p : (Iterable<Path>) stream::iterator) {
                Path rel = src.relativize(p);
                Path out = dst.resolve(rel);
                if (Files.isDirectory(p)) {
                    if (!Files.exists(out)) Files.createDirectories(out);
                } else {
                    Files.copy(p, out, StandardCopyOption.REPLACE_EXISTING);
                    processed++;
                    int percent = (int) Math.min(100, (processed * 100L) / Math.max(1, total));
                    cb.onProgress(percent, "Copying " + p.getFileName());
                }
            }
        }
        return processed;
    }

    private static int countFiles(String[] sources) {
        int count = 0;
        for (String s : sources) {
            File f = new File(s.trim());
            if (!f.exists()) continue;
            if (f.isDirectory()) {
                try (java.util.stream.Stream<Path> stream = Files.walk(f.toPath())) {
                    count += (int) stream.filter(Files::isRegularFile).count();
                } catch (IOException ignored) {}
            } else {
                count++;
            }
        }
        return count;
    }
}
