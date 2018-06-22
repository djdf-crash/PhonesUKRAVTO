package ua.in.ukravto.kb.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.loadtoast.LoadToast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.adapters.ListOrganizationRecyclerAdapter;
import ua.in.ukravto.kb.databinding.ActivitySelectOrganizationBinding;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.utils.Pref;
import ua.in.ukravto.kb.viewmodel.SelectOrganizationViewModel;

public class SelectOrganizationActivity extends AppCompatActivity {

    private ActivitySelectOrganizationBinding mBinding;
    private SelectOrganizationViewModel mViewModel;
    private LoadToast mLoadToast;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<EmployeeOrganizationModel> listOrganization;
    private ListOrganizationRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_organization);
        mBinding.recyclerListOrganization.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mBinding.recyclerListOrganization.setLayoutManager(mLayoutManager);

        mViewModel = ViewModelProviders.of(this).get(SelectOrganizationViewModel.class);

        mLoadToast =  new LoadToast(this);
        mLoadToast.setText("I will get data on organizations...");
        mLoadToast.show();

        String token = Pref.getInstance(getApplicationContext()).getString(Pref.USER_TOKEN, "");

        mViewModel.getListOrganizations(token).observe(this, new Observer<PhoneResponse<EmployeeOrganizationModel>>() {
            @Override
            public void onChanged(@Nullable PhoneResponse<EmployeeOrganizationModel> employeeOrganizationModelPhoneResponse) {
                if (employeeOrganizationModelPhoneResponse != null && employeeOrganizationModelPhoneResponse.getResult() && TextUtils.isEmpty(employeeOrganizationModelPhoneResponse.getError())) {
                    listOrganization = employeeOrganizationModelPhoneResponse.getBody();
                    mAdapter = new ListOrganizationRecyclerAdapter();
                    mViewModel.checkListOrganization(listOrganization);
                    mAdapter.setData(listOrganization);
                    mBinding.recyclerListOrganization.setAdapter(mAdapter);
                    mLoadToast.success();
                }else {
                    Toast.makeText(getApplicationContext(), employeeOrganizationModelPhoneResponse.getError(), Toast.LENGTH_LONG).show();
                    mLoadToast.error();
                }
            }
        });

        mBinding.btSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.selectOrganization(listOrganization, true);
                mAdapter.notifyDataSetChanged();
            }
        });

        mBinding.btSelectNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.selectOrganization(listOrganization, false);
                mAdapter.notifyDataSetChanged();
            }
        });

        mBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.saveOrganizationForSync(listOrganization);
                finish();
            }
        });
    }
}
