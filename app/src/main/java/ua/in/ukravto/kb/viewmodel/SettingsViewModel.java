package ua.in.ukravto.kb.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import ua.in.ukravto.kb.BuildConfig;
import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.utils.Pref;

public class SettingsViewModel extends AndroidViewModel {

    private MutableLiveData<ResponseString<String>> respLastUpdate = new MutableLiveData<>();
    private RepositoryService repositoryService;
    private String mToken;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        repositoryService = new RepositoryServiceImpl(application);
        mToken = Pref.getInstance(application).getString(Pref.USER_TOKEN,"");
    }

    public MutableLiveData<ResponseString<String>> getIsLastUpdateAPPLiveData(){
        return respLastUpdate;
    }

    public void getLastUpdate(){
        repositoryService.getIsLastUpdateAPPEnqueue(mToken, BuildConfig.VERSION_NAME, respLastUpdate);
    }
}
