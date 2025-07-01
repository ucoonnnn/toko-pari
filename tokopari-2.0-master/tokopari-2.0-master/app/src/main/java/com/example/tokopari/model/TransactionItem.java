package com.example.tokopari.model;

public class TransactionItem {
    private String id;        // ID dari produk
    private String title;     // Judul produk
    private String dateTime;  // Tanggal dan waktu transaksi
    private float price;     // Harga produk

    public TransactionItem(String id, String title, String dateTime, float price) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public float getPrice() {
        return price;
    }
}
