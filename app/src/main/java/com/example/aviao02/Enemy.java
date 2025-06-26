package com.example.aviao02;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

public class Enemy {
    private Bitmap bitmap;
    private float x, y;
    private float speed;

    public Enemy(Bitmap bitmap, float x, float y, float speed) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public void update() {
        y += speed;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    public boolean isOutOfScreen(int screenHeight) {
        return y > screenHeight;
    }

    public RectF getRect() {
        return new RectF(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
    }

    // Adicione esses getters:
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
