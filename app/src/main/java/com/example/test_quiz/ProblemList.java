package com.example.test_quiz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar; //デフォルトではandroidxじゃない古い方のToolbarのままimportされるので変更

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class ProblemList extends AppCompatActivity { //一覧表示画面のアクティビティー　一覧表示のため、RecyclerViewを利用している
    private RecyclerView.LayoutManager layoutManager;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_list); //今まで気づいてなかったけど、ここでレイアウトxmlと紐づけしている
        final ArrayList<ArrayList<String>> quizAllList = setQuizList(null);
        myAdapter = new MyAdapter(quizAllList);


        final RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true); //アダプターのコンテンツの大きさによってリサイクルビューの大きさが変わらないならtrueで最適化されるらしい

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        recyclerView.setAdapter(myAdapter); //RecyclerViewを継承したアダプターをセット


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("一覧");

        toolbar.inflateMenu(R.menu.problem_list_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final String str[] = {"全て","人物","アニメ","歌手","芸人","アイドル","スポーツ選手","ニッチ","ニッチな問題を除く","野球","サッカー"};
                final ArrayList<String> checkedItems = new ArrayList<>();
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProblemList.this); //タグ絞り込み用のダイアログ
                //↑getApplicationContext()だとWindowManager$BadTokenExceptionが出た（渡したコンテクストが違うらしい）
                dialogBuilder.setTitle("絞り込み");
                dialogBuilder.setMultiChoiceItems(str,null,new DialogInterface.OnMultiChoiceClickListener(){ //str（String配列）は表示する要素
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) { //ダイアログの要素がクリックされたときの動作
                        if (isChecked) checkedItems.add(str[which]); //whichは要素のindex
                        else checkedItems.remove(str[which]);
                    }
                });
                dialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener(){ //ポジティブボタンクリック時の動作
                   @Override
                   public void onClick(DialogInterface dialog,int witch){
                        quizAllList.clear();
                        if(checkedItems.contains("全て")) quizAllList.addAll(setQuizList(null));
                        else quizAllList.addAll(setQuizList(checkedItems));
                        myAdapter.notifyDataSetChanged(); //MyAdapterに格納されているデータが変更されたことを知らせる
                   }
                });
                dialogBuilder.show();

                return true;
            }
        });
    } //ここまでonCreate関数



    public ArrayList<ArrayList<String>> setQuizList(ArrayList<String> tagList){

        File inputFile = new File(com.example.test_quiz.MyApplication.getMyAppContext().getFilesDir(),"/input.csv");
        //File inputFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/input.csv");
        ArrayList<ArrayList<String>> quizList = new ArrayList<>();
        try{
            InputStreamReader isr = new InputStreamReader(new FileInputStream(inputFile),"SHIFT-JIS");
            BufferedReader br = new BufferedReader(isr);

            String tmp;
            boolean tagFlag;
            boolean isRemoveNicheFlag;
            if(tagList != null) isRemoveNicheFlag = tagList.contains("ニッチな問題を除く");
            else isRemoveNicheFlag = false;

            if(isRemoveNicheFlag) tagList.remove("ニッチな問題を除く");
            Log.e("isRemoveNiche",String.valueOf(isRemoveNicheFlag));

            br.readLine(); br.readLine(); //2行を読み飛ばす
            while((tmp = br.readLine()) != null){
                tagFlag = true;
                if (tagList != null) {
                    if(isRemoveNicheFlag){
                        if(tmp.contains("ニッチ")) tagFlag = false;
                    }
                    for (String str : tagList) {
                        //Log.i("str",str);
                        //Log.i("tmp.contains(str)",String.valueOf(tmp.contains(str)));
                        //Log.i("tagFlag",String.valueOf(tagFlag));
                        tagFlag = tagFlag && (tmp.contains(str)); //該当タグが全て含まれているかどうかを論理積を全てで取ることで判定する
                        //Log.i("tagFlagAfter",String.valueOf(tagFlag));
                    }
                }

                if(tagFlag) { //タグが全て一致すれば
                    ArrayList<String> tmpArray = new ArrayList<>();
                    int startNum = 0;
                    int endNum;
                    while ((endNum = tmp.indexOf(",", startNum)) != -1) { //","が見つからなくなるまで
                        String separatedStr = tmp.substring(startNum, endNum);
                        tmpArray.add(separatedStr);
                        startNum = endNum + 1;
                    }
                    tmpArray.add(tmp.substring(startNum));
                    quizList.add(tmpArray);
                }
            }
            isr.close();
            br.close();

        }catch(Exception e){
            AlertDialog.Builder inAlert = new AlertDialog.Builder(this);
            inAlert.setTitle("クイズのリスト読み込みエラー \n" + e.toString());
            inAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            inAlert.setCancelable(false);
            inAlert.show();
        }

        if(quizList.size() == 0){
            Toast.makeText(getApplicationContext(),"該当問題が存在しません",Toast.LENGTH_LONG).show();
        }

        return quizList; //関数setQuizListの戻り値
    }


} //ここまでActivity class ProblemList



