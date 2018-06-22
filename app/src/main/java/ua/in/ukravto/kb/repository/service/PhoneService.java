package ua.in.ukravto.kb.repository.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.database.model.ResponseString;

public interface PhoneService {

    @GET("phones/all")
    Call<PhoneResponse<EmployeePhoneModel>> listEmployeePhones(String token);

    @GET("organization/all")
    Call<PhoneResponse<EmployeeOrganizationModel>> getListOrganizations(@Header("token") String token);

    @POST("user/token")
    Call<ResponseString<String>> logIn(@Query("email") String email, @Query("deviceid") String idDevice);
}
