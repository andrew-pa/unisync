package com.lightspeed.unisync.apps.im;

import com.lightspeed.unisync.aws.AbstractSyncLambda;
import com.lightspeed.unisync.aws.DynamoDataAccess;
import com.lightspeed.unisync.core.SyncService;
import com.lightspeed.unisync.core.generic.LastWriterWinsConflictResolver;
import com.lightspeed.unisync.core.generic.TrivialValidator;
import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.model.Table;
import software.amazon.awssdk.regions.Region;

import java.util.Map;
import java.util.Optional;

public class SyncLambda extends AbstractSyncLambda {
    @Override
    protected SyncService createService() {
        DataAccess data = new DynamoDataAccess(Region.US_EAST_1);
        return new SyncService(data,
                Optional::of, // pass through session IDs as user IDs
                Map.of("contacts", new Table("contacts", new LastWriterWinsConflictResolver(), new TrivialValidator(), null),
                        "inbox", new Table("inbox", new LastWriterWinsConflictResolver(), new TrivialValidator(), null),
                        "outbox", new Table("outbox", new LastWriterWinsConflictResolver(), new TrivialValidator(), new OutboxTrigger(data))));

    }
}
