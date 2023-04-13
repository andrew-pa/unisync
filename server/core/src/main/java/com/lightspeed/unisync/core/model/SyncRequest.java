package com.lightspeed.unisync.core.model;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SyncRequest {
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Map<Integer, Long> getCurrentRows() {
        return currentRows;
    }

    public void setCurrentRows(Map<Integer, Long> currentRows) {
        this.currentRows = currentRows;
    }

    public Map<Integer, Long> getPreviousRows() {
        return previousRows;
    }

    public void setPreviousRows(Map<Integer, Long> previousRows) {
        this.previousRows = previousRows;
    }

    public Set<Row> getNewRows() {
        return newRows;
    }

    public void setNewRows(Set<Row> newRows) {
        this.newRows = newRows;
    }

    public String tableName;
    public UUID sessionId;

    /**
     * The ID and hash of each row that the client currently has
     */
    public Map<Integer, Long> currentRows;

    /**
     * The ID and hash of each row that was present on the client after the previous sync
     */
    public Map<Integer, Long> previousRows;

    /**
     * row data for rows that are new or have been modified
     */
    public Set<Row> newRows;

    public SyncRequest(String tableName, UUID sessionId, Map<Integer, Long> currentRows, Map<Integer, Long> previousRows, Set<Row> newRows) {
        this.tableName = tableName;
        this.sessionId = sessionId;
        this.currentRows = currentRows;
        this.previousRows = previousRows;
        this.newRows = newRows;
    }

    @Override
    public String toString() {
        return "SyncRequest{" +
                "tableName='" + tableName + '\'' +
                ", sessionId=" + sessionId +
                ", currentRows=" + currentRows +
                ", previousRows=" + previousRows +
                ", newRows=" + newRows +
                '}';
    }
}
