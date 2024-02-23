package com.example.webchatserver;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;
import java.util.Date;
import java.io.IOException;
import java.util.Random;
import java.util.*;

/**
 * This class represents a web socket server, a new connection is created and it receives a roomID as a parameter
 * **/
@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {

    // contains a static List of ChatRoom used to control the existing rooms and their users
    private static List<ChatRoom> roomList = new ArrayList<>();

    private Map<String, String> usernames = new HashMap<String, String>();
    public static String opp;
    public static int flag = 1;
    public static boolean oppFlag = false;
    public static boolean  oppFlag2 = false;

    public static int gameOffset = 0;
    public static int firstGameOffset = 0;

    public static int offset = 0;
    public static String oppAns;
    public static String opp2Ans;
    public static String opp2;

    // you may add other attributes as you see fit
    // ChatServlet helper = new ChatServlet();

    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {
        // Check if the chat room already exists
     //   roomList.put(session.getId(), roomID); // adding userID to a room
        boolean roomExists = false;
        for (ChatRoom room : roomList) {
            if (room.getCode().equals(roomID)) {
                roomExists = true;
                break;
            }
        }

        // If the chat room doesn't exist, create it and add it to the list
        if (!roomExists) {
            ChatRoom newRoom = new ChatRoom(roomID, session.getId());
            roomList.add(newRoom);
        }

        //session.getBasicRemote().sendText("First sample message to the client");
//        accessing the roomID parameter
        System.out.println(session.getId());

        // Create a JSON object with the room code
        JSONObject roomInfo = new JSONObject().put("type", "server")
                .put("message","(Server): This is Room " + roomID);

        // Send the JSON object to the client
        session.getBasicRemote().sendText(roomInfo.toString());
        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): Welcome to the chat room. Please state your username to begin.\"}");
        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): Type challenge to play a game!\"}");
     /*   // Add the user to the chat room
        for (ChatRoom room : roomList) {
            if (room.getCode().equals(roomID)) {
                room.setUserName(session.getId(), "");
                break;
            }
        }*/

        String username = (String) session.getUserProperties().get("username");
        if (username == null || username.isEmpty()) {
            // if the user doesn't have a username, then ask them to provide one
            session.getBasicRemote().sendText("Please enter your username:");
        } else {
            // greet the new user
            for (Session peer : session.getOpenSessions()) {
                if (peer.isOpen() && !peer.getId().equals(session.getId())) {
                    // send welcome message to peers only
                    peer.getBasicRemote().sendText("Welcome to the chat room " + roomID + ", " + username + "!");
                }
            }
            session.getBasicRemote().sendText("Welcome to the chat room " + roomID + ", " + username + "!");
        }

    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
