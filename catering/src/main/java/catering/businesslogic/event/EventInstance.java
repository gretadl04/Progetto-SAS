package catering.businesslogic.event;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

public class EventInstance {
    private int id;
    private int eventId;
    private Date date;
    private String state;
    private boolean modified;
    private String customLocation;

    public EventInstance() {
        this.state = "programmato";
        this.modified = false;
    }

    public EventInstance(int eventId, Date date, String location) {
        this();
        this.eventId = eventId;
        this.date = date;
        this.customLocation = location;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public boolean isModified() { return modified; }
    public void setModified(boolean modified) { this.modified = modified; }
    public String getCustomLocation() { return customLocation; }
    public void setCustomLocation(String customLocation) { this.customLocation = customLocation; }

    public void saveNewInstance() {
        String query = "INSERT INTO EventInstances (event_id, date, state, is_modified, custom_location) VALUES (?, ?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query, eventId, date != null ? date.toString() : null, state, modified ? 1 : 0, customLocation);
        this.id = PersistenceManager.getLastId();
    }

    public void updateInstance() {
        String query = "UPDATE EventInstances SET date = ?, state = ?, is_modified = ?, custom_location = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, date != null ? date.toString() : null, state, modified ? 1 : 0, customLocation, id);
    }

    public void deleteInstance() {
        String query = "DELETE FROM EventInstances WHERE id = ?";
        PersistenceManager.executeUpdate(query, id);
    }

    public static ArrayList<EventInstance> loadInstancesForEvent(int eventId) {
        ArrayList<EventInstance> list = new ArrayList<>();
        String query = "SELECT * FROM EventInstances WHERE event_id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                EventInstance ei = new EventInstance();
                ei.id = rs.getInt("id");
                ei.eventId = rs.getInt("event_id");
                String d = rs.getString("date");
                if (d != null) ei.date = Date.valueOf(d);
                ei.state = rs.getString("state");
                ei.modified = rs.getInt("is_modified") == 1;
                ei.customLocation = rs.getString("custom_location");
                list.add(ei);
            }
        }, eventId);
        return list;
    }
}