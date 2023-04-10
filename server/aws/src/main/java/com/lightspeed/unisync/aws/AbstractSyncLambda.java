package com.lightspeed.unisync.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lightspeed.unisync.core.SyncService;
import com.lightspeed.unisync.core.model.SyncRequest;
import com.lightspeed.unisync.core.model.SyncResponse;

public abstract class AbstractSyncLambda implements RequestHandler<SyncRequest, SyncResponse> {
    protected abstract SyncService createService();

    private final SyncService service = createService();

    public SyncResponse handleRequest(SyncRequest request, Context context) {
        var log = context.getLogger();
        log.log("Processing request: " + request.toString());
        var resp = service.syncTable(request);
        log.log("Sending response: " + resp.toString());
        return resp;
    }
}
