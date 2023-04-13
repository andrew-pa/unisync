package com.lightspeed.unisync.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.lightspeed.unisync.core.SyncService;
import com.lightspeed.unisync.core.model.SyncRequest;
import com.lightspeed.unisync.core.model.SyncResponse;

import java.io.*;

public abstract class AbstractSyncLambda implements RequestStreamHandler {
    protected abstract SyncService createService();

    private final SyncService service = createService();

    private final Gson gson = new Gson();

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        var log = context.getLogger();
        SyncRequest request = gson.fromJson(new InputStreamReader(inputStream), SyncRequest.class);
        inputStream.close();
        log.log("Processing request: " + request.toString());
        var resp = service.syncTable(request);
        log.log("Sending response: " + resp.toString());
        var s = new PrintWriter(outputStream);
        s.println(gson.toJson(resp));
        s.close();
    }
}
