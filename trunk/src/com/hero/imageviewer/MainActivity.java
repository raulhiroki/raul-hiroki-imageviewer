package com.hero.imageviewer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hero.imageviewer.FileListDialog.onFileListDialogListener;

public class MainActivity extends Activity implements View.OnClickListener{
    public static String mimageFolder ;
    private static File[] mfileList;
    private File mSdcard;

    private static final int GRIDSIZE = 16;
    private static int pageOffset = 0;
    private static final int SCROLL_NONE = 0;
    private int slideLimitFlg = SCROLL_NONE;
    private static String ROOTDIR = "/mnt/sdcard";

    private GridView imageGrid;
    private ImageButton prevBtn, nextBtn, goToListBtn, goToGalleryBtn;
    private ImageAdapter imageAdapter;
    private GestureDetector gd;
    private ViewGroup mContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //アプリ名を非表示に設定
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        mContainer = (ViewGroup) findViewById(R.id.mainview);

        gd = new GestureDetector(this, onGestureListener);

        //グリッドビューを作成して配置
        imageGrid = (GridView) findViewById(R.id.image_grid);

        //サブレイアウト内にボタンを配置
        prevBtn = (ImageButton) findViewById(R.id.btnPrev);
        prevBtn.setOnClickListener(this);
        //prevBtn.setEnabled(false);

        //サブレイアウト内にボタンを配置
        goToListBtn = (ImageButton) findViewById(R.id.btnGoList);
        goToListBtn.setOnClickListener(this);

        goToGalleryBtn = (ImageButton) findViewById(R.id.btnGoGallery);
        goToGalleryBtn.setOnClickListener(this);

        //サブレイアウト内にボタンを配置
        nextBtn = (ImageButton) findViewById(R.id.btnNxt);
        nextBtn.setOnClickListener(this);

        //SDカードのフォルダ情報を取得
        mSdcard = Environment.getExternalStorageDirectory();

        SharedPreferences sp = getSharedPreferences("imgpath", MODE_PRIVATE);
        String strSavepath = sp.getString("path", "");

