package com.example.webchatserver;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject; import org.json.JSONArray;

/**
 * This is a class that has services
 * In our case, we are using this to generate unique room IDs**/
@WebServlet(name = "chatServlet", value = "/chat-servlet")
public class ChatServlet extends HttpServlet {
    private String message;

    //static so this set is unique
    public static Set<String> rooms = new HashSet<>();

    /**
     * Method generates unique room codes
     * **/
    public String generatingRandomUpperAlphanumericString(int length) {
        String generatedString = RandomStringUtils.randomAlphanumeric(length).toUpperCase();
        // generating unique room code
        while (rooms.contains(generatedString)){
            generatedString = RandomStringUtils.randomAlphanumeric(length).toUpperCase();
        }
        rooms.add(generatedString);

        return generatedString;
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:63342");

        PrintWriter out = response.getWriter();
        String json = "{\"code\": \"" + generatingRandomUpperAlphanumericString(5) + "\"}";
        out.println(json);*/

        response.setContentType("text/plain");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:63342");

        // send the random code as the response's content
        PrintWriter out = response.getWriter();
        out.println(generatingRandomUpperAlphanumericString(5));

    }

    public JSONObject getRoomsJson() {
        JSONObject json = new JSONObject();
        JSONArray roomsJson = new JSONArray();
        for (String room : rooms) {
            roomsJson.put(room);
        }
        json.put("rooms", roomsJson);
        return json;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:63342");

        JSONObject json = getRoomsJson();
        PrintWriter out = response.getWriter();
        out.println(json.toString());
    }


    public void destroy() {
    }
}