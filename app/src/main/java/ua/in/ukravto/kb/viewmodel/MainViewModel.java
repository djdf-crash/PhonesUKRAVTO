package ua.in.ukravto.kb.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.database.model.ResponseString;

public class MainViewModel extends AndroidViewModel {

    private RepositoryService mRepository;
    private MutableLiveData<ResponseString<String>> responseLoginLiveData = new MutableLiveData<>();
    private MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> responseListOrganizationsLiveData = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        mRepository = new RepositoryServiceImpl(application);
    }

    public void logIn(String email, String devID){
        mRepository.logIn(email, devID, responseLoginLiveData);
    }

    public void getListOrganizations(String token){
        mRepository.getListOrganization(token, responseListOrganizationsLiveData);
    }

    public MutableLiveData<ResponseString<String>> getResponseLoginLiveData() {
        return responseLoginLiveData;
    }

}
