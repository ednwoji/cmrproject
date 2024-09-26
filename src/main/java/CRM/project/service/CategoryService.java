package CRM.project.service;

import CRM.project.entity.Category;

import java.util.List;

public interface CategoryService {
    Category addNewCategory(Category category);
    List<Category> getAllCategory(Category category);

    List<Category> fetchCategories();

    Category findByCategory(String catId);

    void deleteCategory(Category category);
}
