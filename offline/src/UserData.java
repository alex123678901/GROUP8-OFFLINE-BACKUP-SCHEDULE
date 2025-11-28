import java.io.Serializable;

public class UserData implements Serializable {
    public String fullName;
    public String role;
    public String department;
    public String password;
    public boolean approved;
    public String assignedRoles;

    public UserData(String fullName, String department, String password) {
        this.fullName = fullName;
        this.role = "PENDING";
        this.department = department;
        this.password = password;
        this.approved = false;
        this.assignedRoles = "";
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ", " + department + ") - " + (approved ? "APPROVED" : "PENDING APPROVAL");
    }
}