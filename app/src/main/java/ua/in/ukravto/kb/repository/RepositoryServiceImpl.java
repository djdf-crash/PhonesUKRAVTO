package ua.in.ukravto.kb.repository;

import android.content.Context;

import retrofit2.Call;
import retrofit2.Callback;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.Response;
import ua.in.ukravto.kb.repository.database.model.ResponseString;
import ua.in.ukravto.kb.repository.service.RetrofitHelper;
import ua.in.ukravto.kb.utils.Constants;
import ua.in.ukravto.kb.utils.Hmac;

public class RepositoryServiceImpl implements RepositoryService {

    private Context mCtx;

    public RepositoryServiceImpl(Context mCtx) {
        this.mCtx = mCtx;
    }

    @Override
    public void logIn(String email, String idDevice) {
        RetrofitHelper.getPhoneService().logIn(email, idDevice).enqueue(new Callback<ResponseString<String>>() {
            @Override
            public void onResponse(Call<ResponseString<String>> call, retrofit2.Response<ResponseString<String>> response) {
                boolean result = response.body().getResult();
                if (result){

                }
            }

            @Override
            public void onFailure(Call<ResponseString<String>> call, Throwable t) {

            }
        });
    }
}
