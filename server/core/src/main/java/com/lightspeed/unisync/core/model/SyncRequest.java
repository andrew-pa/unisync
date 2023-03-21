package com.lightspeed.unisync.core.model;

import java.util.Set;
import java.util.UUID;

public class SyncRequest {
    public final String tableName;
    public final UUID sessionId;

    public final Set<Integer> currentRows;
    public final Set<Integer> previousSyncRows;

    // rows that are not in previousSyncRows but are in currentRows
    public final Set<Row> newRows;

    public SyncRequest(String tableName, UUID sessionId, Set<Integer> currentRows, Set<Integer> previousSyncRows, Set<Row> newRows) {
        this.tableName = tableName;
        this.sessionId = sessionId;
        this.currentRows = currentRows;
        this.previousSyncRows = previousSyncRows;
        this.newRows = newRows;
    }
}
