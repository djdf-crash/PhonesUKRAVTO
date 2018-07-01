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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.in.ukravto.kb.adapters.ListOrganizationRecyclerAdapter;
import ua.in.ukravto.kb.repository.RepositoryService;
import ua.in.ukravto.kb.repository.RepositoryServiceImpl;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.utils.Pref;

public class SelectOrganizationViewModel extends AndroidViewModel {

    private RepositoryService mRepository;
    private MutableLiveData<PhoneResponse<EmployeeOrganizationModel>> responseListOrganizationsLiveData = new MutableLiveData<>();
    private MutableLiveData<ListOrganizationRecyclerAdapter> adapterMutableLiveData = new MutableLiveData<>();
    private List<EmployeeOrganizationModel> oldListSavedOrganization;
    private List<EmployeeOrganizationModel> oldListDelOrganization;
    private Context ctx;
    private Gson mGson;

    public SelectOrganizationViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = new RepositoryServiceImpl(application);
        this.ctx = application;
        mGson = new Gson();
        String organizationsString = Pref.getInstance(ctx).getString(Pref.SAVED_ORGANIZATIONS,"");
        String delOrganizationsString = Pref.getInstance(ctx).getString(Pref.DELETE_ORGANIZATIONS,"");
        Type type = new TypeToken<List<EmployeeOrganizationModel>>(){}.getType();
        oldListSavedOrganization = mGson.fromJson(organizationsString, type);
        oldListDelOrganization = mGson.fromJson(delOrganizationsString, type);
        if (oldListSavedOrganization == null){
            oldListSavedOrganization = new ArrayList<>();
        }
        if (oldListDelOrganization == null){
            oldListDelOrganization = new ArrayList<>();
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

    public List<EmployeeOrganizationModel> saveOrganizationForSync(List<EmployeeOrganizationModel> listOrganization) {

        List<EmployeeOrganizationModel> newListSavedOrganization = new ArrayList<>();

        for (EmployeeOrganizationModel organizationModel : listOrganization) {
            if (!organizationModel.getIsChecked()){
                continue;
            }
            newListSavedOrganization.add(organizationModel);
        }

        HashMap<Integer, EmployeeOrganizationModel> hashMapOldListSaved = buildOrganizationMap(oldListSavedOrganization);
        for (EmployeeOrganizationModel model : newListSavedOrganization) {
            if (hashMapOldListSaved.get(model.getID()) == null){
                continue;
            }
            hashMapOldListSaved.remove(model.getID());
        }

        oldListDelOrganization.addAll(hashMapOldListSaved.values());

        HashMap<Integer, EmployeeOrganizationModel> hashMapOldListDel = buildOrganizationMap(oldListDelOrganization);
        for (EmployeeOrganizationModel model : newListSavedOrganization) {
            if (hashMapOldListDel.get(model.getID()) == null){
                continue;
            }
            hashMapOldListDel.remove(model.getID());
        }

        Set<EmployeeOrganizationModel> newSetDeleteOrganization = new HashSet<>(hashMapOldListDel.values());
        String savedOrganizations = mGson.toJson(newListSavedOrganization);
        String deleteOrganizations = mGson.toJson(newSetDeleteOrganization);

        Pref.getInstance(ctx).edit().putString(Pref.SAVED_ORGANIZATIONS, savedOrganizations).apply();
        Pref.getInstance(ctx).edit().putString(Pref.DELETE_ORGANIZATIONS, deleteOrganizations).apply();

        if (listOrganization.size() == newListSavedOrganization.size()){
            Pref.getInstance(ctx).edit().putBoolean(Pref.SYNC_ALL_ORGANIZATION, true).apply();
        }else {
            Pref.getInstance(ctx).edit().putBoolean(Pref.SYNC_ALL_ORGANIZATION, false).apply();
        }
        return newListSavedOrganization;
    }

    private HashMap<Integer, EmployeeOrganizationModel> buildOrganizationMap(List<EmployeeOrganizationModel> orgList) {
        HashMap<Integer, EmployeeOrganizationModel> resultMap = new HashMap<>();
        for (EmployeeOrganizationModel model : orgList) {
            resultMap.put(model.getID(), model);
        }
        return resultMap;
    }

    public void checkListOrganization(List<EmployeeOrganizationModel> listOrganization) {
        if (oldListSavedOrganization.size() > 0) {
            for (EmployeeOrganizationModel organizationModel : listOrganization) {
                for (EmployeeOrganizationModel savedOrganizationModel : oldListSavedOrganization) {
                    if (organizationModel.getID().equals(savedOrganizationModel.getID())) {
                        organizationModel.setIsChecked(true);
                        break;
                    }
                }
            }
        }
    }
}
