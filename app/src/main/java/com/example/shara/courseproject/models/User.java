package com.example.shara.courseproject.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.sql.Timestamp;

@IgnoreExtraProperties
public class User {
    public String firstname,lastname, gender, location;
    public String uid, email, mobile, birthdate;
    public String ts;
    public String price;
    public String currency, category,item_title,item_desc,image_path;

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    String product;

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getItem_desc() {
        return item_desc;
    }

    public void setItem_desc(String item_desc) {
        this.item_desc = item_desc;
    }

    public String getItem_title() {
        return item_title;
    }

    public void setItem_title(String item_title) {
        this.item_title = item_title;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }


    public User() {
    }

    public User(String uid, String currency,String price,String item_title,String item_desc,String category,String image_path, String ts){
        this.uid = uid;
        this.currency = currency;
        this.price = price;
        this.item_title = item_title;
        this.item_desc = item_desc;
        this.category = category;
        this.image_path = image_path;
        this.ts = ts;
    }

    public User(String product)
    {
     this.product = product;
    }

    public User(String uid, String email, String firstname) {
        this.uid = uid;
        this.email = email;
        this.firstname = firstname;
    }

    public User(String uid, String email, String firstname, String lastname,String mobile,String birthdate, String gender, String location, String ts) {
        this.uid = uid;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.mobile = mobile;
        this.birthdate = birthdate;
        this.gender = gender;
        this.location = location;
        this.ts = ts;
    }
}
