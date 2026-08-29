package service;

import dao.OrderDAO;
import dao.ProductDAO;
import model.Order;
import model.OrderItem;
import model.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SalesReportService {
    private final AuthenticationService auth;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public SalesReportService(AuthenticationService auth, OrderDAO orderDAO, ProductDAO productDAO) {
        this.auth = auth;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    private void requireAdmin() {
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("Authentication required.");
        }
        auth.requireAdmin();
    }

    public double getTotalRevenue() {
        requireAdmin();
        double total = 0;
        for (Order order : orderDAO.findAll()) {
            total += order.getTotalPrice();
        }
        return total;
    }

    public int getOrderCount() {
        requireAdmin();
        return orderDAO.findAll().size();
    }

    public int getCompletedOrdersCount() {
        requireAdmin();
        int count = 0;
        for (Order order : orderDAO.findAll()) {
            if (order.getStatus() == model.OrderStatus.DELIVERED) {
                count++;
            }
        }
        return count;
    }

    public Map<Integer, Integer> getSalesByProduct() {
        requireAdmin();
        Map<Integer, Integer> salesByProduct = new LinkedHashMap<>();
        for (Order order : orderDAO.findAll()) {
            for (OrderItem item : order.getOrderItems()) {
                int productId = item.getProduct().getId();
                salesByProduct.put(productId, salesByProduct.getOrDefault(productId, 0) + item.getQuantity());
            }
        }
        return salesByProduct;
    }

    public Map<Integer, Integer> getSalesByCategory() {
        requireAdmin();
        Map<Integer, Integer> salesByCategory = new LinkedHashMap<>();
        for (Order order : orderDAO.findAll()) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product == null || product.getCategoryId() == null) {
                    continue;
                }
                int categoryId = product.getCategoryId();
                salesByCategory.put(categoryId, salesByCategory.getOrDefault(categoryId, 0) + item.getQuantity());
            }
        }
        return salesByCategory;
    }

    public List<Product> getLowStockProducts(int threshold) {
        requireAdmin();
        List<Product> lowStock = new ArrayList<>();
        for (Product product : productDAO.findAll()) {
            if (product.getStockQuantity() <= threshold) {
                lowStock.add(product);
            }
        }
        return lowStock;
    }
}
