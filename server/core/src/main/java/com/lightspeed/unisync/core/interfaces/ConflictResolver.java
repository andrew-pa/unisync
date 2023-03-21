package com.lightspeed.unisync.core.interfaces;

import com.lightspeed.unisync.core.model.Row;

public interface ConflictResolver {
    /**
     * Resolves the conflict in a row that has been modified by two clients
     * @param clientRow the row that has just been received
     * @param serverRow the row that made it to the server first
     * @return a new row that represents the resolved conflict
     */
    Row resolveCoflict(Row clientRow, Row serverRow);
}
