package com.example.yummyfridge;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RecipeActivity extends Activity {

    MyDatabaseHelper myDB;
    ImageButton btn_manager;
    TextView detail_name, detail_ingredients1, detail_ingredients2, detail_ingredients3;
    ImageView detail_image;
    ArrayList<String> recipe_step;
    ListView detail_process;
    String ingredients1="", ingredients2="", ingredients3="";
    ToggleButton detail_star;
    String recipe_name="", recipe_image="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_recipe);

        Intent intent = getIntent();
        recipe_name=intent.getStringExtra("recipe_name");
        recipe_image=intent.getStringExtra("recipe_image");

        //레시피 이름 출력
        detail_name=(TextView)findViewById(R.id.detail_name);
        detail_name.setText(recipe_name);

        //레시피 이미지 출력
        detail_image=(ImageView)findViewById(R.id.detail_image);
        new DownloadFilesTask().execute(recipe_image);

        //레시피 순서 조회 & 출력
        recipe_step=new ArrayList<String>();
        detail_process=(ListView)findViewById(R.id.detail_process);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recipe_step);
        detail_process.setAdapter(adapter);

        DataBaseHelper dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COOKING_NO, COOKING_DC" +
                " FROM recipe_process" +
                " WHERE RECIPE_ID =(SELECT RECIPE_ID FROM recipe_basic WHERE RECIPE_NM_KO=?)" +
                " ORDER BY COOKING_NO ASC", new String[]{recipe_name});

        while (cursor.moveToNext())
        {
            recipe_step.add(cursor.getString(1));
            adapter.notifyDataSetChanged();

        }

        cursor.close();
        dbHelper.close();

        //레시피 재료 조회 & 출력
        detail_ingredients1=(TextView)findViewById(R.id.detail_ingredients1);
        detail_ingredients2=(TextView)findViewById(R.id.detail_ingredients2);
        detail_ingredients3=(TextView)findViewById(R.id.detail_ingredients3);

        DataBaseHelper dbHelper2 = new DataBaseHelper(this);
        SQLiteDatabase db2 = dbHelper2.getReadableDatabase();

        Cursor cursor2 = db2.rawQuery("SELECT IRDNT_NM, IRDNT_CPCTY, IRDNT_TY_NM" +
                " FROM recipe_ingredient" +
                " WHERE RECIPE_ID =(SELECT RECIPE_ID FROM recipe_basic WHERE RECIPE_NM_KO=?)" +
                " ORDER BY IRDNT_TY_NM DESC;", new String[]{recipe_name});

        while (cursor2.moveToNext())
        {
            switch(cursor2.getString(2)){
                case "주재료":
                    ingredients1+=cursor2.getString(0)+" "+cursor2.getString(1)+", ";;
                    break;
                case "부재료":
                    ingredients2+=cursor2.getString(0)+" "+cursor2.getString(1)+", ";;
                    break;
                default:
                    ingredients3+=cursor2.getString(0)+" "+cursor2.getString(1)+", ";;
                    break;
            }
        }
        cursor2.close();
        dbHelper2.close();

        detail_ingredients1.setText("주재료: "+ingredients1);
        detail_ingredients2.setText("부재료: "+ingredients2);
        detail_ingredients3.setText("양념: "+ingredients3);

        //레시피 즐겨찾기 출력
        detail_star=(ToggleButton)findViewById(R.id.detail_star);
        StarDatabaseHelper dbHelper3 = new StarDatabaseHelper(this);
        SQLiteDatabase db3 = dbHelper3.getReadableDatabase();

        Cursor cursor3 = db3.rawQuery("SELECT ID" +
                " FROM my_star" +
                " WHERE ID =?;", new String[]{recipe_name});

        int i=0;
        while (cursor3.moveToNext())
        {
            Log.v("MYTAG2",cursor3.getString(0));
           i++;

        }
        cursor3.close();
        dbHelper3.close();

        if(i>0){
            detail_star.setChecked(true);
        }else{
            detail_star.setChecked(false);
        }


        //메인 페이지로 이동
        btn_manager=(ImageButton)findViewById(R.id.btnmanager);
        btn_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent outIntent=new Intent(getApplicationContext(), MainActivity.class);
                setResult(RESULT_OK,outIntent);
                finish();
            }
        });
    }
    //즐겨찾기 변경사항 db 저장
    public void onStarClick(View v){
        boolean on=((ToggleButton) v).isChecked();
        StarDatabaseHelper dbHelper3 = new StarDatabaseHelper(this);
        if(on){
            dbHelper3.addStar(recipe_name);
        }else{
            dbHelper3.deleteOneRow(recipe_name);
        }
    }

    private class DownloadFilesTask extends AsyncTask<String,Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bmp = null;
            try {
                String img_url = strings[0]; //url of the image
                URL url = new URL(img_url);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(Bitmap result) {
            // doInBackground 에서 받아온 total 값 사용 장소
            detail_image.setImageBitmap(result);
        }
    }
}
