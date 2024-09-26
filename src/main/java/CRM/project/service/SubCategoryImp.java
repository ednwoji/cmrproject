package CRM.project.service;

import CRM.project.entity.Category;
import CRM.project.entity.Department;
import CRM.project.entity.SubCategory;
import CRM.project.repository.DepartmentRepository;
import CRM.project.repository.SubCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class SubCategoryImp implements SubCategoryService {
    @Autowired
    private SubCategoryRepository subCategoryRepository;

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
}
