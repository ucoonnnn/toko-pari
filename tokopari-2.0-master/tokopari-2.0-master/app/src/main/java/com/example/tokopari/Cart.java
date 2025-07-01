package com.example.tokopari;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tokopari.adapter.CartAdapter;
import com.example.tokopari.model.Product;
import com.example.tokopari.storage.CartManager;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

public class Cart extends Fragment implements CartAdapter.OnQuantityChangeListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private CartManager cartManager;
    private TextView totalPriceTextView;
    private TextView originalPriceTextView;
    private TextView discountTextView;
    private EditText voucherEditText;
    private Button applyVoucherButton;
    private float voucherDiscount = 0f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCart);
        totalPriceTextView = view.findViewById(R.id.totalPriceTextView);
        discountTextView = view.findViewById(R.id.discountTextView);
        voucherEditText = view.findViewById(R.id.voucherEditText);
        applyVoucherButton = view.findViewById(R.id.applyVoucherButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartManager = new CartManager(getContext());

        List<Product> cartItems = cartManager.getCartItems();

        cartAdapter = new CartAdapter(cartItems, this);
        recyclerView.setAdapter(cartAdapter);

        updateTotalPrice(cartItems);

        applyVoucherButton.setOnClickListener(v -> {
            String voucherCode = voucherEditText.getText().toString().trim();
            applyVoucher(voucherCode);
        });

        Button checkoutButton = view.findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle("Confirm Checkout")
                        .setMessage("Are you sure you want to proceed with checkout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveCheckoutToFirestore(cartManager.getCartItems());
                                cartManager.clearCart();

                                cartAdapter.setCartItems(cartManager.getCartItems());
                                updateTotalPrice(cartManager.getCartItems());
                                Toast.makeText(getActivity(), "Checkout confirmed!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null) // Do nothing on "No"
                        .show();
            }
        });
        return view;
    }

    private void applyVoucher(String voucherCode) {
        if ("DISKON10".equals(voucherCode)) {
            voucherDiscount = 0.10f;  // Diskon 10%
            Toast.makeText(getActivity(), "Voucher applied: 10% discount", Toast.LENGTH_SHORT).show();
        } else if ("DISKON20".equals(voucherCode)) {
            voucherDiscount = 0.20f;  // Diskon 20%
            Toast.makeText(getActivity(), "Voucher applied: 20% discount", Toast.LENGTH_SHORT).show();
        } else {
            voucherDiscount = 0f;  // Voucher tidak valid
            Toast.makeText(getActivity(), "Invalid voucher code", Toast.LENGTH_SHORT).show();
        }

        List<Product> cartItems = cartManager.getCartItems();
        updateTotalPrice(cartItems);
    }

    private void saveCheckoutToFirestore(List<Product> cartItems) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> checkoutData = new HashMap<>();

        float totalPrice = 0;
        for (Product product : cartItems) {
            totalPrice += product.getTotalPrice();
        }

        float discountPercentage = getDiscountPercentage(cartItems);
        float discount = totalPrice * discountPercentage;
        float discountedPrice = totalPrice - discount;

        checkoutData.put("products", cartItems);
        checkoutData.put("totalPrice", discountedPrice);
        checkoutData.put("timestamp", System.currentTimeMillis());

        CollectionReference checkoutCollection = db.collection("checkouts");
        checkoutCollection.add(checkoutData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Cart", "Checkout data saved successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Cart", "Error saving checkout data", e);
                });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        originalPriceTextView = view.findViewById(R.id.productOriginalPrice);
        discountTextView = view.findViewById(R.id.discountTextView);

        if (originalPriceTextView == null || discountTextView == null) {
            Log.e("Cart", "TextView initialization failed");
        }
    }

    @Override
    public void onQuantityChanged(Product product, int newQuantity) {
        cartManager.updateProductQuantity(product, newQuantity);

        List<Product> cartItems = cartManager.getCartItems();

        updateTotalPrice(cartItems);

        int position = cartItems.indexOf(product);
        if (position != -1) {
            cartAdapter.updateProductQuantity(position, product);
        }

        cartAdapter.setCartItems(cartItems);
    }

    @Override
    public void onProductRemoved(Product product) {
        cartManager.removeFromCart(product);

        List<Product> cartItems = cartManager.getCartItems();
        updateTotalPrice(cartItems);

        int position = cartItems.indexOf(product);
        if (position != -1) {
            cartAdapter.notifyItemRemoved(position);
        }
        cartAdapter.setCartItems(cartItems);
    }

    private void updateTotalPrice(List<Product> cartItems) {
        float totalPrice = 0;

        for (Product product : cartItems) {
            totalPrice += product.getTotalPrice();
        }

        float discountPercentage = getDiscountPercentage(cartItems);

        float productDiscount = totalPrice * discountPercentage;

        float priceAfterProductDiscount = totalPrice - productDiscount;
        float voucherDiscountAmount = priceAfterProductDiscount * voucherDiscount;

        float finalPrice = priceAfterProductDiscount - voucherDiscountAmount;

        String formattedTotalPrice = "Rp " + formatPrice(totalPrice);
        String formattedFinalPrice = "Total: Rp " + formatPrice(finalPrice);
        String formattedSavings = "You saved: Rp " + formatPrice(productDiscount + voucherDiscountAmount);

        totalPriceTextView.setText(formattedFinalPrice);

        if (originalPriceTextView != null) {
            originalPriceTextView.setText(formattedTotalPrice);
            originalPriceTextView.setPaintFlags(originalPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        if (discountTextView != null) {
            discountTextView.setText(formattedSavings);
        }
    }

    private String formatPrice(float price) {
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#,###");
        return decimalFormat.format(price).replace(",", ".");
    }

    private float getDiscountPercentage(List<Product> cartItems) {
        int productCount = cartItems.size();
        if (productCount >= 6) {
            return 0.15f; // 15% diskon
        } else if (productCount >= 4) {
            return 0.10f; // 10% diskon
        } else if (productCount >= 1) {
            return 0.05f; // 5% diskon
        }
        return 0; // Tidak ada diskon
    }
}