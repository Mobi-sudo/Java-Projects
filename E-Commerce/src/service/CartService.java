package service;

import dao.CartDAO;
import dao.ProductDAO;
import model.CartItem;
import model.Product;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class CartService {
    private final AuthenticationService auth;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService(AuthenticationService auth, CartDAO cartDAO, ProductDAO productDAO) {
        this.auth = auth;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    private User requireAuthenticatedCustomer() {
        if (!auth.isAuthenticated()) throw new SecurityException("Authentication required");
        User u = auth.getCurrentUser();
        if (!u.isActive()) throw new SecurityException("Account not active");
        return u;
    }

    public List<CartItem> viewCart() {
        User u = requireAuthenticatedCustomer();
        // return defensive copy
        return new ArrayList<>(cartDAO.getCartForUser(u));
    }

    public void addToCart(int productId, int quantity) {
        User u = requireAuthenticatedCustomer();
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Product p = productDAO.findById(productId);
        if (p == null) throw new IllegalArgumentException("Product not found");
        // Validate stock availability
        if (p.getStock() < quantity) throw new IllegalArgumentException("Insufficient stock");
        List<CartItem> items = cartDAO.getCartForUser(u);
        // merge if product already in cart
        boolean merged = false;
        for (CartItem ci : items) {
            if (ci.getProduct() != null && ci.getProduct().getId() == productId) {
                ci.setQuantity(ci.getQuantity() + quantity);
                merged = true;
                break;
            }
        }
        if (!merged) items.add(new CartItem(u, p, quantity));
        cartDAO.saveCartForUser(u, items);
    }

    public void removeFromCart(int productId) {
        User u = requireAuthenticatedCustomer();
        List<CartItem> items = cartDAO.getCartForUser(u);
        items.removeIf(ci -> ci.getProduct() != null && ci.getProduct().getId() == productId);
        cartDAO.saveCartForUser(u, items);
    }

    public void updateQuantity(int productId, int quantity) {
        User u = requireAuthenticatedCustomer();
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Product p = productDAO.findById(productId);
        if (p == null) throw new IllegalArgumentException("Product not found");
        if (p.getStock() < quantity) throw new IllegalArgumentException("Insufficient stock");
        List<CartItem> items = cartDAO.getCartForUser(u);
        for (CartItem ci : items) {
            if (ci.getProduct() != null && ci.getProduct().getId() == productId) {
                ci.setQuantity(quantity);
                break;
            }
        }
        cartDAO.saveCartForUser(u, items);
    }

    public void clearCart() {
        User u = requireAuthenticatedCustomer();
        cartDAO.clearCart(u);
    }
}
