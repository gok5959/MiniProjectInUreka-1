package swing.ui;

import jdbc.domain.user.model.User;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Session {
    public interface Listener {
        void onLogin(User user);
        void onLogout();
    }

    private static Session instance = new Session();
    private User currentUser;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private Session() {}

    public static Session get() { return instance; }

    public void setCurrentUser(User u) {
        this.currentUser = u;
        for (Listener l : listeners) l.onLogin(u);
    }

    public void logout() {
        this.currentUser = null;
        for (Listener l : listeners) l.onLogout();
    }

    public User getCurrentUser() { return currentUser; }
    public Long getUserId() { return currentUser == null ? null : currentUser.getUserId(); }
    public String getUserName() { return currentUser == null ? null : currentUser.getName(); }
    public String getUserRole() { return currentUser == null ? null : (currentUser.getRole()==null?null:currentUser.getRole().name()); }

    public void addListener(Listener r) { listeners.add(r); }
    public void removeListener(Listener r) { listeners.remove(r); }
}
