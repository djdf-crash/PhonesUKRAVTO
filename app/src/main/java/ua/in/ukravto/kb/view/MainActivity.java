package ua.in.ukravto.kb.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.databinding.ActivityMainBinding;
import ua.in.ukravto.kb.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    public static final String AUTHORITY = "TEST";
    private ActivityMainBinding mBinding;
    private MainViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wm.getConnectionInfo().getMacAddress();

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        mBinding.bSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mBinding.editEmail.getText().toString())){
                    mBinding.editEmail.setError("Email is empty!");
                    mBinding.bSendEmail.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake_error));
                }else {
                    mBinding.editEmail.setError(null);
                    mViewModel.logIn(mBinding.editEmail.getText().toString());
                }
            }
        });
    }
}
