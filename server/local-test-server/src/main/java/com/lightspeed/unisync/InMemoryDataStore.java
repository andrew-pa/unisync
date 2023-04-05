package com.lightspeed.unisync;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.model.Row;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryDataStore implements DataAccess {
    Map<String, Map<UUID, Map<Integer, Row>>> tables = new HashMap<>();

    @Override
    public Map<Integer, Long> rowIdsInTable(String tableName, UUID userId) {
        Map<Integer, Long> rowIds = new HashMap<>();
        if(!tables.containsKey(tableName) || !tables.get(tableName).containsKey(userId)) return rowIds;
        for(var r : tables.get(tableName).get(userId).values()) {
            rowIds.put(r.id, r.dataHash);
        }
        return rowIds;
    }

    @Override
    public Row readRow(String tableName, UUID userId, int rowId) {
        return tables.getOrDefault(tableName, new HashMap<>()).getOrDefault(userId, new HashMap<>()).get(rowId);
    }

    @Override
    public void writeRow(String tableName, UUID userId, Row newRow) {
        tables.getOrDefault(tableName, new HashMap<>()).getOrDefault(userId, new HashMap<>()).put(newRow.id, newRow);
    }

    @Override
    public void deleteRow(String tableName, UUID userId, int rowId) {
        tables.get(tableName).get(userId).remove(rowId);
    }
}
