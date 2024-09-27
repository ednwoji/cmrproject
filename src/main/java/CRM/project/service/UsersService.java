package CRM.project.service;

import CRM.project.entity.Department;
import CRM.project.entity.RequestEntity;
import CRM.project.entity.Users;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UsersService {

    Users addNewUser(Map<String,Object> data);

    Users fetchStaffByName(String staffName);

    Users fetchUserById(Long id);

    List<Users> fetchStaffByUnit(String unit);


    List<Users> findAllUsers();

    Object updateAvailability(Map<String, String> data);

    Users saveUser(Users users);

    List<Users> uploadUsers(MultipartFile file, String createdBy) throws IOException;
    public Users fetchStaffByFullName(String staffName);
}
