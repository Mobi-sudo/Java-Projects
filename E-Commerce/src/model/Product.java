package model;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private Integer categoryId;

    public Product() {}

    public Product(int id, String name, String description, double price, int stockQuantity, Integer categoryId) {
        setId(id);
        setName(name);
        setDescription(description);
        setPrice(price);
        setStockQuantity(stockQuantity);
        setCategoryId(categoryId);
    }

    public Product(String name, String description, double price, int stockQuantity, Integer categoryId) {
        this(0, name, description, price, stockQuantity, categoryId);
    }

    public Product(String name, double price, int stockQuantity) {
        this(0, name, "", price, stockQuantity, null);
    }

    public Product(String name, String description, double price, int stockQuantity) {
        this(0, name, description, price, stockQuantity, null);
    }

    public int getId() { return id; }
    public void setId(int id) { if (id < 0) throw new IllegalArgumentException("Product id cannot be negative"); this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Product name is required.");
        this.name = name.trim();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        if (price <= 0) throw new IllegalArgumentException("Price must be greater than zero.");
        this.price = price;
    }

    public int getStockQuantity() { return stockQuantity; }
    public int getStock() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative.");
        this.stockQuantity = stockQuantity;
    }
    public void setStock(int stock) { setStockQuantity(stock); }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}
