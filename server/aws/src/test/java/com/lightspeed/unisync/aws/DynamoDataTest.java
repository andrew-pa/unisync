package com.lightspeed.unisync.aws;

import com.lightspeed.unisync.core.model.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import java.util.List;
import java.util.UUID;

public class DynamoDataTest {
    final UUID testUserId = UUID.randomUUID();

    @Test
    public void simpleIntegration() {
        var d = new DynamoDataAccess(Region.US_EAST_1);
        var r = new Row(12, List.of("data1", "data2", "data3"));
        d.writeRow("test", testUserId, r);
        {
            var ids = d.rowIdsInTable("test", testUserId);
            Assertions.assertNotNull(ids);
            Assertions.assertTrue(ids.size() > 0);
            Assertions.assertTrue(ids.containsKey(r.id));
            Assertions.assertEquals(ids.get(r.id), r.dataHash);
        }
        {
            var rr = d.readRow("test", testUserId, r.id);
            Assertions.assertNotNull(rr);
            Assertions.assertEquals(rr.id, r.id);
            Assertions.assertEquals(rr.dataHash, r.dataHash);
            Assertions.assertEquals(rr.data.size(), r.data.size());
        }
        d.deleteRow("test", testUserId, 12);
        {
            var ids = d.rowIdsInTable("test", testUserId);
            Assertions.assertNotNull(ids);
            Assertions.assertTrue(ids.isEmpty());
        }
    }
}
