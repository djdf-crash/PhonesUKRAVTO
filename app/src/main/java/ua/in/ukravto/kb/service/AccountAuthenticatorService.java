package ua.in.ukravto.kb.service;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import java.util.Objects;

import androidx.annotation.Nullable;
import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.view.MainActivity;

public class AccountAuthenticatorService extends Service {
    private static final String TAG = "AccountAuthenticatorService";
    private AccountAuthenticatorImpl sAccountAuthenticator = null;

    @Nullable
    public IBinder onBind(Intent intent) {
        if (Objects.requireNonNull(intent.getAction()).equals("android.accounts.AccountAuthenticator")) {
            return getAuthenticator().getIBinder();
        }
        return null;
    }

    private AccountAuthenticatorImpl getAuthenticator() {
        if (sAccountAuthenticator == null) {
            sAccountAuthenticator = new AccountAuthenticatorImpl(this);
        }
        return sAccountAuthenticator;
    }

    private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {
        private Context mContext;

        AccountAuthenticatorImpl(Context context) {
            super(context);
            this.mContext = context;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
            Bundle reply = new Bundle();
            Intent i = new Intent(mContext, MainActivity.class);
            i.setAction(mContext.getString(R.string.ACCOUNT_TYPE));
            i.putExtra("accountAuthenticatorResponse", response);
            reply.putParcelable("intent", i);
            return reply;
        }

        @Override
        public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
            Pref.getInstance(mContext).edit().putString(Pref.USER_TOKEN,"").apply();
            Pref.getInstance(mContext).edit().putString(Pref.DELETE_ORGANIZATIONS,"").apply();
            Pref.getInstance(mContext).edit().putString(Pref.SAVED_ORGANIZATIONS,"").apply();
            return super.getAccountRemovalAllowed(response, account);
        }

        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
            return null;
        }

        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }

        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
            String authToken = Pref.getInstance(mContext).getString(Pref.USER_TOKEN,"");
            if (!TextUtils.isEmpty(authToken)){
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }
            return null;
        }

        public String getAuthTokenLabel(String authTokenType) {
            return null;
        }

        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
            return null;
        }

        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
            return null;
        }
    }
}
