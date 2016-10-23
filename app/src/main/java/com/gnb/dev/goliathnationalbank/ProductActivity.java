package com.gnb.dev.goliathnationalbank;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gnb.dev.goliathnationalbank.objects.Conversion;
import com.gnb.dev.goliathnationalbank.objects.Transaction;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.ListIterator;


public class ProductActivity extends AppCompatActivity {

    String productName = null;
    ArrayList<Transaction> transactions;
    ArrayList<Conversion> conversions;
    //MathContext ourDefaultMathContext = new MathContext(2, RoundingMode.HALF_EVEN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // get Intent and the passed extras
        Intent intent = getIntent();
        productName = getIntent().getStringExtra("product");
        transactions = (ArrayList<Transaction>) intent.getSerializableExtra("transactions");
        conversions = (ArrayList<Conversion>) intent.getSerializableExtra("rates");

        // define and inflate the RecyclerView
        RecyclerView recyleViewTransactions = (RecyclerView) findViewById(R.id.recycler_view_transactions);
        recyleViewTransactions.setAdapter(new RecycleAdapter(transactions));
        recyleViewTransactions.setLayoutManager(new LinearLayoutManager(this));

    }

    // custom RecycleView implementation
    class RecycleAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final static int HEADER_VIEW = 0; // Product and Total view
        private final static int ITEM_VIEW = 1; // Transaction view
        private final ArrayList<Transaction> transactions;

        public RecycleAdapter(ArrayList<Transaction> transactions) {
            this.transactions = transactions;
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                    return HEADER_VIEW;
                default:
                    return ITEM_VIEW;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutType = 0;
            View view = null;
            switch (viewType) {
                case HEADER_VIEW:
                    layoutType = R.layout.list_item_header_transaction;
                    view = LayoutInflater.from(parent.getContext()).inflate(layoutType, parent, false);
                    return new ViewHolderHeader(view);
                case ITEM_VIEW:
                    layoutType = R.layout.list_item_transaction;
                    view = LayoutInflater.from(parent.getContext()).inflate(layoutType, parent, false);
                    return new ViewHolderItem(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {

            if (position != 0) {
                // call the Item view bind method
                Transaction transaction = transactions.get(position - 1);
                viewHolder.bindType(transaction, position);
            } else {
                // call the Header view method view
                viewHolder.bindType();
            }

        }

        @Override
        public int getItemCount() {
            return transactions.size() + 1; // compensate for the extra element in the list -> Header view
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bindType() {
        }

        public void bindType(Transaction transaction, int position) {
        }
    }

    // Header View ViewHolder
    class ViewHolderHeader extends ViewHolder {
        private final TextView textViewProduct;
        private final TextView textViewTotalAmount;

        public ViewHolderHeader(View itemView) {
            super(itemView);
            textViewProduct = (TextView) itemView.findViewById(R.id.textViewProductName);
            textViewTotalAmount = (TextView) itemView.findViewById(R.id.textViewTotalAmount);
        }

        @Override
        public void bindType() {
            textViewProduct.setText(productName);
            textViewTotalAmount.setText(calculateTotalInEURO());
        }
    }

    // Item View ViewHolder
    class ViewHolderItem extends ViewHolder {
        private final TextView textViewAmountValue;
        private final TextView textViewCurrency;
        private final TextView textViewTransactionNumber;

        public ViewHolderItem(View itemView) {
            super(itemView);
            textViewAmountValue = (TextView) itemView.findViewById(R.id.textViewTransactionAmountValue);
            textViewCurrency = (TextView) itemView.findViewById(R.id.textViewTransactionCurrency);
            textViewTransactionNumber = (TextView) itemView.findViewById(R.id.textViewTransactionNumber);
        }

        public void bindType(Transaction transaction, int position) {
            textViewCurrency.setText(transaction.getCurrency());
            textViewAmountValue.setText(transaction.getAmount());
            textViewTransactionNumber.setText("Transaction #" + Integer.toString(position) + ": ");
        }

    }

    public String calculateTotalInEURO() {
        // Using BigDecimal to avoid rounding errors
        BigDecimal totalValue = new BigDecimal("0.00");
        for (Transaction tr : transactions) {

            if (tr.getCurrency().equals("EUR")) { // if the transaction is in EURO we add it directly to the sum
                totalValue = totalValue.add(new BigDecimal(tr.getAmount()));

            } else { // if the transaction is not in EURO we convert it to EURO
                totalValue = totalValue.add(new BigDecimal(convertCurrency(tr.getAmount(), tr.getCurrency(), "EUR")));
            }

        }
        return totalValue.toString();
    }

    public String convertCurrency(String amount, String fromCurrency, String toCurrency) {
        BigDecimal amountBigDecimal = new BigDecimal(amount);
        int index;

        // direct conversion is available in the 'conversions' array
        if ((index = conversions.indexOf(new Conversion(fromCurrency, toCurrency))) != -1) {
            BigDecimal auxBigDecimal = new BigDecimal(conversions.get(index).getRate());

            // after the operation it sets the precision to 2 digits using Banker's rounding
            return amountBigDecimal.multiply(auxBigDecimal).setScale(2, RoundingMode.HALF_EVEN).toString();

        } else { // direct conversion is not possible so it looks for derived conversions

            ListIterator<Conversion> li = conversions.listIterator();

            while (li.hasNext()) {
                Conversion iteratorConv = li.next();
                if (iteratorConv.getFrom().equals(fromCurrency)) {
                    // converts current currency to an intermediary currency
                    String auxConversion = convertCurrency(amount, fromCurrency, iteratorConv.getTo());
                    // convers the intermediary currency to EURO
                    String finalConversion = convertCurrency(auxConversion, iteratorConv.getTo(), "EUR");

                    return new BigDecimal(finalConversion).toString();
                }

            }

        }
        return null;
    }

}