        // Since we are caching large views, we want to keep their cache
        // between each animation
        mContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);

        //画像データが保存されているフォルダを取得
        File dir = null;
		try {
			//閲覧フォルダを設定
			if(strSavepath.length() == 0){
				FileDataUtil.setImagePath(null);
			}else{
				FileDataUtil.setImagePath(strSavepath);
			}
			dir = FileDataUtil.getSdCardRootDirectory();
			mimageFolder = dir.getPath().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(dir == null){
			finish();
		}

        //フォルダが存在するかどうかを判定
        if(!(dir.exists())) {
            //SDカードが使用可能なときフォルダを作成
            if (mSdcard.canWrite()){
                if(dir.mkdirs()){
                	showDialog(this, "Complete", "Created "+mimageFolder);
                }
            }

            //ボタンを使用不能に設定
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
        } else {
            //フォルダ内のファイルリストを作成
            mfileList = dir.listFiles();
            Arrays.sort(mfileList);

            //グリッドビューにイメージアダプタを設定
            imageAdapter = new ImageAdapter(this);
            imageGrid.setAdapter(imageAdapter);

            //グリッドビューにコールバックリスナーを設定
            imageGrid.setOnItemClickListener(new OnImageGridClick());
            imageGrid.setOnTouchListener(mTouchListener);

            //画像データが1ページで収まるときはボタンを使用不能に設定
            if (mfileList.length<=GRIDSIZE) nextBtn.setEnabled(false);

            if (pageOffset==0) prevBtn.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
    	SharedPreferences sp = getSharedPreferences("imgpath", MODE_PRIVATE);
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putString("path", mimageFolder);
    	editor.commit();
    	super.onDestroy();
    }

    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	android.util.Log.d("INFO","onTouchEvent");
    	if (gd.onTouchEvent(event)) {
			return true;
		} else {
			return super.onTouchEvent(event);
		}
	}

 // タッチ処理リスナー
    OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
        	int x, y;

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            	android.util.Log.d("INFO", "MotionEvent.ACTION_DOWN");
                break;

            case MotionEvent.ACTION_MOVE:
            	android.util.Log.d("INFO", "MotionEvent.ACTION_MOVE");
                break;

            case MotionEvent.ACTION_UP:
            	android.util.Log.d("INFO", "MotionEvent.ACTION_UP");
                break;

            case MotionEvent.ACTION_CANCEL:
            	android.util.Log.d("INFO", "MotionEvent.ACTION_CANCEL");
                break;
            }

            x = (int)event.getX();
            y = (int)event.getY();
            android.util.Log.d("INFO"," x:" + Integer.toString(x) + " y:" + Integer.toString(y));

            return gd.onTouchEvent(event);
        }
    };

    public void onClick(View v) {
        //前の画面を表示
        if (v==prevBtn) {
            if (pageOffset>0) {
                pageOffset--;
                if (pageOffset==0) prevBtn.setEnabled(false);
                nextBtn.setEnabled(true);
            }
            applyRotation(-1, 180, 90);
        }

        //次の画面を表示
        if (v==nextBtn) {
            if ((pageOffset+1)*GRIDSIZE<mfileList.length) {
                pageOffset++;
                prevBtn.setEnabled(true);
                if (pageOffset==mfileList.length/GRIDSIZE) nextBtn.setEnabled(false);
            }
            applyRotation(pageOffset, 0, 90);
        }


        imageAdapter.notifyDataSetChanged();

        //リスト形式の表示に切替
        if (v==goToListBtn) {
            //明示的なインテントを生成（検索結果のリスト表示画面）
            Intent intent = new Intent(this, ShowListActivity.class);
            try {
				intent.putExtra("filelist", FileDataUtil.getSdCardRootDirectory());
			} catch (IOException e) {
				e.printStackTrace();
			}

            //アクティビティの呼び出し
            startActivity(intent);
        }

        if (v==goToGalleryBtn){
        	//明示的なインテントの生成
            Intent intent = new Intent(this, ShowGalleryViewActivity.class);

            //インテントに渡すパラメータの設定
            intent.putExtra("filepath", mimageFolder);

            //アクティビティの呼び出し
            startActivity(intent);
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        case R.id.setdir:
        	FileListDialog dialog = new FileListDialog(this);
        	dialog.setDirectorySelect(true);
        	dialog.setOnFileListDialogListener(new onFileListDialogListener() {
        	    public void onClickFileList(File file) {
        	        if(file == null){
        	            //not select
        	        }else{
        	            android.util.Log.d("INFO","select = "+ file.getPath());
        	            //閲覧フォルダを設定
        				//FileDataUtil.setImagePath(file.getPath());
        				refleshview(file);
        	        }
        	    }
        	});
        	dialog.show(ROOTDIR, "select");
            ret = true;
            break;
        }
        return ret;
    }

    /**
     * Setup a new 3D rotation on the container view.
     *
     * @param position the item that was clicked to show a picture, or -1 to show the list
     * @param start the start angle at which the rotation must begin
     * @param end the end angle of the rotation
     */
    private void applyRotation(int position, float start, float end) {
        // Find the center of the container
        final float centerX = mContainer.getWidth() / 2.0f;
        final float centerY = mContainer.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Rotate3dAnimation rotation =
                new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        rotation.setDuration(200);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(mContainer,position));

        mContainer.startAnimation(rotation);
    }

    private boolean refleshview(File selectdir){
    	File dir = selectdir;

		mimageFolder = dir.getPath().toString();

		//フォルダ内のファイルリストを作成
        mfileList = dir.listFiles();
        Arrays.sort(mfileList);

        pageOffset=0;
        imageAdapter.notifyDataSetChanged();


        //画像データが1ページで収まるときはボタンを使用不能に設定
        if (mfileList.length<=GRIDSIZE) {
        	nextBtn.setEnabled(false);
        }else{
        	nextBtn.setEnabled(true);
        }

        if (pageOffset==0) prevBtn.setEnabled(false);
		return true;

    }

    private static class ImageAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public ImageAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            int count = Math.min(mfileList.length-pageOffset*GRIDSIZE, GRIDSIZE);
            return count;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            //表示するビューが存在しないときは作成
            if(convertView == null){
                convertView = inflater.inflate(R.layout.griditem, null);
            }

            //イメージビューに画像を表示
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.photo);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(116, 116));
            imageView.setPadding(5, 0, 5, 0);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            final String filePath = mfileList[position+pageOffset*GRIDSIZE].toString();
            BitmapFactory.Options sizeOption = new BitmapFactory.Options();
            sizeOption.inSampleSize = 4; //画像サイズを1/nsamplesizeにする
            Bitmap bmp = null;
            try {
				bmp = FileDataUtil.loadBitmap(filePath,sizeOption);
			} catch (IOException e) {
				e.printStackTrace();
			}
            imageView.setImageBitmap(bmp);


            //テキストビューに文字列を表示
            TextView textView = (TextView) convertView.findViewById(R.id.filename);
            textView.setPadding(5, 0, 5, 20);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextColor(Color.rgb(255, 255, 255));
            String fileName  = mfileList[position+pageOffset*GRIDSIZE].getName();
            fileName = fileName.substring(0, fileName.indexOf('.'));
            textView.setText(fileName);

            notifyDataSetChanged();

            return convertView;
        }
    }

    /* 画像クリック */
    public class OnImageGridClick implements OnItemClickListener {

        @SuppressWarnings("rawtypes")
		public void onItemClick(AdapterView parent
                , View v, int position, long id) {

            //明示的なインテントの生成
            Intent intent = new Intent(MainActivity.this, ShowDetailsActivity.class);

            //インテントに渡すパラメータの設定
            intent.putExtra("filepath"
                , mfileList[position+pageOffset*GRIDSIZE].toString());

            //アクティビティの呼び出し
            startActivity(intent);
        }
    }

    //ダイアログの表示
    private static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }});
        ad.create();
        ad.show();
    }

	private final SimpleOnGestureListener onGestureListener = new SimpleOnGestureListener() {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			android.util.Log.d("INFO", "onDoubleTap");
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			android.util.Log.d("INFO", "onDoubleTapEvent");
			return super.onDoubleTapEvent(e);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			android.util.Log.d("INFO", "onDown");
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			android.util.Log.d("INFO","onFling");
			if (slideLimitFlg == SCROLL_NONE) {
	    		if (velocityX < 0) {
	    			// 左フリック
	    			android.util.Log.d("INFO","Left onFling");
	    			onClick(nextBtn);
	    		} else if (velocityX > 0) {
	    			// 右フリック
	    			android.util.Log.d("INFO","Right onFling");
	    			onClick(nextBtn);
	    		}
	    	}
	    	return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			android.util.Log.d("INFO", "onLongPress");
			super.onLongPress(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			android.util.Log.d("INFO", "onScroll");
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public void onShowPress(MotionEvent e) {
			android.util.Log.d("INFO", "onShowPress");
			super.onShowPress(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			android.util.Log.d("INFO", "onSingleTapConfirmed");
			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			android.util.Log.d("INFO", "onSingleTapUp");
			return super.onSingleTapUp(e);
		}
	};


}