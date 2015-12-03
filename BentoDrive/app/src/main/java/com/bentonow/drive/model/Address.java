package com.bentonow.drive.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

/**
 * Created by joseguadalupetorresfuentes on 15/11/15.
 */
public class Address extends SugarRecord implements Parcelable {

    private String street = "";
    private String residence = "";
    private String city = "";
    private String region = "";
    private String zipCode = "";
    private String country = "";
    private double lat;
    private double lng;

    public Address() {
    }

    public Address(String street, String residence, String city, String region, String zipCode, String country, double lat, double lng) {
        this.street = street;
        this.residence = residence;
        this.city = city;
        this.region = region;
        this.zipCode = zipCode;
        this.country = country;
        this.lat = lat;
        this.lng = lng;
    }

    public Address(Parcel parcel) {
        street = parcel.readString();
        residence = parcel.readString();
        city = parcel.readString();
        region = parcel.readString();
        zipCode = parcel.readString();
        country = parcel.readString();
        lat = parcel.readDouble();
        lng = parcel.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(street);
        dest.writeString(residence);
        dest.writeString(city);
        dest.writeString(region);
        dest.writeString(zipCode);
        dest.writeString(country);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }

        @Override
        public Address createFromParcel(Parcel source) {
            return new Address(source);
        }
    };


    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
