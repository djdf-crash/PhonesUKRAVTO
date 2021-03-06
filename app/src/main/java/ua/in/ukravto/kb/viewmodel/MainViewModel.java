package ua.in.ukravto.kb.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;
import ua.in.ukravto.kb.repository.database.model.ResponseString;

public class MainViewModel extends AndroidViewModel {

    private RepositoryService mRepository;
    private MutableLiveData<ResponseString<String>> responseLoginLiveData = new MutableLiveData<>();
    private MutableLiveData<ResponseString<String>> existTokenLiveData = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        mRepository = new RepositoryServiceImpl(application);
    }

    public void logIn(String email, String devID){
        mRepository.logIn(email, devID, responseLoginLiveData);
    }

    public MutableLiveData<ResponseString<String>> getResponseLoginLiveData() {
        return responseLoginLiveData;
    }

    public MutableLiveData<ResponseString<String>> tokenIsExist(String token) {
        mRepository.tokenIsExist(token, existTokenLiveData);
        return existTokenLiveData;
    }
}
