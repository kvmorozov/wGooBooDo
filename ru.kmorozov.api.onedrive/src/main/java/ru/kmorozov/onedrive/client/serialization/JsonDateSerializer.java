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
        JsonDateSerializer.df.setTimeZone(TimeZone.getTimeZone("UTC"));
        JsonDateSerializer.df2.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public synchronized String serialize(Date value) {
        return JsonDateSerializer.df.format(value);
    }

    public synchronized Date deserialize(String value) throws ParseException {
        try {
            return JsonDateSerializer.df.parse(value);
        } catch (ParseException e) {
            try {
                return JsonDateSerializer.df2.parse(value);
            } catch (ParseException e1) {
                throw e;
            }

        }
    }
}
