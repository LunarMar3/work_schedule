package pers.ember.myapplication.Entity;

import java.util.List;

public class ProgressNode {
    private String id;
    private String name;
    private String description;
    private String icon;
    private List<String> next;
    private String before;
    private boolean finished;
    private double x;

    private double y;

    public ProgressNode(String id, String name, String description, String icon, List<String> next, String before, boolean finished, double x, double y) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.next = next;
        this.before = before;
        this.finished = finished;
        this.x = x;
        this.y = y;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getX() {return x;}

    public void setX(double x) {this.x = x;}

    public double getY() {return y;}

    public void setY(double y) {this.y = y;}

    public String getBefore() {
        return before;
    }
    public void setBefore(String before) {this.before = before;}
    public boolean getFinished() { return finished; }
    public void setFinished(boolean finished) {this.finished = finished;}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getNext() {
        return next;
    }

    public void setNext(List<String> next) {
        this.next = next;
    }
}
