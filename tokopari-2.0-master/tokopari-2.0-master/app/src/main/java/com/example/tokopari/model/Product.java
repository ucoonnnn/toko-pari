package com.example.tokopari.model;

public class Product {
    private String id;
    private String title;
    private float price;
    private float originalPrice;
    private float discountedPrice;
    private String image;
    private int quantity;

    public Product(String id, String title, float price, float originalPrice, float discountedPrice, String image, int quantity) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.image = image;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getTotalPrice() {
        return price * quantity;
    }

    public float getOriginalPrice() {
        return price;
    }

    public float getDiscountedPrice() {
        float totalPrice = price * quantity;
        float discountPercentage = getDiscountPercentage();
        return totalPrice * (1 - discountPercentage) / quantity;
    }


    private float getDiscountPercentage() {
        if (quantity >= 10) {
            return 0.15f; // 15% diskon
        } else if (quantity >= 5) {
            return 0.10f; // 10% diskon
        } else if (quantity >= 2) {
            return 0.05f; // 5% diskon
        }
        return 0; //no diskon
    }
}
