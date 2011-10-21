package com.hero.imageviewer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ShowListActivity extends Activity implements View.OnClickListener {
    private Button goToGridBtn;
    private ListView list;

    private static File[] mfileList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //アプリ名を非表示に設定
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.showlist);

        //リストビューを作成
        list = (ListView) findViewById(R.id.imagelist);
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(500);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        list.setLayoutAnimation(controller);

        //サブレイアウト内にボタン
        goToGridBtn = (Button) findViewById(R.id.backtogridviewbtn);
        goToGridBtn.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
        	File file = (File)extras.get("filelist");
        	mfileList = file.listFiles();
        	Arrays.sort(mfileList);
        }

        //リストビューのアダプタを設定
        list.setAdapter(new ListAdapter(this));
        list.setFastScrollEnabled(mfileList.length>50);

        //リストビューにコールバックリスナーを設定
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressWarnings("rawtypes")
			public void onItemClick(AdapterView parent
                    , View view, int position, long id) {
                goToDetails(position);
            }
        });
    }

    public void onClick(View v) {
        if (v==goToGridBtn) {
            //アクティビティの終了
            finish();
        }
    }

    private static class ListAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
        	if(mfileList != null){
        		return mfileList.length;
        	}
        	return 0;
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
                convertView = inflater.inflate(R.layout.row, null);
            }

            //イメージビューに画像を表示
            ImageView imageView = (ImageView) convertView.findViewById(R.id.photo);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            String filePath = mfileList[position].toString();
            BitmapFactory.Options imageOption = new BitmapFactory.Options();
            imageOption.inSampleSize = 4;
            Bitmap bmp = null;
			try {
				bmp = FileDataUtil.loadBitmap(filePath,imageOption);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
            imageView.setImageBitmap(bmp);

            //テキストビューに文字列を表示
            TextView textView = (TextView) convertView.findViewById(R.id.filename);
            textView.setPadding(10, 0, 0, 0);
            textView.setTextColor(Color.rgb(255, 255, 255));
            filePath = filePath.replace(MainActivity.mimageFolder.toString()+"/", "");
            String fileName = filePath.substring(0, filePath.indexOf('.'));
            textView.setText(fileName);

            return convertView;
        }
    }

    //レコードの詳細表示画面を表示
    private void goToDetails(int position) {

        //明示的なインテントの生成
        Intent intent = new Intent(this, ShowDetailsActivity.class);

        //インテントに渡すパラメータの設定
        intent.putExtra("filepath", mfileList[position].toString());

        //アクティビティの呼び出し
        startActivityForResult(intent, 0);
    }
}