package com.lightspeed.unisync.core.interfaces;

import com.lightspeed.unisync.core.model.Row;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DataAccess {
    /**
     * Gather the IDs and content hashes of every row in the table associated with a user
     * @param tableName the name of the table
     * @param userId the user ID
     * @return a map of row IDs to row hashes in the table associated with the user
     */
    Map<Integer, Long> rowIdsInTable(String tableName, UUID userId);

    /**
     * Reads a row out of the store
     * @param tableName the table the row is in
     * @param userId the user it is associated with
     * @param rowId the id of the desired row
     * @return the row data
     */
    Row readRow(String tableName, UUID userId, int rowId);

    void writeRow(String tableName, UUID userId, Row newRow);

    void deleteRow(String tableName, UUID userId, int rowId);
}
