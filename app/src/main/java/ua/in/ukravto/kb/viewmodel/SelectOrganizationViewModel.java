package ua.in.ukravto.kb.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;
import ua.in.ukravto.kb.repository.database.AppDatabase;
import ua.in.ukravto.kb.repository.database.DatabaseHelper;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;

public class SelectOrganizationViewModel extends AndroidViewModel {

    private RepositoryService mRepository;
    private MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> responseListOrganizationsLiveData = new MutableLiveData<>();
    private AppDatabase appDatabase;

    public SelectOrganizationViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = new RepositoryServiceImpl(application);
        this.appDatabase = DatabaseHelper.getInstanseDB(application.getApplicationContext());
    }

    public MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> getListOrganizations(String token){
        mRepository.getListOrganization(token, responseListOrganizationsLiveData);
        return responseListOrganizationsLiveData;
    }

    public void selectOrganization(List<EmployeeOrganizationModel> listOrganization, boolean flag) {
        for (EmployeeOrganizationModel organizationModel : listOrganization) {
            if (organizationModel.getIsChecked() != flag) {
                organizationModel.setIsChecked(flag);
                organizationModel.setDeleteBase(!flag);
            }
        }
        appDatabase.organizationDao().updateOrganizations(listOrganization);
    }

}
