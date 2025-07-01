package com.example.tokopari.adapter;

import com.example.tokopari.model.Product;

public interface OnQuantityChangeListener {
    void onQuantityChanged(Product product, int newQuantity);
    void onProductRemoved(Product product); // Menambahkan metode untuk penghapusan produk
}
