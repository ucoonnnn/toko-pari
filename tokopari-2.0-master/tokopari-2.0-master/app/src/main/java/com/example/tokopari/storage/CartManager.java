package com.example.tokopari.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.tokopari.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String CART_PREFS = "cart_prefs";
    private static final String CART_KEY = "cart_items";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Menyimpan daftar produk ke shared preferences
    public void saveCartItems(List<Product> cartItems) {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(CART_KEY, json).apply();
    }

    // Mengambil daftar produk dari shared preferences
    public List<Product> getCartItems() {
        String json = sharedPreferences.getString(CART_KEY, "[]");
        Type type = new TypeToken<List<Product>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Menambahkan produk ke dalam cart
    public void addToCart(Product product) {
        List<Product> cartItems = getCartItems();
        boolean productExists = false;

        // Mengecek apakah produk sudah ada di cart
        for (Product item : cartItems) {
            if (item.getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + 1); // Menambah quantity jika produk sudah ada
                productExists = true;
                break;
            }
        }

        // Jika produk belum ada, tambahkan produk baru dengan quantity 1
        if (!productExists) {
            product.setQuantity(1); // Set quantity menjadi 1 saat produk ditambahkan pertama kali
            cartItems.add(product);
        }

        saveCartItems(cartItems); // Simpan kembali daftar produk
    }

    public void updateProductQuantity(Product product, int newQuantity) {
        List<Product> cartItems = getCartItems();

        for (Product item : cartItems) {
            if (item.getId().equals(product.getId())) {
                if (newQuantity <= 0) {
                    // Hapus produk jika kuantitas menjadi 0
                    cartItems.remove(item);
                    break; // Keluar dari loop setelah menghapus item
                } else {
                    // Jika kuantitas lebih besar dari 0, update kuantitas produk
                    item.setQuantity(newQuantity);
                }
            }
        }

        // Simpan daftar produk yang sudah diperbarui
        saveCartItems(cartItems);
    }

    public void removeFromCart(Product product) {
        List<Product> cartItems = getCartItems();
        cartItems.removeIf(item -> item.getId().equals(product.getId())); // Menghapus produk berdasarkan ID
        // Setelah menghapus produk, simpan kembali daftar produk yang sudah diperbarui
        saveCartItems(cartItems);
    }
    public void clearCart() {
        // Get current cart items
        List<Product> cartItems = getCartItems();

        // Clear the cart items
        cartItems.clear();

        // Save the empty cart back to SharedPreferences
        saveCartItems(cartItems);
    }

}
