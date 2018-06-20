package ua.in.ukravto.kb.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;

public class MainViewModel extends AndroidViewModel {

    private RepositoryService mRepository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mRepository = new RepositoryServiceImpl(application);
    }

    public void logIn(String email){
        mRepository.logIn(email);
    }

}
