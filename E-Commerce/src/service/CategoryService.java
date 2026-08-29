package service;

import dao.CategoryDAO;
import dao.ProductDAO;
import model.Category;
import model.Product;

import java.util.List;

public class CategoryService {
    private final AuthenticationService auth;
    private final CategoryDAO categoryDAO;
    private final ProductDAO productDAO;

    public CategoryService(AuthenticationService auth, CategoryDAO categoryDAO, ProductDAO productDAO) {
        this.auth = auth;
        this.categoryDAO = categoryDAO;
        this.productDAO = productDAO;
    }

    private void requireAdmin() {
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("Authentication required.");
        }
        auth.requireAdmin();
    }

    public List<Category> getCategories() {
        return categoryDAO.findAll();
    }

    public Category getCategoryById(int categoryId) {
        return categoryDAO.findById(categoryId);
    }

    public Category createCategory(String name) {
        requireAdmin();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        String trimmed = name.trim();
        if (categoryDAO.findByName(trimmed) != null) {
            throw new IllegalArgumentException("Duplicate category: " + trimmed);
        }
        return categoryDAO.create(new Category(trimmed));
    }

    public Category updateCategory(int categoryId, String name) {
        requireAdmin();
        Category existing = categoryDAO.findById(categoryId);
        if (existing == null) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        String trimmed = name.trim();
        Category duplicate = categoryDAO.findByName(trimmed);
        if (duplicate != null && duplicate.getId() != categoryId) {
            throw new IllegalArgumentException("Duplicate category: " + trimmed);
        }
        existing.setName(trimmed);
        categoryDAO.update(categoryId, existing);
        return existing;
    }

    public void deleteCategory(int categoryId) {
        requireAdmin();
        Category existing = categoryDAO.findById(categoryId);
        if (existing == null) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        List<Product> products = productDAO.findByCategoryId(categoryId);
        for (Product product : products) {
            product.setCategoryId(null);
            productDAO.update(product.getId(), product);
        }

        categoryDAO.deleteById(categoryId);
    }
}
