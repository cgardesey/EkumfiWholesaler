package com.ekumfi.wholesaler.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmPayment extends RealmObject {

    private int id;
    @PrimaryKey
    private String payment_id;
    private String msisdn;
    private String country_code;
    private String network;
    private String currency;
    private String amount;
    private String description;
    private String payment_ref;
    private String message;
    private String response_message;
    private String status;
    private String external_reference_no;
    private String transaction_status_reason;
    private String cart_id;
    private String stock_cart_id;
    private int paid;
    private String created_at;
    private String updated_at;

    private String order_id;
    private String primary_contact;
    private String shop_name;


    public RealmPayment() {

    }

    public RealmPayment(int id, String payment_id, String msisdn, String country_code, String network, String currency, String amount, String description, String payment_ref, String message, String response_message, String status, String external_reference_no, String transaction_status_reason, String cart_id, String stock_cart_id, int paid, String created_at, String updated_at, String order_id, String primary_contact, String shop_name) {
        this.id = id;
        this.payment_id = payment_id;
        this.msisdn = msisdn;
        this.country_code = country_code;
        this.network = network;
        this.currency = currency;
        this.amount = amount;
        this.description = description;
        this.payment_ref = payment_ref;
        this.message = message;
        this.response_message = response_message;
        this.status = status;
        this.external_reference_no = external_reference_no;
        this.transaction_status_reason = transaction_status_reason;
        this.cart_id = cart_id;
        this.stock_cart_id = stock_cart_id;
        this.paid = paid;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.order_id = order_id;
        this.primary_contact = primary_contact;
        this.shop_name = shop_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPayment_ref() {
        return payment_ref;
    }

    public void setPayment_ref(String payment_ref) {
        this.payment_ref = payment_ref;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse_message() {
        return response_message;
    }

    public void setResponse_message(String response_message) {
        this.response_message = response_message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExternal_reference_no() {
        return external_reference_no;
    }

    public void setExternal_reference_no(String external_reference_no) {
        this.external_reference_no = external_reference_no;
    }

    public String getTransaction_status_reason() {
        return transaction_status_reason;
    }

    public void setTransaction_status_reason(String transaction_status_reason) {
        this.transaction_status_reason = transaction_status_reason;
    }

    public String getCart_id() {
        return cart_id;
    }

    public void setCart_id(String cart_id) {
        this.cart_id = cart_id;
    }

    public String getStock_cart_id() {
        return stock_cart_id;
    }

    public void setStock_cart_id(String stock_cart_id) {
        this.stock_cart_id = stock_cart_id;
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

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getPrimary_contact() {
        return primary_contact;
    }

    public void setPrimary_contact(String primary_contact) {
        this.primary_contact = primary_contact;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }
}
