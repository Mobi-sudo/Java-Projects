package model;

public class Cart {
    private User user;
    private Product product;
    private int quantity;

    public Cart(User user, Product product, int quantity) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
    }

    // Getters and setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
