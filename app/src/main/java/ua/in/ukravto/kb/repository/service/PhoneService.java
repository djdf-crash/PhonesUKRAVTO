package ua.in.ukravto.kb.repository.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.repository.database.model.PhoneResponse;
import ua.in.ukravto.kb.repository.database.model.ResponseString;

public interface PhoneService {

    @GET("phone/all")
    Call<PhoneResponse<EmployeePhoneModel>> listEmployeePhones(String token);

    @GET("phone/lastupdate")
    Call<PhoneResponse<EmployeePhoneModel>> getAllPhonesLastUpdate(@Header("token") String token);

    @GET("phone/lastupdate")
    Call<PhoneResponse<EmployeePhoneModel>> getOrganizationIDPhonesLastUpdate(@Query("idorganization") int id, @Header("token") String token);

    @GET("phone/idorganization/{id}")
    Call<PhoneResponse<EmployeePhoneModel>> getPhonesOrganizationID(@Path("id") int id, @Header("token") String token);

    @GET("organization/all")
    Call<PhoneResponse<EmployeeOrganizationModel>> getListOrganizations(@Header("token") String token);

    @POST("user/token")
    Call<ResponseString<String>> logIn(@Query("email") String email, @Query("deviceid") String idDevice);

    @POST("user/update")
    Call<Void> updateUser(@Header("token") String token);

    @GET("apk/lastupdate")
    Call<ResponseString<String>> getIsLastUpdateAPP(@Header("token")String token, @Query("currentversionname")String currentVersionName);

    @GET("apk/download")
    @Headers("Content-Type:application/octet-stream")
    @Streaming
    Call<ResponseBody> getDownloadLastUpdateAPP(@Header("token")String token);
}
