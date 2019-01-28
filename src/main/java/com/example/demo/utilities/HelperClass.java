package com.example.demo.utilities;

import com.example.demo.entities.AvailableRooms;
import com.example.demo.entities.Hotel;
import com.example.demo.entities.User;
import javafx.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.Math.max;

public class HelperClass {


     enum UpdatePayload {
        DOC("doc"),
        DOC_AS_UPSERT("doc_as_upsert");

        private final String key;

        UpdatePayload(String key) {
            this.key = key;
        }
    }

    public Integer getMaximum(Integer... args) {
        Integer max_till_now = args[0] ;

        for (Integer arg : args) {
            max_till_now = max(max_till_now,arg) ;
        }

        return max_till_now ;
    }

    public String fetchCurrentDateParas(){
        String result ;
        Date currDate = new Date() ;
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd");
        SimpleDateFormat formatterHour = new SimpleDateFormat("HH") ;
        SimpleDateFormat formatterSec = new SimpleDateFormat("ss") ;
         result = formatterDate.format(currDate) ;
         result = result + formatterHour.format(currDate) ;
         result = result + formatterSec.format(currDate) ;

         return result ;

    }

    public String fetchBIdAlphas(Hotel hotel, User user){
        String hotelFirstTwo = (hotel.getHotelName()).substring(0,2) ;
        String userFirstTwo = (user.getName()).substring(0,2) ;
        return (hotelFirstTwo + userFirstTwo).toUpperCase() ;
    }

    public String fetchCInCOutParas(String cIn, String cOut){


        String resultDateIn =   cIn.substring(8,10) ;
        String resultDateOut = cOut.substring(8,10) ;


        String result = Long.toString(Long.parseLong(resultDateIn) + Long.parseLong(resultDateOut));;

        return result ;
    }


    public Long fetchDateDifference(String cIn, String cOut) throws ParseException {


        String timeIn = cIn.substring(0,10) ;
        String timeOut = cOut.substring(0,10) ;

        LocalDate dateBefore = LocalDate.parse(timeIn);
        LocalDate dateAfter = LocalDate.parse(timeOut);

        //calculating number of days in between
        Long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);

        return noOfDaysBetween ;

    }
    public <T> Map<String, Object> getUpdatePayload(T entity, boolean upsert) {

        Map<String, Object> payload = new HashMap<>();
        payload.put(UpdatePayload.DOC.key, entity);
        payload.put(UpdatePayload.DOC_AS_UPSERT.key, upsert);
        return payload;
    }


    public String changeCalendarToString(Calendar date){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date.getTime()) ;

    }

    public Boolean updateAlreadyExistingPair(List<Pair<Long,Long>> cityTop , Pair<Long,Long> hotelBooking){
        for(Pair<Long,Long> x : cityTop){
            if( x.getKey() == hotelBooking.getKey() && x.getValue()!=hotelBooking.getValue()){

                x = hotelBooking ;
                return true ;
            }
        }

        return false ;
    }

    public static Comparator<Pair<Long,Long>> pairCompare = new Comparator<Pair<Long,Long>>() {

        public int compare(Pair<Long,Long> s1, Pair<Long,Long> s2) {

            Long num1 = s1.getValue();
            Long num2 = s2.getValue() ;

            /*For ascending order*/
            return (int)(num1-num2) ;
        }
     };
}
