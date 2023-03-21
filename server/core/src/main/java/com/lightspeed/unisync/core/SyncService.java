package com.lightspeed.unisync.core;

import com.lightspeed.unisync.core.interfaces.DataAccess;
import com.lightspeed.unisync.core.interfaces.IdentityService;
import com.lightspeed.unisync.core.model.SyncRequest;
import com.lightspeed.unisync.core.model.SyncResponse;
import com.lightspeed.unisync.core.model.Table;

import java.util.Map;

public class SyncService {
    final DataAccess data;
    final IdentityService identity;

    /// name -> table
    final Map<String, Table> tables;

    public SyncService(DataAccess data, IdentityService identity, Map<String, Table> tables) {
        this.data = data;
        this.identity = identity;
        this.tables = tables;
    }

    public SyncResponse syncTable(SyncRequest request) {
        if(!this.identity.checkSession(request.sessionId)) {
            throw new RuntimeException("unauthorized request");
        }
        return null;
    }
}
