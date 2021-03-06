package ua.in.ukravto.kb.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.adapters.ListOrganizationRecyclerAdapter;
import ua.in.ukravto.kb.databinding.ActivitySelectOrganizationBinding;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.viewmodel.SelectOrganizationViewModel;

public class SelectOrganizationActivity extends AppCompatActivity {

    private static boolean PERMISSION_READ_STATE_GRANTED = false;
    private ActivitySelectOrganizationBinding mBinding;
    private SelectOrganizationViewModel mViewModel;
    private LoadToast mLoadToast;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<EmployeeOrganizationModel> listOrganization;
    private ListOrganizationRecyclerAdapter mAdapter;
    private String mToken;
    private SearchView searchView;

    private static final int REQUEST_PERMISSIONS_CONTACTS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_organization);

       setSupportActionBar(mBinding.toolbar);
       getSupportActionBar().setHomeButtonEnabled(true);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.recyclerListOrganization.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        listOrganization = new ArrayList<>();
        mBinding.recyclerListOrganization.setLayoutManager(mLayoutManager);

        mBinding.recyclerListOrganization.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        mViewModel = ViewModelProviders.of(this).get(SelectOrganizationViewModel.class);

        mAdapter = new ListOrganizationRecyclerAdapter();

        mLoadToast =  new LoadToast(this);
        mLoadToast.setText(getString(R.string.get_data_organizations));
        mLoadToast.setTranslationY(350);
        mLoadToast.show();

        mToken = Pref.getInstance(getApplicationContext()).getString(Pref.USER_TOKEN, "");

        mViewModel.getListOrganizations(mToken).observe(this, new Observer<PhoneResponse<EmployeeOrganizationModel>>() {
            @Override
            public void onChanged(@Nullable PhoneResponse<EmployeeOrganizationModel> employeeOrganizationModelPhoneResponse) {
                if (employeeOrganizationModelPhoneResponse != null) {
                    if (employeeOrganizationModelPhoneResponse.getResult() && TextUtils.isEmpty(employeeOrganizationModelPhoneResponse.getError())) {
                        listOrganization = employeeOrganizationModelPhoneResponse.getBody();
                        mAdapter.setData(listOrganization);
                        mBinding.recyclerListOrganization.setAdapter(mAdapter);
                        mLoadToast.success();
                    } else {
                        Toast.makeText(getApplicationContext(), employeeOrganizationModelPhoneResponse.getError(), Toast.LENGTH_LONG).show();
                        mLoadToast.error();
                    }
                }
            }
        });

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

        mBinding.fabOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               saveAndClose();
            }
        });

        checkPermissionContacts();

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
                        .setMessage(getString(R.string.perm_is_req))
                        .setPositiveButton(R.string.text_retry, new DialogInterface.OnClickListener() {
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
                        .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_organization, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }else if (id == android.R.id.home){
            saveAndClose();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveAndClose() {
        checkPermissionContacts();
        if (PERMISSION_READ_STATE_GRANTED) {
            finish();
        }
    }
}
