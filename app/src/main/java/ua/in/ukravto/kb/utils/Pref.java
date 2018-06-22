package ua.in.ukravto.kb.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Pref {

    private final static String NAME_SHARED_PREF = "pref_ua.in.ukravto.kb";
    public static final String DEVICE_ID = "device_id";
    public static final String USER_TOKEN = "user_token";
    public static final String EMAIL = "user_email";
    private static SharedPreferences sp;

    public static SharedPreferences getInstance(Context ctx){
        if (sp == null){
            sp = ctx.getSharedPreferences(NAME_SHARED_PREF, Context.MODE_PRIVATE);
        }
        return sp;
    }

}
