package com.lightspeed.unisync.apps.im;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.interfaces.Trigger;
import com.lightspeed.unisync.core.model.Row;

import java.util.UUID;

public class OutboxTrigger implements Trigger {
    final DataAccess dataAccess;

    public OutboxTrigger(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void onRowCreated(UUID senderId, Row r) {
        // !!! Assumes that the sender/receiver ID is the first column of the row
        UUID recipientId = UUID.fromString(r.data.get(0));
        r.data.set(0, senderId.toString());
        dataAccess.writeRow("inbox", recipientId, new Row(r.data));
    }

    @Override
    public void onRowModified(UUID userId, Row r) {
        // with a little extra bookkeeping we could support editing messages here
    }

    @Override
    public void onRowDeleted(UUID userId, Row r) {
    }
}
