package ua.in.ukravto.kb.repository.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.in.ukravto.kb.utils.Constants;

public class RetrofitHelper {

    private static Retrofit mInstance;

    public static Retrofit getInstance(){
        if (mInstance == null) {
            mInstance = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(Constants.SERVICE_API)
                    .build();
        }

        return mInstance;
    }

    public static PhoneService getPhoneService(){
        return mInstance.create(PhoneService.class);
    }

}
