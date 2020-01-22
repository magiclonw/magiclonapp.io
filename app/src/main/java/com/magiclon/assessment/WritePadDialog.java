package com.magiclon.assessment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * @author：MagicLon
 * @date：2018/9/6 006
 * email：1348149485@qq.com
 * detail：
 */
public class WritePadDialog extends Dialog {

    Context context;
    WindowManager.LayoutParams p ;
    DialogListener dialogListener;
    int BACKGROUND_COLOR = Color.WHITE;
    PaintView mView;

    public WritePadDialog(Context context,DialogListener dialogListener) {
        super(context);
        this.context = context;
        this.dialogListener = dialogListener;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.write_pad);
        WindowManager wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        p = getWindow().getAttributes();
        p.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        p.height =(int) ((wm.getDefaultDisplay().getWidth())*0.6);
        p.width = (wm.getDefaultDisplay().getWidth());
        getWindow().setAttributes(p);

        mView = new PaintView(context);
        FrameLayout frameLayout = findViewById(R.id.tablet_view);
        frameLayout.addView(mView);
        mView.requestFocus();
        Button btnClear = findViewById(R.id.tablet_clear);
        btnClear.setOnClickListener(v -> mView.clear());

        Button btnOk = findViewById(R.id.tablet_ok);
        btnOk.setOnClickListener(v -> {
            try {
                dialogListener.refreshActivity(Base64BitmapUtil.bitmapToBase64(mView.getCachebBitmap()));
                try {
                    mView.getCachebBitmap().recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                WritePadDialog.this.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button btnCancel = findViewById(R.id.tablet_cancel);
        btnCancel.setOnClickListener(v -> cancel());
    }


    /**
     * This view implements the drawing canvas.
     *
     * It handles all of the input events and drawing functions.
     */
    class PaintView extends View {
        private Paint paint;
        private Canvas cacheCanvas;
        private Bitmap cachebBitmap;
        private Path path;


        public Bitmap getCachebBitmap() {
//            cachebBitmap=Base64BitmapUtil.resizeBitmap(cachebBitmap,160,80);
            return cachebBitmap;
        }

        public PaintView(Context context) {
            super(context);
            init();
        }

        private void init(){
            paint = new Paint(Paint.DITHER_FLAG);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(4);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(Color.BLACK);
            paint.setDither(true);
            path = new Path();
            cachebBitmap = Bitmap.createBitmap(p.width, p.height, Bitmap.Config.RGB_565);
            cacheCanvas = new Canvas(cachebBitmap);
            cacheCanvas.drawColor(Color.WHITE);
        }
        public void clear() {
            if (cacheCanvas != null) {
                paint.setColor(BACKGROUND_COLOR);
                cacheCanvas.drawPaint(paint);
                paint.setColor(Color.BLACK);
                cacheCanvas.drawColor(Color.WHITE);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // canvas.drawColor(BRUSH_COLOR);
            if(cachebBitmap==null||cachebBitmap.isRecycled()){
                cachebBitmap = Bitmap.createBitmap(p.width, p.height, Bitmap.Config.RGB_565);
                cacheCanvas = new Canvas(cachebBitmap);
                cacheCanvas.drawColor(Color.WHITE);
            }
            canvas.drawBitmap(cachebBitmap, 0, 0, null);
            canvas.drawPath(path, paint);
        }

        private float cur_x, cur_y;

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    cur_x = x;
                    cur_y = y;
                    cacheCanvas.drawPoint(cur_x,cur_y,paint);
                    path.moveTo(cur_x, cur_y);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    path.quadTo(cur_x, cur_y, (x+cur_x)/2, (y+cur_y)/2);
                    cur_x = x;
                    cur_y = y;
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    cacheCanvas.drawPath(path, paint);
                    path.reset();
                    break;
                }
                default:
                    break;
            }

            invalidate();

            return true;
        }
    }


}