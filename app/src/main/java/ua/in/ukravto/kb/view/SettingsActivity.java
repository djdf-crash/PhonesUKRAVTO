package ua.in.ukravto.kb.view;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Toast;

import net.steamcrafted.loadtoast.LoadToast;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.databinding.ActivitySettingsBinding;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.viewmodel.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding mBinding;
    private SettingsViewModel mViewModel;
    private LoadToast mLoadToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        mViewModel = mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);

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
                                    //download
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

        mBinding.syncOnlyNewUpdates.setChecked(Pref.getInstance(this).getBoolean(Pref.SYNC_ONLY_NEW_UPDATE_PHONES, false));
        mBinding.syncOnlyNewUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Pref.getInstance(SettingsActivity.this).edit().putBoolean(Pref.SYNC_ONLY_NEW_UPDATE_PHONES, b).apply();
            }
        });

        mBinding.autoCheckUpdateApk.setChecked(Pref.getInstance(this).getBoolean(Pref.AUTO_CHECK_UPDATE_APK, true));
        mBinding.autoCheckUpdateApk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Pref.getInstance(SettingsActivity.this).edit().putBoolean(Pref.AUTO_CHECK_UPDATE_APK, b).apply();
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

}
