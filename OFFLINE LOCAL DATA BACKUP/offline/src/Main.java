public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   RP KARONGI BACKUP SCHEDULER SYSTEM");
        System.out.println("=========================================");
        System.out.println("🚀 Starting Login System...");
        
        // Skip creating default admin user
        // Skip creating default admin user
        // UserDAO.ensureDefaultAdmin();  // Commented out to prevent default admin creation
        
        new LoginSystem();
    }
}