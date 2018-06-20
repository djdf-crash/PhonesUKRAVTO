package ua.in.ukravto.kb.service;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.content.SyncStats;
import android.os.Bundle;
import android.os.IBinder;

public class ContactsSyncAdapterService extends Service {
    private static final String TAG = "ContactsSyncAdapterS";
    private static ContentResolver mContentResolver = null;
    private static SyncAdapterImpl sSyncAdapter = null;

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;

        public SyncAdapterImpl(Context context) {
            super(context, true);
            this.mContext = context;
        }

        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            String str;
            SyncStats syncStats;
            long j;
//            try {
//                //ContactsSyncAdapterService.performSync(this.mContext, account, extras, authority, provider, syncResult);
//                Log.d(ContactsSyncAdapterService.TAG, syncResult.toDebugString());
//            } catch (OperationCanceledException e) {
//                str = ContactsSyncAdapterService.TAG;
//                syncStats = syncResult.stats;
//                j = syncStats.numSkippedEntries;
//                syncStats.numSkippedEntries = j + 1;
//                Log.d(str, String.valueOf(j));
//            } catch (IOException e2) {
//                str = ContactsSyncAdapterService.TAG;
//                syncStats = syncResult.stats;
//                j = syncStats.numIoExceptions;
//                syncStats.numIoExceptions = j + 1;
//                Log.d(str, String.valueOf(j));
//            }
        }
    }


    public IBinder onBind(Intent intent) {
        return getSyncAdapter().getSyncAdapterBinder();
    }

    private SyncAdapterImpl getSyncAdapter() {
        if (sSyncAdapter == null) {
            sSyncAdapter = new SyncAdapterImpl(this);
        }
        return sSyncAdapter;
    }

//    private static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) throws OperationCanceledException, IOException {
//        Log.d(TAG, "performSync: " + account.toString());
//        API api = (API) new Builder().baseUrl(Constants.SERVICE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(API.class);
//        Call<List<ItemContact>> itemListCall = null;
//        if (account.name.contains(context.getString(R.string.mercedes_benz))) {
//            itemListCall = api.getMercedesTelephones();
//        } else if (account.name.contains(context.getString(R.string.odessa_auto))) {
//            itemListCall = api.getOaTelephones();
//        }
//        List<ItemContact> itemContactList = Collections.EMPTY_LIST;
//        if (itemListCall != null) {
//            itemContactList = (List) itemListCall.execute().body();
//        }
//        for (ItemContact itemContact : itemContactList) {
//            Contacts.getContact(context, account, itemContact);
//        }
//    }
}
