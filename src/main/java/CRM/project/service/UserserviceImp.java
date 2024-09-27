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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public Users fetchStaffByFullName(String staffName) {
        return usersRepository.findByStaffName(staffName).orElse(null);
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

    @Override
    public List<Users> uploadUsers(MultipartFile file, String createdBy) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());

        try {
            Sheet sheet = workbook.getSheetAt(0);
            if(sheet.getPhysicalNumberOfRows() > 3001) {
                return null;
            }

            List<CompletableFuture<Users>> futures = IntStream.rangeClosed(1, sheet.getLastRowNum())
                    .parallel()
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            Users users = new Users();
                            Row row = sheet.getRow(i);
                            users.setAvailability(Availability.ONLINE);
                            users.setUserEmail(row.getCell(1).getStringCellValue());
                            users.setCreatedBy(createdBy);
                            users.setStatus(UserStatus.ACTIVE);
                            users.setStaffName(row.getCell(2).getStringCellValue());
                            users.setUnitName(departmentRepository.findByDepartmentName(row.getCell(3).getStringCellValue()).orElse(null));

                            return users;
                        }catch(Exception e) {
                            log.error("Error processing row "+i, e);
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());
            List<Users> usersList = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return usersRepository.saveAll(usersList);
        }catch (Exception e) {
            return null;
        }
    }


}
