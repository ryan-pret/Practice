package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    deploy();
  }

  private void deploy() {
    vertx.deployVerticle(new DBVerticle());
  }

  public static void main(String[] args) {
      Vertx vertx = Vertx.vertx();
      vertx.deployVerticle(new MainVerticle());
  }
}
