package com.ACI.Vertx.example.VertxProject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class DBVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(SensorVerticle.class);
    private static final int DBPort = 5432;
    private static final int HTTPport = 8080;
    private PgPool pgPool;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Set up DB link
        pgPool = PgPool.pool(vertx, new PgConnectOptions()
            .setPort(DBPort)
            .setHost("18.203.249.221")
            .setUser("lulkvssu")
            .setDatabase("lulkvssu")
            .setPassword("ikXUlV2AqixYvwW_cYjqHSn2s3yHO3Sg")
        , new PoolOptions());

        logger.info("Hello I made it past connecting the client");

        // Setup router
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        // Setup API endpoints
        router.get("/all").handler(this::getAllData);
        router.post("/requestAirtime").handler(this::createRequest);
        router.get("/get/:number").handler(this::getnumVoucher);


        // Create HTTPServer for CRUD routes
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(HTTPport)
            .onSuccess(ok -> {
                logger.info("HTTP server started on 127.0.0.1:8080");
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    // {"phone_number": "<number>",
    // "card_number": "<card_number>", 
    // "cvv_num": "<cvvnum>",
    // "voucher_amount": "<voucher_amount>",
    // "expiry_date": "<expiary_date>"
    // }
    private void createRequest(RoutingContext context) {

    }

    /**
     * Retrieve user's vouchers from phone number
     * @param context used to get information from the API GET request
     */
    private void getnumVoucher(RoutingContext context) {
        String phone_numer = context.request().getParam("number");
        logger.info("Requesting all vouchers from cellphone number " + phone_numer);
        String query = "SELECT voucher_number, voucher_amount, was_redeemed, voucher_expiry FROM \"Voucher\" WHERE customer_id IN (SELECT customer_id FROM \"MobileNumbers\" WHERE phone_number = $1)";
        pgPool.preparedQuery(query)
            .execute(Tuple.of(Integer.parseInt(phone_numer)))
            .onSuccess(rows -> {
                logger.info("Successfully queried DB");
                JsonArray array = new JsonArray();
                for (Row row : rows) {
                    array.add(new JsonObject()
                        .put("voucher_number", row.getInteger("voucher_number").toString())
                        .put("voucher_amount", row.getInteger("voucher_amount").toString())
                        .put("was_redeemed", row.getBoolean("was_redeemed").toString())
                        .put("voucher_expiry", row.getInteger("voucher_expiry").toString())
                    );
                }
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(200)
                    .end(new JsonObject().put("data", array).encode());
            })
            .onFailure(failure -> {
                logger.error("Query not executed for getAllData()", failure);
                context.fail(500);
            });
    }

    private void getAllData(RoutingContext context) {
        logger.info("Requesting all data from {" + context.request().remoteAddress() + "}");
        String query = "SELECT * FROM \"Customers\"";
        pgPool.preparedQuery(query)
            .execute()
            .onSuccess(rows -> {
                logger.info("Successfully queried DB");
                JsonArray array = new JsonArray();
                for (Row row : rows) {
                    array.add(new JsonObject()
                        .put("customer_id", row.getInteger("customer_id").toString())
                        .put("first_names", row.getString("first_names"))
                        .put("last_names", row.getString("last_names"))
                        .put("title", row.getString("title"))
                    );
                }
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(200)
                    .end(new JsonObject().put("data", array).encode());
            })
            .onFailure(failure -> {
                logger.error("Query not executed for getAllData()", failure);
                context.fail(500);
            });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DBVerticle());
    }
}