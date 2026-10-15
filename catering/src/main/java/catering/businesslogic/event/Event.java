package catering.businesslogic.event;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

public class Event {

    private int id;
    private String name;
    private Date dateStart;
    private Date dateEnd;
    private User chef;
    private ArrayList<Service> services;
    
    // Nuove variabili aggiunte
    private String location;
    private String state;

    public Event() {
        services = new ArrayList<>();
        this.state = "in lavorazione";
    }

    public Event(String name) {
        this();
        this.name = name;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Date getDateStart() { return dateStart; }
    public void setDateStart(Date dateStart) { this.dateStart = dateStart; }
    public Date getDateEnd() { return dateEnd; }
    public void setDateEnd(Date dateEnd) { this.dateEnd = dateEnd; }
    public User getChef() { return chef; }
    public int getChefId() { return chef != null ? chef.getId() : 0; }
    public void setChef(User chef) { this.chef = chef; }
    public void setChefId(int chefId) { this.chef = User.load(chefId); }
    public ArrayList<Service> getServices() { return services; }
    public void setServices(ArrayList<Service> services) { this.services = services; }

    public void addService(Service service) {
        if (services == null) services = new ArrayList<>();
        services.add(service);
    }
    public void removeService(Service service) {
        if (services != null) services.remove(service);
    }
    public boolean containsService(Service service) {
        if (services != null) return services.contains(service);
        return false;
    }

    // --- QUERY AGGIORNATE CON LOCATION E STATE ---
    
    public void saveNewEvent() {
        String query = "INSERT INTO Events (name, date_start, date_end, chef_id, location, state) VALUES (?, ?, ?, ?, ?, ?)";
        Long startTimestamp = (dateStart != null) ? dateStart.getTime() : null;
        Long endTimestamp = (dateEnd != null) ? dateEnd.getTime() : null;
        PersistenceManager.executeUpdate(query, name, startTimestamp, endTimestamp, getChefId(), location, state);
        id = PersistenceManager.getLastId();
    }

    public void updateEvent() {
        String query = "UPDATE Events SET name = ?, date_start = ?, date_end = ?, chef_id = ?, location = ?, state = ? WHERE id = ?";
        Long startTimestamp = (dateStart != null) ? dateStart.getTime() : null;
        Long endTimestamp = (dateEnd != null) ? dateEnd.getTime() : null;
        PersistenceManager.executeUpdate(query, name, startTimestamp, endTimestamp, getChefId(), location, state, id);
    }

    public boolean deleteEvent() {
        for (Service service : services) {
            service.deleteService();
        }
        services.clear();
        String query = "DELETE FROM Events WHERE id = ?";
        return PersistenceManager.executeUpdate(query, id) > 0;
    }

    public static ArrayList<Event> loadAllEvents() {
        ArrayList<Event> events = new ArrayList<>();
        String query = "SELECT * FROM Events ORDER BY date_start DESC";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Event e = new Event();
                e.id = rs.getInt("id");
                e.name = rs.getString("name");
                e.dateStart = Date.valueOf(rs.getString("date_start"));
                e.dateEnd = Date.valueOf(rs.getString("date_end"));
                e.location = rs.getString("location"); // Lettura nuovo attributo
                e.state = rs.getString("state");       // Lettura nuovo attributo
                e.chef = User.load(rs.getInt("chef_id"));
                events.add(e);
            }
        });
        for (Event e : events) {
            e.services = Service.loadServicesForEvent(e.id);
        }
        return events;
    }

    public static Event loadById(int id) {
        String query = "SELECT * FROM Events WHERE id = ?";
        return loadEventByQuery(query, id);
    }

    public static Event loadByName(String name) {
        String query = "SELECT * FROM Events WHERE name = ?";
        return loadEventByQuery(query, name);
    }

    private static Event loadEventByQuery(String query, Object param) {
        final Event[] eventHolder = new Event[1];
        final boolean[] eventFound = new boolean[1];

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                eventFound[0] = true;
                Event e = new Event();
                e.id = rs.getInt("id");
                e.name = rs.getString("name");
                e.dateStart = Date.valueOf(rs.getString("date_start"));
                e.dateEnd = Date.valueOf(rs.getString("date_end"));
                e.location = rs.getString("location"); // Lettura nuovo attributo
                e.state = rs.getString("state");       // Lettura nuovo attributo
                try {
                    e.chef = User.load(rs.getInt("chef_id"));
                } catch (Exception ex) {
                    e.chef = null;
                }
                eventHolder[0] = e;
            }
        }, param);

        if (!eventFound[0]) return null;
        
        Event result = eventHolder[0];
        if (result != null) {
            try {
                result.services = Service.loadServicesForEvent(result.id);
            } catch (Exception ex) {
                result.services = new ArrayList<>();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Event [id=" + id + ", name=" + name + ", location=" + location + ", state=" + state + "]";
    }
}