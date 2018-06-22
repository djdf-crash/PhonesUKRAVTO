package ua.in.ukravto.kb.repository.database.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PhoneResponse<T> {

    @SerializedName("result")
    @Expose
    private Boolean result;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("body")
    @Expose
    private List<T> body = new ArrayList<>();

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<T> getBody() {
        return body;
    }

    public void setBody(List<T> organization) {
        this.body = organization;
    }

}
