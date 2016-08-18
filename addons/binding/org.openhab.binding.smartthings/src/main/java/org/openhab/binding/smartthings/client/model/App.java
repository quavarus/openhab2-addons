package org.openhab.binding.smartthings.client.model;

public class App {

    private String id;
    private String label;
    private String installedSmartAppParentId;
    private String state;
    private int solutionCount;
    private int sortOrder;
    private boolean isExecutingLocally;
    private Settings settings;

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getInstalledSmartAppParentId() {
        return installedSmartAppParentId;
    }

    public String getState() {
        return state;
    }

    public int getSolutionCount() {
        return solutionCount;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isExecutingLocally() {
        return isExecutingLocally;
    }

    public Settings getSettings() {
        return settings;
    }

}
