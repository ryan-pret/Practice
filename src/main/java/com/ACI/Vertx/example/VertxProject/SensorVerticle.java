package com.ACI.Vertx.example.VertxProject;

import java.util.UUID;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class SensorVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(SensorVerticle.class);
    private static final int httpPort = 8080;
    private static String uuid = UUID.randomUUID().toString();

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Setup router
        Router router = Router.router(vertx);
        router.get("/data").handler(this::getData);

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(httpPort )
            .onSuccess(ok -> {
                logger.info("Http server running: http://127.0.0.1: " + httpPort);
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    private void getData(RoutingContext context) {
        logger.info("Processing HTTP request from {" + context.request().remoteAddress() + "}");
        JsonObject payload = new JsonObject()
            .put("uuid", uuid)
            .put("number", "69")
            .put("timestamp", System.currentTimeMillis());
        context.response()
            .putHeader("Content-Type", "application/json")
            .setStatusCode(200)
            .end(payload.encode());
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new SensorVerticle());
      }
}
