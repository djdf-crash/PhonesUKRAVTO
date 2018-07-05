package ua.in.ukravto.kb.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import net.steamcrafted.loadtoast.LoadToast;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.databinding.ActivityMainBinding;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    public static final String AUTHORITY = "com.android.contacts";
    private ActivityMainBinding mBinding;
    private MainViewModel mViewModel;
    private static final int PERMISSION_READ_STATE = 0;
    private boolean PERMISSION_READ_STATE_GRANTED;
    private LoadToast mLoadToast;
    private String deviceID;

    private TelephonyManager tm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PERMISSION_READ_STATE_GRANTED = false;

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBinding.toolbar);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);

        mLoadToast =  new LoadToast(this);
        mLoadToast.setText(getString(R.string.text_send_request));
        mLoadToast.setTranslationY(350);

        mViewModel.getResponseLoginLiveData().observe(this, new Observer<ResponseString<String>>() {
            @Override
            public void onChanged(@Nullable ResponseString<String> stringResponseString) {
                if (stringResponseString != null && stringResponseString.getResult() && TextUtils.isEmpty(stringResponseString.getError())){
                    Pref.getInstance(getApplicationContext()).edit().putString(Pref.USER_TOKEN, stringResponseString.getBody()).apply();
                    Pref.getInstance(getApplicationContext()).edit().putString(Pref.EMAIL, mBinding.editEmail.getEditableText().toString()).apply();
                    mLoadToast.success();
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    finish();
                }else {
                    mBinding.bSendEmail.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake_error));
                    Toast.makeText(getApplicationContext(), stringResponseString.getError(), Toast.LENGTH_LONG).show();
                    mLoadToast.error();
                }
            }
        });

        mBinding.bSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mBinding.editEmail.getText().toString())) {
                    mBinding.editEmail.setError(getString(R.string.error_email_is_empty));
                    mBinding.bSendEmail.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake_error));
                } else {
                    if (PERMISSION_READ_STATE_GRANTED) {
                        mBinding.editEmail.setError(null);
                        mLoadToast.show();
                        mViewModel.logIn(mBinding.editEmail.getText().toString(), deviceID);
                    }else {
                        checkPermissionReadPhoneState();
                    }
                }
            }
        });

        checkPermissionReadPhoneState();
    }

    private void checkPermissionReadPhoneState() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_STATE);

        } else {

            PERMISSION_READ_STATE_GRANTED = true;

            deviceID = Pref.getInstance(getApplicationContext()).getString(Pref.DEVICE_ID, "");
            String token = Pref.getInstance(getApplicationContext()).getString(Pref.USER_TOKEN, "");
            if (TextUtils.isEmpty(deviceID)) {
                deviceID = tm.getDeviceId();
                Pref.getInstance(getApplicationContext()).edit().putString(Pref.DEVICE_ID, deviceID).apply();
                Pref.getInstance(getApplicationContext()).edit().putString(Pref.EMAIL, mBinding.editEmail.getText().toString()).apply();
            }else if (!TextUtils.isEmpty(token)){
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
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

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_READ_STATE) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                PERMISSION_READ_STATE_GRANTED = true;

                deviceID = Pref.getInstance(getApplicationContext()).getString(Pref.DEVICE_ID, "");
                if (TextUtils.isEmpty(deviceID)) {
                    deviceID = tm.getDeviceId();
                    Pref.getInstance(getApplicationContext()).edit().putString(Pref.DEVICE_ID, deviceID).apply();
                    Pref.getInstance(getApplicationContext()).edit().putString(Pref.EMAIL, mBinding.editEmail.getText().toString()).apply();
                }

            } else {

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getString(R.string.perm_is_req))
                        .setPositiveButton(getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_PHONE_STATE)) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_STATE);
                                }else {
                                    startAppSettingsConfigActivity();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.text_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        }
    }
}
