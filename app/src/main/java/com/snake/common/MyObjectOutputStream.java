package com.snake.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MyObjectOutputStream extends ObjectOutputStream {

    public MyObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    public MyObjectOutputStream() throws IOException{
        super();
    }

    @Override
    protected void writeStreamHeader() throws IOException {

    }


}