package com.lightspeed.unisync.core.interfaces;

import com.lightspeed.unisync.core.model.Row;

public interface ConflictResolver {
    Row resolveCoflict(Row clientRow, Row serverRow);
}
