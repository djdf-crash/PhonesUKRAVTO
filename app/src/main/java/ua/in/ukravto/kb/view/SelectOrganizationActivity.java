package ua.in.ukravto.kb.view;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.loadtoast.LoadToast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.adapters.ListOrganizationRecyclerAdapter;
import ua.in.ukravto.kb.databinding.ActivitySelectOrganizationBinding;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.service.RetrofitHelper;
import ua.in.ukravto.kb.service.ContactsSyncAdapterService;
import ua.in.ukravto.kb.utils.ContactsManager;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.viewmodel.SelectOrganizationViewModel;

import static ua.in.ukravto.kb.service.ContactsSyncAdapterService.SYNC_MARKER_KEY;
import static ua.in.ukravto.kb.view.MainActivity.AUTHORITY;

public class SelectOrganizationActivity extends AppCompatActivity {

    private static boolean PERMISSION_READ_STATE_GRANTED = false;
    private ActivitySelectOrganizationBinding mBinding;
    private SelectOrganizationViewModel mViewModel;
    private LoadToast mLoadToast;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<EmployeeOrganizationModel> listOrganization;
    private ListOrganizationRecyclerAdapter mAdapter;
    private AccountManager mAccountManager;
    private String mToken;

    private static final int REQUEST_PERMISSIONS_CONTACTS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_organization);
        mBinding.recyclerListOrganization.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        listOrganization = new ArrayList<>();
        mBinding.recyclerListOrganization.setLayoutManager(mLayoutManager);

        mBinding.recyclerListOrganization.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        mAccountManager = AccountManager.get(getApplicationContext());

        mViewModel = ViewModelProviders.of(this).get(SelectOrganizationViewModel.class);

        mLoadToast =  new LoadToast(this);
        mLoadToast.setText("I will get data on organizations...");
        mLoadToast.setTranslationY(350);
        mLoadToast.show();

        mBinding.btSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.selectOrganization(listOrganization, true);
                if (listOrganization.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        mBinding.btSelectNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.selectOrganization(listOrganization, false);
                if (listOrganization.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        mBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionContacts();
                if (PERMISSION_READ_STATE_GRANTED) {
                    List<EmployeeOrganizationModel> listSave = mViewModel.saveOrganizationForSync(listOrganization);
                    if (listSave.size() > 0) {
                        Account acc = new Account(getString(R.string.custom_account), getString(R.string.ACCOUNT_TYPE));
                        addAccount(acc);
                        syncNow(acc);
                    }
                    finish();
                }
            }
        });
    }

    private void syncNow(final Account account){
        final AccountManager accountManager = AccountManager.get(getApplicationContext());
        final long lastSyncMarker = getServerSyncMarker(accountManager, account);

        String token = Pref.getInstance(this).getString(Pref.USER_TOKEN,"");
        if (TextUtils.isEmpty(token)){
            return;
        }

        Gson mGson = new Gson();
        String organizationsString = Pref.getInstance(this).getString(Pref.SAVED_ORGANIZATIONS,"");
        Type type = new TypeToken<List<EmployeeOrganizationModel>>(){}.getType();
        List<EmployeeOrganizationModel> listSavedOrganization = mGson.fromJson(organizationsString, type);
        if (listSavedOrganization == null){
            listSavedOrganization = new ArrayList<>();
        }

        for (final EmployeeOrganizationModel organizationModel : listSavedOrganization) {
            RetrofitHelper.getPhoneService().getOrganizationIDPhonesLastUpdate(organizationModel.getID(), token).enqueue(new Callback<PhoneResponse<EmployeePhoneModel>>() {
                @Override
                public void onResponse(Call<PhoneResponse<EmployeePhoneModel>> call, final Response<PhoneResponse<EmployeePhoneModel>> response) {
                    Log.d("IS_Successful:", String.valueOf(response.isSuccessful()));
                    if (response.isSuccessful()){
                        if (response.body() != null) {
                            Log.d("LIST_SIZE_PHONES_ORG:", String.valueOf(response.body().getBody().size()));
                            new AlertDialog.Builder(SelectOrganizationActivity.this)
                                    .setMessage("Do you want to update your phone directory now?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            long newSyncState = ContactsManager.syncContacts(getApplicationContext(), account, response.body().getBody(),lastSyncMarker);
                                            setServerSyncMarker(accountManager, account, newSyncState);
                                            finish();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            finish();
                                        }
                                    }).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<PhoneResponse<EmployeePhoneModel>> call, Throwable t) {
                    Log.d("LIST_SIZE", t.getMessage());
                }
            });
        }
    }

    private static long getServerSyncMarker(AccountManager mAccountManager, Account account) {
        String markerString = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
        if (!TextUtils.isEmpty(markerString)) {
            return Long.parseLong(markerString);
        }
        return 0;
    }

    private static void setServerSyncMarker(AccountManager mAccountManager, Account account, long marker) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, Long.toString(marker));
    }

    private void checkPermissionContacts() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SYNC_SETTINGS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_SYNC_SETTINGS}, REQUEST_PERMISSIONS_CONTACTS);

        }else {
            PERMISSION_READ_STATE_GRANTED = true;
        }
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
    protected void onStart() {
        mToken = Pref.getInstance(getApplicationContext()).getString(Pref.USER_TOKEN, "");

        mViewModel.getListOrganizations(mToken).observe(this, new Observer<PhoneResponse<EmployeeOrganizationModel>>() {
            @Override
            public void onChanged(@Nullable PhoneResponse<EmployeeOrganizationModel> employeeOrganizationModelPhoneResponse) {
                if (employeeOrganizationModelPhoneResponse != null) {
                    if (employeeOrganizationModelPhoneResponse.getResult() && TextUtils.isEmpty(employeeOrganizationModelPhoneResponse.getError())) {
                        listOrganization = employeeOrganizationModelPhoneResponse.getBody();
                        mAdapter = new ListOrganizationRecyclerAdapter();
                        mViewModel.checkListOrganization(listOrganization);
                        mAdapter.setData(listOrganization);
                        mAdapter.notifyDataSetChanged();
                        mBinding.recyclerListOrganization.setAdapter(mAdapter);
                        mLoadToast.success();
                    } else {
                        Toast.makeText(getApplicationContext(), employeeOrganizationModelPhoneResponse.getError(), Toast.LENGTH_LONG).show();
                        mLoadToast.error();
                    }
                }
            }
        });
        super.onStart();
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

        if (requestCode == REQUEST_PERMISSIONS_CONTACTS) {

            PERMISSION_READ_STATE_GRANTED = true;

            List<String> permisionsDenide = new ArrayList<>();

            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                String permission = permissions[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED){
                    permisionsDenide.add(permission);
                }
            }

            if (permisionsDenide.size() > 0){
                PERMISSION_READ_STATE_GRANTED = false;
                new AlertDialog.Builder(SelectOrganizationActivity.this)
                        .setMessage("This permission is required for the correct operation of the application!")
                        .setPositiveButton("retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(SelectOrganizationActivity.this, Manifest.permission.WRITE_CONTACTS) ||
                                        ActivityCompat.shouldShowRequestPermissionRationale(SelectOrganizationActivity.this, Manifest.permission.READ_CONTACTS) ||
                                        ActivityCompat.shouldShowRequestPermissionRationale(SelectOrganizationActivity.this, Manifest.permission.WRITE_SYNC_SETTINGS)) {
                                    ActivityCompat.requestPermissions(SelectOrganizationActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS,
                                                    Manifest.permission.READ_CONTACTS,
                                                    Manifest.permission.WRITE_SYNC_SETTINGS},
                                            REQUEST_PERMISSIONS_CONTACTS);
                                } else {
                                    startAppSettingsConfigActivity();
                                }
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        }
    }
}
