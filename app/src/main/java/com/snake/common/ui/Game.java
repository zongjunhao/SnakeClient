package com.snake.common.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.alibaba.fastjson.JSON;
import com.snake.common.ClientData;
import com.snake.common.Constant;
import com.snake.common.MyObjectInputStream;
import com.snake.common.MyObjectOutputStream;
import com.snake.common.R;
import com.snake.common.ServerData;
import com.snake.common.User;
import com.snake.common.base.BaseAvtivity;
import com.snake.common.bean.Point;
import com.snake.common.bean.SnakeBean;
import com.snake.common.view.SnakeView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 游戏主进程
 */
public class Game extends BaseAvtivity implements GameInterface {

    private SnakeView snakeView;
    private ImageView iv_moving;
    private ImageView iv_speeding;
    private TextView tv_len;
    private TextView tv_rank;

    private ServerData serverData;
    private ClientData clientData;
    private int time = 0;
    private float x;
    private float y;
    private float x2;
    private float y2;
    private float optf;
    private Point opt;
    private boolean isMove = false;
    private static final String TAG = "Game";

    private SnakeBean snakeBean;
    private Socket socket = null;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void exit() {
        new Thread() {
            @Override
            public void run() {
                try {
                    clientData.setFlag(-1);
                    ObjectOutputStream oos = null;
                    if (time == 0) {
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        time++;
                    } else {
                        oos = new MyObjectOutputStream(socket.getOutputStream());
                    }
                    oos.writeObject(clientData);
                    oos.flush();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        clientData = new ClientData();
        DisplayMetrics dm = new DisplayMetrics(); //获取手机分辨率
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //将常量中的屏幕长宽设置为当前手机屏幕的长宽
//        Constant.screenWidth = dm.widthPixels;
//        Constant.screenHeight = dm.heightPixels;
        Log.d(TAG, "onCreate:  Constant.screenWidth:" + Constant.screenWidth + " Constant.screenHeight:" + Constant.screenHeight);
        //将可视范围设置为屏幕长宽的两倍
//        Constant.viewWidth = Constant.screenWidth * 2;
//        Constant.viewHeight = Constant.screenHeight * 2;
        Log.d(TAG, "onCreate:  Constant.viewWidth:" + Constant.viewWidth + " Constant.viewHeight:" + Constant.viewHeight);
        //设置贪吃蛇每节身体的直径
//        Constant.snake_d = Constant.screenWidth / 35;
        Log.d(TAG, "onCreate:  Constant.snake_d:" + Constant.snake_d);
        //贪吃蛇前进的长度，直径的一半
//        Constant.snake_len = Constant.snake_d / 2;
//        Constant.buttonMargin = Constant.snake_d;
        Constant.buttonMargin = dm.widthPixels/35;
        Log.d(TAG, "onCreate:  Constant.snake_len:" + Constant.snake_len + " Constant.buttonMargin:" + Constant.buttonMargin);
        //方向
        opt = new Point(Constant.snake_len, 0);
        Log.i("game", "Constant.snake_d:" + Constant.snake_d);

        createConnect();

        snakeView = (SnakeView) findViewById(R.id.sv_game);//主视图
        snakeView.setGameInterface(this);
        snakeView.setSpeed(1);
        iv_moving = (ImageView) findViewById(R.id.iv_moving);//移动按钮
        iv_speeding = (ImageView) findViewById(R.id.iv_speeding);//加速按钮
        tv_len = (TextView) findViewById(R.id.tv_len);//左上角长度
        tv_rank = (TextView) findViewById(R.id.tv_rank);//右上角排名

        //将两个按钮设置为不可见
        iv_moving.setVisibility(View.INVISIBLE);
        iv_speeding.setVisibility(View.INVISIBLE);

        int mx = (Constant.nullWidth * 2 + Constant.viewWidth) / 2;
        int my = (Constant.nullWidth * 2 + Constant.viewHeight) / 2;
        Log.i("game", "mx--" + mx + "--my--" + my);
        setViewMargin(mx, my);//设置显示区域

        //连接服务器

        //监听移动按钮的动作，并做出相应处理
        findViewById(R.id.iv_move).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (view.getId() == R.id.iv_move) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN://按下按钮
                            x = 40;//按钮的基准点
                            y = 40;
                            iv_moving.setVisibility(View.VISIBLE);//将移动的小按钮设置为可见
                            btMoving(0, 0);
                            isMove = false;
                            Log.i("game", "down:" + x + "---" + y);

                            break;
                        case MotionEvent.ACTION_MOVE://按钮移动
                            isMove = true;
                            moving(motionEvent.getX(), motionEvent.getY());
                            sendToServer();
                            break;
                        case MotionEvent.ACTION_UP://离开按钮
                            moving(motionEvent.getX(), motionEvent.getY());
                            btMoving(0, 0);
                            iv_moving.setVisibility(View.INVISIBLE);//将移动的小按钮设置为不可见

                            break;
                    }
                }

                return true;
            }
        });

        //监听加速按钮的动作
        findViewById(R.id.iv_speed).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (view.getId() == R.id.iv_speed) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN://按下加速按钮时，将速度设置为2，并将小按钮设置为可见
                            snakeView.setSpeed(2);
                            sendToServer();
                            iv_speeding.setVisibility(View.VISIBLE);

                            break;
                        case MotionEvent.ACTION_UP://抬起时，将速度重置为1，并将小按钮设置为不可见
                            snakeView.setSpeed(1);
                            iv_speeding.setVisibility(View.INVISIBLE);

                            break;
                    }
                }

                return true;
            }
        });

    }


    private final Object lock = new Object();

    private synchronized void createConnect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (socket == null)
                        socket = new Socket(Constant.SERVERIP, Constant.SERVERPort);
                    Log.i(TAG, "createConnect: 与服务器建立连接" + socket);
                   while(!socket.isConnected()) {

                   }
                   readObject();
