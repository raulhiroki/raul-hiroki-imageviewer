package com.hero.imageviewer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;

public class FileDataUtil {

	private static final String IMAGE_ROOTPATH = "/mnt/sdcard/dcim/100MEDIA";		//画像フォルダ
	private static String mImgPath = null;

	public static Bitmap loadBitmap(String filePath, Object imageoption) throws IOException {
		Bitmap result = null;

		FileInputStream fileInput = null;
		BufferedInputStream bufInput = null;
		try {
			BitmapFactory.Options option = (Options)imageoption;
			//option = (Options)imageoption;
            String fileName = filePath;
		    result = BitmapFactory.decodeFile(fileName,option);
		}catch(Exception e){
			e.printStackTrace();
		} finally {
			if (fileInput != null) {
				try {
					fileInput.close();
				} catch (IOException e) {
	            }
			}
			if (bufInput != null) {
				try {
					bufInput.close();
				} catch (IOException e) {
	            }
			}
		}
		return result;
	}

	@SuppressWarnings("unused")
	public static File getSdCardRootDirectory() throws IOException {
		// SDカードがマウントされているか
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			throw new IOException(String.format("Do not set sdcard(%s)",
					Environment.getExternalStorageState()));
		}
		File root = new File(mImgPath);
		if (root == null) {
			throw new IOException("Could not read sdcard");
		}
		//android.util.Log.d("INFO","root = "+root.getPath());
		return root;
	}

	//画像閲覧パス設定
	public static void setImagePath(String strPath){
		if(strPath != null){
			mImgPath = strPath;
		}else{
			mImgPath = IMAGE_ROOTPATH;
		}

	}
}
