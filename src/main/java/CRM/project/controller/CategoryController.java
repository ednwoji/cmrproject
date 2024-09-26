package CRM.project.controller;

import CRM.project.entity.Category;
import CRM.project.entity.Department;
import CRM.project.entity.SubCategory;
import CRM.project.response.Responses;
import CRM.project.service.CategoryService;
import CRM.project.service.SubCategoryService;
import CRM.project.utils.ExcelToCategoryutils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
@CrossOrigin
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ExcelToCategoryutils utils;

    @Autowired
    private SubCategoryService subCategoryService;

    @PostMapping("/addCategory")
    public ResponseEntity<?> addNewCategory(@RequestBody Category category)
    {
        log.info("Incoming request is::: "+category.toString());
        Category category1=categoryService.addNewCategory(category);
        return category1!=null? new ResponseEntity<>(new Responses("00", "category details Saved Successfully", null), HttpStatus.OK)
                : new ResponseEntity<>(new Responses("99", "Record not saved, Ensure category name does not exist", null), HttpStatus.OK);
    }
    @PostMapping("/allCategory")
    public ResponseEntity<?> getAllCategoryByDepartment(@RequestBody Category category){
        log.info("Getting all categories "+category.toString());
        List<Category> categories=categoryService.getAllCategory(category);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/deleteCategory")
    public ResponseEntity<?> deleteCategory(@RequestBody Map<String, String> data) {
        try {
            Category category = categoryService.findByCategory(data.get("catId"));
            if (category != null) {
                List<SubCategory> subCategories = subCategoryService.getSubcategoriesByCategory(category);
                for (SubCategory item : subCategories) {
                    subCategoryService.deleteSubCategories(item.getId());
                }
                categoryService.deleteCategory(category);
                return new ResponseEntity<>(new Responses<>("00", "Deleted Successfully", null), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Responses<>("45", "Invalid Category ID", null), HttpStatus.OK);
            }
        }catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new Responses<>("42", "Unexpected Error occurred", null), HttpStatus.OK);
        }
    }

    @PostMapping("/fetchCategory")
    public ResponseEntity<?> getAllProducts(){

        List<Category> categories=categoryService.fetchCategories();
        return ResponseEntity.ok(categories);
    }

    //added the poiji dependency needed
    @PostMapping("/bulkUpload")
    public List<Category> upload(@RequestParam("file")MultipartFile file){
        return utils.convertFromExcel(file);
    }

}
