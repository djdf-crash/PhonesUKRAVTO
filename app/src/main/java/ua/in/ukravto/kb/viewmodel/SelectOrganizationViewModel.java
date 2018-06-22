package ua.in.ukravto.kb.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.utils.Pref;

public class SelectOrganizationViewModel extends AndroidViewModel {

    private RepositoryService mRepository;
    private MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> responseListOrganizationsLiveData = new MutableLiveData<>();
    private List<EmployeeOrganizationModel> listSavedOrganization;
    private Context ctx;
    private Gson mGson;

    public SelectOrganizationViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = new RepositoryServiceImpl(application);
        this.ctx = application;
        mGson = new Gson();
        String organizationsString = Pref.getInstance(ctx).getString(Pref.SAVED_ORGANIZATIONS,"");
        Type type = new TypeToken<List<EmployeeOrganizationModel>>(){}.getType();
        listSavedOrganization = mGson.fromJson(organizationsString, type);
        if (listSavedOrganization == null){
            listSavedOrganization = new ArrayList<>();
        }
    }

    public MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> getListOrganizations(String token){
        mRepository.getListOrganization(token, responseListOrganizationsLiveData);
        return responseListOrganizationsLiveData;
    }

    public void selectOrganization(List<EmployeeOrganizationModel> listOrganization, boolean flag) {
        for (EmployeeOrganizationModel organizationModel : listOrganization) {
            organizationModel.setIsChecked(flag);
        }
    }

    public void saveOrganizationForSync(List<EmployeeOrganizationModel> listOrganization) {
        List<EmployeeOrganizationModel> listSave = new ArrayList<>();
        for (EmployeeOrganizationModel organizationModel : listOrganization) {
            if (!organizationModel.getIsChecked()){
                continue;
            }
            listSave.add(organizationModel);
        }
        mGson = new Gson();
        String organizations = mGson.toJson(listSave);
        Pref.getInstance(ctx).edit().putString(Pref.SAVED_ORGANIZATIONS, organizations).apply();
        if (listOrganization.size() == listSave.size()){
            Pref.getInstance(ctx).edit().putBoolean(Pref.SYNC_ALL_ORGANIZATION, true).apply();
        }else {
            Pref.getInstance(ctx).edit().putBoolean(Pref.SYNC_ALL_ORGANIZATION, false).apply();
        }
    }

    public void checkListOrganization(List<EmployeeOrganizationModel> listOrganization) {
        if (listSavedOrganization.size() > 0) {
            for (EmployeeOrganizationModel organizationModel : listOrganization) {
                for (EmployeeOrganizationModel savedOrganizationModel : listSavedOrganization) {
                    if (organizationModel.getID().equals(savedOrganizationModel.getID())) {
                        organizationModel.setIsChecked(true);
                        break;
                    }
                }
            }
        }
    }
}
