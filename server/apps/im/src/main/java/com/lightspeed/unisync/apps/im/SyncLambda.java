package com.lightspeed.unisync.apps.im;

import com.lightspeed.unisync.aws.AbstractSyncLambda;
import com.lightspeed.unisync.aws.DynamoDataAccess;
import com.lightspeed.unisync.core.SyncService;
import software.amazon.awssdk.regions.Region;

import java.util.Map;
import java.util.Optional;

public class SyncLambda extends AbstractSyncLambda {
    @Override
    protected SyncService createService() {
        return new SyncService(
                new DynamoDataAccess(Region.US_EAST_1),
                Optional::of, // pass through session IDs as user IDs
                Map.of());
    }
}
