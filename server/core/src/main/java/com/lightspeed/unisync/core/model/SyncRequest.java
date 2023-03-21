package com.lightspeed.unisync.core.model;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SyncRequest {
    public final String tableName;
    public final UUID sessionId;

    /**
     * The ID and hash of each row that the client currently has
     */
    public final Map<Integer, Long> currentRows;

    /**
     * The ID and hash of each row that was present on the client after the previous sync
     */
    public final Map<Integer, Long> previousRows;

    /**
     * row data for rows that are not in previousRows but are in currentRows
     */
    public final Set<Row> newRows;

    public SyncRequest(String tableName, UUID sessionId, Map<Integer, Long> currentRows, Map<Integer, Long> previousRows, Set<Row> newRows) {
        this.tableName = tableName;
        this.sessionId = sessionId;
        this.currentRows = currentRows;
        this.previousRows = previousRows;
        this.newRows = newRows;
    }
}
