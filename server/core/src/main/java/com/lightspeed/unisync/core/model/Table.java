package com.lightspeed.unisync.core.model;

import com.lightspeed.unisync.core.interfaces.ConflictResolver;
import com.lightspeed.unisync.core.interfaces.Trigger;
import com.lightspeed.unisync.core.interfaces.Validator;

public class Table {
    public final String name;

    public final ConflictResolver conflictResolver;
    public final Validator validator;
    public final Trigger trigger;

    public Table(String name, ConflictResolver conflictResolver, Validator validator, Trigger trigger) {
        this.name = name;
        this.conflictResolver = conflictResolver;
        this.validator = validator;
        this.trigger = trigger;
    }
}
