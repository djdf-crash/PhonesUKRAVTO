package ua.in.ukravto.kb.repository;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.net.ConnectivityManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
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
            stringResponseString.setError("Check internet connection!");
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
                ResponseString<String> stringResponseString = new ResponseString<>();
                stringResponseString.setError(t.getMessage());
                stringResponseString.setResult(false);
                responseStringMutableLiveData.postValue(stringResponseString);
            }
        });
    }

    @Override
    public void getListOrganization(String token, final MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> mutableLiveDataResponseOrganization) {
        if (!isNetworkAvailable(mCtx)) {
            PhoneResponse<EmployeeOrganizationModel> phoneResponse = new PhoneResponse<>();
            phoneResponse.setError("Check internet connection!");
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
                PhoneResponse<EmployeeOrganizationModel> phoneResponse = new PhoneResponse<>();
                phoneResponse.setError(t.getMessage());
                phoneResponse.setResult(false);
                mutableLiveDataResponseOrganization.postValue(phoneResponse);
            }
        });
    }

    private boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
