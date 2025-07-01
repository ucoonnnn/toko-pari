package com.example.tokopari.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tokopari.R;
import com.example.tokopari.model.Product;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnAddToCartListener listener;
    private List<Product> originalProductList;
    private Context context;
    private Set<String> productIdSet = new HashSet<>();



    public ProductAdapter(List<Product> productList, OnAddToCartListener listener) {
        this.productList = productList;
        this.listener = listener;
        this.originalProductList = new ArrayList<>(productList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        context = parent.getContext();
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productTitle.setText(product.getTitle());

        String formattedPrice = formatPrice(product.getPrice());
        holder.productPrice.setText("Rp " + formattedPrice);

        Glide.with(context).load(product.getImage()).into(holder.productImage);

        holder.buttonAddToCart.setOnClickListener(v -> listener.onAddToCart(product));

        holder.buttonShare.setOnClickListener(v -> {
            shareProduct(product);
        });
    }

    private void shareProduct(Product product) {
        try {
            String qrCodeData = product.getId();
            Bitmap qrCodeBitmap = generateQRCode(qrCodeData);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(context, qrCodeBitmap));

            String message = "Check out this product: " + product.getTitle();
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Share Product"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to generate QR", Toast.LENGTH_SHORT).show();
        }
    }


    private Bitmap generateQRCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);

            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "QR Code", null);
        return Uri.parse(path);
    }

    private String formatPrice(float price) {
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#,###");
        return decimalFormat.format(price).replace(",", ".");
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            // Jika query kosong, kembalikan semua produk
            productList.clear();
            productList.addAll(originalProductList);
        } else {
            List<Product> filteredList = new ArrayList<>();
            for (Product product : originalProductList) {
                if (product.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
            // Memperbarui productList dengan produk yang difilter
            productList.clear();
            productList.addAll(filteredList);
        }
        notifyDataSetChanged();
    }


    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView productTitle, productPrice;
        public ImageView productImage;
        public Button buttonAddToCart;
        public ImageButton buttonShare;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);
            buttonShare = itemView.findViewById(R.id.buttonShare);
        }
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        this.originalProductList = new ArrayList<>(productList);  // Initialize originalProductList
        notifyDataSetChanged();
    }
    public void setOriginalProductList(List<Product> originalProductList) {
        this.originalProductList = originalProductList;
    }

}