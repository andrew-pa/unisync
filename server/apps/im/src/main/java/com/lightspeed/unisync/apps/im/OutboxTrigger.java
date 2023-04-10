package com.lightspeed.unisync.apps.im;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.interfaces.Trigger;
import com.lightspeed.unisync.core.model.Row;

public class OutboxTrigger implements Trigger {
    final DataAccess dataAccess;

    public OutboxTrigger(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onRowCreated(Row r) {
    }

    public void onRowModified(Row r) {
    }

    public void onRowDeleted(Row r) {
    }
}
