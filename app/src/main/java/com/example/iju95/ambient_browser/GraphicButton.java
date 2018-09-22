package com.example.iju95.ambient_browser;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by iju95 on 2018-06-14.
 */

public class GraphicButton {
    public static final int IMAGE_UP = 0;
    public static final int IMAGE_DOWN = 1;
    public static final int IMAGE_RE = 2;
    private Bitmap[] mImage;
    private Rect mRect;
    public int mImageNum;

    public GraphicButton(Rect rect) {
        mRect = rect;
        mImage = new Bitmap[3];
        mImageNum = IMAGE_UP;   // 0
    }

    public void setImages(Bitmap upimage, Bitmap downimage,Bitmap reimage) {
//이미지를 눌린 상태 및 아닌 상태 두가지로 저장한다
        mImage[IMAGE_UP] = upimage;
        mImage[IMAGE_DOWN] = downimage;
        mImage[IMAGE_RE] = reimage;
    }

    public boolean touch(int tx, int ty) {
//이미지 좌표와 터치 좌표를 비교한다
        Rect rect = mRect;
        if((rect.left < tx && rect.right > tx) && (rect.top < ty && rect.bottom > ty))
            return true;
        return false;
    }

    public void setPress(int press) {
//버튼의 이미지를 변경하여 눌린 상태 및 아닌 상태를 표시한다
        if(press ==0){
            mImageNum=0;
        }else if(press ==1){
            mImageNum=1;
        }else{
            mImageNum=2;
        }
    }

    public void draw(Canvas canvas) {
//이미지 번호대로 이미지를 출력한다
        int imagenum = mImageNum;
        Paint paint= new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);                    // 적색

        if(mImage[imagenum] != null) {
            canvas.drawBitmap(mImage[imagenum], null, mRect, null);
            //     canvas.drawText("Test!",100,500,paint);
        }
    }

}