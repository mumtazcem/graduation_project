package com.google.maps.android.utils.management.model;

public class Trigger {
    public String triggerID;
    public int triggeredContainer;
    //public double timeStamp;

    public Trigger(String triggerID, int triggeredContainer) {
        this.triggerID = triggerID;
        this.triggeredContainer = triggeredContainer;
    }
}
