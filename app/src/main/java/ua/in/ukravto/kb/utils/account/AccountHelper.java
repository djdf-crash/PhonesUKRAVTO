package ua.in.ukravto.kb.utils.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.in.ukravto.kb.view.MainActivity.AUTHORITY;

public class AccountHelper {

    public static void addAccount(AccountManager mAccountManager, Account account, String mToken) {
        if (mAccountManager.addAccountExplicitly(account, null, null)) {
            mAccountManager.setAuthToken(account, "full_access", mToken);
            ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 10800);
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
        }
    }

    public static List<Account> findAccountsByType(AccountManager mAccountManager, String typeAccount) {
        List<Account> accountList = new ArrayList<>(Arrays.asList(mAccountManager.getAccountsByType(typeAccount)));
        return accountList;
    }

}
