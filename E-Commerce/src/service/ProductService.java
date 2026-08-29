package service;

import dao.CategoryDAO;
import dao.ProductDAO;
import model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private final AuthenticationService auth;
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;

    public ProductService(AuthenticationService auth, ProductDAO productDAO, CategoryDAO categoryDAO) {
        this.auth = auth;
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
    }

    private void requireAdmin() {
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("Authentication required.");
        }
        auth.requireAdmin();
    }

    public List<Product> browseProducts() {
        List<Product> all = productDAO.findAll();
        List<Product> available = new ArrayList<>();
        for (Product product : all) {
            if (product.getStockQuantity() > 0) {
                available.add(product);
            }
        }
        return available;
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return browseProducts();
        }
        List<Product> results = productDAO.searchByKeyword(keyword.trim());
        List<Product> available = new ArrayList<>();
        for (Product product : results) {
            if (product.getStockQuantity() > 0) {
                available.add(product);
            }
        }
        return available;
    }

    public List<Product> getProductsByCategory(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category id.");
        }
        List<Product> results = productDAO.findByCategoryId(categoryId);
        List<Product> available = new ArrayList<>();
        for (Product product : results) {
            if (product.getStockQuantity() > 0) {
                available.add(product);
            }
        }
        return available;
    }

    public Product getProductById(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product id.");
        }
        return productDAO.findById(productId);
    }

    public Product addProduct(String name, String description, double price, int stockQuantity, Integer categoryId) {
        requireAdmin();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (categoryId != null && categoryDAO.findById(categoryId) == null) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        Product product = new Product(name.trim(), description == null ? "" : description.trim(), price, stockQuantity, categoryId);
        productDAO.save(product);
        return product;
    }

    public Product updateProduct(int productId, String name, String description, double price, int stockQuantity, Integer categoryId) {
        requireAdmin();
        Product existing = productDAO.findById(productId);
        if (existing == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (categoryId != null && categoryDAO.findById(categoryId) == null) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        existing.setName(name.trim());
        existing.setDescription(description == null ? "" : description.trim());
        existing.setPrice(price);
        existing.setStockQuantity(stockQuantity);
        existing.setCategoryId(categoryId);
        productDAO.update(productId, existing);
        return existing;
    }

    public void deleteProduct(int productId) {
        requireAdmin();
        Product existing = productDAO.findById(productId);
        if (existing == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        productDAO.deleteById(productId);
    }

    public void updateInventory(int productId, int newStockQuantity) {
        requireAdmin();
        if (newStockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        product.setStockQuantity(newStockQuantity);
        productDAO.update(productId, product);
    }

    public void addInventory(int productId, int quantityToAdd) {
        requireAdmin();
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive.");
        }
        productDAO.addStock(productId, quantityToAdd);
    }
}
