package CRM.project.controller;


import CRM.project.entity.Solutions;
import CRM.project.response.Responses;
import CRM.project.service.SolutionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/solutions")
@CrossOrigin
@Slf4j
public class SolutionsController {


    @Autowired
    private SolutionsService solutionsService;

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
}