class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
    private ArrayList<ArrayList<String>> dataList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView rvGroupName;
        private TextView rvName;
        private TextView rvPseudonym;
        private ImageView rvImage;
        private ImageButton rvFavorite;
        private int id;

        private MyViewHolder(View v) {
            super(v); //親クラス（RecyclerView.ViewHolder）のインスタンス生成時にコンストラクタへ引数を
            rvGroupName = v.findViewById(R.id.rvGroupName); //Activityじゃなくて、Viewの持つfindViewById()
            rvName = v.findViewById(R.id.rvName);
            rvPseudonym = v.findViewById(R.id.rvPseudonym);
            rvImage = v.findViewById(R.id.rvImage);
            rvFavorite = v.findViewById(R.id.rvFavorite);
        }
    }


    MyAdapter(ArrayList<ArrayList<String>> data){
        dataList = data;
    }


    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //↑内部でLayoutInflater inflater = (LayoutInflater) context.SystemService(Context.LAYOUT_INFLATER_SERVICE);を呼んで返しているらしい
        View inflateView = inflater.inflate(R.layout.list_layout, parent,false);
        //inflater.inflate(int resource, ViewGroup root, boolean attachToRoot) attachToRootがtrueだとViewGroup rootがルートになる
        MyViewHolder myViewHolder = new MyViewHolder(inflateView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position){
        holder.rvName.setText(dataList.get(position).get(1));
        holder.rvPseudonym.setText(dataList.get(position).get(2));
        holder.rvGroupName.setText(dataList.get(position).get(3));
        holder.id = Integer.valueOf(dataList.get(position).get(0));
        if(dataList.get(position).get(7).contains("@@")) {
            //Log.e("INFO","@@");
            //Log.e("INFO",String.valueOf(R.drawable.favorite));
            holder.rvFavorite.setBackgroundResource(R.drawable.a_favorite);
        }else {
            //Log.e("INFO","NO");
            //Log.e("INFO",String.valueOf(R.drawable.not_favorite));
            holder.rvFavorite.setBackgroundResource(R.drawable.a_not_favorite);
        }
        holder.rvFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rvFavoriteButtonClick(holder.id,v); //お気に入り登録
            }
        });

        int id = MyApplication.getMyAppContext().getResources().
                getIdentifier(dataList.get(position).get(4),"drawable",MyApplication.getMyAppContext().getPackageName());
        holder.rvImage.setImageResource(id);
        //Log.e("id",String.valueOf(id));

        if(id == 0){
            String path = com.example.test_quiz.MyApplication.getMyAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" +dataList.get(position).get(4) + ".jpg";
            //Log.e("path",path);
            Bitmap bmp = BitmapFactory.decodeFile(path);
            if(bmp == null){
                path = MyApplication.getMyAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" +dataList.get(position).get(4) + ".png";
                bmp = BitmapFactory.decodeFile(path);
            }
            holder.rvImage.setImageBitmap(bmp);
            if(bmp != null) holder.rvImage.setImageBitmap(bmp);
        }
        //ImageAsyncTask async = new ImageAsyncTask(holder.rvImage);
        //async.execute(dataList.get(position).get(4));
    }

    @Override
    public int getItemCount(){
        return dataList.size();
    }


    private void rvFavoriteButtonClick(int id, View v) { //お気に入り登録ボタンの動作 getMyAppContextでContextが獲得できている原理は不明
        ImageButton favoriteButton = (ImageButton)v;

        try {
            File inputFile = new File(MyApplication.getMyAppContext().getFilesDir(),"/input.csv");

            InputStreamReader isr = new InputStreamReader(new FileInputStream(inputFile), "SHIFT-JIS");
            BufferedReader br = new BufferedReader(isr);

            File tmpExFile = new File(MyApplication.getMyAppContext().getFilesDir(),"/tmpExFile.csv"); //一時的に書き写すため

            String tmp;
            FileOutputStream fos = new FileOutputStream(tmpExFile, true); //書き移すためのFileOutputStream
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "SHIFT-JIS"));
            int count = 0;
            while ((tmp = br.readLine()) != null) {
                if (count != id + 1) {
                    Log.e("count",String.valueOf(count));
                    try {
                        bw.write(tmp + "\n");
                        bw.flush();
                    } catch (Exception e) {
                        Log.e("favButton writeERROR", e.getMessage());
                        Toast.makeText(MyApplication.getMyAppContext(), "favButton ERROR", Toast.LENGTH_LONG);
                    }
                } else {
                    if (tmp.contains("@@")) {
                        Log.e("fav","@@");
                        favoriteButton.setBackgroundResource(R.drawable.a_not_favorite);
                        bw.write(tmp.substring(0, tmp.length() - 3) + "\n");
                        bw.flush();
                    } else {
                        Log.e("fav","@@");
                        favoriteButton.setBackgroundResource(R.drawable.a_favorite);
                        bw.write(tmp + "、@@\n");
                        bw.flush();
                    }
                }
                count++;
            }
            bw.close();
            fos.close();
            isr.close();
            br.close();

            inputFile.delete();
            tmpExFile.renameTo(new File(MyApplication.getMyAppContext().getFilesDir(),"/input.csv"));


        } catch (Exception e) {
            Log.e("favButton ERROR", e.getMessage());
            Toast.makeText(MyApplication.getMyAppContext(), "favButton ERROR", Toast.LENGTH_LONG);
        }
    }


} //ここまでclass MyAdapter




