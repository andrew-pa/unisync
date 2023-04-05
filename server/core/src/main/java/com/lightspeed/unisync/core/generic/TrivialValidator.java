package com.lightspeed.unisync.core.generic;

import com.lightspeed.unisync.core.interfaces.Validator;
import com.lightspeed.unisync.core.model.InvalidRow;
import com.lightspeed.unisync.core.model.Row;

import java.util.Optional;

public class TrivialValidator implements Validator {
    @Override
    public Optional<InvalidRow> isValid(Row r) {
        return Optional.empty();
    }
}
