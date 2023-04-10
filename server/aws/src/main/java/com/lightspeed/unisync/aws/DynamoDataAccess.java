package com.lightspeed.unisync.aws;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.model.Row;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DynamoDataAccess implements DataAccess {
    final DynamoDbClient client;

    public DynamoDataAccess(Region region) {
        client = DynamoDbClient.builder().region(region).build();
    }

    @Override
    public Map<Integer, Long> rowIdsInTable(String tableName, UUID userId) {
        client.getItem(b -> {
        });
        return null;
    }

    @Override
    public Row readRow(String tableName, UUID userId, int rowId) {
        var res = client.getItem(b ->
                b.tableName(tableName)
                        .key(Map.of("userId", AttributeValue.fromS(userId.toString()),
                                "rowId", AttributeValue.fromN(Integer.toString(rowId)))));
        if (res.hasItem()) {
            var item = res.item();
            List<String> data = item.get("data").l().stream().map(AttributeValue::s).collect(Collectors.toList());
            Row r = new Row(Integer.parseInt(item.get("rowId").n()), data);
            assert r.dataHash == Integer.parseInt(item.get("dataHash").n());
            return r;
        } else {
            return null;
        }
    }

    @Override
    public void writeRow(String tableName, UUID userId, Row newRow) {
        client.putItem(b -> b.tableName(tableName).item(
                Map.of("userId", AttributeValue.fromS(userId.toString()),
                "rowId", AttributeValue.fromN(Integer.toString(newRow.id)),
                "dataHash", AttributeValue.fromN(Long.toString(newRow.dataHash)),
                "data", AttributeValue.fromL(newRow.data.stream().map(AttributeValue::fromS).collect(Collectors.toList())))));
        client.updateItem(b ->
                b.tableName("_tableInfo")
                        .key(Map.of("userId", AttributeValue.fromS(userId.toString()),
                                "tableName", AttributeValue.fromS(tableName)))
        );
    }

    @Override
    public void deleteRow(String tableName, UUID userId, int rowId) {
        client.deleteItem(b -> {
        });
    }
}
