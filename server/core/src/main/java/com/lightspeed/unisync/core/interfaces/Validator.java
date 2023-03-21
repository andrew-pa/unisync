package com.lightspeed.unisync.core.interfaces;

import com.lightspeed.unisync.core.model.Pair;
import com.lightspeed.unisync.core.model.Row;

import java.util.Optional;

public interface Validator {
    /**
     * Validate a new row entering the store
     * @param r the new row
     * @return None if the row is valid; otherwise a reason string, and if applicable the column that was invalid
     */
    Optional<Pair<String, Integer>> isValid(Row r);
}
