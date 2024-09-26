package CRM.project.service;

import CRM.project.entity.Category;
import CRM.project.entity.Department;
import CRM.project.entity.SubCategory;
import CRM.project.repository.CategoryRepository;
import CRM.project.repository.DepartmentRepository;
import CRM.project.repository.SubCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class SubCategoryImp implements SubCategoryService {
    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    @Override
    public List<SubCategory> getSubcategoriesByCategory(Category category1) {
        return subCategoryRepository.findByCategory(category1);
    }

    @Override
    public Map<String, String> createSubCategory(Category category1, Map<String, String> data) {
        Optional<Department> departmentName = departmentRepository.findByDepartmentName(data.get("unit"));
        Map<String, String> responseData = new HashMap<>();
        if(departmentName.isPresent()) {
            SubCategory subCategory = new SubCategory();
            subCategory.setCategory(category1);
            subCategory.setSubCategoryName(data.get("subcategory"));
            subCategory.setSla(Integer.parseInt(data.get("sla")));
            subCategory.setDepartment(departmentName.get());

            subCategoryRepository.save(subCategory);
            responseData.put("code", "00");
            responseData.put("message", "Sub-Category saved successfully");
        }

        else {
            responseData.put("code", "99");
            responseData.put("message", "Department does not exist");
        }

        return responseData;
    }

    @Override
    public List<SubCategory> fetchAllSubcategories() {
        List<SubCategory> all =  subCategoryRepository.findAll();
        log.info(""+all.toString());
        return all;
    }

    @Override
    @Transactional
    public void deleteSubCategories(Long id) {
        subCategoryRepository.deleteById(id);
    }

    @Override
    public List<SubCategory> uploadCategories(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());

        try {
            Sheet sheet = workbook.getSheetAt(0);
            if(sheet.getPhysicalNumberOfRows() > 3001) {
                return null;
            }

            List<CompletableFuture<SubCategory>> futures = IntStream.rangeClosed(1, sheet.getLastRowNum())
                    .parallel()
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            SubCategory category = new SubCategory();
                            Row row = sheet.getRow(i);
                            category.setSubCategoryName(row.getCell(1).getStringCellValue());
                            category.setDepartment(departmentRepository.findByDepartmentName(row.getCell(2).getStringCellValue()).orElse(null));
                            category.setSla((int) row.getCell(3).getNumericCellValue());
                            category.setCategory(categoryRepository.findByCategoryName(row.getCell(4).getStringCellValue()).orElse(null));

                            return category;
                        }catch(Exception e) {
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());
            List<SubCategory> categoryList = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return subCategoryRepository.saveAll(categoryList);
        }catch (Exception e) {
            return null;
        }

    }
}
