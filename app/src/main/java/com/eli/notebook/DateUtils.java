package com.eli.notebook;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    // Format the timestamp in East Africa Time (EAT)
    public static String formatTimestamp(long timestamp) {
        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Set the timezone to East Africa Time (EAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("Africa/Nairobi"));

        // Format the timestamp
        return dateFormat.format(new Date(timestamp));
    }
}