/*        String userId = session.getId();
        // do things for when the connection closes
        String roomID = getRoomID(session);
        ChatRoom chatRoom = getChatRoom(roomID);
        String username = usernames.get(userId);
        chatRoom.removeUser(userId);
        usernames.remove(userId);
        // announce to the other users that a user has left the room
        session.getBasicRemote().sendText(username + " has left the room.");*/

        String userId = session.getId();

        if (usernames.containsKey(userId)) {

            String username = usernames.get(userId);

            String roomID = getRoomID(session);

            // broadcasting it to peers in the same room
            int countPeers = 0;
            //goes through chat roomlist
            for (ChatRoom room : roomList) {
                if (room.inRoom(userId)) {
                    roomID = room.getCode();
                    username = room.getUserName(userId);

                    for (Session peer : session.getOpenSessions()) {
                        if (room.inRoom(peer.getId())) {
                            peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + username + " left the chat room.\"}");
                            countPeers++;
                        }
                    }
                    room.removeUser(userId);

                    if (!(countPeers > 0)) {
                        roomList.remove(room);
                        break;
                    }
                }

            }


        }


    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException, InterruptedException {
//        example getting unique userID that sent this message
        String userId = session.getId();
        String roomID = getRoomID(session); // my room
        //        Example conversion of json messages from the client
        JSONObject jsonmsg = new JSONObject(comm);
        String type = (String) jsonmsg.get("type");
        String message = (String) jsonmsg.get("msg");

        // handle the messages
        ChatRoom chatRoom = getChatRoom(roomID);
        String username = usernames.get(userId);

        if(usernames.containsKey(userId)){ // not their first message
            System.out.println(username);
            String Opponent ="";
            // broadcasting it to peers in the same room
            for(Session peer: session.getOpenSessions()){



                if(message.equalsIgnoreCase("rock") || message.equalsIgnoreCase("paper") || message.equalsIgnoreCase("scissors") || message.equals("")){ //doesn't print game answer to prevent cheating

                } else {
                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + message + "\"}");
                }

                if(gameOffset == 1 ){ //resets game conditions
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " +"Please type challenge to play again. " + "\"}");
                    gameOffset++;
                    offset = 0;
                    oppFlag = false;
                    oppFlag2 = false;


                }


                if(message.equals("challenge")){ //start game command
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " +
                            "Please specify the user you would like to face."+ "\"}"); //asks who to play
                    message ="";

                }
                if(message.equals(opp)){ //if a valid user is chosen - Phase 1

                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + "has challenged " + opp + " to a game of rock paper scissors!" + "\"}");
                    message = "";
                    oppFlag =true; //begins next game sequence
                }
                else if(message.equals(opp2)){

                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + "has challenged " + opp2 + " to a game of rock paper scissors!" + "\"}");
                    message = "";
                    oppFlag= true; //begins accepting player choice
                    //oppFlag1 = true; //begins starting the game
                }
                if(oppFlag == true && offset < 1){ //starts game - Phase 2
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + "Server" + "): " + "Please enter in your answer as the game will begin." + "\"}");
                    Thread.sleep(1000);
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + "Server" + "): " + "Rock" + "\"}");
                    Thread.sleep(1000);
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + "Server" + "): " + "Paper" + "\"}");
                    Thread.sleep(1000);
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + "Server" + "): " + "Scissors" + "\"}");
                    Thread.sleep(1000);
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " + "Shoot!" + "\"}");
                    Thread.sleep(3000);
              //      message = "";
                    offset++; //only needs to print once so flag is reset
                }
                if(oppFlag == true && username.equals(opp2)) { //gets player answers for game - Phase 3
                    opp2Ans = message;
                    oppFlag2 = true;
                }
                if(oppFlag == true && username.equals(opp)){
                    oppAns = message;
                    oppFlag2 = true;
                }
                if(oppFlag2 == true && oppAns != null && opp2Ans != null){ //runs this sequence after both opponents have answered and concludes game - Phase 4
                    gameOffset = 0;
                    if(oppAns.equalsIgnoreCase("rock") && opp2Ans.equalsIgnoreCase("scissors")){ //win cases
                        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " + opp + " chose rock and "+ opp2 + " chose scissors, so " + opp + " wins!" + "\"}");
                        gameOffset++;
                    }
                    if(opp2Ans.equalsIgnoreCase("rock") && oppAns.equalsIgnoreCase("scissors")){
                        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " + opp2 + " chose rock and "+ opp + " chose scissors, so " + opp2 + " wins!" + "\"}");
                        gameOffset++;
                    }
                    if(oppAns.equalsIgnoreCase("scissors") && opp2Ans.equalsIgnoreCase("paper")){
                        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " + opp + " chose scissors and "+ opp2 + " chose paper, so " + opp + " wins!" + "\"}");
                        gameOffset++;
                    }
                    if(opp2Ans.equalsIgnoreCase("scissors") && oppAns.equalsIgnoreCase("paper")){
                        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " + opp2 + " chose scissors and "+ opp + " chose paper, so " + opp2 + " wins!" + "\"}");
                        gameOffset++;
                    }
                    if(oppAns.equalsIgnoreCase("rock") && opp2Ans.equalsIgnoreCase("paper")){
                        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " + opp2 + " chose paper and "+ opp + " chose rock, so " + opp2 + " wins!" + "\"}");
                        gameOffset++;
                    }
                    if(opp2Ans.equalsIgnoreCase("rock") && oppAns.equalsIgnoreCase("paper")){
                        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " + opp + " chose paper and "+ opp2 + " chose rock, so " + opp + " wins!" + "\"}");
                        gameOffset++;
                    }
                    if (gameOffset ==0){ //draw case
                        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" +"Server"+ "): " + opp + " chose " + oppAns + " and "+ opp2 + " chose " +opp2Ans+ ", so the game ends in a draw!" + "\"}");
                        gameOffset++;
                    }
                   // message = "";
                }





            }

        }else{ //first message is their username
            usernames.put(userId, message);
            session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): Welcome, " + message + "!\"}");
            if(flag == 1){ //gets opponent two
                opp = message;
                flag++;
            }
            if(flag == 2){
                opp2 = message;

            }

            // broadcasting it to peers in the same room
            for(Session peer: session.getOpenSessions()){
                if (!peer.getId().equals(userId)) {
                    username = usernames.get(userId);
                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server):  " + message + " has joined the chat room!\"}");
                }

            }
        }


    }



    private ChatRoom getChatRoom(String roomID) {
        for (ChatRoom chatRoom : roomList) {
            if (chatRoom.getCode().equals(roomID)) {
                return chatRoom;
            }
        }
        return null;
    }

    private String getRoomID(Session session) {
        return (String) session.getUserProperties().get("roomID");
    }


}