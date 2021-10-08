package com.ACI.Vertx.example.VertxProject;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class Debug {
    private boolean flag;
    private static final Logger logger = LoggerFactory.getLogger(SensorVerticle.class);

    /**
     * Constructor to enable or disable debug info
     * @param on if true then debugs will print out, else won't print
     */
    public Debug (boolean on) {
        flag = on;
    }

    /**
     * Prints the message depending on the flag variable
     * @param message the message you want to print
     */
    public void print(String message) {
        if (flag) {
            logger.info(message);
        }
    }
}
