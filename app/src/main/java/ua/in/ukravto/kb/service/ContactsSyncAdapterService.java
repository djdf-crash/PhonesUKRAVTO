package ua.in.ukravto.kb.service;

import android.accounts.Account;
import android.arch.lifecycle.LifecycleService;
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

public class ContactsSyncAdapterService extends LifecycleService {
    private static final String TAG = "ContactsSyncAdapterS";
    private static ContentResolver mContentResolver = null;
    private static SyncAdapterImpl sSyncAdapter = null;


    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return getSyncAdapter().getSyncAdapterBinder();
    }

    private SyncAdapterImpl getSyncAdapter() {
        if (sSyncAdapter == null) {
            sSyncAdapter = new SyncAdapterImpl(this);
        }
        return sSyncAdapter;
    }

    private static void performSync(final Context context, final Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "performSync: " + account.toString());
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
                            ContactsManager.syncContacts(context, account, response.body().getBody());
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

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;

        public SyncAdapterImpl(Context context) {
            super(context, true);
            this.mContext = context;
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            performSync(this.mContext, account, extras, authority, provider, syncResult);
            Log.d(ContactsSyncAdapterService.TAG, syncResult.toDebugString());
        }
    }
}
