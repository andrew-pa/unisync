package com.lightspeed.unisync.apps.im;

import com.lightspeed.unisync.core.model.Row;
import com.lightspeed.unisync.core.interfaces.Trigger;

public class OutboxTrigger implements Trigger {
    public void onRowCreated(Row r) {}
    public void onRowModified(Row r) {}
    public void onRowDeleted(Row r) {}
}
