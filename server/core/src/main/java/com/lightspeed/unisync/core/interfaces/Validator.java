package com.lightspeed.unisync.core.interfaces;

import com.lightspeed.unisync.core.model.Pair;
import com.lightspeed.unisync.core.model.Row;

import java.util.Optional;

public interface Validator {
    Optional<Pair<String, Integer>> isValid(Row r);
}
