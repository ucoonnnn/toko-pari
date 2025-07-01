package com.example.tokopari.api;

import com.example.tokopari.model.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/tokopari/get_products.php")
    Call<List<Product>> getProducts();

    @GET("/tokopari/get_product_by_id.php")
    Call<Product> getProductById(@Query("id") String id);
}