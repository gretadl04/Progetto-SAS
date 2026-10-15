package catering.businesslogic.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

public class User {

    private static Map<Integer, User> loadedUsers = new HashMap<>();

    public static enum Role {
        SERVIZIO('s'),
        CUOCO('c'),
        CHEF('c'),
        ORGANIZER('o'); // Ruolo aggiunto per gestire gli eventi

        private char id;

        private Role(char id) {
            this.id = id;
        }

        public static Role loadRoleById(char id) {
            for (Role r : Role.values()) {
                if (r.id == id) return r;
            }
            return null;
        }
    }

    private int id;
    private String userName;
    private Set<Role> roles;

    public User() {
        id = 0;
        userName = "";
        roles = new HashSet<>();
    }
    public User(String userName) {
        this();
        this.userName = userName;
    }

    public boolean isChef() {
        return roles.contains(Role.CHEF);
    }

    public boolean isCuoco() {
        return roles.contains(Role.CUOCO);
    }

    public boolean isServizio() {
        return roles.contains(Role.SERVIZIO);
    }

    // Metodo aggiunto per verificare se l'utente è un Organizzatore
    public boolean isOrganizer() {
        return roles.contains(Role.ORGANIZER);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUserName() {
        return userName;
    }

    public static User load(int id) {
        if (loadedUsers.containsKey(id)) return loadedUsers.get(id);

        User[] result = new User[1];
        String query = "SELECT * FROM Users WHERE id = " + id;
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                result[0] = new User();
                result[0].id = rs.getInt("id");
                result[0].userName = rs.getString("username");
            }
        });

        if (result[0] != null) {
            String roleQuery = "SELECT * FROM UserRoles WHERE user_id = " + id;
            PersistenceManager.executeQuery(roleQuery, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    String role = rs.getString("role_id");
                    result[0].roles.add(Role.loadRoleById(role.charAt(0)));
                }
            });
            loadedUsers.put(id, result[0]);
        }
        return result[0];
    }

    public static User load(String username) {
        User[] result = new User[1];
        String query = "SELECT * FROM Users WHERE username = '" + username + "'";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                int id = rs.getInt("id");
                result[0] = load(id);
            }
        });
        return result[0];
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", userName=" + userName + ", roles=" + roles + "]";
    }
}