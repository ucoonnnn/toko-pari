        package com.example.tokopari;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.net.Uri;
        import android.os.Bundle;
        import android.provider.MediaStore;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;
        import android.widget.SearchView;
        import android.widget.Toast;
        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.GridLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;
        import com.example.tokopari.adapter.ProductAdapter;
        import com.example.tokopari.api.ApiClient;
        import com.example.tokopari.api.ApiService;
        import com.example.tokopari.model.Product;
        import com.example.tokopari.storage.CartManager;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;
        import retrofit2.Call;
        import retrofit2.Callback;
        import retrofit2.Response;
        import com.google.zxing.BinaryBitmap;
        import com.google.zxing.RGBLuminanceSource;
        import com.google.zxing.Result;
        import com.google.zxing.common.HybridBinarizer;
        import com.google.zxing.qrcode.QRCodeReader;

        public class Home extends Fragment implements ProductAdapter.OnAddToCartListener {

            private static final int GALLERY_REQUEST_CODE = 1001;
            private RecyclerView recyclerView;
            private ProductAdapter productAdapter;
            private ImageButton buttonScanQR;
            private SearchView searchView;

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                View view = inflater.inflate(R.layout.fragment_home, container, false);

                recyclerView = view.findViewById(R.id.recyclerViewRecommendation);
                recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2));

                productAdapter = new ProductAdapter(new ArrayList<>(), Home.this);
                recyclerView.setAdapter(productAdapter);

                buttonScanQR = view.findViewById(R.id.buttonScanQR);
                buttonScanQR.setOnClickListener(v -> openGallery());

                fetchProducts();

                searchView = view.findViewById(R.id.search);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        productAdapter.filter(newText);
                        return false;
                    }
                });
                return view;
            }

            private void openGallery() {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);

                if (requestCode == GALLERY_REQUEST_CODE && resultCode == requireActivity().RESULT_OK && data != null) {
                    Uri selectedImageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                        scanQRCodeFromBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireActivity(), "Failed to process the image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            private void scanQRCodeFromBitmap(Bitmap bitmap) {
                try {
                    int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                    bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

                    RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    QRCodeReader qrCodeReader = new QRCodeReader();

                    Result result = qrCodeReader.decode(binaryBitmap);
                    String qrCodeContent = result.getText();

                    Log.d("Home", "Scanned QR Code from Image: " + qrCodeContent);

                    fetchProductById(qrCodeContent);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireActivity(), "Failed to decode QR Code", Toast.LENGTH_SHORT).show();
                }
            }

            private void fetchProductById(String productId) {
                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                Call<Product> call = apiService.getProductById(productId);

                call.enqueue(new Callback<Product>() {
                    @Override
                    public void onResponse(Call<Product> call, Response<Product> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Product product = response.body();
                            Log.d("Home", "Product received: " + product.getTitle());

                            List<Product> productList = new ArrayList<>();
                            productList.add(product);

                            productAdapter.setProductList(productList);
                        } else {
                            Log.e("Home", "Product not found!");
                            Toast.makeText(requireActivity(), "Product not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Product> call, Throwable t) {
                        Log.e("Home", "Failed to fetch product", t);
                        Toast.makeText(requireActivity(), "Failed to fetch product", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            private void fetchProducts() {
                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                Call<List<Product>> call = apiService.getProducts();

                call.enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        if (response.isSuccessful()) {
                            List<Product> productList = response.body();
                            Collections.shuffle(productList);

                            productAdapter.setProductList(productList);
                            productAdapter.setOriginalProductList(new ArrayList<>(productList));
                        } else {
                            Toast.makeText(requireActivity(), "Failed to load products", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Toast.makeText(requireActivity(), "Failed to connect to API", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAddToCart(Product product) {
                CartManager cartManager = new CartManager(requireContext());
                cartManager.addToCart(product);

                Toast.makeText(requireActivity(), "Product added to cart: " + product.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }