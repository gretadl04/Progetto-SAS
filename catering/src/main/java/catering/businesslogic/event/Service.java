package catering.businesslogic.event;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;

import catering.businesslogic.menu.Menu;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

public class Service {
    private int id;
    private String name;
    private Date date;
    private Time timeStart;
    private Time timeEnd;
    private String location;
    private int eventId;
    private Menu menu;

    // Estensioni per Gestire gli Eventi
    private boolean menuApproved;
    private String proposedMenuModifications;
    private ArrayList<StaffRequest> staffRequests;

    public Service() {
        this.staffRequests = new ArrayList<>();
        this.menuApproved = false;
    }

    public Service(String name) {
        this();
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public Time getTimeStart() { return timeStart; }
    public void setTimeStart(Time timeStart) { this.timeStart = timeStart; }
    public Time getTimeEnd() { return timeEnd; }
    public void setTimeEnd(Time timeEnd) { this.timeEnd = timeEnd; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public Menu getMenu() { return menu; }
    public int getMenuId() { return menu != null ? menu.getId() : 0; }
    public void setMenu(Menu menu) { this.menu = menu; }

    public boolean isMenuApproved() { return menuApproved; }
    public void setMenuApproved(boolean menuApproved) { this.menuApproved = menuApproved; }
    public String getProposedMenuModifications() { return proposedMenuModifications; }
    public void setProposedMenuModifications(String mods) { this.proposedMenuModifications = mods; }
    public ArrayList<StaffRequest> getStaffRequests() { return staffRequests; }

    public void addStaffRequest(StaffRequest sr) {
        if (this.staffRequests == null) this.staffRequests = new ArrayList<>();
        this.staffRequests.add(sr);
    }

    public void approveMenu(Menu m, String modifications) {
        this.menu = m;
        this.menuApproved = true;
        this.proposedMenuModifications = modifications;
    }

    public void saveNewService() {
        String query = "INSERT INTO Services (event_id, name, service_date, time_start, time_end, location, approved_menu_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String dStr = (date != null) ? date.toString() : null;
        String tStart = (timeStart != null) ? timeStart.toString() : null;
        String tEnd = (timeEnd != null) ? timeEnd.toString() : null;

        PersistenceManager.executeUpdate(query, eventId, name, dStr, tStart, tEnd, location, getMenuId());
        this.id = PersistenceManager.getLastId();
    }

    public void updateService() {
        String query = "UPDATE Services SET name = ?, service_date = ?, time_start = ?, time_end = ?, location = ?, approved_menu_id = ? WHERE id = ?";
        String dStr = (date != null) ? date.toString() : null;
        String tStart = (timeStart != null) ? timeStart.toString() : null;
        String tEnd = (timeEnd != null) ? timeEnd.toString() : null;

        PersistenceManager.executeUpdate(query, name, dStr, tStart, tEnd, location, getMenuId(), id);
    }

    public void saveMenuProposal() {
        String query = "INSERT INTO MenuProposals (service_id, menu_id, approved, proposed_modifications) VALUES (?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query, id, getMenuId(), menuApproved ? 1 : 0, proposedMenuModifications);
    }

    public boolean deleteService() {
        for (StaffRequest sr : staffRequests) {
            sr.deleteStaffRequest();
        }
        staffRequests.clear();
        String query = "DELETE FROM Services WHERE id = ?";
        return PersistenceManager.executeUpdate(query, id) > 0;
    }

    public static ArrayList<Service> loadServicesForEvent(int eventId) {
        ArrayList<Service> services = new ArrayList<>();
        String query = "SELECT * FROM Services WHERE event_id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                services.add(mapRowToService(rs));
            }
        }, eventId);

        for (Service s : services) {
            try {
                s.staffRequests = StaffRequest.loadRequestsForService(s.id);
            } catch (Exception ignore) {}
        }
        return services;
    }

    public static Service loadById(int id) {
        String query = "SELECT * FROM Services WHERE id = ?";
        return loadServiceByQuery(query, id);
    }

    public static Service loadByName(String name) {
        String query = "SELECT * FROM Services WHERE name = ?";
        return loadServiceByQuery(query, name);
    }

    private static Service mapRowToService(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.id = rs.getInt("id");
        s.name = rs.getString("name");
        String dStr = rs.getString("service_date");
        if (dStr != null && !dStr.isEmpty()) {
            try { s.date = Date.valueOf(dStr); } catch (Exception ignore) {}
        }
        String tsStr = rs.getString("time_start");
        if (tsStr != null && !tsStr.isEmpty()) {
            try { s.timeStart = Time.valueOf(tsStr); } catch (Exception ignore) {}
        }
        String teStr = rs.getString("time_end");
        if (teStr != null && !teStr.isEmpty()) {
            try { s.timeEnd = Time.valueOf(teStr); } catch (Exception ignore) {}
        }
        try { s.location = rs.getString("location"); } catch (SQLException ignore) {}
        s.eventId = rs.getInt("event_id");
        int menuId = 0;
        try { menuId = rs.getInt("approved_menu_id"); } catch (SQLException ignore) {}
        if (menuId > 0) {
            try { s.menu = Menu.load(menuId); } catch (Exception ignore) {}
        }
        return s;
    }

    private static Service loadServiceByQuery(String query, Object param) {
        final Service[] holder = new Service[1];
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                holder[0] = mapRowToService(rs);
            }
        }, param);
        if (holder[0] != null) {
            try {
                holder[0].staffRequests = StaffRequest.loadRequestsForService(holder[0].id);
            } catch (Exception ignore) {}
        }
        return holder[0];
    }

    @Override
    public String toString() {
        return "Service [id=" + id + ", name=" + name + ", date=" + date + "]";
    }
}