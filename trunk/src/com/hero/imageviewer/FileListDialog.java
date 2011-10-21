package com.hero.imageviewer;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;

import com.hero.imageviewer.FileListView.onFileListListener;


/**
 * ファイルリストダイアログクラス
 *
 */
public class FileListDialog implements onFileListListener{

	private Context _parent = null;                                                 //親
	private File _currentFile = null;                                               //現在の選択
	private onFileListDialogListener _listener = null;              //リスナー
	private boolean _isDirectorySelect = false;                     //ディレクトリ選択をするか？
	private CustomAlertDialog _dialog = null;

	/**
	 * ディレクトリ選択をするか？
	 * @param is
	 */
	public void setDirectorySelect(boolean is){
	        _isDirectorySelect = is;
	}
	public boolean isDirectorySelect(){
	        return _isDirectorySelect;
	}

	/**
	 * 選択されたファイル名取得
	 * @return
	 */
	public String getSelectedFileName(){
	        String ret = "";
	        if(_currentFile != null){
	                ret = _currentFile.getAbsolutePath();
	        }
	        return ret;
	}

	/**
	 * ファイル選択ダイアログ
	 * @param context 親
	 */
	public FileListDialog(Context context){
	        _parent = context;
	}

	/**
	 * ダイアログ表示
	 * @param context 親
	 * @param path 表示したいディレクトリ
	 * @param title ダイアログのタイトル
	 */
	public void show(String path, String title){

	        if((path == null) || (path.length() == 0)){
	                path = Environment.getExternalStorageDirectory().getPath();
	        }

	        FileListView list = new FileListView(_parent, new File(path), isDirectorySelect());
	        list.setOnFileListListener(this);

	        _dialog = new CustomAlertDialog(_parent);
	        _dialog.setTitle(title);
	        _dialog.setView(list);
	        _dialog.setButton("Cancel", new OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                        //nop
	                }
	        });
	        _dialog.show();
	}

	/**
	 * リスナーのセット
	 * @param listener
	 */
	public void setOnFileListDialogListener(onFileListDialogListener listener){
	        _listener = listener;
	}


	/**
	 * ダイアログでファイルが選択された
	 */
	public void onSelectFile(File file) {
	        _currentFile = file;
	        if(_dialog != null){
	                _dialog.dismiss();
	                _dialog = null;
	        }
	        if(_listener != null){
	                _listener.onClickFileList(file);
	        }
	}

	/**
	 * ダイアログでディレクトリが選択された
	 */
	public void onSelectDirectory(File file) {
	        _currentFile = file;
	        if(_dialog != null){
	                _dialog.dismiss();
	                _dialog = null;
	        }
	        if(_listener != null){
	                _listener.onClickFileList(file);
	        }
	}

	/**
	 * ディレクトリが変更された
	 */
	public void onChangeDirectory(File file) {
	        //nop
	}



	/**
	 * クリックイベントのインターフェースクラス
	 *
	 */
	public interface onFileListDialogListener{
	        public void onClickFileList(File file);
	}

	/**
	 * カスタムダイアログ
	 *
	 */
	private static class CustomAlertDialog extends AlertDialog {

	        protected CustomAlertDialog(Context context) {
	                super(context);
	        }


	        protected CustomAlertDialog(Context context, int theme) {
	                super(context, theme);
	        }


	        protected CustomAlertDialog(Context context, boolean cancelable,
	                        OnCancelListener cancelListener) {
	                super(context, cancelable, cancelListener);
	        }
	}

}
