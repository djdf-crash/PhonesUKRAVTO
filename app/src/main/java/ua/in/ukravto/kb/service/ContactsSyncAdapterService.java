package ua.in.ukravto.kb.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import retrofit2.Response;
import ua.in.ukravto.kb.BuildConfig;
import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;
import ua.in.ukravto.kb.repository.database.AppDatabase;
import ua.in.ukravto.kb.repository.database.DatabaseHelper;
import ua.in.ukravto.kb.repository.database.OrganizationDao;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.repository.service.RetrofitHelper;
import ua.in.ukravto.kb.utils.NotificationBuilderHelper;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.utils.contacts.ContactsManager;
import ua.in.ukravto.kb.view.MainActivity;

public class ContactsSyncAdapterService extends Service {
    public static final String TAG = "ContactsSyncAdapterS";
    private SyncAdapterImpl sSyncAdapter = null;
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

    public static void performSync(final Context context, final AppDatabase mAppDatabase, final AccountManager accountManager, final Account account, Bundle extras, final SyncResult syncResult) {
        Log.d(TAG, "performSync: " + account.toString());


        long lastSyncMarker = getServerSyncMarker(accountManager, account);
        long newSyncState = lastSyncMarker;
        final String token = Pref.getInstance(context).getString(Pref.USER_TOKEN, "");

        if (TextUtils.isEmpty(token)) {
            return;
        }

        syncDeleteContacts(context, account, mAppDatabase, syncResult, token);

        newSyncState = syncUpdateContacts(context, account, mAppDatabase, extras, syncResult, lastSyncMarker, newSyncState, token);

        setServerSyncMarker(accountManager, account, newSyncState);
    }

    private static long syncUpdateContacts(Context context, Account account, AppDatabase mAppDatabase, Bundle extras, SyncResult syncResult, long lastSyncMarker, long newSyncState, String token) {

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

        List<EmployeeOrganizationModel> listSavedOrganization = mAppDatabase.organizationDao().getAllByChecked(true);

        if (listSavedOrganization == null) {
            listSavedOrganization = new ArrayList<>();
        }

        final ContactsManager cm = new ContactsManager(context, account);

        Log.d(TAG, "Organization list for sync: " + listSavedOrganization.size());

        try {
            for (final EmployeeOrganizationModel organizationModel : listSavedOrganization) {
                Log.d(TAG, "name: " + organizationModel.getName());
                Response<PhoneResponse<EmployeePhoneModel>> response;
                if (syncOnlyLastUpdate) {
                    response = RetrofitHelper.getPhoneService().getOrganizationIDPhonesLastUpdate(organizationModel.getID(), token).execute();
                }else {
                    response = RetrofitHelper.getPhoneService().getPhonesOrganizationID(organizationModel.getID(), token).execute();
                }
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getBody() != null) {
                        Log.d(TAG, "size: " + response.body().getBody().size());
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

    private static void syncDeleteContacts(Context context, Account account, AppDatabase mAppDatabase, SyncResult syncResult, String token) {

        final ContactsManager cm = new ContactsManager(context, account);

        OrganizationDao dao = mAppDatabase.organizationDao();
        List<EmployeeOrganizationModel> listDeleteOrganization = dao.getAllByDelete(true);

        if (listDeleteOrganization == null) {
            listDeleteOrganization = new ArrayList<>();
        }

        Log.d(TAG, "Organization list for delete: " + listDeleteOrganization.size());

        for (EmployeeOrganizationModel delOrganizationModel : listDeleteOrganization) {
            Log.d(TAG, "org name del: " + delOrganizationModel.getName());
            try {
                final Response<PhoneResponse<EmployeePhoneModel>> response = RetrofitHelper.getPhoneService().getPhonesOrganizationID(delOrganizationModel.getID(), token).execute();
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getBody() != null) {
                        Log.d(TAG, "size org del: " + response.body().getBody().size());
                        cm.deleteContacts(response.body().getBody());

                        if (delOrganizationModel.getIsDelete()){
                            dao.deleteOrganization(delOrganizationModel);
                        }else {
                            delOrganizationModel.setIsChecked(false);
                            delOrganizationModel.setDeleteBase(false);
                            dao.updateOrganization(delOrganizationModel);
                        }
                    }
                }
            } catch (IOException e) {
                delOrganizationModel.setDeleteBase(true);
                Log.e(TAG, "IOException", e);
                syncResult.stats.numIoExceptions++;
            }
        }
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
        private AppDatabase mAppDatabase;

        SyncAdapterImpl(Context context) {
            super(context, true);
            this.mContext = context;
            this.mAccountManager = AccountManager.get(context);
            this.mAppDatabase = DatabaseHelper.getInstanseDB(context);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

            checkNewAndDelOrganization(mContext);

            performSync(mContext, mAppDatabase, mAccountManager, account, extras, syncResult);

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
            if (extras.containsKey("send_toast")){
                NotificationCompat.Builder mBuilder = NotificationBuilderHelper.buildMessage(mContext,
                        mContext.getString(R.string.sync_successful),
                        mContext.getString(R.string.sync_successful),
                        NotificationCompat.PRIORITY_DEFAULT,
                        NotificationCompat.CATEGORY_MESSAGE);
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                        R.mipmap.ic_launcher));

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                notificationManager.notify(10, mBuilder.build());
            }
            Log.d(ContactsSyncAdapterService.TAG, syncResult.toDebugString());
        }

