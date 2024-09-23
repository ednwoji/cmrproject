package CRM.project.controller;

import CRM.project.dto.Requestdto;
import CRM.project.entity.Department;
import CRM.project.entity.RequestEntity;
import CRM.project.entity.Users;
import CRM.project.response.Responses;
import CRM.project.service.DepartmentService;
import CRM.project.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
@CrossOrigin
public class UsersController {
    @Autowired
    private UsersService usersService;

    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestBody Map<String, Object> data)
    {
        Users user1=usersService.addNewUser(data);
        return (user1!=null) ? new ResponseEntity<>(new Responses("00", "User details Saved Successfully", null), HttpStatus.OK)
                : new ResponseEntity<>(new Responses("99", "Record not saved, Ensure staff name has not been created", null), HttpStatus.OK);
    }
    @PostMapping("/findByStaff")
    public Users fetchByStaffName(@RequestBody Requestdto requestdto)
    {
        log.info("Incoming request::: "+requestdto);
        return usersService.fetchStaffByName(requestdto.getStaffName());
    }
    @PostMapping("/findById")
    public Users fetchById(@RequestBody Requestdto requestdto)
    {
        return usersService.fetchUserById(requestdto.getUnitId());
    }
    @PostMapping("/findByUnit")
    public ResponseEntity<?> fetchByUnit(@RequestBody Requestdto requestdto)
    {
        List<Users> unit=usersService.fetchStaffByUnit(requestdto.getUnitName());
        return new ResponseEntity<>(unit, HttpStatus.OK);
    }


    @PostMapping("/fetchUsers")
    public ResponseEntity<?> findAllUsers() {
        return new ResponseEntity<>(usersService.findAllUsers(), HttpStatus.OK);
    }


    @PostMapping("/modify-availability")
    public ResponseEntity<?> modifyUserAvailability(@RequestBody Map<String, String> data) {
        log.info("Modifying user with id "+data.get("userId"));
        return new ResponseEntity<>(usersService.updateAvailability(data), HttpStatus.OK);
    }
}
