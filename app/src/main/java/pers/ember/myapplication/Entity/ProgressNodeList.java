package pers.ember.myapplication.Entity;

import java.util.List;


public class ProgressNodeList {
    private List<ProgressNode> progressNodes;

    public ProgressNodeList(List<ProgressNode> progressNodes) {
        this.progressNodes = progressNodes;
    }
    public List<ProgressNode> getProgressNodes() {
        return progressNodes;
    }
    public void setProgressNodes(List<ProgressNode> progressNodes) {
        this.progressNodes = progressNodes;
    }
}
