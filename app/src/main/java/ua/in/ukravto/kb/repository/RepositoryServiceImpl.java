package ua.in.ukravto.kb.repository;

import android.content.Context;

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
        RetrofitHelper.getPhoneService().logIn(email, idDevice);
    }
}
