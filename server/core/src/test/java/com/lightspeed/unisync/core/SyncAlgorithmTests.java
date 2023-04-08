package com.lightspeed.unisync.core;

import com.lightspeed.unisync.core.interfaces.ConflictResolver;
import com.lightspeed.unisync.core.model.Row;
import com.lightspeed.unisync.core.model.SyncAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SyncAlgorithmTests {
    Map<Integer, Row> serverRows;

    @BeforeEach
    public void initTestData() {
        serverRows = new HashMap<>(Map.of(
                0, new Row(0, List.of("Bob", "bob@example.com", "34")),
                1, new Row(1, List.of("Alice", "alice@example.com", "27")),
                2, new Row(2, List.of("Frank", "frank@example.com", "61"))));
    }

    public Map<Integer, Long> getServerRowHashes() {
        return serverRows.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, ir -> ir.getValue().dataHash));
    }

    @Test
    public void initialSync() {
        // client is empty, server has data; at end, client and server data should be equal
        Map<Integer, Row> validNewRows = Map.of();
        Map<Integer, Long> currentClientRows = Map.of();
        Map<Integer, Long> previousClientRows = Map.of();

        var algo = new SyncAlgorithm(null, null, serverRows::get, null, null,
                validNewRows, getServerRowHashes(), currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Assertions.assertEquals(serverRows.size(), newOrModifiedRows.size());
        for (var row : serverRows.values()) {
            Assertions.assertTrue(newOrModifiedRows.contains(row));
        }
        Assertions.assertEquals(0, deletedRows.size());
    }

    @Test
    public void newRowOnServerTransferredToClient() {
        Map<Integer, Row> validNewRows = Map.of();
        var serverRowHashes = getServerRowHashes();
        Map<Integer, Long> currentClientRows = Map.copyOf(serverRowHashes);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        var newRow = new Row(4, List.of("George", "george@example.com", "23"));
        serverRows.put(newRow.id, newRow);

        var algo = new SyncAlgorithm(null, null, serverRows::get, null, null,
                validNewRows, getServerRowHashes(), currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Assertions.assertEquals(1, newOrModifiedRows.size());
        Assertions.assertTrue(newOrModifiedRows.contains(newRow));
        Assertions.assertEquals(0, deletedRows.size());
    }

    @Test
    public void rowDeletedOnServerDeletedOnClient() {
        Map<Integer, Row> validNewRows = Map.of();
        var serverRowHashes = getServerRowHashes();
        Map<Integer, Long> currentClientRows = Map.copyOf(serverRowHashes);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        serverRows.remove(1);

        var algo = new SyncAlgorithm(null, null, serverRows::get, null, null,
                validNewRows, getServerRowHashes(), currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Assertions.assertEquals(0, newOrModifiedRows.size());
        Assertions.assertEquals(1, deletedRows.size());
        Assertions.assertTrue(deletedRows.contains(1));
    }

    @Test
    public void modifiedRowOnServerTransferredToClient() {
        Map<Integer, Row> validNewRows = Map.of();
        var serverRowHashes = getServerRowHashes();
        Map<Integer, Long> currentClientRows = Map.copyOf(serverRowHashes);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        var newRow = new Row(1, List.of("Alice", "alice@example.com", "28"), 1);
        serverRows.put(newRow.id, newRow);

        var algo = new SyncAlgorithm(null, null, serverRows::get, null, null,
                validNewRows, getServerRowHashes(), currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Assertions.assertEquals(1, newOrModifiedRows.size());
        Assertions.assertTrue(newOrModifiedRows.contains(newRow));
        Assertions.assertEquals(0, deletedRows.size());
    }

    @Test
    public void newRowOnClientTransferredToServer() {
        var newRow = new Row(4, List.of("George", "george@example.com", "23"));
        Map<Integer, Row> validNewRows = Map.of(newRow.id, newRow);
        var serverRowHashes = getServerRowHashes();
        Map<Integer, Long> currentClientRows = new HashMap<>(serverRowHashes);
        currentClientRows.put(newRow.id, newRow.dataHash);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        var writeRow = (Consumer<Row>) Mockito.mock(Consumer.class);

        var algo = new SyncAlgorithm(writeRow, null, serverRows::get, null, null,
                validNewRows, serverRowHashes, currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Mockito.verify(writeRow).accept(newRow);

        Assertions.assertEquals(0, newOrModifiedRows.size());
        Assertions.assertEquals(0, deletedRows.size());
    }

    @Test
    public void rowDeletedOnClientDeletedOnServer() {
        Map<Integer, Row> validNewRows = Map.of();
        var serverRowHashes = getServerRowHashes();
        Map<Integer, Long> currentClientRows = new HashMap<>(serverRowHashes);
        currentClientRows.remove(1);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        var deleteRow = (Consumer<Integer>) Mockito.mock(Consumer.class);

        var algo = new SyncAlgorithm(null, deleteRow, serverRows::get, null, null,
                validNewRows, serverRowHashes, currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Mockito.verify(deleteRow).accept(1);

        Assertions.assertEquals(0, newOrModifiedRows.size());
        Assertions.assertEquals(0, deletedRows.size());
    }

    @Test
    public void modifiedRowOnClientTransferredToServer() {
        var newRow = new Row(1, List.of("Alice", "alice@example.com", "28"), 1);
        Map<Integer, Row> validNewRows = Map.of(newRow.id, newRow);
        var serverRowHashes = getServerRowHashes();
        Map<Integer, Long> currentClientRows = new HashMap<>(serverRowHashes);
        currentClientRows.put(newRow.id, newRow.dataHash);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        var writeRow = (Consumer<Row>) Mockito.mock(Consumer.class);

        var algo = new SyncAlgorithm(writeRow, null, serverRows::get, null, null,
                validNewRows, serverRowHashes, currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Mockito.verify(writeRow).accept(newRow);

        Assertions.assertEquals(0, newOrModifiedRows.size());
        Assertions.assertEquals(0, deletedRows.size());
    }

    @Test
    public void modifiedRowOnClientConflictOnServerClientWins() {
        var newRowClient = new Row(1, List.of("Alice", "alice@example.com", "28"), 1);
        Map<Integer, Row> validNewRows = Map.of(newRowClient.id, newRowClient);
        var serverRowHashes = getServerRowHashes();
        Map<Integer, Long> currentClientRows = new HashMap<>(serverRowHashes);
        currentClientRows.put(newRowClient.id, newRowClient.dataHash);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        var newRowServer = new Row(1, List.of("Alice", "alice@example.com", "18"), 2);
        serverRows.put(newRowServer.id, newRowServer);

        var writeRow = (Consumer<Row>) Mockito.mock(Consumer.class);
        var conflictResolver = Mockito.mock(ConflictResolver.class);

        // client wins
        Mockito.doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(conflictResolver).resolveConflict(Mockito.any(), Mockito.any());

        var algo = new SyncAlgorithm(writeRow, null, serverRows::get, conflictResolver, null,
                validNewRows, getServerRowHashes(), currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Mockito.verify(conflictResolver).resolveConflict(newRowClient, newRowServer);
        Mockito.verify(writeRow).accept(newRowClient);

        Assertions.assertEquals(1, newOrModifiedRows.size());
        Assertions.assertTrue(newOrModifiedRows.contains(newRowClient));
        Assertions.assertEquals(0, deletedRows.size());
    }

    @Test
    public void modifiedRowOnClientConflictOnServerServerWins() {
        var newRowClient = new Row(1, List.of("Alice", "alice@example.com", "28"), 1);
        Map<Integer, Row> validNewRows = Map.of(newRowClient.id, newRowClient);
        var serverRowHashes = getServerRowHashes();
        Map<Integer, Long> currentClientRows = new HashMap<>(serverRowHashes);
        currentClientRows.put(newRowClient.id, newRowClient.dataHash);
        Map<Integer, Long> previousClientRows = Map.copyOf(serverRowHashes);

        var newRowServer = new Row(1, List.of("Alice", "alice@example.com", "18"), 2);
        serverRows.put(newRowServer.id, newRowServer);

        var writeRow = (Consumer<Row>) Mockito.mock(Consumer.class);
        var conflictResolver = Mockito.mock(ConflictResolver.class);

        // server wins
        Mockito.doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(conflictResolver).resolveConflict(Mockito.any(), Mockito.any());

        var algo = new SyncAlgorithm(writeRow, null, serverRows::get, conflictResolver, null,
                validNewRows, getServerRowHashes(), currentClientRows, previousClientRows);

        Set<Row> newOrModifiedRows = new HashSet<>();
        Set<Integer> deletedRows = new HashSet<>();
        Assertions.assertDoesNotThrow(() -> algo.run(newOrModifiedRows, deletedRows));

        Mockito.verify(conflictResolver).resolveConflict(newRowClient, newRowServer);
        Mockito.verify(writeRow).accept(newRowServer);

        Assertions.assertEquals(1, newOrModifiedRows.size());
        Assertions.assertTrue(newOrModifiedRows.contains(newRowServer));
        Assertions.assertEquals(0, deletedRows.size());
    }
}
