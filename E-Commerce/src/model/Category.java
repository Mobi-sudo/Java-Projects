package model;

public class Category {
    private int id;
    private String name;

    public Category() {}

    public Category(int id, String name) {
        setId(id);
        setName(name);
    }

    public Category(String name) {
        this(0, name);
    }

    public int getId() { return id; }
    public void setId(int id) { if (id < 0) throw new IllegalArgumentException("Category id cannot be negative."); this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Category name is required.");
        this.name = name.trim();
    }
}
