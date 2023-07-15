package com.ekumfi.wholesaler.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmWholesalerProduct extends RealmObject {

    private int id;
    @PrimaryKey
    private String wholesaler_product_id;
    private String product_id;
    private String wholesaler_id;
    private int unit_quantity;
    private double unit_price;
    private int quantity_available;
    private String created_at;
    private String updated_at;

    private String product_name;
    private String product_image_url;

    private String shop_name;
    private String shop_image_url;
    private double longitude;
    private double latitude;
    private int verified;
    private String availability;

    public RealmWholesalerProduct() {

    }

    public RealmWholesalerProduct(int id, String wholesaler_product_id, String product_id, String wholesaler_id, int unit_quantity, double unit_price, int quantity_available, String created_at, String updated_at, String product_name, String product_image_url) {
        this.id = id;
        this.wholesaler_product_id = wholesaler_product_id;
        this.product_id = product_id;
        this.wholesaler_id = wholesaler_id;
        this.unit_quantity = unit_quantity;
        this.unit_price = unit_price;
        this.quantity_available = quantity_available;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.product_name = product_name;
        this.product_image_url = product_image_url;
    }

    public RealmWholesalerProduct(int id, String wholesaler_product_id, String product_id, String wholesaler_id, int unit_quantity, double unit_price, int quantity_available, String created_at, String updated_at, String product_name, String shop_name, String shop_image_url, double longitude, double latitude, int verified, String availability) {
        this.id = id;
        this.wholesaler_product_id = wholesaler_product_id;
        this.product_id = product_id;
        this.wholesaler_id = wholesaler_id;
        this.unit_quantity = unit_quantity;
        this.unit_price = unit_price;
        this.quantity_available = quantity_available;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.product_name = product_name;
        this.shop_name = shop_name;
        this.shop_image_url = shop_image_url;
        this.longitude = longitude;
        this.latitude = latitude;
        this.verified = verified;
        this.availability = availability;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWholesaler_product_id() {
        return wholesaler_product_id;
    }

    public void setWholesaler_product_id(String wholesaler_product_id) {
        this.wholesaler_product_id = wholesaler_product_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getWholesaler_id() {
        return wholesaler_id;
    }

    public void setWholesaler_id(String wholesaler_id) {
        this.wholesaler_id = wholesaler_id;
    }

    public int getUnit_quantity() {
        return unit_quantity;
    }

    public void setUnit_quantity(int unit_quantity) {
        this.unit_quantity = unit_quantity;
    }

    public double getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(double unit_price) {
        this.unit_price = unit_price;
    }

    public int getQuantity_available() {
        return quantity_available;
    }

    public void setQuantity_available(int quantity_available) {
        this.quantity_available = quantity_available;
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

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_image_url() {
        return product_image_url;
    }

    public void setProduct_image_url(String product_image_url) {
        this.product_image_url = product_image_url;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getShop_image_url() {
        return shop_image_url;
    }

    public void setShop_image_url(String shop_image_url) {
        this.shop_image_url = shop_image_url;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getVerified() {
        return verified;
    }

    public void setVerified(int verified) {
        this.verified = verified;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}
