package pers.ember.myapplication.Entity;

import java.util.List;


public class ProgressNodeList {

    private int id;
    private List<ProgressNode> progressNodes;

    public ProgressNodeList(List<ProgressNode> progressNodes, int id) {
        this.progressNodes = progressNodes;
        this.id = id;
    }
    public ProgressNodeList() {
    }
    public List<ProgressNode> getProgressNodes() {
        return progressNodes;
    }
    public void setProgressNodes(List<ProgressNode> progressNodes) {
        this.progressNodes = progressNodes;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
