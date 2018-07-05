package ua.in.ukravto.kb.view;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.steamcrafted.loadtoast.LoadToast;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.databinding.ActivitySettingsBinding;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.service.DownloadService;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.viewmodel.SettingsViewModel;

import static ua.in.ukravto.kb.view.MainActivity.AUTHORITY;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding mBinding;
    private SettingsViewModel mViewModel;
    private AccountManager mAccountManager;
    private String mToken;
    private LoadToast mLoadToast;

    private static boolean PERMISSION_READ_STORAGE_GRANTED = false;
    private static final int REQUEST_PERMISSIONS_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLoadToast =  new LoadToast(this);

        mBinding.selectSyncOrganization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SelectOrganizationActivity.class));
            }
        });

        mViewModel.getIsLastUpdateAPPLiveData().observe(this, new Observer<ResponseString<String>>() {
            @Override
            public void onChanged(@Nullable ResponseString<String> stringResponseString) {
                if (stringResponseString != null && stringResponseString.getResult()){

                    new AlertDialog.Builder(SettingsActivity.this)
                            .setMessage(R.string.dowload_new_version_app)
                            .setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    checkPermissionWriteExternalStorage();
                                    if (PERMISSION_READ_STORAGE_GRANTED) {
                                        startService(new Intent(getApplicationContext(), DownloadService.class));
                                    }
                                }
                            })
                            .setNegativeButton(R.string.text_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();


                }else {
                    Toast.makeText(getApplicationContext(), R.string.text_app_is_latest, Toast.LENGTH_LONG).show();
                }
                if (stringResponseString != null) {
                    mLoadToast.success();
                }else {
                    mLoadToast.error();
                }
            }
        });

        mToken = Pref.getInstance(getApplicationContext()).getString(Pref.USER_TOKEN, "");

        mAccountManager = AccountManager.get(getApplicationContext());

        Account acc = new Account(getResources().getString(R.string.custom_account), getResources().getString(R.string.ACCOUNT_TYPE));
        addAccount(acc);

    }

    private void addAccount(Account account) {
        if (mAccountManager.addAccountExplicitly(account, null, null)) {
            mAccountManager.setAuthToken(account, "full_access", mToken);
            ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 10800);
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            mLoadToast.setText(getString(R.string.text_send_request));
            mLoadToast.setTranslationY(350);
            mLoadToast.show();
            mViewModel.getLastUpdate();
        }else if (id == android.R.id.home){
            finish();
        }else if (id == R.id.action_sync_now){
            syncNow();
        }

        return super.onOptionsItemSelected(item);
    }

    private void syncNow() {
        for (Account acc : AccountManager.get(getApplicationContext()).getAccountsByType(getString(R.string.ACCOUNT_TYPE))) {
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean("force", true);
            settingsBundle.putBoolean("expedited", true);
            ContentResolver.requestSync(acc, MainActivity.AUTHORITY, settingsBundle);
        }
    }


    @Override
    protected void onStart() {
        mBinding.syncOnlyNewUpdates.setChecked(Pref.getInstance(this).getBoolean(Pref.SYNC_ONLY_NEW_UPDATE_PHONES, true));
        mBinding.syncOnlyWithPhone.setChecked(Pref.getInstance(this).getBoolean(Pref.SYNC_WITH_PHONES_ONLY, true));
        mBinding.autoCheckUpdateApk.setChecked(Pref.getInstance(this).getBoolean(Pref.AUTO_CHECK_UPDATE_APK, true));
        super.onStart();
    }

    @Override
    protected void onPause() {
        Pref.getInstance(getApplicationContext()).edit().putBoolean(Pref.SYNC_ONLY_NEW_UPDATE_PHONES, mBinding.syncOnlyNewUpdates.isChecked()).apply();
        Pref.getInstance(getApplicationContext()).edit().putBoolean(Pref.SYNC_WITH_PHONES_ONLY, mBinding.syncOnlyWithPhone.isChecked()).apply();
        Pref.getInstance(getApplicationContext()).edit().putBoolean(Pref.AUTO_CHECK_UPDATE_APK, mBinding.autoCheckUpdateApk.isChecked()).apply();
        super.onPause();
    }

    private void checkPermissionWriteExternalStorage() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_STORAGE);

        } else {

            PERMISSION_READ_STORAGE_GRANTED = true;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSIONS_STORAGE) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                PERMISSION_READ_STORAGE_GRANTED = true;

            } else {

                new AlertDialog.Builder(SettingsActivity.this)
                        .setMessage(R.string.perm_is_req)
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_STORAGE);
                                }else {
                                    startAppSettingsConfigActivity();
                                }
                            }
                        })
                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        }
    }

    private void startAppSettingsConfigActivity() {
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);
    }

}
