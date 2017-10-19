package server.endpoints;

import com.google.gson.Gson;
import server.controllers.UserController;
import server.models.Event;
import server.providers.EventProvider;

import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import server.controllers.ContentController;


import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import server.models.Event;
import server.providers.EventProvider;
import server.providers.PostProvider;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;



/**
 * Created by Filip on 10-10-2017.
 */

@Path("/events")
public class EventEndpoint {

    /*
    This method returns all events. To do so, the method creates an object of the EventProvider class
    and inserts this object in an ArrayList along with the user from the models package.

    The method return response status codes and converts the ArrayList "allEvents" from GSON to JSON
     */
    @GET
    public Response getAllEvents(){

        EventProvider eventProvider = new EventProvider();

        ArrayList<Event> allEvents = eventProvider.getAllEvents();

        return Response.status(200).type("text/plain").entity(new Gson().toJson(allEvents)).build();
    }

    /** This method returns one event chosen by the specific id for the event. The method creates objects of the classes EventProvider,
     * PostProvider and UserController and inserts the object for the class EventProvider in the ArrayList "Event".
     *
     * @param event_id
     *
     * @return It returns a response that converts the ArrayList from GSON to JSON
     */
    @GET
    @Path("{id}")
    public Response getEvent(@PathParam("id") int event_id){

        EventProvider eventProvider = new EventProvider();
        PostProvider postProvider = new PostProvider();
        UserController userController = new UserController();

        Event event = eventProvider.getEvent(event_id);

        //Getting all posts in the event
        event.getPosts().addAll(postProvider.getAllPostsByEventId(event_id));

        //Getting all participants in the event
        event.getParticipants().addAll(userController.getParticipants(event_id));

        return Response.status(200).type("application/json").entity(new Gson().toJson(event)).build();

    }

    /**
     *
     * @param eventJson
     * @return It returns a response with a status code 200.
     */
    @POST
    public Response createEvent(String eventJson) {

        JsonObject eventData = new Gson().fromJson(eventJson, JsonObject.class);

        Event event = new Event(
                eventData.get("owner_id").getAsInt(),
                eventData.get("title").getAsString(),
                Timestamp.valueOf(eventData.get("startDate").getAsString()),
                Timestamp.valueOf(eventData.get("endDate").getAsString()),
                eventData.get("description").getAsString()
        );
        //Creates an object of the class EventProvider
        EventProvider eventProvider = new EventProvider();

        //Creating try-catch method and if it fails to create an event, it throws and SQL exception
        try {
            eventProvider.createEvent(event);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Response.status(200).type("application/json").entity(new Gson().toJson(event)).build();


    }

    /** This method lets the user subscribe to a specific event.
     * The method converts from JSON to GSON
     *
     * @param jsonData
     * @return It returns a response with a status code 200.
     */
    @POST
    @Path("/subscribe")
    public Response subscribeToEvent(String jsonData){

        JsonObject jsonObj = new Gson().fromJson(jsonData, JsonObject.class);
        int user_id = jsonObj.get("user_id").getAsInt();
        int event_id = jsonObj.get("event_id").getAsInt();

        //Creates an object of the class EventProvider
        EventProvider eventProvider = new EventProvider();

        eventProvider.subscribeToEvent(user_id, event_id);

        return Response.status(200).type("text/plain").entity("User subscribed to event").build();

            }


}
