package ua.in.ukravto.kb.repository.database.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmployeeOrganizationModel {
    @SerializedName("ID")
    @Expose
    private Integer iD;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("IsDelete")
    @Expose
    private Boolean isDelete;

    private Boolean isChecked = false;

    public Integer getID() {
        return iD;
    }

    public void setID(Integer iD) {
        this.iD = iD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Boolean isDelete) {
        this.isDelete = isDelete;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)return true;
        EmployeeOrganizationModel model = (EmployeeOrganizationModel)obj;
        return model.getID() == iD && model.getName() == name;
    }
}
