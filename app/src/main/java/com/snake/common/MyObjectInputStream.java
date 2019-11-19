package com.snake.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MyObjectInputStream {
    public static ServerData readObject(Socket socket) {
       ServerData sd = null;
       ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            sd = (ServerData) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sd;
    }
}
