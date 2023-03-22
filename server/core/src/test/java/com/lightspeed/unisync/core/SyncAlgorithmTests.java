package com.lightspeed.unisync.core;

import com.lightspeed.unisync.core.model.Row;
import com.lightspeed.unisync.core.model.SyncAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

public class SyncAlgorithmTests {
    Map<Integer, Row> serverRows;
    Map<Integer, Long> serverRowHashes;

    @BeforeEach
    public void initTestData() {
        serverRows = new HashMap<>(Map.of(
                0, new Row(0, List.of("Bob", "bob@example.com", "34")),
                1, new Row(1, List.of("Alice", "alice@example.com", "27")),
                2, new Row(2, List.of("Frank", "frank@example.com", "61"))));
        serverRowHashes
                = serverRows.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, ir -> ir.getValue().dataHash));
    }

    @Test
    public void initialSync() {
        // client is empty, server has data; at end, client and server data should be equal
        Map<Integer, Row> validNewRows = Map.of();
        Map<Integer, Long> currentClientRows = Map.of();
        Map<Integer, Long> previousClientRows = Map.of();

        var algo = new SyncAlgorithm(null, null, serverRows::get, null,
                validNewRows, serverRowHashes, currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Assertions.assertEquals(serverRows.size(), newOrModifiedRows.size());
        for(var row : serverRows.values()) {
            Assertions.assertTrue(newOrModifiedRows.contains(row));
        }
        Assertions.assertEquals(0, deletedRows.size());
    }

    @Test
    public void newRowOnServerTransferredToClient() {
        Map<Integer, Row> validNewRows = Map.of();
        Map<Integer, Long> currentClientRows = Map.copyOf(serverRowHashes);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        var newRow = new Row(4, List.of("George", "george@example.com", "23"));
        serverRows.put(newRow.id, newRow);
        serverRowHashes.put(newRow.id, newRow.dataHash);

        var algo = new SyncAlgorithm(null, null, serverRows::get, null,
                validNewRows, serverRowHashes, currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Assertions.assertEquals(1, newOrModifiedRows.size());
        Assertions.assertTrue(newOrModifiedRows.contains(newRow));
        Assertions.assertEquals(0, deletedRows.size());
    }
}
