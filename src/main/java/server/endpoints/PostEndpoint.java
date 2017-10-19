package server.endpoints;



import com.google.gson.Gson;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;


import com.google.gson.JsonObject;
import server.controllers.ContentController;

import server.models.Post;
import server.providers.PostProvider;


import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.sql.*;
import java.util.ArrayList;



/**
 * Created by Filip on 10-10-2017.
 */
@Path("/posts")
public class PostEndpoint {
    ContentController contentController = new ContentController();

    /*
    This method returns all posts. To do so, the method creates an object of the PostProvider class
    and inserts this object in an arraylist along with the post from the models-package.

    Return response converts the arraylist getAllPosts from GSON to JSON.
     */
    @GET
    public Response getAllPosts() {

        PostProvider postProvider = new PostProvider();
        ArrayList<Post> allPosts = null;
        try {
            allPosts = postProvider.getAllPosts();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
        return Response.status(200).type("application/json").entity(new Gson().toJson(allPosts)).build();
    }


    @POST
    public Response createPost (String jsonPost) {

        JsonObject postData = new Gson().fromJson(jsonPost, JsonObject.class);

        int localOwner;
        String localContent;
        int localEvent = 0;
        int localParent = 0;

        localOwner = postData.get("owner").getAsInt();
        localContent = postData.get("content").getAsString();


        try {
            localEvent = postData.get("event").getAsInt();
        }
        catch (NullPointerException e){
            localEvent = 0;

        }

        try {
            localParent = postData.get("parent").getAsInt();
        }
        catch (NullPointerException e2){
            localParent = 0;
        }

        if (localEvent < 0 || localParent < 0 ){
            throw new IllegalArgumentException("Event og Parent Id can't be less than 0");
        }

        if (localParent != 0){
            localEvent = 0;
        }


        Post createdPost = new Post(localOwner, localContent, localEvent, localParent);

        PostProvider postProvider = new PostProvider();

        /**
         * ValidatePostInput is called to make sure, that the post content is not empty.
         */
        try{
            createdPost = contentController.validatePostCreation(createdPost.getId(), createdPost.getCreated(),
                    createdPost.getOwner(), createdPost.getContent(),
                    createdPost.getEvent(),createdPost.getParent());
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            return Response.status(400).build();
        }

        try {
           postProvider.createPost(createdPost);
        }
        catch (SQLException e){
            e.printStackTrace();
            return Response.status(400).type("text/plain").entity("Could not create post").build();
        }

        return Response.status(201).type("text/plain").entity("Post created").build();


    }


    /** This method returns one specific post chosen by id. The method creates an object of the PostProvider class and inserts
     * this object in an ArrayList and also the variables from the Post model-package.
     *
     *
     * @return It returns an response that converts the ArrayList onePost from Gson to Json.
     */
    @GET
    @Path("{id}")
    public Response getPost(@PathParam("id") int post_id) {
        PostProvider postProvider = new PostProvider(); //Creates an object
        Post post;

        try {
            post = postProvider.getPost(post_id);

            post.getComments().addAll(postProvider.getPostsByParentId(post_id));

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(500).build();
        }

        return Response.status(200).type("application/json").entity(new Gson().toJson(post)).build();

    }

}

