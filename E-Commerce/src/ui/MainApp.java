package ui;

import dao.CartDAO;
import dao.CategoryDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import model.CartItem;
import model.Category;
import model.Order;
import model.Product;
import model.User;
import model.UserRole;
import service.AuthenticationService;
import service.CartService;
import service.CategoryService;
import service.OrderService;
import service.ProductService;
import service.SalesReportService;
import util.SetupAdmin;

import java.util.List;
import java.util.Scanner;

public class MainApp {
    private final AuthenticationService authService = new AuthenticationService();
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductService productService = new ProductService(authService, productDAO, categoryDAO);
    private final CategoryService categoryService = new CategoryService(authService, categoryDAO, productDAO);
    private final CartService cartService = new CartService(authService, cartDAO, productDAO);
    private final OrderService orderService = new OrderService(authService, orderDAO, productDAO, cartDAO);
    private final SalesReportService salesReportService = new SalesReportService(authService, orderDAO, productDAO);
    private final Scanner scanner = new Scanner(System.in);
    private boolean running = true;

    public static void main(String[] args) {
        new MainApp().run();
    }

    private void run() {
        SetupAdmin.ensureAdminExists("admin@ecommerce.local", "admin123");
        while (running) {
            if (!authService.isAuthenticated()) {
                showPublicMenu();
            } else if (authService.isAdmin()) {
                showAdminMenu();
            } else {
                showCustomerMenu();
            }
        }
        scanner.close();
    }

