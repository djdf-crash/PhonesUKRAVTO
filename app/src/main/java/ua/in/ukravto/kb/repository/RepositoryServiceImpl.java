package ua.in.ukravto.kb.repository;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.repository.service.RetrofitHelper;

public class RepositoryServiceImpl implements RepositoryService {

    private Context mCtx;

    public RepositoryServiceImpl(Context mCtx) {
        this.mCtx = mCtx;
    }

    @Override
    public void logIn(String email, String idDevice, final MutableLiveData<ResponseString<String>> responseStringMutableLiveData) {
        if (!isNetworkAvailable(mCtx)) {
            ResponseString<String> stringResponseString = new ResponseString<>();
            stringResponseString.setError(mCtx.getString(R.string.check_internet_connection));
            stringResponseString.setResult(false);
            responseStringMutableLiveData.setValue(stringResponseString);
            return;
        }
        RetrofitHelper.getPhoneService().logIn(email, idDevice).enqueue(new Callback<ResponseString<String>>() {
            @Override
            public void onResponse(Call<ResponseString<String>> call, Response<ResponseString<String>> response) {
                responseStringMutableLiveData.postValue(response.body());
            }

            @Override
            public void onFailure(Call<ResponseString<String>> call, Throwable t) {
                Log.e("onFailure",t.getMessage(),t);
                ResponseString<String> stringResponseString = new ResponseString<>();
                stringResponseString.setError(mCtx.getString(R.string.fail_connect_to_server));
                stringResponseString.setResult(false);
                responseStringMutableLiveData.postValue(stringResponseString);
            }
        });
    }

    @Override
    public void getListOrganization(String token, final MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> mutableLiveDataResponseOrganization) {
        if (!isNetworkAvailable(mCtx)) {
            PhoneResponse<EmployeeOrganizationModel> phoneResponse = new PhoneResponse<>();
            phoneResponse.setError(mCtx.getString(R.string.check_internet_connection));
            phoneResponse.setResult(false);
            mutableLiveDataResponseOrganization.setValue(phoneResponse);
            return;
        }
        RetrofitHelper.getPhoneService().getListOrganizations(token).enqueue(new Callback<PhoneResponse<EmployeeOrganizationModel>>() {
            @Override
            public void onResponse(Call<PhoneResponse<EmployeeOrganizationModel>> call, Response<PhoneResponse<EmployeeOrganizationModel>> response) {
                PhoneResponse<EmployeeOrganizationModel> phoneResponse = response.body();
                List<EmployeeOrganizationModel> list = new ArrayList<>();
                for (EmployeeOrganizationModel organizationModel : phoneResponse.getBody()) {
                    if (organizationModel.getIsDelete()){
                        continue;
                    }
                    list.add(organizationModel);
                }
                phoneResponse.setBody(list);
                mutableLiveDataResponseOrganization.postValue(phoneResponse);
            }

            @Override
            public void onFailure(Call<PhoneResponse<EmployeeOrganizationModel>> call, Throwable t) {
                Log.e("onFailure",t.getMessage(),t);
                PhoneResponse<EmployeeOrganizationModel> phoneResponse = new PhoneResponse<>();
                phoneResponse.setError(mCtx.getString(R.string.fail_connect_to_server));
                phoneResponse.setResult(false);
                mutableLiveDataResponseOrganization.postValue(phoneResponse);
            }
        });
    }

    @Override
    public void getAllPhonesLastUpdate(String token, final MutableLiveData<PhoneResponse<EmployeePhoneModel>> mutableLiveData) {

        if (!isNetworkAvailable(mCtx)) {
            PhoneResponse<EmployeePhoneModel> phoneResponse = new PhoneResponse<>();
            phoneResponse.setError(mCtx.getString(R.string.check_internet_connection));
            phoneResponse.setResult(false);
            mutableLiveData.setValue(phoneResponse);
            return;
        }
        RetrofitHelper.getPhoneService().getAllPhonesLastUpdate(token).enqueue(new Callback<PhoneResponse<EmployeePhoneModel>>() {
            @Override
            public void onResponse(Call<PhoneResponse<EmployeePhoneModel>> call, Response<PhoneResponse<EmployeePhoneModel>> response) {
                mutableLiveData.postValue(response.body());
            }

            @Override
            public void onFailure(Call<PhoneResponse<EmployeePhoneModel>> call, Throwable t) {
                Log.e("onFailure",t.getMessage(),t);
                PhoneResponse<EmployeePhoneModel> phoneResponse = new PhoneResponse<>();
                phoneResponse.setError(mCtx.getString(R.string.fail_connect_to_server));
                phoneResponse.setResult(false);
                mutableLiveData.postValue(phoneResponse);
            }
        });
    }

    private boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
