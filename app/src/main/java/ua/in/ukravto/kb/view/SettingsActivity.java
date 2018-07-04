package ua.in.ukravto.kb.view;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.CompoundButton;
import android.widget.Toast;

import net.steamcrafted.loadtoast.LoadToast;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.databinding.ActivitySettingsBinding;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.service.DownloadService;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.viewmodel.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding mBinding;
    private SettingsViewModel mViewModel;
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
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    checkPermissionReadPhoneState();
                                    if (PERMISSION_READ_STORAGE_GRANTED) {
                                        startService(new Intent(getApplicationContext(), DownloadService.class));
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

        mBinding.syncOnlyNewUpdates.setChecked(Pref.getInstance(this).getBoolean(Pref.SYNC_ONLY_NEW_UPDATE_PHONES, true));
        mBinding.syncOnlyNewUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });

        mBinding.autoCheckUpdateApk.setChecked(Pref.getInstance(this).getBoolean(Pref.AUTO_CHECK_UPDATE_APK, true));
        mBinding.autoCheckUpdateApk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });

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
            mLoadToast.setText("Send request...");
            mLoadToast.setTranslationY(350);
            mLoadToast.show();
            mViewModel.getLastUpdate();
        }else if (id == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        Pref.getInstance(getApplicationContext()).edit().putBoolean(Pref.SYNC_ONLY_NEW_UPDATE_PHONES, mBinding.syncOnlyNewUpdates.isChecked()).apply();
        Pref.getInstance(getApplicationContext()).edit().putBoolean(Pref.AUTO_CHECK_UPDATE_APK, mBinding.autoCheckUpdateApk.isChecked()).apply();
        super.onStop();
    }

    private void checkPermissionReadPhoneState() {

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
                        .setMessage("This permission is required for the correct operation of the application!")
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
