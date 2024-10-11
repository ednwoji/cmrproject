package CRM.project.service;

import CRM.project.dto.Availability;
import CRM.project.entity.Category;
import CRM.project.entity.Department;
import CRM.project.entity.UserStatus;
import CRM.project.entity.Users;
import CRM.project.repository.CategoryRepository;
import CRM.project.repository.DepartmentRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CategoryServiceImp implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    @Override
    public Category addNewCategory(Category category) {

        if(category.getCategoryName() != null) {
            Category category1 = categoryRepository.findByCategoryName(category.getCategoryName()).orElse(null);
            return category1 == null ? categoryRepository.save(category) : null;
        }
        return null;
    }

    @Override
    public List<Category> getAllCategory(Category category) {
        Department department1=departmentRepository.findByDepartmentName(category.getDepartment()).orElse(null);
        return department1!=null? categoryRepository.findByUnitName(department1): null;
    }

    @Override
    public List<Category> fetchCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findByCategory(String catId) {
        return categoryRepository.findByCategoryId(Long.valueOf(catId)).orElse(null);
    }

    @Override
    @Transactional
    public void deleteCategory(Category category) {
        categoryRepository.deleteByCategoryId(category.getCategoryId());
    }

    @Override
    public List<Category> uploadCategories(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());

        try {
            Sheet sheet = workbook.getSheetAt(0);
            if(sheet.getPhysicalNumberOfRows() > 3001) {
                return null;
            }

            List<CompletableFuture<Category>> futures = IntStream.rangeClosed(1, sheet.getLastRowNum())
                    .parallel()
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            Category category = new Category();
                            Row row = sheet.getRow(i);
                            category.setCategoryName(row.getCell(1).getStringCellValue());
                            category.setUnitName(departmentRepository.findByDepartmentName(row.getCell(2).getStringCellValue()).orElse(null));

                            return category;
                        }catch(Exception e) {
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());
            List<Category> categoryList = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return categoryRepository.saveAll(categoryList);
        }catch (Exception e) {
            return null;
        }

    }
}
