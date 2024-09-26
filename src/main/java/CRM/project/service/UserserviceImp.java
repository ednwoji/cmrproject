package CRM.project.service;

import CRM.project.dto.Availability;
import CRM.project.dto.Roles;
import CRM.project.entity.Department;
import CRM.project.entity.RequestEntity;
import CRM.project.entity.UserStatus;
import CRM.project.entity.Users;
import CRM.project.repository.DepartmentRepository;
import CRM.project.repository.UsersRepository;
import CRM.project.response.Responses;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UserserviceImp implements UsersService {
    @Autowired
    private UsersRepository usersRepository;


    @Autowired
    private DepartmentRepository departmentRepository;
    @Override
    public Users addNewUser(Map<String,Object> data) {
        Users users1=usersRepository.findByStaffName(String.valueOf(data.get("technician"))).orElse(null);
        Department department1=departmentRepository.findByDepartmentName(String.valueOf(data.get("unit")).split("~~")[0]).orElse(null);

        if(department1 != null && users1 == null) {
            Users users = new Users();
            users.setStaffName(String.valueOf(data.get("technician")));
            users.setUnitName(department1);
            users.setUserEmail(String.valueOf(data.get("email")));
            users.setStatus(UserStatus.ACTIVE);

            users.setAvailability(Availability.ONLINE);
            return usersRepository.save(users);
        }
        return null;
    }

    @Override
    public Users fetchStaffByName(String staffName) {
        Users user = usersRepository.findByUserEmail(staffName).orElse(null);
        return user;
    }

    @Override
    public Users fetchUserById(Long id) {
        return usersRepository.findByUserId(id).orElse(null);
    }

    @Override
    public List<Users> fetchStaffByUnit(String unitName) {

        Optional<Department> department = departmentRepository.findByDepartmentName(unitName);
        if(department.isPresent()) {
            return usersRepository.findByUnitName(department.get());
        }
        return null;
    }

    @Override
    public List<Users> findAllUsers() {
        return usersRepository.findAll();
    }

    @Override
    public Object updateAvailability(Map<String, String> data) {
        Users users = usersRepository.findByUserId(Long.valueOf(data.get("userId"))).orElse(null);
        if(users == null) {
            return new Responses("90", "Invalid User", null);
        }
        else {
            users.setAvailability(Availability.fromCode(data.get("availability")));
            Users users1 = usersRepository.save(users);
            return new Responses("00", "User updated successfully", users1);
        }
    }

    @Override
    public Users saveUser(Users users) {
        return usersRepository.save(users);
    }


}
