package ua.in.ukravto.kb.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import ua.in.ukravto.kb.BuildConfig;
import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.repository.service.RetrofitHelper;
import ua.in.ukravto.kb.utils.contacts.ContactsManager;
import ua.in.ukravto.kb.utils.NotificationBuilderHelper;
import ua.in.ukravto.kb.utils.Pref;

public class ContactsSyncAdapterService extends Service {
    public static final String TAG = "ContactsSyncAdapterS";
    private static ContentResolver mContentResolver = null;
    private static SyncAdapterImpl sSyncAdapter = null;
    public static final String SYNC_MARKER_KEY = "ua.in.ukravto.kb.samplesync.marker";


    public IBinder onBind(Intent intent) {
        return getSyncAdapter().getSyncAdapterBinder();
    }

    private SyncAdapterImpl getSyncAdapter() {
        if (sSyncAdapter == null) {
            sSyncAdapter = new SyncAdapterImpl(this);
        }
        return sSyncAdapter;
    }

    public static void performSync(final Context context, final AccountManager accountManager, final Account account, Bundle extras, String authority, ContentProviderClient provider, final SyncResult syncResult) {
        Log.d(TAG, "performSync: " + account.toString());

        long lastSyncMarker = getServerSyncMarker(accountManager, account);
        long newSyncState = lastSyncMarker;
        final String token = Pref.getInstance(context).getString(Pref.USER_TOKEN, "");
        if (TextUtils.isEmpty(token)) {
            return;
        }

        Gson mGson = new Gson();

        final Type type = new TypeToken<List<EmployeeOrganizationModel>>() {}.getType();

        syncDeleteContacts(context, account, extras, syncResult, token, mGson, type);

        newSyncState = syncUpdateContacts(context, account, extras, syncResult, lastSyncMarker, newSyncState, token, mGson, type);
        setServerSyncMarker(accountManager, account, newSyncState);
    }

