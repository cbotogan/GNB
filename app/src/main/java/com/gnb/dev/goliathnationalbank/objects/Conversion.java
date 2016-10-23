package com.gnb.dev.goliathnationalbank.objects;

import java.io.Serializable;

/**
 * Created by Cristian on 22.10.2016.
 */

public class Conversion implements Serializable{
    private String from, to;
    private String rate;

    public Conversion(String from, String to, String rate) {
        this.from = from;
        this.to = to;
        this.rate = rate;
    }

    public Conversion(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Conversion conversion = (Conversion) o;

        if (from != null ? !from.equals(conversion.from) : conversion.from != null) return false;
        return to != null ? to.equals(conversion.to) : conversion.to == null;

    }

    @Override
    public int hashCode() {
        return 0;
    }
}
