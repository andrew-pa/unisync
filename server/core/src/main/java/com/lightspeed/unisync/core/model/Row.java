package com.lightspeed.unisync.core.model;

import java.util.List;
import java.util.Random;

public class Row {
    public final int id;

    // TODO: should we make things more concrete than representing each column as a string?
    public final List<String> data;

    public final long dataHash;

    public Row(int id, List<String> data, long dataHash) {
        this.id = id;
        this.data = data;
        this.dataHash = dataHash;
    }

    public Row(int id, List<String> data) {
        this(id, data, data.hashCode()); // Java's default hash is good enough for a prototype
    }

    public Row(List<String> data) {
        this((new Random()).nextInt() % 0x3fff_ffff, data);
    }
}