    private void showPublicMenu() {
        System.out.println("\n=== E-Commerce ===");
        System.out.println("1) Register customer");
        System.out.println("2) Login");
        System.out.println("0) Exit");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": doRegisterCustomer(); break;
            case "2": doLogin(); break;
            case "0": System.out.println("Bye!"); running = false; break;
            default: System.out.println("Unknown option.");
        }
    }

    private void doRegisterCustomer() {
        try {
            System.out.print("Full name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            User user = authService.register(name, email, password, UserRole.CUSTOMER);
            System.out.println("Customer registered successfully. ID=" + user.getId());
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void doLogin() {
        try {
            System.out.print("Email or username: ");
            String identifier = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            User user = authService.login(identifier, password);
            if (user == null) {
                System.out.println("Login failed: invalid credentials.");
            } else {
                System.out.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    private void showCustomerMenu() {
        User user = authService.getCurrentUser();
        System.out.println("\n=== Customer Menu (" + user.getName() + ") ===");
        System.out.println("1) Browse products");
        System.out.println("2) Search products");
        System.out.println("3) View categories");
        System.out.println("4) View product details");
        System.out.println("5) Add item to cart");
        System.out.println("6) Remove item from cart");
        System.out.println("7) Update cart quantity");
        System.out.println("8) View cart");
        System.out.println("9) Checkout");
        System.out.println("10) View my orders");
        System.out.println("11) View my order status");
        System.out.println("12) Logout");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": browseProducts(); break;
            case "2": searchProducts(); break;
            case "3": viewCategories(); break;
            case "4": viewProductDetails(); break;
            case "5": addToCart(); break;
            case "6": removeFromCart(); break;
            case "7": updateCartQuantity(); break;
            case "8": viewCart(); break;
            case "9": checkout(); break;
            case "10": viewCustomerOrders(); break;
            case "11": viewCustomerOrderStatus(); break;
            case "12": authService.logout(); System.out.println("Logged out."); break;
            default: System.out.println("Unknown option.");
        }
    }

    private void showAdminMenu() {
        User admin = authService.getCurrentUser();
        System.out.println("\n=== Admin Menu (" + admin.getName() + ") ===");
        System.out.println("1) View products");
        System.out.println("2) Add product");
        System.out.println("3) Update product");
        System.out.println("4) Delete product");
        System.out.println("5) View categories");
        System.out.println("6) Add category");
        System.out.println("7) Update category");
        System.out.println("8) Delete category");
        System.out.println("9) Update inventory");
        System.out.println("10) View all orders");
        System.out.println("11) Update order status");
        System.out.println("12) Sales report");
        System.out.println("13) Logout");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": showAllProducts(); break;
            case "2": addProduct(); break;
            case "3": updateProduct(); break;
            case "4": deleteProduct(); break;
            case "5": viewCategories(); break;
            case "6": addCategory(); break;
            case "7": updateCategory(); break;
            case "8": deleteCategory(); break;
            case "9": updateInventory(); break;
            case "10": viewAllOrders(); break;
            case "11": updateOrderStatus(); break;
            case "12": salesReport(); break;
            case "13": authService.logout(); System.out.println("Logged out."); break;
            default: System.out.println("Unknown option.");
        }
    }

    private void browseProducts() {
        List<Product> products = productService.browseProducts();
        printProducts(products, "Available Products");
    }

    private void searchProducts() {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine().trim();
        List<Product> products = productService.searchProducts(keyword);
        printProducts(products, "Search Results");
    }

    private void viewCategories() {
        List<Category> categories = categoryService.getCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories found.");
            return;
        }
        for (Category category : categories) {
            System.out.println("ID=" + category.getId() + " | " + category.getName());
        }
    }

    private void viewProductDetails() {
        System.out.print("Product ID: ");
        int productId = readInt();
        Product product = productService.getProductById(productId);
        if (product == null) {
            System.out.println("Product not found.");
            return;
        }
        System.out.println("ID=" + product.getId());
        System.out.println("Name=" + product.getName());
        System.out.println("Description=" + product.getDescription());
        System.out.println("Price=$" + product.getPrice());
        System.out.println("Stock=" + product.getStockQuantity());
        System.out.println("Category ID=" + product.getCategoryId());
    }

    private void addToCart() {
        try {
            System.out.print("Product ID: ");
            int productId = readInt();
            System.out.print("Quantity: ");
            int quantity = readInt();
            cartService.addToCart(productId, quantity);
            System.out.println("Added to cart.");
        } catch (Exception e) {
            System.out.println("Failed to add to cart: " + e.getMessage());
        }
    }

    private void removeFromCart() {
        try {
            System.out.print("Product ID: ");
            int productId = readInt();
            cartService.removeFromCart(productId);
            System.out.println("Removed from cart.");
        } catch (Exception e) {
            System.out.println("Failed to remove item: " + e.getMessage());
        }
    }

    private void updateCartQuantity() {
        try {
            System.out.print("Product ID: ");
            int productId = readInt();
            System.out.print("New quantity: ");
            int quantity = readInt();
            cartService.updateQuantity(productId, quantity);
            System.out.println("Cart updated.");
        } catch (Exception e) {
            System.out.println("Failed to update cart: " + e.getMessage());
        }
    }

    private void viewCart() {
        List<CartItem> items = cartService.viewCart();
        if (items.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        for (CartItem item : items) {
            System.out.println("Product ID=" + item.getProduct().getId()
                    + " | " + item.getProduct().getName()
                    + " | Qty=" + item.getQuantity()
                    + " | Unit Price=$" + item.getProduct().getPrice());
        }
    }

    private void checkout() {
        try {
            Order order = orderService.checkout();
            System.out.println("Checkout successful. Order ID=" + order.getOrderId() + " Total=$" + order.getTotalPrice());
        } catch (Exception e) {
            System.out.println("Checkout failed: " + e.getMessage());
        }
    }

    private void viewCustomerOrders() {
        List<Order> orders = orderService.getMyOrders();
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        for (Order order : orders) {
            System.out.println("Order ID=" + order.getOrderId() + " | Status=" + order.getStatus() + " | Total=$" + order.getTotalPrice());
        }
    }

    private void viewCustomerOrderStatus() {
        System.out.print("Order ID: ");
        int orderId = readInt();
        Order order = orderDAO.findById(orderId);
        if (order == null || order.getUser().getId() != authService.getCurrentUser().getId()) {
            System.out.println("Order not found for this customer.");
            return;
        }
        System.out.println("Order ID=" + order.getOrderId() + " | Status=" + order.getStatus());
    }

    private void showAllProducts() {
        List<Product> products = productDAO.findAll();
        printProducts(products, "All Products");
    }

    private void addProduct() {
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Description: ");
            String description = scanner.nextLine();
            System.out.print("Price: ");
            double price = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Stock: ");
            int stock = readInt();
            System.out.print("Category ID (blank for none): ");
            String categoryInput = scanner.nextLine().trim();
            Integer categoryId = categoryInput.isEmpty() ? null : Integer.parseInt(categoryInput);
            Product product = productService.addProduct(name, description, price, stock, categoryId);
            System.out.println("Product added. ID=" + product.getId());
        } catch (Exception e) {
            System.out.println("Failed to add product: " + e.getMessage());
        }
    }

    private void updateProduct() {
        try {
            System.out.print("Product ID: ");
            int productId = readInt();
            System.out.print("New name: ");
            String name = scanner.nextLine().trim();
            System.out.print("New description: ");
            String description = scanner.nextLine();
            System.out.print("New price: ");
            double price = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("New stock: ");
            int stock = readInt();
            System.out.print("Category ID (blank for none): ");
            String categoryInput = scanner.nextLine().trim();
            Integer categoryId = categoryInput.isEmpty() ? null : Integer.parseInt(categoryInput);
            Product product = productService.updateProduct(productId, name, description, price, stock, categoryId);
            System.out.println("Product updated. ID=" + product.getId());
        } catch (Exception e) {
            System.out.println("Failed to update product: " + e.getMessage());
        }
    }

    private void deleteProduct() {
        try {
            System.out.print("Product ID: ");
            int productId = readInt();
            productService.deleteProduct(productId);
            System.out.println("Product deleted.");
        } catch (Exception e) {
            System.out.println("Failed to delete product: " + e.getMessage());
        }
    }

    private void addCategory() {
        try {
            System.out.print("Category name: ");
            String name = scanner.nextLine().trim();
            Category category = categoryService.createCategory(name);
            System.out.println("Category created. ID=" + category.getId());
        } catch (Exception e) {
            System.out.println("Failed to create category: " + e.getMessage());
        }
    }

    private void updateCategory() {
        try {
            System.out.print("Category ID: ");
            int categoryId = readInt();
            System.out.print("New category name: ");
            String name = scanner.nextLine().trim();
            Category category = categoryService.updateCategory(categoryId, name);
            System.out.println("Category updated. ID=" + category.getId());
        } catch (Exception e) {
            System.out.println("Failed to update category: " + e.getMessage());
        }
    }

    private void deleteCategory() {
        try {
            System.out.print("Category ID: ");
            int categoryId = readInt();
            categoryService.deleteCategory(categoryId);
            System.out.println("Category deleted.");
        } catch (Exception e) {
            System.out.println("Failed to delete category: " + e.getMessage());
        }
    }

    private void updateInventory() {
        try {
            System.out.print("Product ID: ");
            int productId = readInt();
            System.out.print("New stock quantity: ");
            int stock = readInt();
            productService.updateInventory(productId, stock);
            System.out.println("Inventory updated.");
        } catch (Exception e) {
            System.out.println("Failed to update inventory: " + e.getMessage());
        }
    }

    private void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        for (Order order : orders) {
            System.out.println("Order ID=" + order.getOrderId() + " | User=" + order.getUser().getEmail() + " | Status=" + order.getStatus() + " | Total=$" + order.getTotalPrice());
        }
    }

    private void updateOrderStatus() {
        try {
            System.out.print("Order ID: ");
            int orderId = readInt();
            System.out.print("New status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED): ");
            String status = scanner.nextLine().trim();
            orderService.updateOrderStatus(orderId, status);
            System.out.println("Order status updated.");
        } catch (Exception e) {
            System.out.println("Failed to update order status: " + e.getMessage());
        }
    }

    private void salesReport() {
        try {
            System.out.println("Total revenue: $" + salesReportService.getTotalRevenue());
            System.out.println("Order count: " + salesReportService.getOrderCount());
            System.out.println("Completed orders: " + salesReportService.getCompletedOrdersCount());
            System.out.println("Sales by product: " + salesReportService.getSalesByProduct());
            System.out.println("Sales by category: " + salesReportService.getSalesByCategory());
        } catch (Exception e) {
            System.out.println("Failed to generate sales report: " + e.getMessage());
        }
    }

    private void printProducts(List<Product> products, String title) {
        System.out.println("\n--- " + title + " ---");
        if (products == null || products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        for (Product product : products) {
            System.out.println("ID=" + product.getId()
                    + " | Name=" + product.getName()
                    + " | Price=$" + product.getPrice()
                    + " | Stock=" + product.getStockQuantity()
                    + " | Category=" + product.getCategoryId());
        }
    }

    private int readInt() {
        String input = scanner.nextLine().trim();
        return Integer.parseInt(input);
    }
}