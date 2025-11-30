package dev.educore.eduxel.ui.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ActivityEntry {

    private final StringProperty time = new SimpleStringProperty();
    private final StringProperty area = new SimpleStringProperty();
    private final StringProperty action = new SimpleStringProperty();
    private final StringProperty user = new SimpleStringProperty();

    public ActivityEntry(String time, String area, String action, String user) {
        this.time.set(time);
        this.area.set(area);
        this.action.set(action);
        this.user.set(user);
    }

    public String getTime() {
        return time.get();
    }

    public StringProperty timeProperty() {
        return time;
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public String getArea() {
        return area.get();
    }

    public StringProperty areaProperty() {
        return area;
    }

    public void setArea(String area) {
        this.area.set(area);
    }

    public String getAction() {
        return action.get();
    }

    public StringProperty actionProperty() {
        return action;
    }

    public void setAction(String action) {
        this.action.set(action);
    }

    public String getUser() {
        return user.get();
    }

    public StringProperty userProperty() {
        return user;
    }

    public void setUser(String user) {
        this.user.set(user);
    }
}
