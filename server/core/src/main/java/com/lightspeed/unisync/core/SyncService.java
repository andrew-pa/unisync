package com.lightspeed.unisync.core;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.interfaces.IdentityService;
import com.lightspeed.unisync.core.interfaces.Validator;
import com.lightspeed.unisync.core.model.*;

import java.util.*;

public class SyncService {
    final DataAccess data;
    final IdentityService identity;

    /// name -> table
    final Map<String, Table> tables;

    public SyncService(DataAccess data, IdentityService identity, Map<String, Table> tables) {
        this.data = data;
        this.identity = identity;
        this.tables = tables;
    }

    /**
     * Split incoming new rows into a set of invalid rows and a map of valid row IDs to valid row data objects
     *
     * @param validator    the validator to use to perform validation
     * @param newRows      the incoming new rows
     * @param invalidRows  output set of invalid rows
     * @param validNewRows output map of valid rows
     */
    void validateIncomingRows(Validator validator, Set<Row> newRows, Set<InvalidRow> invalidRows, Map<Integer, Row> validNewRows) {
        for (Row newRow : newRows) {
            Optional<InvalidRow> maybeInvalid = validator.isValid(newRow);
            if (maybeInvalid.isPresent()) {
                invalidRows.add(maybeInvalid.get());
            } else {
                validNewRows.put(newRow.id, newRow);
            }
        }
    }

    public SyncResponse syncTable(SyncRequest request) {
        var maybeUserId = this.identity.checkSession(request.sessionId);
        if (maybeUserId.isEmpty()) {
            throw new RuntimeException("unauthorized request");
        }
        var userId = maybeUserId.get();
        var table = this.tables.get(request.tableName);
        var serverRows = this.data.rowIdsInTable(table.name, userId);

        Set<InvalidRow> invalidRows = new HashSet<>();
        Map<Integer, Row> validNewRows = new HashMap<>();
        this.validateIncomingRows(table.validator, request.newRows, invalidRows, validNewRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();

        (new SyncAlgorithm(
                r -> this.data.writeRow(table.name, userId, r),
                id -> this.data.deleteRow(table.name, userId, id),
                id -> this.data.readRow(table.name, userId, id),
                table.conflictResolver,
                validNewRows,
                serverRows,
                request.currentRows,
                request.previousRows)).run(newOrModifiedRows, deletedRows);

        return new SyncResponse(deletedRows, newOrModifiedRows, invalidRows);
    }
}
