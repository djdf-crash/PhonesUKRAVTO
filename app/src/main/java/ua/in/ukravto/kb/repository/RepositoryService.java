package ua.in.ukravto.kb.repository;

import android.arch.lifecycle.MutableLiveData;

import retrofit2.Response;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.database.model.ResponseString;

public interface RepositoryService {

    void logIn(String email, String idDevice, MutableLiveData<ResponseString<String>> responseStringMutableLiveData);
    void getListOrganization(String token, MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> mutableLiveDataResponseOrganization);
    void getAllPhonesLastUpdate(String token, MutableLiveData<PhoneResponse<EmployeePhoneModel>> mutableLiveData);
    ResponseString<String> getIsLastUpdateAPPExecute(String token, String currentVersionName);
    void getIsLastUpdateAPPEnqueue(String token, String currentVersionName, MutableLiveData<ResponseString<String>> respLastUpdate);
}
