package com.example.tokopari.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.tokopari.model.TransactionItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {

    private static final String TRANSACTION_PREFS = "transaction_prefs";
    private static final String TRANSACTION_KEY = "transactions";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public TransactionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(TRANSACTION_PREFS, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void addTransaction(TransactionItem transaction) {
        List<TransactionItem> transactions = getTransactions();
        transactions.add(transaction);
        saveTransactions(transactions);
    }

    public List<TransactionItem> getTransactions() {
        String json = sharedPreferences.getString(TRANSACTION_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<TransactionItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveTransactions(List<TransactionItem> transactions) {
        String json = gson.toJson(transactions);
        sharedPreferences.edit().putString(TRANSACTION_KEY, json).apply();
    }
}
