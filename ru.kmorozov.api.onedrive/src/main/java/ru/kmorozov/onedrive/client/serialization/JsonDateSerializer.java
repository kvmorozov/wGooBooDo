package ru.kmorozov.onedrive.client.serialization;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class JsonDateSerializer {

    public static final JsonDateSerializer INSTANCE = new JsonDateSerializer();
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static {
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        df2.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public synchronized String serialize(final Date value) {
        return df.format(value);
    }

    public synchronized Date deserialize(final String value) throws ParseException {
        try {
            return df.parse(value);
        } catch (final ParseException e) {
            try {
                return df2.parse(value);
            } catch (final ParseException e1) {
                throw e;
            }

        }
    }
}
