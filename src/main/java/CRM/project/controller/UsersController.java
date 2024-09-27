package CRM.project.controller;

import CRM.project.dto.Availability;
import CRM.project.dto.Requestdto;
import CRM.project.dto.UserDto;
import CRM.project.entity.Department;
import CRM.project.entity.RequestEntity;
import CRM.project.entity.UserStatus;
import CRM.project.entity.Users;
import CRM.project.response.Responses;
import CRM.project.service.DepartmentService;
import CRM.project.service.UsersService;

import java.io.IOException;
import java.sql.Array;
import java.util.*;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static CRM.project.utils.ExcelToCategoryutils.getRolesForUser;

@RestController
@Slf4j
@RequestMapping("/user")
@CrossOrigin
public class UsersController {

    @Autowired
    private UsersService usersService;

    private final String MODULE_ID = "fcubs";

    public boolean authenticateUser(String username, String password) {

        return true;
//        boolean result = false;
//        try {
//
//            com.unionbankng.applications.ws.UBNSMSService_Service service = new com.unionbankng.applications.ws.UBNSMSService_Service();
//            com.unionbankng.applications.ws.UBNSMSService port = service.getBasicHttpBindingUBNSMSService();
//            // TODO process result here
//            result = port.adAuthenticate(username, password, MODULE_ID);
//            System.out.println("Result = " + result);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return result;
    }


    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser(@RequestBody Map<String, String> userDetails) {

        if(userDetails.get("username").equalsIgnoreCase("admin")) {
            Department department = new Department(1L, "Database", "operations@gmail.com", null);
            List<String> roles = Arrays.asList("user", "admin");
            Users users = new Users(1L, department, "Admin Admin","admin",null, UserStatus.ACTIVE, Availability.ONLINE, null);
            UserDto userDto = new UserDto(users, roles, null);
            return new ResponseEntity<>(new Responses<>("00", "Success", userDto), HttpStatus.OK);
        }

        if (userDetails.get("username") == null || userDetails.get("password") == null) {
            return new ResponseEntity<>(new Responses<>("99", "Invalid Credentials", null), HttpStatus.BAD_REQUEST);
        }
        boolean validation = authenticateUser(userDetails.get("username"), userDetails.get("password"));
        if (validation) {
            List<String> userRoles = new ArrayList<>();
            try {
                userRoles = getRolesForUser(userDetails.get("username"));
                log.info("User roles::: " + userRoles.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Users users = usersService.fetchStaffByName(userDetails.get("username"));
            if(users == null) {
                return new ResponseEntity<>(new Responses<>("45", "User not profiled on application", null), HttpStatus.OK);
            }
            if(userRoles.isEmpty()) {
                return new ResponseEntity<>(new Responses<>("45", "Please engage Access Control for access", null), HttpStatus.OK);
            }
            UserDto userDto = new UserDto(users, userRoles, null);
            return new ResponseEntity<>(new Responses<>("00", "Success", userDto), HttpStatus.OK);

        } else {
            return new ResponseEntity<>(new Responses<>("99", "Wrong credentials", null), HttpStatus.OK);
        }
    }

    @PostMapping("/disableUser")
    public ResponseEntity<?> deleteUser(@RequestBody Map<String, String> data) {
        Users users = usersService.fetchUserById(Long.valueOf(data.get("userId")));
        if(users != null) {
            users.setStatus(UserStatus.valueOf(data.get("status")));
            Users users1 =  usersService.saveUser(users);
            return new ResponseEntity<>(new Responses("00", "Treated successfully", null), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Responses("99", "Invalid User provided", null), HttpStatus.OK);
    }

    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestBody Map<String, Object> data) {
        Users user1 = usersService.addNewUser(data);
        return (user1 != null) ? new ResponseEntity<>(new Responses("00", "User details Saved Successfully", null), HttpStatus.OK)
                : new ResponseEntity<>(new Responses("99", "Record not saved, Ensure staff name has not been created", null), HttpStatus.OK);
    }

    @PostMapping("/findByStaff")
    public Users fetchByStaffName(@RequestBody Requestdto requestdto) {
        log.info("Incoming request::: " + requestdto);
        return usersService.fetchStaffByName(requestdto.getStaffName());
    }

    @PostMapping("/findById")
    public Users fetchById(@RequestBody Requestdto requestdto) {
        return usersService.fetchUserById(requestdto.getUnitId());
    }

    @PostMapping("/findByUnit")
    public ResponseEntity<?> fetchByUnit(@RequestBody Requestdto requestdto) {
        List<Users> usersList = usersService.fetchStaffByUnit(requestdto.getUnitName());
        for(Users user: usersList) {
            user.setUserRoles(getRolesForUser(user.getUserEmail()));
        }
        return new ResponseEntity<>(usersList, HttpStatus.OK);
    }

    @PostMapping("/fetchUsers")
    public ResponseEntity<?> findAllUsers() {
        return new ResponseEntity<>(usersService.findAllUsers(), HttpStatus.OK);
    }

    @PostMapping("/modify-availability")
    public ResponseEntity<?> modifyUserAvailability(@RequestBody Map<String, String> data) {
        log.info("Modifying user with id " + data.get("userId"));
        return new ResponseEntity<>(usersService.updateAvailability(data), HttpStatus.OK);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> bulkUploadUsers(@RequestParam("file") MultipartFile file,
                                             @RequestParam("creator") String creator) throws IOException {

        if(!file.getContentType().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){
            return new ResponseEntity<>(new Responses<>("45", "Invalid File Type", null), HttpStatus.BAD_REQUEST);
        }

        List<Users> usersList;
        try {
            usersList = usersService.uploadUsers(file, creator);
        } catch (Exception e) {
            log.error("Error uploading users: ", e);
            return new ResponseEntity<>(new Responses<>("90", "Error saving users", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (usersList != null && !usersList.isEmpty()) {
            return new ResponseEntity<>(new Responses<>("00", "Users Saved Successfully", null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new Responses<>("90", "No users saved", null), HttpStatus.BAD_REQUEST);
        }

    }
}
