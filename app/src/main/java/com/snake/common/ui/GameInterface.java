package com.snake.common.ui;

/**
 *
 */
public interface GameInterface {

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
    /**
     * 显示排名
     * @param rank
     */
    void setRank(String rank);

    /**
     *玩家死亡，结束游戏
     */
    void setDeath();

    /**
     * 绘制图形
     */
    void draw();
}
