package CRM.project.service;

import CRM.project.entity.Category;
import CRM.project.entity.Department;
import CRM.project.entity.SubCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SubCategoryService {
    List<SubCategory> getSubcategoriesByCategory(Category category1);

    Map<String, String> createSubCategory(Category category1, Map<String, String> data);

    List<SubCategory> fetchAllSubcategories();

    void deleteSubCategories(Long id);

    List<SubCategory> uploadCategories(MultipartFile file) throws IOException;

    Department fetchSubCategoriesByDept(String subCategoryName);
}
