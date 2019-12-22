package org.orson.io.nio.chat;

public class ChatServer {


    public static void main(String[] args) {


        ChatRoom room = new ChatRoom("Adventure");

        room.open();

    }
}
