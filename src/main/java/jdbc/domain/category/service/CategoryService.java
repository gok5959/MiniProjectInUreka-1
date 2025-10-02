package jdbc.domain.category.service;

import jdbc.common.exception.NotFoundException;
import jdbc.domain.category.dao.CategoryDao;
import jdbc.domain.category.model.Category;

import java.util.List;

public class CategoryService {
    private CategoryDao categoryDao;

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public Category getById(Long id) {
        Category category = categoryDao.findCategoryById(id);
        if (category == null) throw new NotFoundException("Category not found. id=" + id);
        return category;
    }

    public List<Category> getAllOrdered() { return categoryDao.findAllOrdered();}

    public int create(String name, Long parentId) {
        return categoryDao.insertCategory(new Category(null, name, parentId));
    }

    public int rename(Long id, String newName) {
        return categoryDao.renameCategory(id, newName);
    }

    public int delete(Long id) {
        return categoryDao.delete(id);
    }
}
