package dk.easv.be;

public class User {
    private int id;
    private String role;
    private String name;
    private String login;
    private String password;
    private String salt;

    public User(int id, String role, String name, String login, String password, String salt) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.login = login;
        this.password = password;
        this.salt = salt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void  setRole(String role) {
        this.role = role;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getLogin() {
        return login;
    }

    public void setLogin(String email) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}