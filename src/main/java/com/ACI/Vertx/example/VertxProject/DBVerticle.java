package com.ACI.Vertx.example.VertxProject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.hazelcast.internal.json.Json;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
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
    private static String uuid = UUID.randomUUID().toString();

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Set up DB link
        pgPool = PgPool.pool(vertx, new PgConnectOptions()
            .setPort(DBPort)
            .setHost("tai.db.elephantsql.com")
            .setUser("czaqanje")
            .setDatabase("czaqanje")
            .setPassword("g_2GuI_wytgl7DsAVPJqgDWe3-7h5S_N")
        , new PoolOptions());

        logger.info("Hello I made it past connecting the client");

        // Setup router
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Setup API endpoints
        router.post("/requestAirtime").handler(this::createRequest);
        router.get("/get/:number").handler(this::getnumVoucher);
        router.get("/getDetails/:details").handler(this::getdetails);
        router.post("/postCard").handler(this::addCard);
        router.get("/help").handler(this::displayRoutes);


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

    // request airtime
    private void createRequest(RoutingContext context) {
        // Get body as Json
        JsonObject body = context.getBodyAsJson();
        String phone_number = body.getString("phone_number");
        String card_number = body.getString("card_number");
        String cvv_num = body.getString("cvv_num");
        String voucher_amount = body.getString("voucher_amount");
        String expiry_date = body.getString("expiry_date");
        String client_id = body.getString("client_id");
        // Need to figure this out?
        // Why on earth is cliend_id not in scope
        // int client_id = 1;
        logger.info(phone_number + " "  + card_number + " " + cvv_num + " "
        + voucher_amount + " " + expiry_date);
        
        WebClient client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(8080).setDefaultHost("127.0.0.1"));
        
        client.get("/getdetails/" + phone_number).as(BodyCodec.string()).send(ar -> {
            JsonArray response = new JsonObject(ar.result().body()).getJsonArray("data");
            logger.info(response.encode());

            // Get's object
            // String getNumber = response.getJsonObject(0).encode();
            logger.info("Phone number ======== " +phone_number);
            if (ar.succeeded()) {
                logger.info("The request successfuly went through nani the fuck?");
                // if (getNumber.equals("")) {
                //     logger.info("No number in DB");
                //     // Insert client into DB
                // }
            } else {
                ar.cause().printStackTrace();
            }
        });

        // Get date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 30);
        Date date = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formatDate = formatter.format(date);
        logger.info(formatDate);

        // Insert voucher into Voucher DB

        logger.info("Client in DB");
        
        String query = "INSERT INTO \"Voucher\" (voucher_number, client_id, voucher_amount, was_redeemed, voucher_expiry) VALUES ('"+uuid+"', '"+client_id+"', '"+voucher_amount+"', false, '"+formatDate+"')";

        pgPool.preparedQuery(query)
            .execute()
            .onSuccess(rows -> {
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(200)
                    .end("200");
            })
            .onFailure(failure -> {
                logger.error("Query not executed for getAllData()", failure);
                context.fail(500);
            });
        
        // context.response().end(voucher_amount);
    }

    // Needs to be fully tested! FIXME
    private void addCard(RoutingContext context) {
        
        JsonObject body = context.getBodyAsJson();
        String card_number = body.getString("card_number");
        String cvv_num = body.getString("cvv_num");
        String client_id = body.getString("client_id");
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 30);
        Date date = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formatDate = formatter.format(date);
        logger.info(formatDate);
        
        logger.info("Inserting Card Details into Database");
        
        int available_balance = 10000; // FIXME hardcoded
        String card_expiry = formatDate; // FIXME hardcoded

        String queryCard = "INSERT INTO \"CardDetails\" (card_number, card_expiry, card_cvv, client_id, available_balance) VALUES ('"+card_number+"', '"+card_expiry+"', '"+cvv_num+"', '"+client_id+"', '"+available_balance+"')";

        pgPool.preparedQuery(queryCard)
            .execute()
            .onSuccess(rows -> {
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(200)
                    .end("200");
            })
            .onFailure(failure -> {
                logger.error("Query not executed for getAllData()", failure);
                context.fail(500);
            });
    }

    private void displayRoutes(RoutingContext context) {
        
        logger.info("List of available routes (change variable values where applicable):\nhttp :8080/getDetails/0721178956\nhttp :8080/getNumber/0721178956\nhttp :8080/requestAirtime phone_number=0721178956 card_number=123456789 cvv_num=077 voucher_amount=450 expiray_date=2021-01-01\nhttp :8080/postCard card_number=123456789 card_expiry=2021-01-01 card_cvv=077 client_id=1 available_balance=10000");
        //***************************************************
        String query = "SELECT client_id, first_names, last_names, title FROM \"Clients\" WHERE client_id IN (SELECT client_id FROM \"MobileNumbers\" WHERE phone_number = $1)";
        String phone_numer = "0721178956";
        pgPool.preparedQuery(query)
            .execute(Tuple.of(phone_numer))
            .onSuccess(rows -> {
                JsonArray array = new JsonArray();
                for (Row row : rows) {
                    array.add(new JsonObject()

                        .put("List of available routes (assign values where applicable):", row.getInteger("client_id").toString())
                        .put("http :8080/getDetails/0721178956", row.getInteger("client_id").toString())
                        .put("http :8080/getNumber/0721178956", row.getInteger("client_id").toString())
                        .put("http :8080/requestAirtime phone_number= card_number= cvv_num= voucher_amount= expiray_date=", row.getInteger("client_id").toString())
                        .put("http :8080/postCard card_number= card_expiry= card_cvv= client_id= available_balance=", row.getInteger("client_id").toString())
                        
                    );
                }
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("routes", array).encode());
            })
            .onFailure(failure -> {
                context.fail(500);
            });

        //***************************************************
        

    }

    private void getdetails(RoutingContext context) {
        logger.info("Requesting all user details from given cellphone number");
        String query = "SELECT client_id, first_names, last_names, title FROM \"Clients\" WHERE client_id IN (SELECT client_id FROM \"MobileNumbers\" WHERE phone_number = $1)";
        
        String phone_numer = context.request().getParam("details");
        logger.info("Error detector 1");
        pgPool.preparedQuery(query)
            .execute(Tuple.of(phone_numer))
            .onSuccess(rows -> {
                logger.info("Successfully queried DB");
                JsonArray array = new JsonArray();
                for (Row row : rows) {
                    array.add(new JsonObject()
                        .put("client_id", row.getInteger("client_id").toString())
                        .put("first_names", row.getString("first_names"))
                        .put("last_names", row.getString("last_names"))
                        .put("title", row.getString("title"))
                    );
                }
                context.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("data", array).encode());
            })
            .onFailure(failure -> {
                logger.error("Query not executed for getAllData()", failure);
                context.fail(500);
            });
    }

    /**
     * Retrieve user's vouchers from phone number
     * @param context used to get information from the API GET request
     */
    private void getnumVoucher(RoutingContext context) {
        String phone_numer = context.request().getParam("number");
        logger.info("Requesting all vouchers from cellphone number " + phone_numer);
        String query = "SELECT voucher_number, voucher_amount, was_redeemed, voucher_expiry FROM \"Voucher\" WHERE client_id IN (SELECT client_id FROM \"MobileNumbers\" WHERE phone_number = $1)";
        pgPool.preparedQuery(query)
            .execute(Tuple.of(phone_numer))
            .onSuccess(rows -> {
                logger.info("Successfully queried DB");
                JsonArray array = new JsonArray();
                for (Row row : rows) {
                    array.add(new JsonObject()
                        .put("voucher_number", row.getString("voucher_number"))
                        .put("voucher_amount", row.getInteger("voucher_amount").toString())
                        .put("was_redeemed", row.getBoolean("was_redeemed").toString())
                        .put("voucher_expiry", row.getLocalDate("voucher_expiry").toString())
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