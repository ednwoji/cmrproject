package CRM.project.controller;


import CRM.project.entity.Solutions;
import CRM.project.response.Responses;
//import CRM.project.service.NotificationService;
import CRM.project.service.SolutionsService;
import CRM.project.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/solutions")
@CrossOrigin
//@CrossOrigin(origins = {"http://localhost:3001"}, allowCredentials = "false")
@Slf4j
public class SolutionsController {


    @Autowired
    private SolutionsService solutionsService;

//    @Autowired
//    private NotificationService notificationService;

    @PostMapping("/add-solutions")
    public ResponseEntity<?> addSolutions(@RequestParam("file")MultipartFile file,
                                          @RequestParam("department") String department,
                                          @RequestParam("category") String category,
                                          @RequestParam("subcategory") String subcategory,
                                          @RequestParam("description") String description) {

        if (file.isEmpty()) {
            return new ResponseEntity<>(new Responses<>("46", "Invalid file uploaded", null), HttpStatus.BAD_REQUEST);
        }
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            return new ResponseEntity<>(new Responses<>("99", "Failed to read file", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Solutions solutions = new Solutions();
        solutions.setFile(fileBytes);
        solutions.setDepartment(department);
        solutions.setDescription(description);
        solutions.setCategoryName(category);
        solutions.setSubCategoryName(subcategory);
        solutions.setFileType(file.getContentType());
        solutions.setFileName(file.getOriginalFilename());
        Solutions status = solutionsService.createSolution(solutions);
        if(status.getSolutionId() != null) {
            return new ResponseEntity<>(new Responses<>("00", "Solutions saved successfully", status), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(new Responses<>("99", "Failed to add solutions", null), HttpStatus.OK);
        }
    }


    @PostMapping("/get-solutions")
    public ResponseEntity<?> fetchSolutions() {
        List<Solutions> solutionsList = solutionsService.fetchAllSolutions();
        return new ResponseEntity<>(new Responses<>("00", "Success", solutionsList), HttpStatus.OK);
    }


    @PostMapping("/deleteSolution")
    public ResponseEntity<?> deleteSolution(@RequestBody Map<String, String> payload) {
        Solutions solutions = solutionsService.findSolutionById(payload.get("solutionId"));
        if(solutions != null) {
            solutionsService.deleteSolution(solutions);
            return new ResponseEntity<>(new Responses<>("00", "Solution deleted", null), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Responses<>("90", "Invalid Solution", null), HttpStatus.OK);
    }


    @PostMapping("/downloadSolution")
    public ResponseEntity<?> downloadSolutionMaterial(@RequestBody Map<String, String> data) throws IOException {
        log.info(data.get("fileName"));
        log.info(data.get("filePath"));
        try {
            byte[] imageData= Utils.readFile(data.get("filePath"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(data.get("fileType")));
            headers.setContentDispositionFormData("attachment", data.get("fileName").split("\\.")[0]);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }


//    @PostMapping("/testWeb")
//    public ResponseEntity<?> testingWebSockets() {
//
//        log.info("Sending request to test sockets");
//        CompletableFuture.runAsync(() -> {
//            try {
//                log.info("Inside the thread:::");
//                notificationService.sendNotification("Confirmed Okay");
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Error processing payment: {}", e.getMessage());
//            }
//        });
//        return new ResponseEntity<>(new Responses<>("00", "Success", null), HttpStatus.OK);
//
//    }
}
