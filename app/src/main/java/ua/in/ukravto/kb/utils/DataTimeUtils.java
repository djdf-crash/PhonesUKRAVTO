package ua.in.ukravto.kb.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by djdf.crash on 24.06.2018.
 */
public class DataTimeUtils {


    public static long getMillis(String date){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date past = format.parse(date);
            return past.getTime();
        } catch (ParseException e) {
            return 0;
        }
    }


}
