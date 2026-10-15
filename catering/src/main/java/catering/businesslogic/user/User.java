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
        CHEF('h'),
        ORGANIZER('o');

        private char id;

        private Role(char id) {
            this.id = id;
        }

        public char getId() {
            return id;
        }

        public static Role getRoleById(char id) {
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
        this(null);
    }

    public User(String userName) {
        id = 0;
        this.userName = userName != null ? userName : "";
        roles = new HashSet<>();
    }

    public boolean isChef() {
        return roles.contains(Role.CHEF);
    }

    public boolean isCook() {
        return roles.contains(Role.CUOCO);
    }

    public boolean isCuoco() {
        return roles.contains(Role.CUOCO);
    }

    public boolean isServizio() {
        return roles.contains(Role.SERVIZIO);
    }

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

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public static User load(int id) {
        if (loadedUsers.containsKey(id)) return loadedUsers.get(id);

        User[] result = new User[1];
        String query = "SELECT * FROM Users WHERE id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                result[0] = new User();
                result[0].id = rs.getInt("id");
                result[0].userName = rs.getString("username");
            }
        }, id);

        if (result[0] != null) {
            loadRolesForUser(result[0]);
            loadedUsers.put(id, result[0]);
        }
        return result[0];
    }

    public static User load(String username) {
        User[] result = new User[1];
        String query = "SELECT * FROM Users WHERE username = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                result[0] = new User();
                result[0].id = rs.getInt("id");
                result[0].userName = rs.getString("username");
            }
        }, username);

        if (result[0] != null) {
            loadRolesForUser(result[0]);
            loadedUsers.put(result[0].id, result[0]);
        }
        return result[0];
    }

    private static void loadRolesForUser(User u) {
        String roleQuery = "SELECT * FROM UserRoles WHERE user_id = ?";
        PersistenceManager.executeQuery(roleQuery, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                String roleStr = rs.getString("role_id");
                if (roleStr != null && !roleStr.isEmpty()) {
                    Role r = Role.getRoleById(roleStr.charAt(0));
                    if (r != null) u.roles.add(r);
                }
            }
        }, u.id);
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", userName=" + userName + ", roles=" + roles + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User other = (User) obj;
        if (this.id > 0 && other.id > 0) return this.id == other.id;
        return this.userName != null && this.userName.equals(other.userName);
    }

    @Override
    public int hashCode() {
        return id > 0 ? Integer.hashCode(id) : (userName != null ? userName.hashCode() : 0);
    }
}