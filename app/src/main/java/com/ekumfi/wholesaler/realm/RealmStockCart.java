package com.ekumfi.wholesaler.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmStockCart extends RealmObject {

    private int id;
    @PrimaryKey
    private String stock_cart_id;
    private String order_id;
    private String seller_id;
    private double shipping_fee;
    private int delivered;
    private int paid;
    private String created_at;
    private String updated_at;

    private String status;

    private double seller_longitude;
    private double seller_latitude;
    private String shop_name;
    private int verified;
    private String shop_image_url;
    private int item_count;

    public RealmStockCart() {

    }

    public RealmStockCart(int id, String stock_cart_id, String order_id, String seller_id, double shipping_fee, int delivered, int paid, String created_at, String updated_at, String status, double seller_longitude, double seller_latitude, String shop_name, int verified, String shop_image_url, int item_count) {
        this.id = id;
        this.stock_cart_id = stock_cart_id;
        this.order_id = order_id;
        this.seller_id = seller_id;
        this.shipping_fee = shipping_fee;
        this.delivered = delivered;
        this.paid = paid;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.status = status;
        this.seller_longitude = seller_longitude;
        this.seller_latitude = seller_latitude;
        this.shop_name = shop_name;
        this.verified = verified;
        this.shop_image_url = shop_image_url;
        this.item_count = item_count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStock_cart_id() {
        return stock_cart_id;
    }

    public void setStock_cart_id(String stock_cart_id) {
        this.stock_cart_id = stock_cart_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public double getShipping_fee() {
        return shipping_fee;
    }

    public void setShipping_fee(double shipping_fee) {
        this.shipping_fee = shipping_fee;
    }

    public int getDelivered() {
        return delivered;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getSeller_longitude() {
        return seller_longitude;
    }

    public void setSeller_longitude(double seller_longitude) {
        this.seller_longitude = seller_longitude;
    }

    public double getSeller_latitude() {
        return seller_latitude;
    }

    public void setSeller_latitude(double seller_latitude) {
        this.seller_latitude = seller_latitude;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public int getVerified() {
        return verified;
    }

    public void setVerified(int verified) {
        this.verified = verified;
    }

    public String getShop_image_url() {
        return shop_image_url;
    }

    public void setShop_image_url(String shop_image_url) {
        this.shop_image_url = shop_image_url;
    }

    public int getItem_count() {
        return item_count;
    }

    public void setItem_count(int item_count) {
        this.item_count = item_count;
    }
}
