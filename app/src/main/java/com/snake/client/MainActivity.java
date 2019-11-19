package com.snake.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.snake.common.Constant;
import com.snake.common.MyObjectOutputStream;
import com.snake.common.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Socket socket;
    private final String msg = "你好，服务器，我是客户端";
    private static int time = 0;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static TextView textView;

    @Override
//    111
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Button button1 = (Button) findViewById(R.id.sendMsg);
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sendMessage();
//            }
//        });
//        Button button2 = (Button) findViewById(R.id.sendObject);
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick: 发送对象");
//                sendObject(time, "201721130058");
//                if (time == 0)
//                    time++;
//            }
//        });
//        textView = (TextView)findViewById(R.id.textView);
//
//        createConnect();
//        time = 0;


    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: 活动结束");
        synchronized (this) {
            try {
                sendObject(time, "exit");
                this.wait(1000);
                socket.close();
                Log.d(TAG, "onDestroy: 断开连接");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            if (msg.what == 1){
                textView.setText(bundle.getString("msgData"));
            }
        }
    };

    private synchronized void createConnect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    socket = new Socket(Constant.SERVERIP, 9999);
                    Log.i(TAG, "createConnect: 与服务器建立连接" + socket);
                    readObject();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void sendMessage() {
        new Thread() {
            @Override
            public void run() {
                try {
                    DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                    writer.writeUTF(msg); // 写一个UTF-8的信息
                    System.out.println("发送消息");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void sendObject(final int time, final String id) {
        new Thread() {
            @Override
            public void run() {
                try {
                    ObjectOutputStream oos = null;
                    if (time == 0)
                        oos = new ObjectOutputStream(socket.getOutputStream());
                    else
                        oos = new MyObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(new User(id, "宗俊豪", 20));
                    oos.flush();
                    Log.d(TAG, "run: 发送对象成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void readObject() {
        new Thread() {
            @Override
            public void run() {
                ObjectInputStream ois;
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                    while (true) {
                        Log.d(TAG, "readObject run: *等待服务端输入对象*");
                        User user = (User) ois.readObject();
                        String msg = " id:" + user.getId() + " name:" + user.getName() + " age:" + user.getAge() + " time:" + sdf.format(new Date(user.getTime()));
//                        System.out.println("获取到客户端的信息：接收时间:" + sdf.format(new Date(System.currentTimeMillis())) + msg);
                        Log.d(TAG, "readObject run:获取到客户端的信息：接收时间: " + sdf.format(new Date(System.currentTimeMillis())) + msg);
                        Message message = new Message();
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("msgData", msg);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
