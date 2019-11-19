package com.snake.common.base;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 *基础视图
 */
public abstract class BaseView extends View {

    private MyThread thread;
    private boolean running = true;

    public BaseView(Context context) {
        super(context);
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected final void onDraw(Canvas canvas) {

        if(thread == null){
            thread = new MyThread();
            thread.start();
        } else {
            drawSub(canvas);//绘制图形（背景、食物、蛇）
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        running = false;
        super.onDetachedFromWindow();
    }

    protected abstract void drawSub(Canvas canvas);
    protected abstract void logic();
    protected abstract void checkPlayer();
    protected abstract void init();

    //游戏线程
    class MyThread extends Thread {
        @Override
        public void run() {
            init();//初始化

            while(running){
                logic();
                checkPlayer();
                postInvalidate();
                try {
                    Thread.sleep(100);//休眠100毫秒
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
