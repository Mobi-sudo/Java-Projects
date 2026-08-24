package model;

public class OrderItem {
    private int id; // 0 if not persisted
    private int orderId; // parent order id, optional in model object
    private Product product;
    private int quantity;
    private double unitPrice; // snapshot of product price at purchase

    public OrderItem(Product product, int quantity, double unitPrice) {
        setProduct(product);
        setQuantity(quantity);
        setUnitPrice(unitPrice);
    }

    public OrderItem(int id, int orderId, Product product, int quantity, double unitPrice) {
        this.id = id;
        this.orderId = orderId;
        setProduct(product);
        setQuantity(quantity);
        setUnitPrice(unitPrice);
    }

    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null.");
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice < 0) throw new IllegalArgumentException("Unit price cannot be negative.");
        this.unitPrice = unitPrice;
    }
}
