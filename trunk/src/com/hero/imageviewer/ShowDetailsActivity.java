package com.hero.imageviewer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class ShowDetailsActivity extends Activity implements View.OnClickListener, OnTouchListener {
    private ImageView imageView;
    private Button backBtn;
    //private static final float MAX_SCALE = 5;
	//private static final float MIN_SCALE = 0.3f;
	//private static final float MIN_LENGTH = 30f;
	//private static final int NONE = 0;
	//private static final int DRAG = 1;
	//private static final int ZOOM = 2;
	/** MatrixのgetValues用 */
	//private float[] values = new float[9];
	/** ドラッグ用マトリックス */
	//private Matrix moveMatrix = new Matrix();
	/** マトリックス */
	//private Matrix matrix = new Matrix();
	/** 画像移動用の位置 */
	//private PointF point = new PointF();
	/** ズーム時の座標 */
	//private PointF middle = new PointF();
	/** タッチモード。何も無し、ドラッグ、ズーム */
	//private int mode = NONE;
	/** Zoom開始時の二点間距離 */
	//private float initLength = 1;

	// 移動とズームに利用する
    // res/layout/main.xml にて android:scaleType=”matrix” 指定
    private Matrix matrix      = new Matrix();
    private Matrix savedMatrix = new Matrix();
    //private Matrix initmatrix  = new Matrix();
    private PointF start       = new PointF();
    private float oldDist      = 0f;
    private PointF mid         = new PointF();
    private float curRatio     = 1f;

    // 以下の状態を取り得る
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        String filepath = null;

        //アプリ名を非表示に設定
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.showdetails);

        //インテントの値を取得
        Bundle extras = getIntent().getExtras();
        if(extras!=null) filepath = extras.getString("filepath");


        //イメージビューを作成
        imageView = (ImageView) findViewById(R.id.preview);

        //画像データを表示
        if (filepath!=null) {
        	Bitmap bmp = null;
        	try{
        		BitmapFactory.Options sizeOption = new BitmapFactory.Options();
                sizeOption.inSampleSize = 4; //画像サイズを1/nsamplesizeにする
                bmp = FileDataUtil.loadBitmap(filepath,sizeOption);
                //android.util.Log.d("INFO","filepath = "+filepath);
                imageView.setImageBitmap(bmp);
        	}catch(Exception e){
        		e.printStackTrace();
        	}finally{
        	}
        }
        imageView.setOnTouchListener(this);

        //ボタンを作成
        backBtn = (Button) findViewById(R.id.backtolistbtn);
        backBtn.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v==backBtn) {
            //アクティビティの終了
            finish();
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView)v;

        // イベントのダンプ
        dumpEvent(event);

        /***********
         * ドラッグ
         ***********/
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            savedMatrix.set(matrix);
            start.set(event.getX(), event.getY());
            android.util.Log.d("MyApp", "mode=DRAG");
            mode = DRAG;
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            android.util.Log.d("MyApp", "mode=NONE");
            break;
        case MotionEvent.ACTION_MOVE:
            if (mode == DRAG) {
                matrix.set(savedMatrix);
                matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
            }
            break;
        }

        /***********
         * ズーム
         ***********/
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_POINTER_DOWN:
            oldDist = spacing(event);
            android.util.Log.d("MyApp", "oldDist=" + oldDist);
            // Android のポジション誤検知を無視
            if (oldDist > 10f) {
                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
                android.util.Log.d("MyApp", "mode=ZOOM");
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (mode != DRAG) {
                float newDist = spacing(event);
                float scale = newDist / oldDist;
                android.util.Log.d("MyApp", "scale=" + scale);
                float tmpRatio = curRatio * scale;
                if (0.1f < tmpRatio && tmpRatio < 20f) {
                    curRatio = tmpRatio;
                    matrix.postScale(scale, scale, mid.x, mid.y);
                }
            }
            break;
        }

        // 変換の実行
        view.setImageMatrix(matrix);

        return true; // イベントがハンドリングされたことを示す
    }
    /**
     * 2点間の距離を計算
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    /**
     * 2点間の中間点を計算
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
                             "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        // event.getAction() の 下位8bitはアクションコード、次の8bitはポインターID
        // ビット演算 の & と ビットシフト>> で分離する。
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_" ).append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
         || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid " ).append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")" );
        }
        sb.append("[" );
        //　event.getPointerCount() 何カ所ポイントされているか、
        // event.getX(),event.getY() で座標が取得できる
        // getPointerId() で、どのポインターIDについての情報かを判定出来る
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#" ).append(i);
            sb.append("(pid " ).append(event.getPointerId(i));
            sb.append(")=" ).append((int) event.getX(i));
            sb.append("," ).append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";" );
        }
        sb.append("]" );
        android.util.Log.d("MyApp", sb.toString());
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
    	int action = event.getAction();
    	int count = event.getPointerCount();
    	int id = (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;

    	switch(action & MotionEvent.ACTION_MASK) {
    		case MotionEvent.ACTION_DOWN:
    			Log.v("Multi", "Touch Down" + " count=" + count + ", id=" + id);
    			point.set(event.getX(), event.getY());
    			moveMatrix.set(matrix);
    			break;
    		case MotionEvent.ACTION_POINTER_DOWN:
    			Log.v("Multi", "Touch PTR Down" + " count=" + count + ", id=" + id);
    			if (initLength > MIN_LENGTH) {
    				moveMatrix.set(matrix);
    				mode = ZOOM;
    			}
    			break;
    		case MotionEvent.ACTION_UP:
    			Log.v("Multi", "Touch Up" + " count=" + count + ", id=" + id);
    			mode = NONE;
    			break;
    		case MotionEvent.ACTION_POINTER_UP:
    			Log.v("Multi", "Touch PTR Up" + " count=" + count + ", id=" + id);
    			mode = NONE;
    			break;
    		case MotionEvent.ACTION_MOVE:
    			Log.v("Multi", "Touch Move" + " count=" + count + ", id=" + id);
    			switch (mode) {
    			case DRAG:
    				matrix.set(moveMatrix);
    				matrix.postTranslate(event.getX() - point.x, event.getY() - point.y);
    				imageView.setImageMatrix(matrix);
    				break;
    			case ZOOM:
    				if (mode == ZOOM) {
    					float currentLength = getLength(event);
    					middle = getMiddle(event, middle);
    					if (currentLength > MIN_LENGTH) {
    						matrix.set(moveMatrix);
    						float scale = filter(matrix,currentLength / initLength);
    						matrix.postScale(scale, scale, middle.x, middle.y);
    						imageView.setImageMatrix(matrix);
    					}
    					break;
    				}
    				break;
    			}
    			break;
    	}
    	for(int i=0; i<count; i++) {
    		Log.v("Multi", " X=" + event.getX(i) + ", Y=" + event.getY(i) + ", id=" + event.getPointerId(i) );
    	}
    	return super.onTouchEvent(event);
    }*/

    /**
	 * 拡大縮小可能かどうかを判定する
	 * @param m
	 * @param s
	 * @return
	 */
	/*private float filter(Matrix m, float s){
		m.getValues(values);
		float nextScale = values[0]*s;
		if(nextScale > MAX_SCALE){
			s=MAX_SCALE/values[0];
		}
		else if(nextScale < MIN_SCALE){
			s=MIN_SCALE/values[0];
		}
		return s;
	}*/

	/**
	 * 比率を計算
	 * @param x
	 * @param y
	 * @return
	 */
	/*private float getLength(MotionEvent e) {
		float xx = e.getX(1) - e.getX(0);
		float yy = e.getY(1) - e.getY(0);
		return FloatMath.sqrt(xx * xx + yy * yy);
	}*/

	/**
	 * 中間点を求める
	 * @param e
	 * @param p
	 * @return
	 */
	/*private PointF getMiddle(MotionEvent e, PointF p) {
		float x = e.getX(0) + e.getX(1);
		float y = e.getY(0) + e.getY(1);
		p.set(x / 2, y / 2);
		return p;
	}*/
}
