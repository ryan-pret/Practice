package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.*;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

public class DBVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("tai.db.elephantsql.com")
      .setDatabase("lulkvssu")
      .setUser("lulkvssu")
      .setPassword("ikXUlV2AqixYvwW_cYjqHSn2s3yHO3Sg");

      // Pool options
      PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(5);

      // Create the client pool
      SqlClient client = PgPool.client(connectOptions, poolOptions);
      System.out.println("Connected successfully");

      client.close();
  }
}