    private static long syncUpdateContacts(Context context, Account account, Bundle extras, SyncResult syncResult, long lastSyncMarker, long newSyncState, String token, Gson mGson, Type type) {

        boolean syncOnlyLastUpdate, syncWithPhoneOnly;


        if (extras.containsKey(Pref.SYNC_ONLY_NEW_UPDATE_PHONES)){
            syncOnlyLastUpdate = extras.getBoolean(Pref.SYNC_ONLY_NEW_UPDATE_PHONES);
        }else {
            syncOnlyLastUpdate = Pref.getInstance(context).getBoolean(Pref.SYNC_ONLY_NEW_UPDATE_PHONES, true);
        }

        if (extras.containsKey(Pref.SYNC_WITH_PHONES_ONLY)){
            syncWithPhoneOnly = extras.getBoolean(Pref.SYNC_WITH_PHONES_ONLY);
        }else {
            syncWithPhoneOnly = Pref.getInstance(context).getBoolean(Pref.SYNC_WITH_PHONES_ONLY, false);
        }


        Log.d(TAG, "syncOnlyLastUpdate: " + syncOnlyLastUpdate);

        String savedOrganizationsString = Pref.getInstance(context).getString(Pref.SAVED_ORGANIZATIONS, "");
        List<EmployeeOrganizationModel> listSavedOrganization = mGson.fromJson(savedOrganizationsString, type);

        if (listSavedOrganization == null) {
            listSavedOrganization = new ArrayList<>();
        }

        final ContactsManager cm = new ContactsManager(context, account);

        Log.d(TAG, "org list: " + listSavedOrganization.size());

        try {
            for (final EmployeeOrganizationModel organizationModel : listSavedOrganization) {
                Log.d(TAG, "org name: " + organizationModel.getName());
                Response<PhoneResponse<EmployeePhoneModel>> response;
                if (syncOnlyLastUpdate) {
                    response = RetrofitHelper.getPhoneService().getOrganizationIDPhonesLastUpdate(organizationModel.getID(), token).execute();
                }else {
                    response = RetrofitHelper.getPhoneService().getPhonesOrganizationID(organizationModel.getID(), token).execute();
                }
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getBody() != null) {
                        Log.d(TAG, "size org: " + response.body().getBody().size());
                        newSyncState = cm.syncContacts(response.body().getBody(), lastSyncMarker, syncWithPhoneOnly);
                    }
                }
            }
            if (syncOnlyLastUpdate && listSavedOrganization.size() != 0) {
                RetrofitHelper.getPhoneService().updateUser(token).execute();
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
        }
        return newSyncState;
    }

    private static void syncDeleteContacts(Context context, Account account, Bundle extras, SyncResult syncResult, String token, Gson mGson, Type type) {

        final ContactsManager cm = new ContactsManager(context, account);

        String deleteOrganizationsString = Pref.getInstance(context).getString(Pref.DELETE_ORGANIZATIONS, "");
        List<EmployeeOrganizationModel> listDeleteOrganization = mGson.fromJson(deleteOrganizationsString, type);

        if (listDeleteOrganization == null) {
            listDeleteOrganization = new ArrayList<>();
        }

        Log.d(TAG, "Organization list for delete: " + listDeleteOrganization.size());

        List<EmployeeOrganizationModel> listNotDeleteOrganization = new ArrayList<>();
        for (EmployeeOrganizationModel delOrganizationModel : listDeleteOrganization) {
            Log.d(TAG, "org name del: " + delOrganizationModel.getName());
            try {
                final Response<PhoneResponse<EmployeePhoneModel>> response = RetrofitHelper.getPhoneService().getPhonesOrganizationID(delOrganizationModel.getID(), token).execute();
                if (response.isSuccessful()) {
                    if (response.body() != null || response.body().getBody() != null) {
                        Log.d(TAG, "size org del: " + response.body().getBody().size());
                        cm.deleteContacts(response.body().getBody());
                    }
                }
            } catch (IOException e) {
                listNotDeleteOrganization.add(delOrganizationModel);
                Log.e(TAG, "IOException", e);
                syncResult.stats.numIoExceptions++;
            }
        }

        String deleteOrganizations = mGson.toJson(listNotDeleteOrganization);
        Pref.getInstance(context).edit().putString(Pref.DELETE_ORGANIZATIONS, deleteOrganizations).apply();
    }

    private static long getServerSyncMarker(AccountManager mAccountManager, Account account) {
        String markerString = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
        if (!TextUtils.isEmpty(markerString)) {
            return Long.parseLong(markerString);
        }
        return 0;
    }

    private static void setServerSyncMarker(AccountManager mAccountManager, Account account, long marker) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, Long.toString(marker));
    }

    private static void checkLastUpdateAPP(Context ctx) {

        final String token = Pref.getInstance(ctx).getString(Pref.USER_TOKEN, "");
        if (TextUtils.isEmpty(token)) {
            return;
        }

        RepositoryService rep = new RepositoryServiceImpl(ctx);
        ResponseString<String> response = rep.getIsLastUpdateAPPExecute(token, BuildConfig.VERSION_NAME);
        if (response != null && response.getResult()){

            Intent intent = new Intent(ctx, DownloadService.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, 0);
            NotificationCompat.Builder mBuilder = NotificationBuilderHelper.buildMessage(ctx,
                    ctx.getString(R.string.title_new_version_app),
                    ctx.getString(R.string.text_new_version_app),
                    NotificationCompat.PRIORITY_DEFAULT,
                    NotificationCompat.CATEGORY_MESSAGE);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(),
                    R.mipmap.ic_launcher));
            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(ctx.getString(R.string.big_text_new_version_change_log)));
            mBuilder.setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
            notificationManager.notify(1, mBuilder.build());
        }
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;
        private AccountManager mAccountManager;

        public SyncAdapterImpl(Context context) {
            super(context, true);
            this.mContext = context;
            mAccountManager = AccountManager.get(context);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            performSync(this.mContext, mAccountManager, account, extras, authority, provider, syncResult);

            boolean autoCheckUpdateAPK;

            if (extras.containsKey(Pref.AUTO_CHECK_UPDATE_APK)){
                autoCheckUpdateAPK = extras.getBoolean(Pref.AUTO_CHECK_UPDATE_APK);
            }else {
                autoCheckUpdateAPK =  Pref.getInstance(mContext).getBoolean(Pref.AUTO_CHECK_UPDATE_APK, true);
            }

            Log.d(TAG, "autoCheckUpadteAPK: " + autoCheckUpdateAPK);
            if (autoCheckUpdateAPK) {
                checkLastUpdateAPP(mContext);
            }
            Log.d(ContactsSyncAdapterService.TAG, syncResult.toDebugString());
        }
    }
}
