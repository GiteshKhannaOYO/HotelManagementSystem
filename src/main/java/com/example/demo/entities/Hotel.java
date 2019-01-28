package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.searchbox.annotations.JestId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Hotel {
    @JestId
    private Long hotelId ;

    private String hotelName ;
    private Long cityId ;
    private Long stateId ;
    private String latLong ;
    private String hotelType ;
    private String addressDescription ;
    private Long totalRooms ;
    private Long price ;

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public String getLatLong() {
        return latLong;
    }

    public void setLatLong(String latLong) {
        this.latLong = latLong;
    }

    public String getHotelType() {
        return hotelType;
    }

    public void setHotelType(String hotelType) {
        this.hotelType = hotelType;
    }

    public String getAddressDescription() {
        return addressDescription;
    }

    public void setAddressDescription(String addressDescription) {
        this.addressDescription = addressDescription;
    }

    public Long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(Long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }


    @Override
    public String toString() {
        return "Hotel{" +
                "hotelId=" + hotelId +
                ", hotelName='" + hotelName + '\'' +
                ", cityId=" + cityId +
                ", stateId=" + stateId +
                ", latLong='" + latLong + '\'' +
                ", hotelType='" + hotelType + '\'' +
                ", addressDescription='" + addressDescription + '\'' +
                ", totalRooms=" + totalRooms +
                ", price=" + price +
                '}';
    }
}
