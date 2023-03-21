package com.lightspeed.unisync.core.model;

import com.lightspeed.unisync.core.interfaces.ConflictResolver;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class SyncAlgorithm {
    final Consumer<Row> writeRow;
    final Consumer<Integer> deleteRow;
    final Function<Integer, Row> readRow;
    final ConflictResolver conflictResolver;


    final Map<Integer, Row> validNewRows;
    final Map<Integer, Long> serverRows;
    final Map<Integer, Long> currentClientRows;
    final Map<Integer, Long> previousClientRows;

    public SyncAlgorithm(Consumer<Row> writeRow, Consumer<Integer> deleteRow, Function<Integer, Row> readRow, ConflictResolver conflictResolver, Map<Integer, Row> validNewRows, Map<Integer, Long> serverRows, Map<Integer, Long> currentClientRows, Map<Integer, Long> previousClientRows) {
        this.writeRow = writeRow;
        this.deleteRow = deleteRow;
        this.readRow = readRow;
        this.conflictResolver = conflictResolver;
        this.validNewRows = validNewRows;
        this.serverRows = serverRows;
        this.currentClientRows = currentClientRows;
        this.previousClientRows = previousClientRows;
    }

    public void run(Set<Row> newOrModifiedRows, Set<Integer> deletedRows) {
        // process all the rows in the client current set
        for(int currentClientRowId : currentClientRows.keySet()) {
            if(!validNewRows.containsKey(currentClientRowId)) continue;

            var inPre = previousClientRows.containsKey(currentClientRowId);
            var inSrv = serverRows.containsKey(currentClientRowId);

            if(!inPre && !inSrv) {
                // new row in server store
                this.writeRow.accept(validNewRows.get(currentClientRowId));
            } else if(inPre && !inSrv) {
                // deleted row in server store
                deletedRows.add(currentClientRowId);
            } else if(inSrv) {
                // both client and server have the row - has it been modified?
                long serverRowHash = serverRows.get(currentClientRowId);
                long clientRowHash = currentClientRows.get(currentClientRowId);
                if(serverRowHash != clientRowHash) {
                    // row has been modified
                    boolean changedOnClient = currentClientRows.get(currentClientRowId) == previousClientRows.get(currentClientRowId);
                    boolean changedOnServer = serverRows.get(currentClientRowId) == previousClientRows.get(currentClientRowId);
                    if(changedOnClient && !changedOnServer) {
                        if(validNewRows.containsKey(currentClientRowId)) {
                            this.writeRow.accept(validNewRows.get(currentClientRowId));
                        }
                    } else if(!changedOnClient && changedOnServer) {
                        newOrModifiedRows.add(this.readRow.apply(currentClientRowId));
                    } else if(changedOnClient && changedOnServer) {
                        if(validNewRows.containsKey(currentClientRowId)) {
                            Row clientRow = validNewRows.get(currentClientRowId);
                            Row serverRow = this.readRow.apply(currentClientRowId);
                            Row resolvedRow = this.conflictResolver.resolveCoflict(clientRow, serverRow);
                            this.writeRow.accept(resolvedRow);
                            newOrModifiedRows.add(resolvedRow);
                        }
                    }
                }
            }
        }

        // process all the rows in the server set that aren't in the client current set
        for(int serverRowId : serverRows.keySet()) {
            var inClient = currentClientRows.containsKey(serverRowId);
            if(inClient) continue;

            var inPre = previousClientRows.containsKey(serverRowId);
            if(!inPre) {
                // new row for client store
                newOrModifiedRows.add(this.readRow.apply(serverRowId));
            } else {
                // deleted row in client store
                this.deleteRow.accept(serverRowId);
            }
        }
    }
}
