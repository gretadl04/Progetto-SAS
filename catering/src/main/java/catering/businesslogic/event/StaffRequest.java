package catering.businesslogic.event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import catering.businesslogic.shift.Shift;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

public class StaffRequest {
    private int id;
    private int serviceId;
    private Shift shift;
    private String role;
    private int quantity;
    private User assignedUser;

    public StaffRequest() {}

    public StaffRequest(int serviceId, Shift shift, String role, int quantity) {
        this.serviceId = serviceId;
        this.shift = shift;
        this.role = role;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public User getAssignedUser() { return assignedUser; }
    public void setAssignedUser(User assignedUser) { this.assignedUser = assignedUser; }

    public void saveNewStaffRequest() {
        int shiftId = (shift != null) ? shift.getId() : 0;
        int userId = (assignedUser != null) ? assignedUser.getId() : 0;
        String query = "INSERT INTO StaffRequests (service_id, shift_id, role, quantity, assigned_user_id) VALUES (?, ?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query, serviceId, shiftId, role, quantity, userId);
        this.id = PersistenceManager.getLastId();
    }

    public void deleteStaffRequest() {
        String query = "DELETE FROM StaffRequests WHERE id = ?";
        PersistenceManager.executeUpdate(query, id);
    }

    public static ArrayList<StaffRequest> loadRequestsForService(int serviceId) {
        ArrayList<StaffRequest> list = new ArrayList<>();
        String query = "SELECT * FROM StaffRequests WHERE service_id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                StaffRequest sr = new StaffRequest();
                sr.id = rs.getInt("id");
                sr.serviceId = rs.getInt("service_id");
                sr.role = rs.getString("role");
                sr.quantity = rs.getInt("quantity");
                int shiftId = rs.getInt("shift_id");
                if (shiftId > 0) {
                    sr.shift = Shift.loadItemById(shiftId);
                }
                int userId = rs.getInt("assigned_user_id");
                if (userId > 0) {
                    sr.assignedUser = User.load(userId);
                }
                list.add(sr);
            }
        }, serviceId);
        return list;
    }
}