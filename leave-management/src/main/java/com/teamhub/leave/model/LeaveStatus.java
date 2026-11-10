package com.teamhub.leave.model;

public enum LeaveStatus {
    PENDING("Pending"), APPROVED("Approved"), REJECTED("Rejected");
    private final String label;
    LeaveStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
