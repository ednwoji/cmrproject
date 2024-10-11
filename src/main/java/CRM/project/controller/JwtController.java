package CRM.project.controller;


import CRM.project.dto.UserDto;
import CRM.project.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/jwt")
@Slf4j
public class JwtController {



    @PostMapping("/login")
    public ResponseEntity<?> getToken() throws Exception {
        log.info("Fetching token:::::");
        return new ResponseEntity<>(new UserDto(null, null, Utils.getAuthServToken()), HttpStatus.OK);
    }
}
