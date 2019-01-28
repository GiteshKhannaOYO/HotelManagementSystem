package com.example.demo.entities;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.searchbox.annotations.JestId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableRooms {




    @JestId
    String id ;

    String date ;
    Long availableRooms ;
    Long hotelId ;

    public String getId() {
        return id;
    }

    public void setId() {
        this.id = date + " " + Long.toString(hotelId) ;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(Long availableRooms) {
        this.availableRooms = availableRooms;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }


    @Override
    public String toString() {
        return "AvailableRooms{" +
                "date='" + date + '\'' +
                ", availableRooms=" + availableRooms +
                ", hotelId=" + hotelId +
                '}';
    }


}
