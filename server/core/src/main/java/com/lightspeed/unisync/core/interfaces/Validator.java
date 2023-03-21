package com.lightspeed.unisync.core.interfaces;

import com.lightspeed.unisync.core.model.InvalidRow;
import com.lightspeed.unisync.core.model.Pair;
import com.lightspeed.unisync.core.model.Row;

import java.util.Optional;

public interface Validator {
    /**
     * Validate a new row entering the store
     * @param r the new row
     * @return None if the row is valid; otherwise the invalid row object representing the failure to validate
     */
    Optional<InvalidRow> isValid(Row r);
}
