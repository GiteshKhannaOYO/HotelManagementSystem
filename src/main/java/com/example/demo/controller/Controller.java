package com.example.demo.controller;


import com.example.demo.entities.*;
import com.example.demo.services.HotelService;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
public class Controller {

    @Autowired
    private HotelService hs ;


    @PostMapping("/addhotel")
    public Boolean addHotel(@RequestBody Hotel hotel) throws IOException {
        hs.newHotelServ(hotel);
        return true;
    }

    @PostMapping("/addcity")
    public Boolean addCity(@RequestBody City city) throws IOException {
        hs.newCityServ(city);
        return true;
    }

    @PostMapping("/addstate")
    public Boolean addState(@RequestBody State state) throws IOException {
        hs.newStateServ(state);
        return true;
    }

    @PostMapping("/adduser")
    public Boolean addHotel(@RequestBody User user) throws IOException {
        hs.newUserServ(user);
        return true;
    }

    @PostMapping("/updatehotel")
    public Boolean updateHotel(@RequestParam("hotelId") Long hotelId, @RequestBody Hotel hotel) throws IOException{
        if(hs.updateHotelServ(hotelId,hotel)) {
            System.out.println(hotel.toString()) ;
            return true;
        }
        else
            return false ;
    }


    @GetMapping("/searchhotelbyname")
    public Boolean searchServ(@RequestParam("hotelName") String hotelName) throws IOException {
        hs.searchServ(hotelName) ;
        return true ;
    }

    @PostMapping("/bookhotel")
    public String bookHotel(@RequestParam("hotelId") Long hotelId,
                             @RequestParam("userId") Long userId,
                             @RequestParam("checkIn") String checkIn,
                             @RequestParam("checkOut") String checkOut) throws IOException, ParseException {


        if(!hs.newBookingServ(hotelId, userId, checkIn, checkOut))
            return "Rooms unavailable" ;


        return "Rooms succesfully booked" ;

    }

    @GetMapping("/bookingsondate")
    public Boolean searchBookingDate(@RequestParam("date") String date) throws IOException {
        List<Booking> articles = hs.getBookingsByDate(date) ;

        for(Booking b : articles){
            System.out.println(b.toString()) ;
        }


        return true ;

    }

    @GetMapping("/bookingsondatehotel")
    public Boolean searchBookingDateHotel(@RequestParam("date") String date,@RequestParam("hotelId") Long hotelId) throws IOException {
        List<Booking> articles = hs.getBookingsByDateHotelId(date, hotelId);
        for (Booking b : articles) {
            System.out.println(b.toString());
        }

        return true ;
    }

    @GetMapping("/bookingsondobhotel")
    public Boolean searchBookingDobHotel(@RequestParam("date") String date,@RequestParam("hotelId") Long hotelId) throws IOException {
        List<Booking> articles = hs.getBookingsOnDobHotelId(date, hotelId);
        for (Booking b : articles) {
            System.out.println(b.toString());
        }

        return true ;
    }


    @GetMapping("/bookingsondateuser")
    public Boolean searchBookingDateUser(@RequestParam("date") String date,@RequestParam("userId") Long userId) throws IOException {
        List<Booking> articles = hs.getBookingsByDateHotelId(date, userId);
        for (Booking b : articles) {
            System.out.println(b.toString());
        }
        return true ;
    }

    @GetMapping("/getAllHotels")
    public Boolean getAllHotels() throws IOException {
        List<Hotel> hotels = hs.getAllEntities(Hotel.class,"hotel","hoteldoc") ;

        for(Hotel h : hotels) {
            System.out.println(h.toString()) ;
        }

        return true ;
    }

    @GetMapping("/getAllBookings")
    public Boolean getAllBookings() throws IOException {
        List<Booking> bookings = hs.getAllEntities(Booking.class,"booking","bookingdoc") ;

        for(Booking b : bookings) {
            System.out.println(b.toString()) ;
        }

        return true ;
    }


    @GetMapping("/getHotelById")
    public Boolean getHotelById(@RequestParam("hotelId") Long id) throws IOException {
        Hotel h = hs.getById(Hotel.class,"hotel","hoteldoc","hotelId",id) ;
        System.out.println(h.toString()) ;
        return true ;
    }

    @GetMapping("/getHotelByIds")
    public List<Hotel> getHotelByIds(@RequestParam("hotelId") Set<Long> id) throws IOException {
        List<Hotel> hotels = hs.getByIds(Hotel.class,"hotel","hoteldoc","hotelId",id) ;

        for(Hotel h : hotels)
        System.out.println(h.toString()) ;

        return hotels ;
    }


    @GetMapping("/getCityById")
    public Boolean getCityById(@RequestParam("cityId") Long id) throws IOException {
        City c = hs.getById(City.class,"city","citydoc","cityId",id) ;
        System.out.println(c.toString()) ;
        return true ;
    }

    @GetMapping("/getCityByIds")
    public List<City> getCityByIds(@RequestParam("cityId") Set<Long> id) throws IOException {
        List<City> cities = hs.getByIds(City.class,"city","citydoc","cityId",id) ;

        for(City c : cities)
            System.out.println(c.toString()) ;

        return cities ;
    }

    @GetMapping("/getUserById")
    public Boolean getUserById(@RequestParam("userId") Long id) throws IOException {
        User u = hs.getById(User.class,"user","userdoc","userId",id) ;
        System.out.println(u.toString()) ;
        return true ;
    }

    @GetMapping("/getUserByIds")
    public List<User> getUserByIds(@RequestParam("userId") Set<Long> id) throws IOException {
        List<User> users = hs.getByIds(User.class,"user","userdoc","userId",id) ;

        for(User u : users)
            System.out.println(u.toString()) ;

        return users ;
    }

    @GetMapping("/getStateById")
    public Boolean getStateById(@RequestParam("stateId") Long id) throws IOException {
        State s = hs.getById(State.class,"state","statedoc","stateId",id) ;
        System.out.println(s.toString()) ;
        return true ;
    }

    @GetMapping("/getStateByIds")
    public List<State> getStateByIds(@RequestParam("stateId") Set<Long> id) throws IOException {
        List<State> states = hs.getByIds(State.class,"state","statedoc","stateId",id) ;

        for(State s: states)
            System.out.println(s.toString()) ;

        return states ;
    }

    @GetMapping("/getBookingById")
    public Boolean getBookingById(@RequestParam("bookingId") String id) throws IOException {
        Booking b = hs.getBookingById(id) ;
        System.out.println(b.toString()) ;
        return true ;
    }

    @GetMapping("/getBookingByIds")
    public List<Booking> getBookingsByIds(@RequestParam("bookingId") Set<Long> id) throws IOException {
        List<Booking> bookings = hs.getByIds(Booking.class,"booking","bookingdoc","bookingId",id) ;

        for(Booking b: bookings)
            System.out.println(b.toString()) ;

        return bookings ;
    }

    @GetMapping("/trendingHotelsPrinter")
    public Map<Long,ArrayList<Pair<Long,Long>>> trendingHotels() throws IOException {
        return hs.getTrendingHotels() ;
    }

    @GetMapping("/trendingHotels")
    public boolean trendingHotelsPrinter() throws IOException {
        hs.trendingHotels();
        return true ;
    }

    @PostMapping("/cancelbooking")
    public boolean cancelBooking(@RequestParam("bookingId") String bId) throws IOException, ParseException {
        hs.cancelBooking(bId);
        return true ;
    }



}