//                    readMsg();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void readMsg() {
        new Thread() {
            @Override
            public void run() {
                DataInputStream reader;
                try {
                    //获取读取流
                    reader = new DataInputStream(socket.getInputStream());
                    boolean flag = true;
                    while (flag) {
                        Log.d(TAG, "run: *等待服务器输入*");
                        String msg = reader.readUTF();

//                        byte[] buffer = new byte[1024 * 1024];
//                        //消息长度
//                        int rlength = reader.read(buffer, 0, 1024 * 1024);
//                        Log.d(TAG, "run: 接收的消息长度:" + rlength);
////                        System.out.println("接收的消息长度:" + rlength);
//                        //传输的实际byte[]
//                        byte[] buffer1 = new byte[rlength];
//                        for (int i = 0; i < buffer1.length; i++) {
//                            buffer1[i] = buffer[i];
//                        }
//                        String msg = new String(buffer1);
                        Log.d(TAG, "run: Game readMsg" + msg);
//                        String messageContent1 = new String(buffer1, "GBK").toString().trim();
//                        System.out.println("接收的消息（gbk转码）：" + messageContent1);
//
//                        String messageContent = new String(buffer, 0, rlength).toString().trim();
//                        System.out.println("接收的消息：" + messageContent);


                        serverData = JSON.parseObject(msg, ServerData.class);
                        System.out.println("获取到客户端的信息：" + System.currentTimeMillis() + msg);
                        Constant.flag = serverData.getFlag();
                        Log.d(TAG, "Game readObject run: " + serverData.getFlag());
                        snakeView.setSnakes(serverData.getSnakes());
                        Log.d(TAG, "Game readObject run: " + serverData.getSnakes());
                        snakeView.setFoods(serverData.getFoods());
                        Log.d(TAG, "Game readObject run: " + serverData.getFoods());
                        snakeView.setRank(serverData.getRank());
                        Log.d(TAG, "Game readObject run: " + serverData.getRank());
                        Log.d(TAG, "Game readObject run: " + serverData.toString());
                        //得到当前蛇，这里可能会重复判断，因为后面也会对蛇进行遍历。。
                        for (SnakeBean sb : serverData.getSnakes()) {
                            Log.d(TAG, "readObject run: snake flag" + sb.flag);
                            if (sb.flag == Constant.flag) {
                                Log.d(TAG, "readObject run: " + sb.flag);
                                snakeBean = sb;
                                break;
                            }
                        }
                        if (snakeBean.isDeath) {
                            Constant.playerAlive = 0;
                            flag = false;
                            exit();
                            Log.d(TAG, "run: 贪吃蛇死亡，停止绘制");
                        }
                        snakeView.setSnakeBean(snakeBean);

                        draw();
                    }
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
                boolean flag = true;
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                    while (flag) {
                        Log.d(TAG, "readObject run: *等待服务端输入对象*");
//                        serverData = (ServerData) ois.readObject();
                        try {
                            Object object = ois.readObject();
                            if (object instanceof ServerData) {
                                serverData = (ServerData) object;
                            } else {
                                Log.d(TAG, "run: 接收到的数据异常，跳过，进行下一次循环");
                                continue;
                            }


                            Log.d(TAG, "readObject run: 接收到serverData对象" + serverData);
                            //设置snakeView的各种值
                            Constant.flag = serverData.getFlag();
                            Log.d(TAG, "Game readObject run: " + serverData.getFlag());
                            snakeView.setSnakes(serverData.getSnakes());
                            Log.d(TAG, "Game readObject run: " + serverData.getSnakes());
                            snakeView.setFoods(serverData.getFoods());
                            Log.d(TAG, "Game readObject run: " + serverData.getFoods());
                            snakeView.setRank(serverData.getRank());
                            Log.d(TAG, "Game readObject run: " + serverData.getRank());
                            Log.d(TAG, "Game readObject run: " + serverData.toString());
                            //得到当前蛇，这里可能会重复判断，因为后面也会对蛇进行遍历。。
                            for (SnakeBean sb : serverData.getSnakes()) {
                                Log.d(TAG, "readObject run: snake flag" + sb.flag);
                                if (sb.flag == Constant.flag) {
                                    Log.d(TAG, "readObject run: " + sb.flag);
                                    snakeBean = sb;
                                    setViewMargin((int) snakeBean.head.getX() + Constant.snake_d / 2, (int) snakeBean.head.getY() + Constant.snake_d / 2);
                                    break;
                                }
                            }
                            if (snakeBean.isDeath) {
                                Constant.playerAlive = 0;
                                flag = false;
                                exit();
                                Log.d(TAG, "run: 贪吃蛇死亡，停止绘制");
                            }
                            snakeView.setSnakeBean(snakeBean);
                        } catch (ClassNotFoundException | ClassCastException e) {
                            e.printStackTrace();
                        }
                        draw();
//                        String msg = " id:" + user.getId() + " name:" + user.getName() + " age:" + user.getAge() + " time:" + sdf.format(new Date(user.getTime()));
//                        System.out.println("获取到客户端的信息：接收时间:" + sdf.format(new Date(System.currentTimeMillis())) + msg);
//                        Log.d(TAG, "readObject run:获取到客户端的信息：接收时间: " + sdf.format(new Date(System.currentTimeMillis())) + msg);
//                        Message message = new Message();
//                        message.what = 1;
//                        Bundle bundle = new Bundle();
//                        bundle.putString("msgData", msg);
//                        message.setData(bundle);
//                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //移动按钮的同时，将ClientData数据发到服务器
    private void sendToServer() {
        new Thread() {
            @Override
            public void run() {
                try {
                    clientData.setSpeed(snakeView.getSpeed());
                    clientData.setFlag(Constant.flag);
                    clientData.setOpt(opt);
                    ObjectOutputStream oos = null;
                    if (time == 0) {
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        time++;
                    } else {
                        oos = new MyObjectOutputStream(socket.getOutputStream());
                    }
                    oos.writeObject(clientData);
                    oos.flush();
                    oos.reset();
                    Log.d(TAG, "发送数据 speed:" + clientData.getSpeed() + "flag:" + clientData.getFlag() + "opt:" + clientData.getOpt());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    //移动贪吃蛇，同时移动按钮
    private void moving(float x1, float y1) {

        x2 = x1 - x;
        y2 = y1 - y;
        btMoving(x2, y2);

        if (isMove) {
            optf = (float) Math.sqrt(x2 * x2 + y2 * y2);
            opt.setX(Constant.snake_len * x2 / optf);
            opt.setY(Constant.snake_len * y2 / optf);

        }

    }

    //移动按钮的显示
    private void btMoving(float mx, float my) {
        float tf = (float) Math.sqrt(mx * mx + my * my);
        RelativeLayout.LayoutParams lP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        if (mx == 0 && my == 0) {
            lP.setMargins(Constant.buttonMargin, Constant.buttonMargin, Constant.buttonMargin, Constant.buttonMargin);
        } else {
            mx = Constant.buttonMargin * mx / tf;
            my = Constant.buttonMargin * my / tf;

            int left = (int) (Constant.buttonMargin + mx);
            int top = (int) (Constant.buttonMargin + my);
            int right = Constant.buttonMargin * 2 - left;
            int bottom = Constant.buttonMargin * 2 - top;

            lP.setMargins(left, top, right, bottom);
        }
        iv_moving.setLayoutParams(lP);
    }


    @Override
    public void setViewMargin(int x, int y) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("x", x);
        bundle.putInt("y", y);
        msg.setData(bundle);
        msg.what = Constant.GAME_VIEW_MARGIN;
        synchronized (mHandler) {
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void setSnakeLen(int len) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("len", len);
        msg.setData(bundle);
        msg.what = Constant.SNAKE_LEN;
        synchronized (mHandler) {
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public synchronized void setRank(String rank) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("rank", rank);
        msg.setData(bundle);
        msg.what = Constant.SNAKE_RANK;
        synchronized (mHandler) {
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void setDeath() {
        Message msg = new Message();
        msg.what = Constant.PLAYER_ALIVE;
        synchronized (mHandler) {
            mHandler.sendMessage(msg);
        }
        Constant.playerAlive = 1;
    }

    @Override
    public void draw() {
        Log.d(TAG, "draw: Game draw");
        Message msg = new Message();
        msg.what = Constant.DRAW;
        synchronized (mHandler) {
            mHandler.sendMessage(msg);
        }
    }

    private void reStartActivity() {
        Log.d(TAG, "reStartActivity: ");
        Constant.playerAlive = 1;
//        PopDialog dialog = new PopDialog(this);
//        dialog.setCancelable(false);
//        if (dialog.showDialog() == 1) {
//            finish();
//        }
        Log.d(TAG, "reStartActivity: " + Constant.playerAlive);
        AlertDialog.Builder dialog = new AlertDialog.Builder(Game.this);
        dialog.setMessage("您已死亡，点击重新开始游戏");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case Constant.GAME_VIEW_MARGIN:
                    setMargin(msg);
                    break;
                case Constant.SNAKE_LEN://设置长度显示
                    int len = bundle.getInt("len");
                    tv_len.setText("长度：" + len);

                    break;
                case Constant.SNAKE_RANK://设置排名显示的字符串
                    String rank = bundle.getString("rank");
                    tv_rank.setText("排名" + rank);

                    break;
                case Constant.PLAYER_ALIVE:
                    reStartActivity();
                    break;
                case Constant.DRAW:
                    drawSub();

            }

        }
    };

    //设置显示区域
    private void setMargin(Message msg) {

        Bundle bundle = msg.getData();
        int x = bundle.getInt("x");
        int y = bundle.getInt("y");

        RelativeLayout.LayoutParams lP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        x -= (Constant.screenWidth / 2);
        y -= (Constant.screenHeight / 2);
        if (x < 0) {
            x = 0;
        }
        if (x > Constant.nullWidth * 2 + Constant.viewWidth - Constant.screenWidth) {
            x = Constant.nullWidth * 2 + Constant.viewWidth - Constant.screenWidth;
        }
        if (y < 0) {
            y = 0;
        }
        if (y > Constant.nullWidth * 2 + Constant.viewHeight - Constant.screenHeight) {
            y = Constant.nullWidth * 2 + Constant.viewHeight - Constant.screenHeight;
        }

        lP.setMargins(-x, -y, 0, 0);
        snakeView.setLayoutParams(lP);
    }

    private void drawSub() {
        Log.d(TAG, "drawSub: 绘制postInvalidate");
        snakeView.postInvalidate();
    }

}
