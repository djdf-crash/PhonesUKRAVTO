package ua.in.ukravto.kb.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.service.RetrofitHelper;
import ua.in.ukravto.kb.utils.ContactsManager;
import ua.in.ukravto.kb.utils.Pref;

public class ContactsSyncAdapterService extends Service {
    private static final String TAG = "ContactsSyncAdapterS";
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

    public static void performSync(final Context context, final AccountManager accountManager , final Account account, Bundle extras, String authority, ContentProviderClient provider, final SyncResult syncResult) {
        Log.d(TAG, "performSync: " + account.toString());

        long lastSyncMarker = getServerSyncMarker(accountManager, account);
        long newSyncState = lastSyncMarker;

        final String token = Pref.getInstance(context).getString(Pref.USER_TOKEN,"");
        if (TextUtils.isEmpty(token)){
            return;
        }


        Gson mGson = new Gson();
        String savedOrganizationsString = Pref.getInstance(context).getString(Pref.SAVED_ORGANIZATIONS,"");
        String deleteOrganizationsString = Pref.getInstance(context).getString(Pref.DELETE_ORGANIZATIONS,"");

        Type type = new TypeToken<List<EmployeeOrganizationModel>>(){}.getType();
        List<EmployeeOrganizationModel> listSavedOrganization = mGson.fromJson(savedOrganizationsString, type);
        List<EmployeeOrganizationModel> listDeleteOrganization = mGson.fromJson(deleteOrganizationsString, type);
        List<EmployeeOrganizationModel> listNotDeleteOrganization = new ArrayList<>();

        if (listDeleteOrganization == null){
            listDeleteOrganization = new ArrayList<>();
        }

        if (listSavedOrganization == null){
            listSavedOrganization = new ArrayList<>();
        }

        Log.d(TAG, "org list: " + listSavedOrganization.size());
        Log.d(TAG, "org list del: " + listDeleteOrganization.size());

        for (EmployeeOrganizationModel delOrganizationModel : listDeleteOrganization) {
            Log.d(TAG, "org name del: " + delOrganizationModel.getName());
            try {
                Response<PhoneResponse<EmployeePhoneModel>> response = RetrofitHelper.getPhoneService().getOrganizationIDPhonesLastUpdate(delOrganizationModel.getID(), token).execute();
                if (response.isSuccessful()){
                    if (response.body() != null || response.body().getBody() != null) {
                        Log.d(TAG, "size org del: " + response.body().getBody().size());
                        ContactsManager.deleteContacts(context, account, response.body().getBody());
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

        for (final EmployeeOrganizationModel organizationModel : listSavedOrganization) {
            Log.d(TAG, "org name: " + organizationModel.getName());
            try {
                Response<PhoneResponse<EmployeePhoneModel>> response = RetrofitHelper.getPhoneService().getOrganizationIDPhonesLastUpdate(organizationModel.getID(), token).execute();
                if (response.isSuccessful()){
                    if (response.body() != null || response.body().getBody() != null) {
                        Log.d(TAG, "size org: " + response.body().getBody().size());
                        newSyncState = ContactsManager.syncContacts(context, account, response.body().getBody(), lastSyncMarker);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
                syncResult.stats.numIoExceptions++;
            }

//            RetrofitHelper.getPhoneService().getOrganizationIDPhonesLastUpdate(organizationModel.getID(), token).enqueue(new Callback<PhoneResponse<EmployeePhoneModel>>() {
//                @Override
//                public void onResponse(Call<PhoneResponse<EmployeePhoneModel>> call, Response<PhoneResponse<EmployeePhoneModel>> response) {
//                    Log.d("IS_Successful:", String.valueOf(response.isSuccessful()));
//                    if (response.isSuccessful()){
//                        if (response.body() != null) {
//                            Log.d("LIST_SIZE_PHONES_ORG:", String.valueOf(response.body().getBody().size()));
//                            long newSyncState = ContactsManager.syncContacts(context, account, response.body().getBody(), lastSyncMarker);
//                            setServerSyncMarker(accountManager, account, newSyncState);
//                            //RetrofitHelper.getPhoneService().updateUser(token);
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<PhoneResponse<EmployeePhoneModel>> call, Throwable t) {
//                    Log.d("LIST_SIZE", t.getMessage());
//                    syncResult.stats.numIoExceptions ++;
//                }
//            });
        }
        setServerSyncMarker(accountManager, account, newSyncState);
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
            Log.d(ContactsSyncAdapterService.TAG, syncResult.toDebugString());
        }
    }
}
