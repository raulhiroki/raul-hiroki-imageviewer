package com.hero.imageviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;


public class ShowGalleryViewActivity extends Activity implements DialogInterface.OnClickListener,OnItemClickListener,Runnable{

	/** Called when the activity is first created. */
	//private static final String IMAGE_ROOTPATH = "/dcim/100MEDIA";		//画像フォルダ

	private ProgressDialog dialog;
	private ImageView imageView;
	private Gallery gallery;
	private GalleryAdapter galleryAdapter;
	private Bitmap[] thumbnail;
	private List<String> dirList = new ArrayList<String>();
	private List<String> tmp = new ArrayList<String>();
	private String strImagePath = "";
	private static final int DIALOG_ID_PROGRESS = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		if(!sdcardReadReady()){
			new AlertDialog.Builder(this)
			.setMessage("Could not access to SD Card")
			.setNeutralButton("OK", this)
			.show();
			return;
		}

		//インテントの値を取得
        Bundle extras = getIntent().getExtras();
        if(extras!=null) strImagePath = extras.getString("filepath");
		//strImagePath = Environment.getExternalStorageDirectory().getPath() + IMAGE_ROOTPATH;

		dirList.add(strImagePath);
		int i = 0;
		int j = 0;
		while(dirList.size() > i){
			File subDir = new File(dirList.get(i));
			String subFileName[] = subDir.list();
			Arrays.sort(subFileName);
			j = 0;
			if(subFileName != null){
				while(subFileName.length > j){
					File subFile = new File(subDir.getPath() + "/" + subFileName[j]);
					if(subFile.isDirectory()){
						String _st =subDir.getPath() + "/" + subFileName[j];
						dirList.add(_st);
					}else{
						tmp.add(subDir.getPath() + "/" + subFileName[j]);
					}
					j++;
				}
			}
			i++;
		}

		if(tmp.isEmpty()){
			new AlertDialog.Builder(this)
			.setMessage("Not Found Images")
			.setNeutralButton("OK", this)
			.show();
			return;
		}

		galleryAdapter = new GalleryAdapter(this);

		gallery = (Gallery)findViewById(R.id.gallery1);
		gallery.setSpacing(4);
		gallery.setAdapter(galleryAdapter);
		gallery.setOnItemClickListener(this);

		imageView = (ImageView)findViewById(R.id.imageView1);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
		imageView.setAnimation(animation);
		//Bitmap picture = BitmapFactory.decodeFile(tmp.get(0));
		Bitmap picture = null;
		BitmapFactory.Options imageption = new BitmapFactory.Options();
        imageption.inSampleSize = 4;
		try {
			picture = FileDataUtil.loadBitmap(tmp.get(0),imageption);
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageView.setImageBitmap(picture);

		//tmp.toArray();
		//dialog = new ProgressDialog(this);
		//dialog.setMessage("Loading Images...");
		//dialog.show();


	}

	@Override
	public void onStart(){
		super.onStart();
		showDialog(DIALOG_ID_PROGRESS);
		thumbnail = new Bitmap[tmp.size()];
		new Thread(this).start();
		galleryAdapter.notifyDataSetChanged();
	}

	@Override
    protected Dialog onCreateDialog(int id) {
        return createDialog(id);
    }

	private Dialog createDialog(int id) {
        if (id == DIALOG_ID_PROGRESS) {
        	dialog = new ProgressDialog(this);
    		dialog.setMessage("Loading Images...");
    		//dialog.show();
    		return dialog;
        }
        return null;
    }

	public void run(){
		for(int i = 0 ; i < tmp.size(); i++){
			BitmapFactory.Options imageption = new BitmapFactory.Options();
			imageption.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(tmp.get(i), imageption);
			int height = imageption.outHeight;
			int scale = height /50;
			imageption.inSampleSize = scale;
			imageption.inJustDecodeBounds = false;
			try {
				thumbnail[i] = FileDataUtil.loadBitmap(tmp.get(i),imageption);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		dialog.dismiss();
	}

	private boolean sdcardReadReady(){
		String state = Environment.getExternalStorageState();
		return (Environment.MEDIA_MOUNTED.equals(state)||Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
	}

	public void onClick(DialogInterface dialog, int which) {
		switch(which){
		case DialogInterface.BUTTON_NEUTRAL:
			dialog.dismiss();
			finish();
		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if(arg0 == gallery){
			//Bitmap picture = BitmapFactory.decodeFile(tmp.get(arg2));
			Bitmap picture = null;
			BitmapFactory.Options imageption = new BitmapFactory.Options();
            imageption.inSampleSize = 4;
			try {
				picture = FileDataUtil.loadBitmap(tmp.get(arg2),imageption);
			} catch (IOException e) {
				e.printStackTrace();
			}
			imageView.setImageBitmap(picture);
		}
	}

	public class GalleryAdapter extends BaseAdapter{
		private int galleryItemBackground;

		public GalleryAdapter(Context context) {

		      TypedArray typedArray =
		         context.obtainStyledAttributes(R.styleable.myGallery);

		      // typedArray.getResourceId(int index, int defValue)
		      //    Retrieve the resource identifier for the attribute at index.
		      galleryItemBackground = typedArray.getResourceId(
		            R.styleable.myGallery_android_galleryItemBackground, 0);

		      // Give back a previously retrieved StyledAttributes, for later re-use.
		      //  後の再使用のために以前に検索されたStyledAttributesを返す。
		      typedArray.recycle();
		   }

		public int getCount() {
			// TODO Auto-generated method stub
			return tmp.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView view;
			if(convertView == null){
				view = new ImageView(ShowGalleryViewActivity.this);
				view.setImageBitmap(thumbnail[position]);
			}else{
				view = (ImageView)convertView;
			}
			// イメージビューの背景
			view.setBackgroundResource(galleryItemBackground);
			notifyDataSetChanged();
			return view;
		}
	}
}
