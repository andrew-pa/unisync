package com.lightspeed.unisync.core.generic;

import com.lightspeed.unisync.core.interfaces.ConflictResolver;
import com.lightspeed.unisync.core.model.Row;

public class LastWriterWinsConflictResolver implements ConflictResolver {
    @Override
    public Row resolveConflict(Row clientRow, Row serverRow) {
        // client is always the latest writer
        return clientRow;
    }
}
