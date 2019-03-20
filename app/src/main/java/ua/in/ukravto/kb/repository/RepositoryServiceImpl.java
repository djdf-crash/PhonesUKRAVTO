package ua.in.ukravto.kb.repository;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.repository.database.DatabaseHelper;
import ua.in.ukravto.kb.repository.database.OrganizationDao;
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

                    organizationModel.setName(organizationModel.getName().toUpperCase());

                    list.add(organizationModel);
                }
                Collections.sort(list, new Comparator<EmployeeOrganizationModel>() {
                    @Override
                    public int compare(EmployeeOrganizationModel e1, EmployeeOrganizationModel e2) {
                        return e1.getName().compareToIgnoreCase(e2.getName());
                    }
                });
                phoneResponse.setBody(addOrganizationInDataBase(list));
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

    private List<EmployeeOrganizationModel> addOrganizationInDataBase(List<EmployeeOrganizationModel> organizationModelList) {

        List<EmployeeOrganizationModel> tmpList = new ArrayList<>();

        OrganizationDao dao = DatabaseHelper.getInstanseDB(mCtx).organizationDao();

        for (EmployeeOrganizationModel apiModel : organizationModelList) {

            EmployeeOrganizationModel dataBaseModel = dao.getById(apiModel.getID());

            if (dataBaseModel == null) {
                if (!apiModel.getIsDelete()) {
                    dao.addOrganization(apiModel);
                    tmpList.add(apiModel);
                }
                continue;
            } else if (!dataBaseModel.getDeleteBase() && !apiModel.getIsDelete()){
                apiModel.setIsChecked(dataBaseModel.getIsChecked());
                tmpList.add(apiModel);
                continue;
            }

            if (apiModel.getIsDelete()) {
                if (dataBaseModel.getIsChecked()) {
                    dataBaseModel.setIsDelete(true);
                    dataBaseModel.setDeleteBase(true);
                    dataBaseModel.setIsChecked(false);
                    dao.updateOrganization(dataBaseModel);
                } else if (!dataBaseModel.getDeleteBase()){
                    dao.deleteOrganization(dataBaseModel);
                }
            }
        }
        return tmpList;
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

    @Override
    @Nullable
    public ResponseString<String> getIsLastUpdateAPPExecute(String token, String currentVersionName) {
        try {
            Response<ResponseString<String>> response = RetrofitHelper.getPhoneService().getIsLastUpdateAPP(token, currentVersionName).execute();
            if (response.isSuccessful()){
                return response.body();
            }
        } catch (IOException e) {
            Log.e(RetrofitHelper.class.getName(),e.getMessage(),e);
        }
        return null;
    }

    @Override
    public void getIsLastUpdateAPPEnqueue(String token, String currentVersionName, final MutableLiveData<ResponseString<String>> respLastUpdate) {
        if (!isNetworkAvailable(mCtx)) {
            ResponseString<String> stringResponseString = new ResponseString<>();
            stringResponseString.setError(mCtx.getString(R.string.check_internet_connection));
            stringResponseString.setResult(false);
            respLastUpdate.setValue(stringResponseString);
            return;
        }

        RetrofitHelper.getPhoneService().getIsLastUpdateAPP(token, currentVersionName).enqueue(new Callback<ResponseString<String>>() {
            @Override
            public void onResponse(Call<ResponseString<String>> call, Response<ResponseString<String>> response) {
                respLastUpdate.postValue(response.body());
            }

            @Override
            public void onFailure(Call<ResponseString<String>> call, Throwable t) {
                Log.e("onFailure",t.getMessage(),t);
                ResponseString<String> stringResponseString = new ResponseString<>();
                stringResponseString.setError(mCtx.getString(R.string.fail_connect_to_server));
                stringResponseString.setResult(false);
                respLastUpdate.postValue(stringResponseString);
            }
        });


    }

    @Override
    public void tokenIsExist(String token, final MutableLiveData<ResponseString<String>> existTokenLiveData) {
        if (!isNetworkAvailable(mCtx)) {
            ResponseString<String> stringResponseString = new ResponseString<>();
            stringResponseString.setError(mCtx.getString(R.string.check_internet_connection));
            stringResponseString.setResult(true);
            existTokenLiveData.setValue(stringResponseString);
            return;
        }
        RetrofitHelper.getPhoneService().tokenIsExist(token).enqueue(new Callback<ResponseString<String>>() {
            @Override
            public void onResponse(Call<ResponseString<String>> call, Response<ResponseString<String>> response) {
                existTokenLiveData.postValue(response.body());
            }

            @Override
            public void onFailure(Call<ResponseString<String>> call, Throwable t) {
                Log.e("onFailure",t.getMessage(),t);
                ResponseString<String> stringResponseString = new ResponseString<>();
                stringResponseString.setError(mCtx.getString(R.string.fail_connect_to_server));
                stringResponseString.setResult(false);
                existTokenLiveData.postValue(stringResponseString);
            }
        });
    }

    private boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
