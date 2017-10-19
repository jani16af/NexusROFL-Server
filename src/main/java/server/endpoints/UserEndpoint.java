package server.endpoints;


import com.google.gson.Gson;
import server.models.User;
import server.providers.EventProvider;
import server.providers.PostProvider;
import server.providers.UserProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import server.controllers.UserController;


import java.sql.SQLException;

/**
 * Created by Marc & Mikkel on 17-10-2017.
 *
 * This is our user endpoint, which handles the input given from the client regarding user-creation.
 */
@Path("/users")
public class UserEndpoint {

    //Creates object of the two classes UserProvider and UserController
    UserProvider userProvider = new UserProvider();
    UserController userController = new UserController();

    /*
    This method returns all users. To do so, the method creates an object of the UserProvider class
    and inserts this object in an ArrayList along with the user from the models package.

    Return response converts the ArrayList "allUsers" from GSON to JSON
     */
    @GET
    public Response getAllUsers() {

        ArrayList<User> allUsers = userProvider.getAllUsers();

        return Response.status(200).type("application/json").entity(new Gson().toJson(allUsers)).build();

    }

    /** This method returns one user by the users specific id.
     *
     * @param user_id
     * @return The method returns a response that converts the "user" from GSON to JSON.
     */
    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") int user_id){

        //Creates objects of the classes EventProvider and PostProvider
        EventProvider eventProvider = new EventProvider();
        PostProvider postProvider = new PostProvider();

        User user = userProvider.getUser(user_id);

        //Adding all events and posts to a specific user
        user.getEvents().addAll(eventProvider.getEventByUserId(user_id));

        user.getPosts().addAll(postProvider.getPostByUserId(user_id));

        return Response.status(200).type("application/json").entity(new Gson().toJson(user)).build();
    }
  
  
    /**
     * This method lets the client create a new user. The parameters catches the specific input from the client.
     * The Endpoint creates a User object using the parameters stated below.
     * The User object is validated in UserController to makes that it is fitted for the database
     * The Endpoint throws 3 different Reponses, Statuscode: 201 (Succesful user creation), 400 (Wrong input by client), 501 (Database Error).
     */

    @POST
    public Response createUser(String jsonUser) {

        User createdUser;
        try {
            createdUser = new Gson().fromJson(jsonUser, User.class);
        } catch (IllegalArgumentException e) {
            System.out.print(e.getMessage());
            return Response.status(400).build();
        }

        try {

            /**
             * "validateGendeInput" is called to make sure the String gender is no longer than 1 character.
             * This way you can't register as male by inputting 'male' instead of 'm'
             */
            createdUser = userController.validateUserCreation(createdUser.getPassword(), createdUser.getFirstName(),
                    createdUser.getLastName(), createdUser.getEmail(), createdUser.getGender(),
                    createdUser.getDescription(), createdUser.getMajor(), createdUser.getSemester());
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            return Response.status(400).build();
        }
        try {
            userProvider.createUser(createdUser);
        } catch (SQLException e) {
            return Response.status(501).type("text/plain").entity("Server couldn't store the validated user object (SQL Error)").build();

        }

        return Response.status(201).type("text/plain").entity("User Created").build();
        
        }
    }