        private void checkNewAndDelOrganization(final Context mContext) {

            final String token = Pref.getInstance(mContext).getString(Pref.USER_TOKEN, "");
            if (TextUtils.isEmpty(token)) {
                return;
            }

            try {
                Response<PhoneResponse<EmployeeOrganizationModel>> response = RetrofitHelper.getPhoneService().getListOrganizations(token).execute();
                if (response.isSuccessful() && response.body() != null){
                    List<EmployeeOrganizationModel> listOrganizationAPI = response.body().getBody();

                    if (listOrganizationAPI == null){
                        return;
                    }

                    StringBuilder textDeleteOrganization = new StringBuilder();
                    StringBuilder textNewOrganization = new StringBuilder();

                    OrganizationDao dao = mAppDatabase.organizationDao();

                    final HashMap<Integer, EmployeeOrganizationModel> mapCheckedOrganization = buildOrganizationMap(dao.getAllByChecked(true));
                    final HashMap<Integer, EmployeeOrganizationModel> mapNotCheckedOrganization = buildOrganizationMap(dao.getAllByChecked(false));

                    for (EmployeeOrganizationModel organizationAPI: listOrganizationAPI) {
                        if ((mapCheckedOrganization.containsKey(organizationAPI.getID())
                                || mapNotCheckedOrganization.containsKey(organizationAPI.getID()))
                                && organizationAPI.getIsDelete()){
                            EmployeeOrganizationModel tmpOrganization = mapCheckedOrganization.get(organizationAPI.getID());
                            if (tmpOrganization != null) {
                                tmpOrganization.setIsChecked(false);
                                tmpOrganization.setIsDelete(true);
                                tmpOrganization.setDeleteBase(true);
                                dao.updateOrganization(tmpOrganization);
                                if (textDeleteOrganization.length() == 0){
                                    textDeleteOrganization = new StringBuilder(organizationAPI.getName().toUpperCase());
                                }else {
                                    textDeleteOrganization.append("\n").append(organizationAPI.getName().toUpperCase());
                                }
                                Log.d(TAG, "updateOrganization: " + organizationAPI.getName());
                            }

                            tmpOrganization = mapNotCheckedOrganization.get(organizationAPI.getID());
                            if (tmpOrganization != null) {
                                mAppDatabase.organizationDao().deleteOrganization(tmpOrganization);
                                if (textDeleteOrganization.length() == 0){
                                    textDeleteOrganization = new StringBuilder(organizationAPI.getName().toUpperCase());
                                }else {
                                    textDeleteOrganization.append("\n").append(organizationAPI.getName().toUpperCase());
                                }
                                Log.d(TAG, "deleteOrganization: " + organizationAPI.getName());
                            }
                        }else if (!mapNotCheckedOrganization.containsKey(organizationAPI.getID())
                                && !mapCheckedOrganization.containsKey(organizationAPI.getID())
                                && !organizationAPI.getIsDelete()){
                            if (textNewOrganization.length() == 0){
                                textNewOrganization = new StringBuilder(organizationAPI.getName().toUpperCase());
                            }else {
                                textNewOrganization.append("\n").append(organizationAPI.getName().toUpperCase());
                            }

                            mAppDatabase.organizationDao().addOrganization(organizationAPI);
                        }
                    }

                    if (textDeleteOrganization.length() > 0) {

                        Log.d(TAG, "textDeleteOrganization: " + textDeleteOrganization);

                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                        NotificationCompat.Builder mBuilder = NotificationBuilderHelper.buildMessage(mContext,
                                mContext.getString(R.string.delete_organization),
                                textDeleteOrganization.toString(),
                                NotificationCompat.PRIORITY_DEFAULT,
                                NotificationCompat.CATEGORY_MESSAGE);
                        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                                R.mipmap.ic_launcher));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(textDeleteOrganization.toString()));
                        mBuilder.setContentIntent(pendingIntent);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                        notificationManager.notify(2, mBuilder.build());
                    }

                    if (textNewOrganization.length() > 0) {

                        Log.d(TAG, "textNewOrganization: " + textNewOrganization);

                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                        NotificationCompat.Builder mBuilder = NotificationBuilderHelper.buildMessage(mContext,
                                mContext.getString(R.string.add_new_organization),
                                textNewOrganization.toString(),
                                NotificationCompat.PRIORITY_DEFAULT,
                                NotificationCompat.CATEGORY_MESSAGE);
                        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                                R.mipmap.ic_launcher));
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(textNewOrganization.toString()));
                        mBuilder.setContentIntent(pendingIntent);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                        notificationManager.notify(3, mBuilder.build());
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }

        }

        private HashMap<Integer, EmployeeOrganizationModel> buildOrganizationMap(List<EmployeeOrganizationModel> orgList) {
            HashMap<Integer, EmployeeOrganizationModel> resultMap = new HashMap<>();
            for (EmployeeOrganizationModel model : orgList) {
                resultMap.put(model.getID(), model);
            }
            return resultMap;
        }
    }
}
