package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.utilities.HelperClass;
import com.example.demo.utilities.JestConnector;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import javafx.util.Pair;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


import static com.example.demo.utilities.HelperClass.pairCompare;
import static org.elasticsearch.common.unit.Fuzziness.AUTO;

@EnableScheduling
@Service
public class HotelService{

    @Autowired
    private RedisService rs ;
    private JestClient jc = (JestClient) (new JestConnector()).makeJestClient();
    private HelperClass hc = new HelperClass();
    private final String fixedCheckOutTime = "11:00" ;
    private final int numTrendingHotels = 2  ;

//    @PostConstruct
//    public void test(){
//        System.out.println("Bean constructor working");
//    }


    public SearchResult searchResult( String indexName, String indexType,QueryBuilder queryBuilder) throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String searchString = searchSourceBuilder.query(queryBuilder).size(1000).toString();

        Search search = new Search.Builder(searchString).
                addIndex(indexName).
                addType(indexType).
                build() ;
        SearchResult result = jc.execute(search) ;

        return result ;

    }


    public <T> List<T> getAllEntities(Class<T> type,String indexName, String indexType) throws IOException {

        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery() ;
        SearchResult result = searchResult(indexName,indexType,queryBuilder) ;

        List<T> articles = result.getSourceAsObjectList(type,false);

        return articles ;
    }

    public <T,K> T getById(Class<T> type,String indexName, String indexType,String paraName,K id) throws IOException {


        QueryBuilder queryBuilder = QueryBuilders.matchQuery(paraName,id) ;
        SearchResult result = searchResult(indexName, indexType,queryBuilder) ;
        T article = result.getSourceAsObject(type,false);

        return article ;
    }

    public <T,K> List<T> getByIds(Class<T> type, String indexName, String indexType, String paraName, Set<K> ids) throws IOException{

        List<T> articles = new ArrayList<T>() ;

        for(K id : ids) {
            QueryBuilder queryBuilder = QueryBuilders.matchQuery(paraName, id);
            SearchResult result = searchResult(indexName, indexType, queryBuilder);
            T article = result.getSourceAsObject(type, false);
            articles.add(article) ;
        }
        return articles ;
    }

    public Booking getBookingById(String id) throws IOException {
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("bookingId",id) ;
        SearchResult result = searchResult("booking", "bookingdoc",queryBuilder) ;
        Booking article = result.getSourceAsObject(Booking.class,false);

        return article ;
    }

    public Boolean newHotelServ(Hotel hotel) throws IOException {
        Index index = new Index.Builder(hotel).index("hotel").type("hoteldoc").build();
        jc.execute(index);
        return true ;
    }

    public Boolean newCityServ(City newCity) throws IOException {
        Index index = new Index.Builder(newCity).index("city").type("citydoc").build();
        jc.execute(index);
        return true ;
    }

    public Boolean newStateServ(State newState) throws IOException {

        Index index = new Index.Builder(newState).index("state").type("statedoc").build();
        jc.execute(index);
        return true ;
    }

    public Boolean newUserServ(User user)throws IOException{
        Index index = new Index.Builder(user).index("user").type("userdoc").build();
        jc.execute(index);
        return true ;
    }

    public Boolean newAvailableRoomsServ(AvailableRooms availrooms)throws IOException{
        Index index = new Index.Builder(availrooms).index("availablerooms").type("availableroomsdoc").build();
        jc.execute(index);
        return true ;
    }



    public Boolean updateHotelServ(Long hId,Hotel hotel) throws IOException {

        List<Hotel> hotelList = getAllEntities(Hotel.class,"hotel","hoteldoc") ;

        List<AvailableRooms> availableRooms = getAvailRoomsByHotel(hId) ;

        for(Hotel x : hotelList){

                Hotel earlierHotel = getById(Hotel.class,"hotel","hoteldoc","hotelId",hId) ;
                Long changeInRooms = hotel.getTotalRooms() - earlierHotel.getTotalRooms() ;

                for(AvailableRooms ar : availableRooms){
                    //If the change in rooms is supported by availablerooms
                    if(ar.getAvailableRooms() + changeInRooms > 0){
                        ar.setAvailableRooms(ar.getAvailableRooms()+changeInRooms);
                    }
                    else {
                        System.out.println("Some rooms are already booked, can't decrease the number of rooms.") ;
                        return false;
                    }
                }


            //Updating AvailableRooms at elasticSearch
            for(AvailableRooms ar : availableRooms) {
                updateAvailableRoomsServ(ar);
            }

            //Updating hotel at elasticSearch
            Update updateAction = new Update.Builder(hc.getUpdatePayload(hotel, false))
                    .index("hotel")
                    .type("hoteldoc")
                    .id(Long.toString(hId))
                    .build();

            jc.execute(updateAction);

            return true ;
        }

        return false ;


    }


    public Boolean updateAvailableRoomsServ(AvailableRooms ar) throws IOException {
        Update updateAction = new Update.Builder(hc.getUpdatePayload(ar, false))
                .index("availablerooms")
                .type("availableroomsdoc")
                .id(ar.getDate() + " " + Long.toString(ar.getHotelId()))
                .build() ;
        jc.execute(updateAction) ;

        return true ;
    }

    public Boolean updateBookingServ(Booking b) throws IOException {
        Update updateAction = new Update.Builder(hc.getUpdatePayload(b, false))
                .index("booking")
                .type("bookingdoc")
                .id(b.getBookingId())
                .build() ;
        jc.execute(updateAction) ;

        return true ;
    }



    public List<Booking> getBookingsByDate(String date) throws IOException {

        QueryBuilder rangeQuery = QueryBuilders.rangeQuery("checkIn").lte(date);
        QueryBuilder rangeQuery1= QueryBuilders.rangeQuery("checkOut").gte(date);
        QueryBuilder matchQuery  = QueryBuilders.matchQuery("status", "Active" );

        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(rangeQuery).must(rangeQuery1).must(matchQuery);


       SearchResult result = searchResult("booking","bookingdoc",queryBuilder) ;

        List<Booking> articles = result.getSourceAsObjectList(Booking.class,false);

        return articles ;
    }

    public List<Booking> getBookingsOnDob(String date) throws IOException {

        String lowerDate = date + " 00:00" ;
        String upperDate = date + " 23:59" ;

        QueryBuilder rangeLower = QueryBuilders.rangeQuery("dob").gte(lowerDate) ;
        QueryBuilder rangeUpper = QueryBuilders.rangeQuery("dob").lte(upperDate) ;

        QueryBuilder matchQuery2  = QueryBuilders.matchQuery("status", "Active" );

        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(rangeLower).must(rangeUpper).must(matchQuery2) ;


        SearchResult result = searchResult("booking","bookingdoc",queryBuilder) ;

        List<Booking> articles = result.getSourceAsObjectList(Booking.class,false);

        return articles ;
    }

    public List<AvailableRooms> getAvailableRoomsByCheckInCheckOut(String checkIn, String checkOut) throws IOException {
        QueryBuilder rangeQuery = QueryBuilders.rangeQuery("date").lt(checkOut).gte(checkIn);
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(rangeQuery) ;

        SearchResult result = searchResult("availablerooms","availableroomsdoc",queryBuilder) ;

        List<AvailableRooms> articles = result.getSourceAsObjectList(AvailableRooms.class,false);

        return articles ;
    }



    public List<Booking> getBookingsByDateHotelId(String date,Long hotelId) throws IOException {
       List<Booking> bookings =  getBookingsByDate(date) ;
       List<Booking> finalBookings = new ArrayList<>() ;

       for(Booking b : bookings){
           if(b.getHotelId() == hotelId){
               finalBookings.add(b) ;
           }
       }

       return finalBookings ;

    }

    public List<Booking> getBookingsOnDobHotelId(String date,Long hotelId) throws IOException {
        List<Booking> bookings =  getBookingsOnDob(date) ;
        List<Booking> finalBookings = new ArrayList<>() ;

        for(Booking b : bookings){
            if(b.getHotelId() == hotelId){
                finalBookings.add(b) ;
            }
        }

        return finalBookings ;

    }



    public List<Booking> getBookingsByDateUser(String date,Long userId) throws IOException {
        List<Booking> bookings =  getBookingsByDate(date) ;
        List<Booking> finalBookings = new ArrayList<>() ;

        for(Booking b : bookings){
            if(b.getUserId() == userId){
                finalBookings.add(b) ;
            }
        }

        return finalBookings ;

    }




