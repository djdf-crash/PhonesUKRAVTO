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

import java.lang.reflect.Type;
import java.util.ArrayList;
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

    public static void performSync(final Context context, final AccountManager accountManager , final Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "performSync: " + account.toString());

        final long lastSyncMarker = getServerSyncMarker(accountManager, account);

        String token = Pref.getInstance(context).getString(Pref.USER_TOKEN,"");
        if (TextUtils.isEmpty(token)){
            return;
        }


        Gson mGson = new Gson();
        String organizationsString = Pref.getInstance(context).getString(Pref.SAVED_ORGANIZATIONS,"");
        Type type = new TypeToken<List<EmployeeOrganizationModel>>(){}.getType();
        List<EmployeeOrganizationModel> listSavedOrganization = mGson.fromJson(organizationsString, type);
        if (listSavedOrganization == null){
            listSavedOrganization = new ArrayList<>();
        }

        for (final EmployeeOrganizationModel organizationModel : listSavedOrganization) {
            RetrofitHelper.getPhoneService().getOrganizationIDPhonesLastUpdate(organizationModel.getID(), token).enqueue(new Callback<PhoneResponse<EmployeePhoneModel>>() {
                @Override
                public void onResponse(Call<PhoneResponse<EmployeePhoneModel>> call, Response<PhoneResponse<EmployeePhoneModel>> response) {
                    Log.d("IS_Successful:", String.valueOf(response.isSuccessful()));
                    if (response.isSuccessful()){
                        if (response.body() != null) {
                            Log.d("LIST_SIZE_PHONES_ORG:", String.valueOf(response.body().getBody().size()));
                            long newSyncState = ContactsManager.syncContacts(context, account, response.body().getBody(), lastSyncMarker);
                            setServerSyncMarker(accountManager, account, newSyncState);
                        }
                    }
                }

                @Override
                public void onFailure(Call<PhoneResponse<EmployeePhoneModel>> call, Throwable t) {
                    Log.d("LIST_SIZE", t.getMessage());
                }
            });
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
