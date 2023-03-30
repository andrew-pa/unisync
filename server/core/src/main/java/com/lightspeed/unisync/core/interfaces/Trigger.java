package com.lightspeed.unisync.core.interfaces;

import com.lightspeed.unisync.core.model.Row;

public interface Trigger {
    void onRowCreated(Row r);

    void onRowModified(Row r);

    void onRowDeleted(Row r);
}
