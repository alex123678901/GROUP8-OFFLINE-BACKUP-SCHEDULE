import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class PasswordUtil {
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static String validatePasswordStrength(String pwd) {
        if (pwd == null || pwd.length() < 8) return "Password must be at least 8 characters";
        if (pwd.contains(" ")) return "Password must not contain spaces";
        if (!Pattern.compile("[A-Z]").matcher(pwd).find()) return "Password must contain an uppercase letter";
        if (!Pattern.compile("[a-z]").matcher(pwd).find()) return "Password must contain a lowercase letter";
        if (!Pattern.compile("[0-9]").matcher(pwd).find()) return "Password must contain a digit";
        // Any non-alphanumeric character counts as special
        if (!Pattern.compile("[^A-Za-z0-9]").matcher(pwd).find()) return "Password must contain a special character";
        return null; // null means OK
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        // Simple email regex
        return Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matcher(email).matches();
    }
}
