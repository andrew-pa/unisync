package com.lightspeed.unisync.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.lightspeed.unisync.core.SyncService;
import com.lightspeed.unisync.core.model.SyncRequest;
import com.lightspeed.unisync.core.model.SyncResponse;

import java.io.*;
import java.util.Base64;
import java.util.Map;

public abstract class AbstractSyncLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    protected abstract SyncService createService();

    private final SyncService service = createService();

    private final Gson gson = new Gson();

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        var log = context.getLogger();
        SyncRequest request = null;
        log.log(event.getPath());
        log.log(event.getBody());
        if(event.getIsBase64Encoded()) {
            var body = Base64.getDecoder().decode(event.getBody());
            request = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(body)), SyncRequest.class);
        } else {
            request = gson.fromJson(event.getBody(), SyncRequest.class);
        }
        log.log("Processing request: " + request.toString());
        var resp = service.syncTable(request);
        log.log("Sending response: " + resp.toString());
        var r = new APIGatewayProxyResponseEvent();
        r.setBody(gson.toJson(resp));
        r.setStatusCode(200);
        r.setHeaders(Map.of("Content-Type", "application/json"));
        return r;
    }
}
