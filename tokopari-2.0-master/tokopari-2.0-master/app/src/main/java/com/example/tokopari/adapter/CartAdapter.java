package com.example.tokopari.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tokopari.R;
import com.example.tokopari.model.Product;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Product> cartItems;
    private OnQuantityChangeListener listener;
    private Context context;

    public CartAdapter(List<Product> cartItems, OnQuantityChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        context = parent.getContext();
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        Product product = cartItems.get(position);
        holder.productTitle.setText(product.getTitle());

        String formattedOriginalPrice = formatPrice(product.getOriginalPrice());
        holder.productOriginalPrice.setText("Rp " + formattedOriginalPrice);
        holder.productOriginalPrice.setPaintFlags(holder.productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        String formattedDiscountedPrice = formatPrice(product.getDiscountedPrice());
        holder.productDiscountedPrice.setText("Rp " + formattedDiscountedPrice);

        holder.productQuantity.setText(String.valueOf(product.getQuantity()));
        Glide.with(context).load(product.getImage()).into(holder.productImage);

        //Update quantity
        holder.btnMinus.setOnClickListener(v -> {
            if (product.getQuantity() > 1) {
                product.setQuantity(product.getQuantity() - 1);
                listener.onQuantityChanged(product, product.getQuantity());
            } else {
                listener.onProductRemoved(product);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            product.setQuantity(product.getQuantity() + 1);
            listener.onQuantityChanged(product, product.getQuantity());
        });
    }


    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private String formatPrice(float price) {
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#,###");
        return decimalFormat.format(price).replace(",", ".");
    }

    public interface OnQuantityChangeListener {
        void onQuantityChanged(Product product, int newQuantity);
        void onProductRemoved(Product product);
    }

    public void setCartItems(List<Product> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    public void updateProductQuantity(int position, Product product) {
        cartItems.set(position, product);

        notifyItemChanged(position);
    }


    public class CartViewHolder extends RecyclerView.ViewHolder {
        public TextView productTitle, productOriginalPrice, productDiscountedPrice, productQuantity;
        public ImageView productImage;
        public Button btnMinus, btnPlus;

        public CartViewHolder(View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.productTitle);
            productOriginalPrice = itemView.findViewById(R.id.productOriginalPrice);
            productDiscountedPrice = itemView.findViewById(R.id.productDiscountedPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            productImage = itemView.findViewById(R.id.productImage);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
