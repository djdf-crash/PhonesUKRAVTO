package ua.in.ukravto.kb.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.databinding.ActivityMainBinding;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    public static final String AUTHORITY = "TEST";
    private ActivityMainBinding mBinding;
    private MainViewModel mViewModel;
    private static final int PERMISSION_READ_STATE = 0;
    private boolean PERMISSION_READ_STATE_GRANTED;

    private String deviceID;

    private TelephonyManager tm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PERMISSION_READ_STATE_GRANTED = false;

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);

        mBinding.bSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mBinding.editEmail.getText().toString())) {
                    mBinding.editEmail.setError("Email is empty!");
                    mBinding.bSendEmail.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake_error));
                } else {
                    checkPermission();
                    if (PERMISSION_READ_STATE_GRANTED) {
                        mBinding.editEmail.setError(null);
                        mViewModel.logIn(mBinding.editEmail.getText().toString(), deviceID);
                    }
                }
            }
        });
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                        PERMISSION_READ_STATE);
            }
        } else {

            PERMISSION_READ_STATE_GRANTED = true;

            deviceID = Pref.getInstance(getApplicationContext()).getString(Pref.DEVICE_ID, "");
            if (TextUtils.isEmpty(deviceID)) {
                deviceID = tm.getDeviceId();
                Pref.getInstance(getApplicationContext()).edit().putString(Pref.DEVICE_ID, deviceID).apply();
            }

            Toast.makeText(this, "Alredy granted", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_READ_STATE) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                PERMISSION_READ_STATE_GRANTED = true;

                deviceID = Pref.getInstance(getApplicationContext()).getString(Pref.DEVICE_ID,"");
                if (TextUtils.isEmpty(deviceID)){
                    deviceID = tm.getDeviceId();
                    Pref.getInstance(getApplicationContext()).edit().putString(Pref.DEVICE_ID, deviceID).apply();
                }

            } else {
//                finish();
            }
        }
    }
}
