package CRM.project.utils;

import CRM.project.entity.Category;
import CRM.project.entity.Department;
import CRM.project.repository.CategoryRepository;
import CRM.project.repository.DepartmentRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ExcelToCategoryutils {
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    public List<Category> convertFromExcel(MultipartFile file){

        String fileName= file.getName();
        if (!fileName.contains(".xlsx")||!fileName.contains(".xls"))
            throw new RuntimeException("file you have requested for reading must be in xlsx or .xls");
        try {
            Workbook workbook=null;
            if (file instanceof MultipartFile){
                byte [] b=file.getBytes();
                InputStream inputStream=new ByteArrayInputStream(b);
                if (fileName.contains("xlsx"))
                     workbook=new XSSFWorkbook(inputStream);
                else if (fileName.contains("xls"))
                    workbook=new HSSFWorkbook(inputStream);
            }
            Sheet sheet=workbook.getSheetAt(0);
            FormulaEvaluator evaluator=workbook.getCreationHelper().createFormulaEvaluator();
            Category category=new Category();
            List<Category> categories=new ArrayList<>();
            for (int i=1;i<sheet.getPhysicalNumberOfRows();i++){
                category=new Category();
                Row row= sheet.getRow(i);
                if (row!=null){
                    Cell c0= row.getCell(0,Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (c0!=null){
                        DataFormatter formatter=new DataFormatter();
                        long categoryId= Long.parseLong(formatter.formatCellValue(c0,evaluator));
                        category.setCategoryId(categoryId);
                    }
                    Cell c1= row.getCell(1,Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (c1!=null){
                        DataFormatter formatter=new DataFormatter();
                        String categoryName= formatter.formatCellValue(c1,evaluator);
                        category.setCategoryName(categoryName);
                    }
                    Cell c2= row.getCell(2,Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (c2!=null){
                        DataFormatter formatter=new DataFormatter();
                         String unitName= formatter.formatCellValue(c2,evaluator);
                         Optional<Department> department=departmentRepository.findByDepartmentName(unitName);
                         if (department.isPresent()){
                             category.setUnitName(department.get());
                         }
                    }
                }
                categories.add(category);
            }
            categoryRepository.saveAll(categories);

        }catch (Exception e){

        }
        return null;
    }
}

