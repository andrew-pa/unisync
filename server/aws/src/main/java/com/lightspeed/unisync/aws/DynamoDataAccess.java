package com.lightspeed.unisync.aws;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.model.Row;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DynamoDataAccess implements DataAccess {
    final DynamoDbClient client;

    public DynamoDataAccess(Region region) {
        client = DynamoDbClient.builder().region(region).build();
    }

    Map<String, AttributeValue> makeKey(String tableName, UUID userId) {
        return Map.of("userId", AttributeValue.fromS(userId.toString()),
                "tableName", AttributeValue.fromS(tableName));
    }

    Map<String, AttributeValue> makeKey(UUID userId, int rowId) {
        return Map.of("userId", AttributeValue.fromS(userId.toString()),
                "rowId", AttributeValue.fromN(Integer.toString(rowId)));
    }

    @Override
    public Map<Integer, Long> rowIdsInTable(String tableName, UUID userId) {
        var res = client.getItem(b -> b.tableName("_tableInfo")
                .key(makeKey(tableName, userId)));
        if (res.hasItem()) {
            return res.item().get("rowsInfo").m().entrySet().stream()
                    .collect(Collectors.toMap(
                            kv -> Integer.parseInt(kv.getKey()),
                            kv -> Long.parseLong(kv.getValue().n())));
        } else {
            return Map.of();
        }
    }

    @Override
    public Row readRow(String tableName, UUID userId, int rowId) {
        var res = client.getItem(b ->
                b.tableName(tableName).key(makeKey(userId, rowId)));
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
        try {
            client.updateItem(b ->
                    b.tableName("_tableInfo")
                            .key(makeKey(tableName, userId))
                            .updateExpression("SET rowsInfo.#rowId = :dataHash")
                            .expressionAttributeNames(Map.of("#rowId", Integer.toString(newRow.id)))
                            .expressionAttributeValues(Map.of(":dataHash", AttributeValue.fromN(Long.toString(newRow.dataHash))))
            );
        } catch (DynamoDbException e) {
            // assume the error is because the item does not exist
            client.putItem(b -> b.tableName("_tableInfo")
                    .item(Map.of("tableName", AttributeValue.fromS(tableName),
                            "userId", AttributeValue.fromS(userId.toString()),
                            "rowsInfo", AttributeValue.fromM(Map.of(Integer.toString(newRow.id), AttributeValue.fromN(Long.toString(newRow.dataHash)))))));
        }
    }

    @Override
    public void deleteRow(String tableName, UUID userId, int rowId) {
        var res = client.deleteItem(b -> b.tableName(tableName)
                .key(makeKey(userId, rowId)));
        client.updateItem(b ->
                b.tableName("_tableInfo")
                        .key(makeKey(tableName, userId))
                        .updateExpression("REMOVE rowsInfo.#rowId")
                        .expressionAttributeNames(Map.of("#rowId", Integer.toString(rowId)))
        );
    }
}
