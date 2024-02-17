package com.google.dataProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateProvider {

    public static String currentDate() {
        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("yyyy-MM-dd");
        return ft.format(dNow);
    }

    public static String currentTime() {
        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("HH-mm-ss");
        return ft.format(dNow);
    }
}
