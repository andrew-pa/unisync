package com.lightspeed.unisync.aws;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.model.Row;

import java.util.Map;
import java.util.UUID;

public class DynamoDataAccess implements DataAccess {
    @Override
    public Map<Integer, Long> rowIdsInTable(String tableName, UUID userId) {
        return null;
    }

    @Override
    public Row readRow(String tableName, UUID userId, int rowId) {
        return null;
    }

    @Override
    public void writeRow(String tableName, UUID userId, Row newRow) {

    }

    @Override
    public void deleteRow(String tableName, UUID userId, int rowId) {

    }
}
