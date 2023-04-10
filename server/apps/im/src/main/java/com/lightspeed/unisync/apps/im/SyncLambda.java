package com.lightspeed.unisync.apps.im;

import com.lightspeed.unisync.aws.AbstractSyncLambda;
import com.lightspeed.unisync.aws.DynamoDataAccess;
import com.lightspeed.unisync.core.SyncService;
import com.lightspeed.unisync.core.interfaces.DataAccess;
import software.amazon.awssdk.regions.Region;

import java.util.Map;
import java.util.Optional;

public class SyncLambda extends AbstractSyncLambda {
    @Override
    protected SyncService createService() {
        DataAccess data = new DynamoDataAccess(Region.US_EAST_1);
        return new SyncService(data,
                Optional::of, // pass through session IDs as user IDs
                Map.of());
    }
}
