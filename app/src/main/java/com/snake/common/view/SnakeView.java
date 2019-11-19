package com.snake.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.snake.common.Constant;
import com.snake.common.base.BaseView;
import com.snake.common.bean.Food;
import com.snake.common.bean.Point;
import com.snake.common.bean.SnakeBean;
import com.snake.common.ui.GameInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 *
 */
public class SnakeView extends View implements SnakeViewInterface {

    private List<SnakeBean> snakes;
    private List<Food> foods;
    private GameInterface gameInterface;
    private int speed = 1;
    private String rank;
    private SnakeBean snakeBean;
    private static final String TAG = "SnakeView";

    public SnakeView(Context context) {
        super(context);
        init();
    }

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setSnakes(List<SnakeBean> snakes) {
        this.snakes = snakes;
    }

    public void setSnakeBean(SnakeBean snakeBean) {
        this.snakeBean = snakeBean;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }


    @Override
    public void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: 绘制onDraw");
        drawSub(canvas);
    }

    public void drawSub(Canvas canvas) {
        Log.d(TAG, "drawSub: 绘制drawSub");
        Paint paint = new Paint();

        //绘制网格线
        paint.setARGB(100, 100, 100, 100);//设置画笔的透明度和颜色
        for (int i = Constant.nullWidth; i < Constant.nullWidth + Constant.viewHeight; i += Constant.gridWidth) {
            //(float startX, float startY, float stopX, float stopY, Paint paint)
            canvas.drawLine(Constant.nullWidth, i, Constant.nullWidth + Constant.viewWidth, i, paint);
        }
        for (int i = Constant.nullWidth; i < Constant.nullWidth + Constant.viewWidth; i += Constant.gridWidth) {
            canvas.drawLine(i, Constant.nullWidth, i, Constant.nullWidth + Constant.viewHeight, paint);
        }

        //绘制边框 左 上 右 下
        paint.setARGB(50, 100, 100, 100);
        //public void drawRect(float left, float top, float right, float bottom, Paint paint)
        canvas.drawRect(0, 0, Constant.nullWidth, Constant.nullWidth + Constant.viewHeight, paint);
        canvas.drawRect(Constant.nullWidth, 0, Constant.nullWidth + Constant.viewWidth, Constant.nullWidth, paint);
        canvas.drawRect(Constant.nullWidth + Constant.viewWidth, 0, Constant.nullWidth * 2 + Constant.viewWidth,
                Constant.nullWidth + Constant.viewHeight, paint);
        canvas.drawRect(0, Constant.nullWidth + Constant.viewHeight, Constant.nullWidth * 2 + Constant.viewWidth,
                Constant.nullWidth * 2 + Constant.viewHeight, paint);

        //绘制食物
        int fSize = foods.size();
        for (int i = 0; i < fSize; ++i) {
            Food food = foods.get(i);
            if (food.isNull()) {
//                foods.remove(i);
//                addFood();
            } else {
                paint.setColor(food.getColor());
                canvas.drawOval(new RectF(food.getX(), food.getY(), food.getX() + Constant.snake_d / 3, food.getY() +
                        Constant.snake_d / 3), paint);
            }

        }

        //绘制贪吃蛇
        drawSnake(canvas);

        logic();
        checkPlayer();
    }

    public void drawSnake(Canvas canvas) {
        Paint paint = new Paint();
        if (snakes == null) return;

        for (SnakeBean sb : snakes) {//遍历所有蛇的列表
            List<Point> pointSnakes = sb.snake; //得到一条蛇的身体
            //如果蛇已经死亡
//            if (sb.flag == Constant.flag && Constant.playerAlive == 1) {
//                int mx = (int) (sb.head.getX() + Constant.snake_d / 2);
//                int my = (int) (sb.head.getX() + Constant.snake_d / 2);
//                setViewMargin(mx, my);
//            }
            if (sb.isDeath) {
                for (Point p : pointSnakes) {
                    paint.setColor(sb.snakeColor);
                    canvas.drawOval(new RectF(p.getX(), p.getY(), p.getX() + Constant.snake_d / 2, p.getY() + Constant.snake_d / 2), paint);
                }
                if (sb.flag == Constant.flag) {
                    setSnakeLen(0);
//                    Constant.playerAlive = 0;
                }
            } else {
                for(int i = 0; i < pointSnakes.size(); i++) {
                    Point p = pointSnakes.get(i);
                    paint.setColor(Color.BLACK);//设置节点边界的颜色为黑色
                    canvas.drawOval(new RectF(p.getX(), p.getY(), p.getX() + Constant.snake_d, p.getY() + Constant.snake_d), paint);
                    paint.setColor(sb.snakeColor);//设置贪吃蛇节点的颜色
                    canvas.drawOval(new RectF(p.getX() + 2, p.getY() + 2, p.getX() + Constant.snake_d - 2, p.getY() + Constant.snake_d - 2), paint);
                    if (i == pointSnakes.size() - 1) {//在最后一个节点的上部绘制贪吃蛇名字
                        paint.setColor(Color.BLACK);
                        paint.setTextSize(35);//字体大小与半径相同
//                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                        canvas.drawText(sb.name, p.getX() - 2, p.getY() - 2, paint);
                        float x = p.getX() + (Constant.snake_d - 4) / 2, y = p.getY() + (Constant.snake_d - 4) / 2;
                        canvas.drawOval(new RectF(x - 2, y - 2, x + 2, y + 2), paint);
                    }
                }
//                for (Point p : pointSnakes) {
//                    if (p.getX() == sb.head.getX() && p.getY() == sb.head.getY()) {
//                        paint.setColor(Color.BLACK);
//                        paint.setTextSize(Constant.snake_len);//字体大小与半径相同
//                        canvas.drawText(sb.name, p.getX() - 2, p.getY() - 2, paint);
//                        //绘制眼睛
//                        canvas.drawOval(new RectF(p.getX() + Constant.snake_d / 2 - 2, p.getY() + Constant.snake_d / 2 - 2, p.getX() + 2, p.getY() + 2), paint);
//                    } else {
//                        paint.setColor(Color.BLACK);//设置节点边界的颜色为黑色
//                        canvas.drawOval(new RectF(p.getX(), p.getY(), p.getX() + Constant.snake_d, p.getY() + Constant.snake_d), paint);
//                        paint.setColor(sb.snakeColor);//设置贪吃蛇节点的颜色
//                        canvas.drawOval(new RectF(p.getX() + 2, p.getY() + 2, p.getX() + Constant.snake_d - 2, p.getY() + Constant.snake_d - 2), paint);
//                    }
//                }

            }
        }

    }


    protected void logic() {

        setSnakeRank(rank);
        if (snakeBean != null)
            setSnakeLen(snakeBean.eat / 75);

    }

    protected void checkPlayer() {
        if (Constant.playerAlive == 0) {
            gameInterface.setDeath();
            Log.d(TAG, "玩家已经死亡");
        }
        Log.d(TAG, "checkPlayer: 检查玩家是否死亡");
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    /**
     * 初始化界面，向界面中添加贪吃蛇和食物
     */

    protected void init() {

        if (snakes == null)
            snakes = Collections.synchronizedList(new ArrayList<SnakeBean>());
        if (foods == null)
            foods = Collections.synchronizedList(new ArrayList<Food>());

    }

    /**
     * 添加食物，随机生成食物的位置和颜色
     */
//    private void addFood() {
//        Random random = new Random();
//        int fNum = Constant.food_num - foods.size();
//        for (int i = 0; i < fNum; ++i) {
//            int x = random.nextInt(Constant.viewWidth - Constant.nullWidth / 2) + Constant.snake_d + Constant.nullWidth;
//            int y = random.nextInt(Constant.viewHeight - Constant.nullWidth / 2) + Constant.snake_d + Constant.nullWidth;
//            int c = random.nextInt(colors.length);
//            foods.add(new Food(x, y, colors[c]));
//        }
//    }
    public void setGameInterface(GameInterface gameInterface) {
        this.gameInterface = gameInterface;
    }


    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return this.speed;
    }

    @Override
    public void setSnakeRank(String rank) {
        gameInterface.setRank(rank);
    }

    @Override
    public void setViewMargin(int x, int y) {
        gameInterface.setViewMargin(x, y);
    }

    @Override
    public void setSnakeLen(int len) {
        gameInterface.setSnakeLen(len);
    }
}
