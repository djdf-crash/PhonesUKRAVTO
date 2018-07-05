package ua.in.ukravto.kb.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Pref {

    private final static String NAME_SHARED_PREF = "pref_ua.in.ukravto.kb";
    public static final String DEVICE_ID = "device_id";
    public static final String USER_TOKEN = "user_token";
    public static final String EMAIL = "user_email";
    public static final String SAVED_ORGANIZATIONS = "saved_organizations";
    public static final String SYNC_ALL_ORGANIZATION = "sync_all_organization";
    public static final String DELETE_ORGANIZATIONS = "deleted_organization";
    public static final String SYNC_ONLY_NEW_UPDATE_PHONES = "sync_only_new_updates_phones";
    public static final String SYNC_WITH_PHONES_ONLY = "sync_with_phones_only";
    public static final String AUTO_CHECK_UPDATE_APK = "auto_check_update_apk";
    private static SharedPreferences sp;

    public static SharedPreferences getInstance(Context ctx){
        sp = ctx.getSharedPreferences(NAME_SHARED_PREF, Context.MODE_PRIVATE);
        return sp;
    }

}
