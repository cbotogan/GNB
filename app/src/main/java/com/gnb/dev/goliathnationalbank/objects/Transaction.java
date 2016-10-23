package com.gnb.dev.goliathnationalbank.objects;

import java.io.Serializable;

/**
 * Created by Cristian on 22.10.2016.
 */

public class Transaction implements Serializable {
    private String product;
    private String amount;
    private String currency;

    public Transaction(String product, String amount, String currency) {
        this.product = product;
        this.amount = amount;
        this.currency = currency;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
