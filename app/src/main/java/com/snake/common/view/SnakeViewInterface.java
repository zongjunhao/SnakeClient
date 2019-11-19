package com.snake.common.view;

import com.snake.common.bean.Food;
//import com.snake.bean.Snake;
import com.snake.common.bean.Food;
import com.snake.common.bean.SnakeBean;

import java.util.List;

/**
 * Created by smile on 2016/9/8.
 */
public interface SnakeViewInterface {

    /**
     * 设置界面显示的区域
     * @param x
     * @param y
     */
    void setViewMargin(int x, int y);

    /**
     * 显示蛇的长度
     * @param len
     */
    void setSnakeLen(int len);

    void setSnakeRank(String rank);



}
