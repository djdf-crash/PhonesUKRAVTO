package ua.in.ukravto.kb.repository.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.database.model.ResponseString;

public interface PhoneService {

    @GET("phones/all")
    Call<PhoneResponse<EmployeePhoneModel>> listEmployeePhones(String token);

    @GET("phones/lastupdate")
    Call<PhoneResponse<EmployeePhoneModel>> listEmployeePhonesLastUpdate(String token);

//    @GET("phones/lastupdate")
//    Call<Response<EmployeePhoneModel>> listEmployeePhonesLastUpdate(Query("idorganization") String organizationID);

    @POST("user/token")
    Call<ResponseString<String>> logIn(@Query("email") String email, @Query("deviceid") String idDevice);
}
