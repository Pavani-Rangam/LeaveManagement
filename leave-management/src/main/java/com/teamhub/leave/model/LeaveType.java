package com.teamhub.leave.model;

public enum LeaveType {
    ANNUAL("Annual Leave"), SICK("Sick Leave"), CASUAL("Casual Leave"), OTHER("Other Leave");
    private final String label;
    LeaveType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
