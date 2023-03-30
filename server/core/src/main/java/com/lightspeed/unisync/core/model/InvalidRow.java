package com.lightspeed.unisync.core.model;

import java.util.List;

public class InvalidRow extends Row {
    public final String reason;
    public final int columnIndex;

    public InvalidRow(int id, List<String> data, long dataHash, String reason, int columnIndex) {
        super(id, data, dataHash);
        this.reason = reason;
        this.columnIndex = columnIndex;
    }
}
