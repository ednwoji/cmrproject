package CRM.project.controller;

import CRM.project.dto.Requestdto;
import CRM.project.entity.Category;
import CRM.project.entity.Department;
import CRM.project.entity.SubCategory;
import CRM.project.entity.Users;
import CRM.project.repository.CategoryRepository;
import CRM.project.response.Responses;
import CRM.project.service.SubCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subcategories")
@CrossOrigin
@Slf4j
public class SubCategoryController {
    @Autowired
    private SubCategoryService subCategoryService;
    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping("/createSubCategory")
    public ResponseEntity<?> createSubCategory(@RequestBody Map<String, String> data) {
        log.info("Adding Subcategories with "+data.toString());
        Category category1 = categoryRepository.findByCategoryName(data.get("category")).orElse(null);

        if(category1 != null) {
            return new ResponseEntity<>(subCategoryService.createSubCategory(category1, data), HttpStatus.OK);
        }

        return null;
    }

    @PostMapping("/getSubCategoryByCategory")
    public List<SubCategory> getSubCategory(@RequestBody Requestdto category) {

        log.info("Retrieving Subcategories with ID "+category.toString());
        Category category1 = categoryRepository.findByCategoryName(category.getCategoryName()).orElse(null);
        if (category1 != null) {
            return subCategoryService.getSubcategoriesByCategory(category1);
        } else
            return null;
    }

    @PostMapping("/getAllSubCategories")
    public ResponseEntity<?> getAllSubCategories() {
        return new ResponseEntity<>(subCategoryService.fetchAllSubcategories(), HttpStatus.OK);
    }

    @PostMapping("/deletesubcategory")
    public ResponseEntity<?> deleteSubCategory(@RequestBody Map<String, String> data) {
        try {
            subCategoryService.deleteSubCategories(Long.valueOf(data.get("subCatId")));
            return new ResponseEntity<>(new Responses<>("00", "Deleted Successfully", null), HttpStatus.OK);
        }catch(Exception e) {
            return new ResponseEntity<>(new Responses<>("42", "Unexpected Error occurred", null), HttpStatus.OK);
        }
    }
}
