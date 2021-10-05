package com.ACI.Vertx.example.VertxProject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import io.vertx.core.Vertx;

public class Main {

  public static void main(String[] args) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, 30);
    Date date = calendar.getTime();
    System.out.println(date.toString());
  }
}
