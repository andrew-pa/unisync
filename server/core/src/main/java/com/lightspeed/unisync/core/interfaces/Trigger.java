package com.lightspeed.unisync.core.interfaces;

import com.lightspeed.unisync.core.model.Row;

import java.util.UUID;

public interface Trigger {
    void onRowCreated(UUID userId, Row r);

    void onRowModified(UUID userId, Row r);

    void onRowDeleted(UUID userId, Row r);
}
