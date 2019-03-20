package ua.in.ukravto.kb.repository.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;

@Dao
public interface OrganizationDao {

    @Query("SELECT * FROM organizations")
    List<EmployeeOrganizationModel> getAll();

    @Query("SELECT * FROM organizations WHERE organizations.is_delete = :isDelete")
    List<EmployeeOrganizationModel> getAllByDelete(Boolean isDelete);

    @Query("SELECT * FROM organizations WHERE organizations.is_delete_api = :isDelete")
    List<EmployeeOrganizationModel> getAllByDeleteAPI(Boolean isDelete);

    @Query("SELECT * FROM organizations WHERE organizations.is_checked = :check")
    List<EmployeeOrganizationModel> getAllByChecked(Boolean check);

    @Query("SELECT * FROM organizations WHERE organizations.id = :id")
    EmployeeOrganizationModel getById(int id);

    @Update
    void updateOrganizations(List<EmployeeOrganizationModel> organization);

    @Update
    void updateOrganization(EmployeeOrganizationModel organization);

    @Delete
    void deleteOrganization(EmployeeOrganizationModel... organization);

    @Insert
    void addOrganization(EmployeeOrganizationModel... organization);
}
