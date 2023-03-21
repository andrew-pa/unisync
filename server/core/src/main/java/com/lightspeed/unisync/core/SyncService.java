package com.lightspeed.unisync.core;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.interfaces.IdentityService;
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

    public SyncResponse syncTable(SyncRequest request) {
        var maybeUserId = this.identity.checkSession(request.sessionId);
        if(maybeUserId.isEmpty()) {
            throw new RuntimeException("unauthorized request");
        }
        var userId = maybeUserId.get();
        var table = this.tables.get(request.tableName);
        var serverRows = this.data.rowIdsInTable(table.name, userId);

        // TODO: this sync code should go somewhere else so it is easier to unit test

        // separate new rows into valid and invalid rows using the validator
        Set<InvalidRow> invalidRows = new HashSet<>();
        Map<Integer, Row> validNewRows = new HashMap<>();
        for (Row newRow : request.newRows) {
            Optional<InvalidRow> maybeInvalid = table.validator.isValid(newRow);
            if (maybeInvalid.isPresent()) {
                invalidRows.add(maybeInvalid.get());
            } else {
                validNewRows.put(newRow.id, newRow);
            }
        }

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();

        for(int currentClientRowId : request.currentRows.keySet()) {
            var inPre = request.previousRows.containsKey(currentClientRowId);
            var inSrv = serverRows.containsKey(currentClientRowId);
            if(!inPre && !inSrv) {
                // new row
            } else if(inPre && !inSrv) {
                // deleted row
            } else if(inSrv) {
                // both client and server have the row - has it been modified?
                long serverRowHash = serverRows.get(currentClientRowId);
                long clientRowHash = request.currentRows.get(currentClientRowId);
                if(serverRowHash != clientRowHash) {
                    // row has been modified
                }
            }
        }

        for(int serverRowId : serverRows.keySet()) {
            var inPre = request.previousRows.containsKey(serverRowId);
            var inClient = request.currentRows.containsKey(serverRowId);
            if(!inPre && !inClient) {
                // new row
            } else if(inPre && !inClient) {
                // deleted row
            }
        }

        return new SyncResponse(deletedRows, newOrModifiedRows, invalidRows);
    }
}
