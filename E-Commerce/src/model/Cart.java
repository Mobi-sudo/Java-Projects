package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {
    private User user;
    private final List<CartItem> items;

    public Cart(User user) {
        this(user, new ArrayList<>());
    }

    public Cart(User user, List<CartItem> items) {
        setUser(user);
        this.items = new ArrayList<>();
        if (items != null) {
            for (CartItem item : items) {
                addItem(item);
            }
        }
    }

    public Cart(User user, Product product, int quantity) {
        this(user);
        addItem(product, quantity);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        this.user = user;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<CartItem> items) {
        this.items.clear();
        if (items != null) {
            for (CartItem item : items) {
                addItem(item);
            }
        }
    }

    public void addItem(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cart item cannot be null.");
        }
        items.add(item);
    }

    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        addItem(new CartItem(user, product, quantity));
    }

    public void removeItem(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        items.removeIf(item -> item.getProduct().equals(product));
    }

    public Product getProduct() {
        return items.isEmpty() ? null : items.get(0).getProduct();
    }

    public void setProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (items.isEmpty()) {
            addItem(product, 1);
            return;
        }
        items.get(0).setProduct(product);
    }

    public int getQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot set quantity on an empty cart.");
        }
        items.get(0).setQuantity(quantity);
    }
}