//class ImageAsyncTask extends AsyncTask<String,String, Bitmap> { //ネット上の画像を非同期で読み込むためのAsyncTask　というかAsyncTask継承してないと
//    //たぶんネットにつなげなかった（android.os.NetworkOnMainThreadException）
//    //あとネットにつなぐのにManifestファイルへPermission書き込みが必要だった
//    Bitmap bmp = null;
//    private ImageView imageView;
//
//    ImageAsyncTask(ImageView view) {
//        imageView = view;
//    }
//
//    @Override
//    protected Bitmap doInBackground(String... params) { //ジェネリクスだから型は自由だけど、変数名はparams固定だった
//
//        try { //この辺のtry catchはチェック例外のため
//            URL url = new URL(params[0]);
//            try {
//                InputStream is = url.openStream();
//                BitmapFactory.Options options = new BitmapFactory.Options();
////                options.inJustDecodeBounds = true; //Bitmapがメモリに展開されない
////                BitmapFactory.decodeStream(is,null,options);
////
////                float imageHeight = options.outHeight;
////                float imageWidth = options.outWidth;
////                //String imageType = options.outMimeType;
////                if(imageHeight > 400 || imageWidth > 400){
////                    if(imageHeight > imageWidth) {
////                        //Log.i("height/400",String.valueOf(Math.round(imageHeight / 400)));
////                        options.inSampleSize = Math.round(imageHeight / 400) + 2;
////                    }
////                    else{
////                        //Log.i("width/400",String.valueOf(Math.round(imageWidth / 400)));
////                        options.inSampleSize = Math.round(imageWidth / 400) + 2;
////                    }
////
////                }else {
////                    //Log.i("params",params[0]);
////                    //Log.i("height,width",String.valueOf(imageHeight) + "," + String.valueOf(imageWidth));
////                    options.inSampleSize = 2;
////                }
////                options.inJustDecodeBounds = false;
////                is = url.openStream(); //ここでもう一度呼ばないと、bmpがnullとなる
//
//                //↑なんか余計に遅くなった（OutOfMemory対策しつつ画像ごとに解像度調整するのにはいいかもしれんけど）
//                options.inSampleSize = 2; //画像の解像度を半分に
//                options.inPreferredConfig = Bitmap.Config.RGB_565;
//                bmp = BitmapFactory.decodeStream(is, null, options);
//                is.close();
//            } catch (IOException e) {
//                Log.e("URL OpenStream ERROR", e.getMessage());
//            }
//        } catch (MalformedURLException e) {
//            Log.e("URL ERROR", e.getMessage());
//        }
//        return bmp;
//    }
//
//    @Override
//    protected void onPostExecute(Bitmap result) { //非同期処理終了後にUIスレッドで行う処理
//        if (bmp != null) {
//            imageView.setImageBitmap(bmp);
//        } else {
//            Log.e("ERROR", "bmp null");
//        }
//    }
//}


