package com.lightspeed.unisync.core.model;

import com.lightspeed.unisync.core.interfaces.ConflictResolver;
import com.lightspeed.unisync.core.interfaces.Trigger;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class SyncAlgorithm {
    final Consumer<Row> writeRow;
    final Consumer<Integer> deleteRow;
    final Function<Integer, Row> readRow;
    final ConflictResolver conflictResolver;
    final Trigger tableTriggers;


    final Map<Integer, Row> validNewRows;
    final Map<Integer, Long> serverRows;
    final Map<Integer, Long> currentClientRows;
    final Map<Integer, Long> previousClientRows;

    public SyncAlgorithm(Consumer<Row> writeRow, Consumer<Integer> deleteRow, Function<Integer, Row> readRow, ConflictResolver conflictResolver, Trigger tableTriggers, Map<Integer, Row> validNewRows, Map<Integer, Long> serverRows, Map<Integer, Long> currentClientRows, Map<Integer, Long> previousClientRows) {
        this.writeRow = writeRow;
        this.deleteRow = deleteRow;
        this.readRow = readRow;
        this.conflictResolver = conflictResolver;
        this.tableTriggers = tableTriggers;
        this.validNewRows = validNewRows;
        this.serverRows = serverRows;
        this.currentClientRows = currentClientRows;
        this.previousClientRows = previousClientRows;
    }

    void processSharedRow(int rowId, Set<Row> newOrModifiedRows) {
        long serverRowHash = serverRows.get(rowId);
        long clientRowHash = currentClientRows.get(rowId);
        if (serverRowHash != clientRowHash) {
            // row has been modified
            boolean changedOnClient = currentClientRows.get(rowId) != previousClientRows.get(rowId);
            boolean changedOnServer = serverRows.get(rowId) != previousClientRows.get(rowId);
            if (changedOnClient && !changedOnServer) {
                if (validNewRows.containsKey(rowId)) {
                    Row r = validNewRows.get(rowId);
                    this.writeRow.accept(r);
                    if (this.tableTriggers != null)
                        this.tableTriggers.onRowModified(r);
                }
            } else if (!changedOnClient && changedOnServer) {
                newOrModifiedRows.add(this.readRow.apply(rowId));
            } else if (changedOnClient && changedOnServer) {
                if (validNewRows.containsKey(rowId)) {
                    Row clientRow = validNewRows.get(rowId);
                    Row serverRow = this.readRow.apply(rowId);
                    Row resolvedRow = this.conflictResolver.resolveConflict(clientRow, serverRow);
                    this.writeRow.accept(resolvedRow);
                    newOrModifiedRows.add(resolvedRow);
                    if (this.tableTriggers != null)
                        this.tableTriggers.onRowModified(resolvedRow);
                }
            }
        }
    }

    public void run(Set<Row> newOrModifiedRows, Set<Integer> deletedRows) {
        // process all the rows in the client current set
        for (int currentClientRowId : currentClientRows.keySet()) {
            var inPre = previousClientRows.containsKey(currentClientRowId);
            var inSrv = serverRows.containsKey(currentClientRowId);

            if (!inPre && !inSrv) {
                // new row in server store
                if (validNewRows.containsKey(currentClientRowId)) {
                    Row r = validNewRows.get(currentClientRowId);
                    this.writeRow.accept(r);
                    if (this.tableTriggers != null)
                        this.tableTriggers.onRowCreated(r);
                }
            } else if (inPre && !inSrv) {
                // deleted row in server store
                deletedRows.add(currentClientRowId);
            } else if (inSrv) {
                // both client and server have the row
                processSharedRow(currentClientRowId, newOrModifiedRows);
            }
        }

        // process all the rows in the server set that aren't in the client current set
        for (int serverRowId : serverRows.keySet()) {
            var inClient = currentClientRows.containsKey(serverRowId);
            if (inClient) continue;

            var inPre = previousClientRows.containsKey(serverRowId);
            if (!inPre) {
                // new row for client store
                newOrModifiedRows.add(this.readRow.apply(serverRowId));
            } else {
                // deleted row in client store
                if (this.tableTriggers != null)
                    this.tableTriggers.onRowDeleted(this.readRow.apply(serverRowId));
                this.deleteRow.accept(serverRowId);
            }
        }
    }
}
