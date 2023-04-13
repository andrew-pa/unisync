package com.lightspeed.unisync.core.model;

import java.util.Set;

public class SyncResponse {
    public Set<Integer> getDeletedRows() {
        return deletedRows;
    }

    public void setDeletedRows(Set<Integer> deletedRows) {
        this.deletedRows = deletedRows;
    }

    public Set<Row> getNewOrModifiedRows() {
        return newOrModifiedRows;
    }

    public void setNewOrModifiedRows(Set<Row> newOrModifiedRows) {
        this.newOrModifiedRows = newOrModifiedRows;
    }

    public Set<InvalidRow> getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(Set<InvalidRow> invalidRows) {
        this.invalidRows = invalidRows;
    }

    public Set<Integer> deletedRows;
    public Set<Row> newOrModifiedRows;
    public Set<InvalidRow> invalidRows;

    public SyncResponse(Set<Integer> deletedRows, Set<Row> newOrModifiedRows, Set<InvalidRow> invalidRows) {
        this.deletedRows = deletedRows;
        this.newOrModifiedRows = newOrModifiedRows;
        this.invalidRows = invalidRows;
    }

    @Override
    public String toString() {
        return "SyncResponse{" +
                "deletedRows=" + deletedRows +
                ", newOrModifiedRows=" + newOrModifiedRows +
                ", invalidRows=" + invalidRows +
                '}';
    }
}
