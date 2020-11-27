package com.example.test_quiz;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity { //一番最初の画面のアクティビティー

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // "/data" + MyApplication.getMyAppContext().getFilesDir().getPath() + "/input.csv"で内部ストレージのパスを指定すると、
        // なぜか /user/0/ の文言が挟まってうまく指定できない

        File exFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/exFile.csv");
        if(!exFile.exists()){ //アプリの初回起動時のみ外部ストレージ上のファイルexFileを作成するため、リソースのrawフォルダoriginal.csvをコピーする
            try {
                Resources resources = this.getResources();
                InputStream is = resources.openRawResource(R.raw.original); //resフォルダ内ファイルの命名規則により半角英数のみ(おそらく大文字もアウト)
                InputStreamReader isr = new InputStreamReader(is,"UTF-8");
                BufferedReader br = new BufferedReader(isr);

                FileOutputStream fos = new FileOutputStream(exFile,true);
                OutputStreamWriter osw = new OutputStreamWriter(fos,"SHIFT-JIS");
                BufferedWriter bw = new BufferedWriter(osw);

                String tmp;
                while((tmp = br.readLine()) != null){
                    bw.write(tmp + "\n");
                    bw.flush();
                }
                is.close(); isr.close(); br.close();
                fos.close(); osw.close(); bw.close();
            }catch(Exception e){
                Log.e("exFile copy error",e.getMessage());
            }

            importCSV(null);
        }

        File image = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        if(!image.exists()) image.mkdirs();
    }



    public void startSettingActivity(View view) {  //問題選択の画面へ移行
        Intent intent = new Intent(getApplicationContext(), com.example.test_quiz.SettingActivity.class);
        startActivity(intent);
    }

    public void startListActivity(View view){ //問題リスト（一覧）の画面へ移行
        Intent intent = new Intent(getApplicationContext(), ProblemList.class);
        startActivity(intent);
    }

    public void startManualActivity(View view){ //使い方説明画面へ移行
        Intent intent = new Intent(getApplicationContext(), ManualActivity.class);
        startActivity(intent);
    }




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void importCSV(View view){ //自分がエクセルで作ったCSVファイルが、携帯のアプリデータ（外部）の中に入っていたら読み込む
        try{
            File outFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/exFile.csv");
            Log.e("exFile",getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/exFile.csv");
            //FileReader fr = new FileReader(outFile);  これ使うと文字コードが指定できず文字化け

            //文字コードがUTF-8だとエクセルで開いたとき文字化けした
            InputStreamReader isr = new InputStreamReader(new FileInputStream(outFile),"SHIFT-JIS");
            BufferedReader br = new BufferedReader(isr); //BufferedReaderみたいなフィルタの引数は、なんとかReader

            File inputFile = new File(com.example.test_quiz.MyApplication.getMyAppContext().getFilesDir(),"/input.csv"); //携帯繋げても通常見えない内部データ
            //File inputFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/input.csv"); //デバッグのため
            if(inputFile.exists()){ //既にinputFileが存在すれば
                inputFile.delete(); //古いファイルがあったら消去
            }

            FileOutputStream fos = new FileOutputStream(inputFile,true);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,"SHIFT-JIS"));
            String tmp;
            while((tmp = br.readLine()) != null) {
                try{
                    bw.write(tmp + "\n");
                    bw.flush();
                }catch(Exception e) {
                    AlertDialog.Builder inAlert = new AlertDialog.Builder(this);
                    inAlert.setTitle("importCSV ERROR in while try-catch  \n" + e.toString());
                    inAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    inAlert.setCancelable(false);
                    inAlert.show();
                }
            }
            bw.close();
            fos.close();
            isr.close();
            br.close();
            Toast.makeText(com.example.test_quiz.MyApplication.getMyAppContext(),"CSVファイルを読み込みました",Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            AlertDialog.Builder inAlert = new AlertDialog.Builder(this);
            inAlert.setTitle(e.toString());
            inAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            inAlert.setCancelable(false);
            inAlert.show();
            Log.e("importCSV error",e.getMessage());
        }



    }




}
