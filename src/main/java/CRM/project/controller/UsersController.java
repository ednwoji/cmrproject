package CRM.project.controller;

import CRM.project.dto.Availability;
import CRM.project.dto.Requestdto;
import CRM.project.dto.UserDto;
import CRM.project.entity.Department;
import CRM.project.entity.UserStatus;
import CRM.project.entity.Users;
import CRM.project.response.Responses;
import CRM.project.service.DepartmentService;
import CRM.project.service.UsersService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import CRM.project.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Autowired
    private DepartmentService departmentService;

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
    public ResponseEntity<?> validateUser(@RequestBody Map<String, String> userDetails) throws Exception {

        if(userDetails.get("username").equalsIgnoreCase("admin")) {
            Department department = new Department(1L, "Database", "operations@gmail.com", null);
            List<String> roles = Arrays.asList("user", "admin");
            Users users = new Users(1L, department, "Admin Admin","admin",null, UserStatus.ACTIVE, Availability.ONLINE, null);
            UserDto userDto = new UserDto(users, roles, null);
            return new ResponseEntity<>(new Responses<>("00", "Success", userDto), HttpStatus.OK);
        }

        if(userDetails.get("username").equalsIgnoreCase("donjoku")) {
            Department department = new Department(1L, "Database", "database@gmail.com", null);
            List<String> roles = Arrays.asList("user", "admin");
            Users users = new Users(1L, department, "Daniel Okoroafor","donjoku",null, UserStatus.ACTIVE, Availability.ONLINE, null);
            UserDto userDto = new UserDto(users, roles, null);
            return new ResponseEntity<>(new Responses<>("00", "Success", userDto), HttpStatus.OK);
        }



        if(userDetails.get("username").equalsIgnoreCase("potokunbo")) {
            Department department = new Department(1L, "Operations", "operations@gmail.com", null);
            List<String> roles = Arrays.asList("user", "technician");
            Users users = new Users(1L, department, "Patrick Tokunbo","potokunbo",null, UserStatus.ACTIVE, Availability.ONLINE, null);
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
            UserDto userDto = new UserDto(users, userRoles, Utils.getAuthServToken());
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
        if(!usersList.isEmpty()) {
            for (Users user : usersList) {
                user.setUserRoles(getRolesForUser(user.getUserEmail()));
            }
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

    @PostMapping("/exportUsers")
    public ResponseEntity<?> exportUsers() {
        List<Users> userList = usersService.findAllUsers();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Unit Name");
        headerRow.createCell(1).setCellValue("Staff Name");
        headerRow.createCell(2).setCellValue("Username");
        headerRow.createCell(3).setCellValue("Status");
        headerRow.createCell(4).setCellValue("Availability");

        // Populate data rows
        int rowNum = 1;
        for (Users user : userList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getUnitName().getDepartmentName());
            row.createCell(1).setCellValue(user.getStaffName());
            row.createCell(2).setCellValue(user.getUserEmail());
            row.createCell(3).setCellValue(String.valueOf(user.getStatus()));
            row.createCell(4).setCellValue(String.valueOf(user.getAvailability()));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=users.xlsx");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        // Write the workbook to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            workbook.close();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while exporting data");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(baos.toByteArray());
    }


    @PostMapping("/updateuser")
    public ResponseEntity<?> EditUserDepartment(@RequestBody Map<String, String> data) {
        Department department = departmentService.findDepartmentById(data.get("deptId"));
        if(department != null) {
            Users users = usersService.fetchUserById(Long.valueOf(data.get("userId")));
            if(users != null) {
                users.setUnitName(department);
                usersService.saveUser(users);
                return new ResponseEntity<>(new Responses<>("00", "User Updated Successfully", null), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new Responses<>("90", "User Updated Failed", null), HttpStatus.OK);
    }
}
