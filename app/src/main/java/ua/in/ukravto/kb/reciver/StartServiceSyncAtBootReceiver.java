package ua.in.ukravto.kb.reciver;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.view.MainActivity;

public class StartServiceSyncAtBootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            for (Account acc : AccountManager.get(context).getAccountsByType(context.getString(R.string.ACCOUNT_TYPE))) {
                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean("force", true);
                settingsBundle.putBoolean("expedited", true);
                ContentResolver.requestSync(acc, MainActivity.AUTHORITY, settingsBundle);
            }
        }
    }
}
