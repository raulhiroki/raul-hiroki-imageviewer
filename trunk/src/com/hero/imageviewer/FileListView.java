package com.hero.imageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FileListView extends ViewGroup
        implements AdapterView.OnItemClickListener, View.OnClickListener{


        //属性名
        public final static String STR_ATTR_SELECT_DIR = "select_dir";          //ディレクトリ選択
        public final static String STR_ATTR_DEFAULT_DIR = "default_dir";        //初期ディレクトリ
        public final static String STR_ATTR_DISPATCH_BACK_KEY = "dispatch_back";        //戻るキーを受けるか

        private View _mainView = null;

        private boolean _isSelectDir = false;                   //ディレクトリ選択
        private File _currentDir = null;                                //カレントディレクトリ
        private ArrayList<File> _currentDirFileList = null;             //現在のディレクトリのファイルの一覧
        private Stack<File> _historyFileList = null;    //たどったパスのスタック
        private boolean _isDispatchBackKey = true;              //戻るキーを受けるか

        private onFileListListener _listener = null;    //リスナ


        /**
         * コンストラクタ
         * @param context
         * @param is_select_dir
         */
        public FileListView(Context context, File default_dir, boolean is_select_dir){
                super(context);
                setCurrentDirectory(default_dir);
                setIsSelectDir(is_select_dir);
                init(null);
        }

        /**
         * コンストラクタ
         * @param context
         * @param attrs
         */
        public FileListView(Context context, AttributeSet attrs) {
                super(context, attrs);

                init(attrs);
        }


        /**
         * ディレクトリ選択
         * @param _isSelectDir the _isSelectDir to set
         * 後から変更不可
         */
        private void setIsSelectDir(boolean _isSelectDir) {
                this._isSelectDir = _isSelectDir;
        }
        /**
         * ディレクトリ選択
         * @return the _isSelectDir
         */
        public boolean isSelectDir() {
                return _isSelectDir;
        }


        /**
         * カレントディレクトリ
         * @param _currentDir the _currentDir to set
         */
        public void setCurrentDirectory(File _currentDir) {
                this._currentDir = _currentDir;
        }
        /**
         * カレントディレクトリ
         * @return the _currentDir
         */
        public File getCurrentDirectory() {
                if(_currentDir == null){
                        _currentDir = new File(Environment.getExternalStorageDirectory(), null);
                }
                return _currentDir;
        }

        /**
         * 現在のディレクトリのファイルの一覧
         * @return
         */
        public ArrayList<File> getCurrentDirFileList() {
                if(_currentDirFileList == null){
                        _currentDirFileList = new ArrayList<File>();
                }
                return _currentDirFileList;
        }

        /**
         * たどったパスのスタック
         * @return the _historyFileList
         */
        public Stack<File> getHistoryFileList() {
                if(_historyFileList == null){
                        _historyFileList = new Stack<File>();
                }
                return _historyFileList;
        }


        /**
         * 戻るキーを受けるか
         * @param _isDispatchBackKey the _isDispatchBackKey to set
         */
        public void setIsDispatchBackKey(boolean _isDispatchBackKey) {
                this._isDispatchBackKey = _isDispatchBackKey;
        }
        /**
         * 戻るキーを受けるか
         * @return the _isDispatchBackKey
         */
        public boolean isDispatchBackKey() {
                return _isDispatchBackKey;
        }

        /**
         * リスナ
         * @param _listener the _listener to set
         */
        public void setOnFileListListener(onFileListListener _listener) {
                this._listener = _listener;
        }
        /**
         * リスナ
         * @return the _listener
         */
        public onFileListListener getOnFileListListener() {
                return _listener;
        }


        /**
         * 初期化
         * @param attrs
         */
        private void init(AttributeSet attrs){
                String temp = null;

                if(attrs == null){
                }else{
                        //ディレクトリ選択
                        setIsSelectDir(attrs.getAttributeBooleanValue(null, STR_ATTR_SELECT_DIR, false));
                        //初期ディレクトリ
                        temp = attrs.getAttributeValue(null, STR_ATTR_DEFAULT_DIR);
                        if(temp != null){
                                setCurrentDirectory(new File(temp));
                        }else{
                                //未指定の場合はmicroSD
                                setCurrentDirectory(new File(Environment.getExternalStorageDirectory(), null));
                        }
                        //戻るキーを受けるか
                        setIsDispatchBackKey(attrs.getAttributeBooleanValue(null, STR_ATTR_DISPATCH_BACK_KEY, true));
                }

            //inflaterを使ってxmlのレイアウトをViewに反映する
            LayoutInflater inflater = (LayoutInflater)getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int id = 0;
            if(isSelectDir()){
                id = com.hero.imageviewer.R.layout.file_list_view_dir;
            }else{
                id = com.hero.imageviewer.R.layout.file_list_view;
            }
            _mainView = inflater.inflate(id, null);

            //ディレクトリ選択イベント
            Button button = (Button)_mainView.findViewById(R.id.FileSelectListOK_Button);
            if(button != null){
                    button.setOnClickListener(this);
            }
            //リストビューのイベント
                ListView list = (ListView)_mainView.findViewById(R.id.FileList_ListView);
                if(list != null){
                        list.setOnItemClickListener(this);
                }

                //初期表示
            viewFiles(getCurrentDirectory());
        }


        /**
         * レイアウト確定
         */
        @Override
        protected void onLayout(boolean changed, int left, int top, int right,
                int bottom) {

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT);

            addViewInLayout(_mainView, -1, lp);

            //子要素の描画範囲でレイアウトを作成する
            _mainView.layout(0, 0, _mainView.getMeasuredWidth(), _mainView.getMeasuredHeight());

        }

        /**
         * サイズ計測
         */
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //子要素に必要な大きさを計測させる
            _mainView.measure(widthMeasureSpec, heightMeasureSpec);

                int spec_width = MeasureSpec.getSize(widthMeasureSpec);
                int spec_height = _mainView.getMeasuredHeight();

                //サイズ設定
                setMeasuredDimension(spec_width, spec_height);
        }

        /**
         * キーイベント
         */
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
                boolean ret = false;
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                        if(!isDispatchBackKey()){
                                //キーは無視
                        }else if(event.getAction() == KeyEvent.ACTION_DOWN){
                                //戻るボタンが押された
                                try{
                                        File dir = getHistoryFileList().pop();
                                        dir = getHistoryFileList().pop();
                                        ret = viewFiles(dir);
                                }catch(Exception e){
                                }
                        }
                }
                if(ret){
                        return ret;
                }else{
                        return super.dispatchKeyEvent(event);
                }
        }

        /**
         * ファイル一覧を登録する
         * @param path
         */
        private boolean viewFiles(File dir){
                boolean ret = false;
                ListView list = (ListView)_mainView.findViewById(R.id.FileList_ListView);
                if(dir == null || list == null){
                }else if(!dir.canRead()){
                        //読めない
                }else{
                        //アダプタ作成
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext()
                                        , android.R.layout.simple_list_item_1);

                        File[] file_lists = dir.listFiles();
                        Arrays.sort(file_lists);

                        //クリア
                        getCurrentDirFileList().clear();
                        //現在のディレクトリ
                        setCurrentDirectory(dir);
                        viewCurrentDirectory(dir);

                        if(file_lists == null){
                        }else{
                                //追加
                                for (File file : file_lists) {
                                        if(file.isDirectory()){
                                                //ディレクトリの場合
                                                adapter.add(file.getName() + "/");
                                                getCurrentDirFileList().add(file);
                                        }else{
                                                //通常のファイル
                                                if(isSelectDir()){
                                                        //ディレクトリ選択モードの時は何もしない
                                                }else{
                                                        adapter.add(file.getName());
                                                        getCurrentDirFileList().add(file);
                                                }
                                        }
                                }
                        }
                        //空っぽ
                        if(adapter.getCount() == 0){
                                //adapter.add(getContext().getString(R.string.file_list_empty));
                        }
                        //履歴に保存
                        getHistoryFileList().push(dir);

                        //アダプタを設定
                        list.setAdapter(adapter);

                        ret = true;
                }
                return ret;
        }

        /**
         * 現在のパスを表示する
         * @param dir
         */
        private void viewCurrentDirectory(File dir){
                TextView text = (TextView)_mainView.findViewById(R.id.FileListCurrentPath_TextView);
                if(dir == null || text == null){
                }else{
                        text.setText(dir.getAbsolutePath());
                }
        }

        /**
         * アイテムの選択
         */
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(getCurrentDirFileList() == null){
                }else if(getCurrentDirFileList().size() <= position){
                }else{
                        File file = getCurrentDirFileList().get(position);

                        if(file.isDirectory()){
                                //ディレクトリをたどる
                                if(!viewFiles(file)){
                                        file = null;    //失敗はnullを通知する
                                }
                                if(getOnFileListListener() != null){
                                        getOnFileListListener().onChangeDirectory(file);
                                }
                        }else{
                                //ファイルだったのでイベントで通知
                                if(getOnFileListListener() != null){
                                        getOnFileListListener().onSelectFile(file);
                                }
                        }
                }
        }


        /**
         * ボタンが押された
         */
        public void onClick(View v) {
                switch(v.getId()){
                case R.id.FileSelectListOK_Button:
                        //現在のディレクトリをイベントで通知
                        if(getOnFileListListener() != null){
                                getOnFileListListener().onSelectDirectory(getCurrentDirectory());
                        }
                        break;
                default:
                        break;
                }
        }


        /**
         * 選択した時のリスナ用インターフェースクラス
         * @author Iori
         *
         */
        public interface onFileListListener{
                public void onSelectFile(File file);
                public void onSelectDirectory(File file);
                public void onChangeDirectory(File file);
        }

}