/*
    //Search using query
    public Boolean searchServ(String query) throws IOException{
        Search search = new Search.Builder(query)
                // multiple index or types can be added.
                .addIndex("hotelmanagement")
                .addType("hotel")
                .build();

        SearchResult result = jc.execute(search);
        List<Hotel> articles = result.getSourceAsObjectList(Hotel.class,false);
        for(Hotel h : articles){
            System.out.println(h.toString()) ;
        }
        return true ;
    }

  */
//Searching city through id
    public City citySearchServ(Long id) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("cityId",id)) ;
        Search search = new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added.
                .addIndex("city")
                .addType("citydoc")
                .build();

        SearchResult result = jc.execute(search);
        City article = result.getSourceAsObject(City.class,false);

        return article ;

    }


    public AvailableRooms getAvailRoomsByDate(String date) throws IOException {

        QueryBuilder queryBuilder = QueryBuilders.matchQuery("date",date) ;


        SearchResult result = searchResult("availablerooms", "availableroomsdoc",queryBuilder) ;
        AvailableRooms article = result.getSourceAsObject(AvailableRooms.class,false);

        return article ;

    }

    public List<AvailableRooms> getAvailRoomsByHotel(Long id) throws IOException {

        QueryBuilder queryBuilder = QueryBuilders.matchQuery("hotelId",id) ;


        SearchResult result = searchResult("availablerooms", "availableroomsdoc",queryBuilder) ;
        List<AvailableRooms> article = result.getSourceAsObjectList(AvailableRooms.class,false);

        return article ;

    }


    public Boolean existAvailableRoomsByDateHotel(String date, Long hotelId) throws IOException {

        QueryBuilder dateMatcher = QueryBuilders.matchQuery("date",date) ;
        QueryBuilder hotelMatcher = QueryBuilders.matchQuery("hotelId",hotelId) ;
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(dateMatcher).must(hotelMatcher) ;

        SearchResult result = searchResult("availablerooms","availableroomsdoc",queryBuilder) ;
        List<AvailableRooms> articles = result.getSourceAsObjectList(AvailableRooms.class,false);

        return articles.size() == 1 ? true : false ;

    }

    public Boolean searchServ(String hotelName) throws IOException {


        QueryBuilder matchQuery = QueryBuilders.matchQuery("hotelName",hotelName).fuzziness(AUTO) ;
        QueryBuilder prefixQuery = QueryBuilders.prefixQuery("hotelName",hotelName) ;

        QueryBuilder queryBuilder = QueryBuilders.boolQuery().should(matchQuery).should(prefixQuery) ;
        SearchResult result = searchResult("hotel", "hoteldoc",queryBuilder) ;

        List<Hotel> articles = result.getSourceAsObjectList(Hotel.class,false);
        for(Hotel h : articles){
            System.out.print(h.toString());
        }

        System.out.println() ;

        return true ;
    }

    public List<Hotel> getHotelByCityAndState(Long cityId,Long stateId ) throws IOException {

        QueryBuilder matchQuery = QueryBuilders.boolQuery().
                must(QueryBuilders.matchQuery("cityId", cityId)).
                must(QueryBuilders.matchQuery("stateId", stateId));

        SearchResult result = searchResult("hotel", "hoteldoc",matchQuery) ;
        List<Hotel> articles = result.getSourceAsObjectList(Hotel.class,false);

        return articles ;


    }

    public Boolean newBookingServ(Long hotelId,
                                  Long userId,
                                  String checkIn,
                                  String checkOut) throws IOException, ParseException {


        Long total_amount ;

        Hotel myHotelData = getById(Hotel.class,"hotel","hoteldoc","hotelId",hotelId) ;


        List<AvailableRooms> alreadyFormedAvailableRooms = getAvailableRoomsByCheckInCheckOut(checkIn.substring(0,10),
                                                                                            checkOut.substring(0,10)) ;


            //Checking if on a particular day b/w checkIn & checkOut , there is room unavailability
            for (AvailableRooms ar : alreadyFormedAvailableRooms) {

                if (ar.getAvailableRooms() <= 0 &&
                ar.getHotelId() == myHotelData.getHotelId())
                    return false;
            }



        //Updating already formed Available rooms
        for(AvailableRooms ar : alreadyFormedAvailableRooms ){
            ar.setAvailableRooms(ar.getAvailableRooms()-1);
            updateAvailableRoomsServ(ar) ;
        }


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String justDateCheckIn = checkIn.substring(0,10) ;
        String justDateCheckOut = checkOut.substring(0,10) ;
        Date dateCheckIn = formatter.parse(justDateCheckIn) ;
        Date dateCheckOut = formatter.parse(justDateCheckOut) ;

        Calendar calCheckIn = Calendar.getInstance() ;
        Calendar calCheckOut = Calendar.getInstance() ;
        calCheckIn.setTime(dateCheckIn);
        calCheckOut.setTime(dateCheckOut);


        Calendar calIterator = calCheckIn ;
        //Creating new AvailableRooms object for every non existing ones
        while(calIterator.before(calCheckOut)){

            String stringDateIterator =  hc.changeCalendarToString(calIterator) ;

            if(!existAvailableRoomsByDateHotel(stringDateIterator,hotelId)){
                AvailableRooms avail_rooms = new AvailableRooms() ;
                avail_rooms.setHotelId(hotelId);
                avail_rooms.setAvailableRooms(myHotelData.getTotalRooms()-1);
                avail_rooms.setDate(stringDateIterator.substring(0,10)); //Passing just the date part so that es picks date mapping and since the rooms are always booked for a round value of one day
                avail_rooms.setId();
                newAvailableRoomsServ(avail_rooms) ;
            }else{
                AvailableRooms article = getById(AvailableRooms.class,"availablerooms","availableroomsdoc","id",stringDateIterator+" "+Long.toString(hotelId)) ;
                article.setAvailableRooms(article.getAvailableRooms()-1);
                updateAvailableRoomsServ(article) ;
            }

            calIterator.add(Calendar.DATE,1) ;
        }


        //Creating a document for booking
            Booking booking = new Booking();
            booking.setHotelId(hotelId);
            booking.setUserId(userId);
            booking.setCheckIn(checkIn);
            booking.setCheckOut(checkOut.substring(0,10) + " " + fixedCheckOutTime);


            //Calculating amount
            total_amount = hc.fetchDateDifference(checkIn, checkOut) * myHotelData.getPrice() ;
            booking.setAmount(total_amount);


        Random rand = new Random();
        int random_value = rand.nextInt(50);

        //Creating booking id
            String bId = hc.fetchBIdAlphas(getById(Hotel.class,"hotel","hoteldoc","hotelId",hotelId),
                                            getById(User.class,"user","userdoc","userId",userId))
                    + hc.fetchCurrentDateParas()
                    + hc.fetchCInCOutParas(checkIn,checkOut)
                    + String.valueOf(random_value);

            booking.setBookingId(bId);
            booking.setStatus(Booking.Status.Active);


//            updateHotelServ(myHotelData);

            SimpleDateFormat dateHourMinuteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            booking.setDob(dateHourMinuteFormat.format(new Date())) ;

            //Printing
            System.out.println(booking.toString());

            Index index = new Index.Builder(booking).index("booking").type("bookingdoc").build();
            jc.execute(index);

            return true ;
    }


    @Scheduled(fixedRate = 5000)
    @PostConstruct
    public void trendingHotels() throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String current_date = formatter.format(new Date());

        List<Hotel> allHotels = getAllEntities(Hotel.class, "hotel", "hoteldoc");
        Map<Long,ArrayList<Pair<Long,Long>>> mapping = new HashMap<Long,ArrayList<Pair<Long,Long>>>();

        //Adding all cities
        List<City> cities = getAllEntities(City.class,"city","citydoc") ;

        //Adding cities which do not exist in the map
        for(City x : cities){
            if(!mapping.containsKey(x.getCityId())){
                ArrayList<Pair<Long,Long>> newList = new ArrayList<Pair<Long,Long>>() ;
                mapping.put(x.getCityId(),newList) ;
            }
        }


        for (Hotel h : allHotels) {
            List<Booking> bookings = getBookingsOnDobHotelId(current_date, h.getHotelId());
            Long cityId = h.getCityId() ;
            //System.out.println(new Long(bookings.size())) ;
            Pair<Long,Long> hotelBookings = new Pair<Long,Long>(h.getHotelId(),new Long(bookings.size())) ;

            //Getting the list of hotels in the city which have maximum booking.
            ArrayList<Pair<Long,Long>> cityTop = mapping.get(cityId) ;

            if(cityTop.isEmpty()){
                if(hotelBookings.getValue() > 0)
                cityTop.add(hotelBookings) ;
            }
            else{
                        if(cityTop.get(0).getValue() < hotelBookings.getValue() && cityTop.size() == numTrendingHotels){
                            cityTop.remove(0) ;
                        }
                        if(cityTop.size() < numTrendingHotels) {
                            if(hotelBookings.getValue() > 0)
                            cityTop.add(hotelBookings);
                        }

            }

            cityTop.sort(pairCompare);
            mapping.put(cityId,cityTop) ;
        }


        rs.hmset("TrendingHotels",mapping,15);
        System.out.println(mapping.toString()) ;
    }

    public Map<Long,ArrayList<Pair<Long,Long>>> getTrendingHotels() throws IOException {
        return rs.hgetall("TrendingHotels") ;
    }

    public Boolean cancelBooking(String bookingId) throws ParseException, IOException {

        Booking booking = getById(Booking.class,"booking","bookingdoc","bookingId",bookingId) ;

        if(booking==null || booking.getStatus()==Booking.Status.Inactive)
            return false ;

        booking.setStatus(Booking.Status.Inactive);
        updateBookingServ(booking) ;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String justDateCheckIn = booking.getCheckIn().substring(0,10) ;
        String justDateCheckOut = booking.getCheckOut().substring(0,10) ;
        Date dateCheckIn = formatter.parse(justDateCheckIn) ;
        Date dateCheckOut = formatter.parse(justDateCheckOut) ;

        Calendar calCheckIn = Calendar.getInstance() ;
        Calendar calCheckOut = Calendar.getInstance() ;
        calCheckIn.setTime(dateCheckIn);
        calCheckOut.setTime(dateCheckOut);

        Calendar calIterator = calCheckIn ;

        while(calIterator.before(calCheckOut)){
            Date calDate =  calIterator.getTime();
            String iterator = formatter.format(calDate) ;

            AvailableRooms ar = getById(AvailableRooms.class,"availablerooms","availableroomsdoc","id",iterator+" "+booking.getHotelId()) ;
            ar.setAvailableRooms(ar.getAvailableRooms()+1);
            updateAvailableRoomsServ(ar) ;

            calIterator.add(Calendar.DATE,1) ;
        }

        return true ;
    }



}
