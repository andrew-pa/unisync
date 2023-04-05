package com.lightspeed.unisync.core.model;

import java.util.Set;

public class SyncResponse {
    public final Set<Integer> deletedRows;
    public final Set<Row> newOrModifiedRows;
    public final Set<InvalidRow> invalidRows;

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
