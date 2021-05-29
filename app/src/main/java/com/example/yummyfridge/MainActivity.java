package com.example.yummyfridge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    //Data[] list_data;
    String val = "";

    MyDatabaseHelper myDB;
    ArrayList <String> fridge_ingredients, fridge_type, fridge_date;
    ArrayList<String> recipe_name, recipe_info, recipe_image, recipe_star;
    ArrayList<String> star_name, star_info, star_image, star_star, star_sql;
    ListView fridge_listView, recipe_listView, star_listView;
    ListViewAdapter fridge_adapter;
    RecipeListViewAdapter recipe_adapter, star_adapter;
    TextView myFridge;
    Bitmap bmp;
    int count=0, count2=0, view_type=0;
    EditText inputShop;
    Button addShop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();
        TabHost.TabSpec spec;

        // 탭에 넣을 이미지 설정
        ImageView tabwidget01 = new ImageView(this);
        tabwidget01.setImageResource(R.drawable.refrigerator);

        ImageView tabwidget02 = new ImageView(this);
        tabwidget02.setImageResource(R.drawable.recipe);

        ImageView tabwidget03 = new ImageView(this);
        tabwidget03.setImageResource(R.drawable.checklist);

        ImageView tabwidget04 = new ImageView(this);
        tabwidget04.setImageResource(R.drawable.bookmark);

        //탭에 이미지 생성한 것 넣어주기
        TabHost.TabSpec tabFridge = tabHost.newTabSpec("FRIDGE").setIndicator(tabwidget01);
        tabFridge.setContent(R.id.fridge);
        tabHost.addTab(tabFridge);

        TabHost.TabSpec tabRecipe = tabHost.newTabSpec("Recipe").setIndicator(tabwidget02);
        tabRecipe.setContent(R.id.recipe);
        tabHost.addTab(tabRecipe);

        TabHost.TabSpec tabChecklist = tabHost.newTabSpec("Checklist").setIndicator(tabwidget03);
        tabChecklist.setContent(R.id.checklist);
        tabHost.addTab(tabChecklist);

        TabHost.TabSpec tabBookmark = tabHost.newTabSpec("Bookmark").setIndicator(tabwidget04);
        tabBookmark.setContent(R.id.bookmark);
        tabHost.addTab(tabBookmark);

        fridge_listView = (ListView) findViewById(R.id.fridge_list);
        fridge_adapter = new ListViewAdapter(MainActivity.this);
        fridge_listView.setAdapter(fridge_adapter);

        recipe_listView = (ListView) findViewById(R.id.sqlResult);
        recipe_adapter = new RecipeListViewAdapter(MainActivity.this);
        recipe_listView.setAdapter(recipe_adapter);

        star_listView = (ListView) findViewById(R.id.starResult);
        star_adapter = new RecipeListViewAdapter(MainActivity.this);
        star_listView.setAdapter(star_adapter);

        //장 볼 리스트
        final ArrayList<String> cklist = new ArrayList<String>();
        ListView cklistview = (ListView) findViewById(R.id.checklistview);
        final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, cklist);
        cklistview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        cklistview.setAdapter(adapter2);
        Button addShop = (Button) findViewById(R.id.addShop);
        final EditText inputShop = (EditText) findViewById(R.id.inputShop);

        addShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cklist.add(inputShop.getText().toString());
                adapter2.notifyDataSetChanged();
            }
        });

        cklistview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                cklist.remove(i);
                adapter2.notifyDataSetChanged();
                return false;
            }
        });

        cklistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent mIntent=new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.emart.ssg.com/search.ssg?query="+cklist.get(i)));
                startActivity(mIntent);
            }
        });


        // 식재료 추가 페이지로 넘어가기
        ImageButton btnmanager = (ImageButton) findViewById(R.id.btnmanager);
        btnmanager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), YoloActivity.class);
                startActivityForResult(intent,0);
            }
        });

        //냉장고 DB불러오기
        myDB = new MyDatabaseHelper(MainActivity.this);
        fridge_ingredients = new ArrayList<>();
        fridge_type = new ArrayList<>();
        fridge_date = new ArrayList<>();
        storeDataInArrays();
        displayData();

        //재료 삭제
        fridge_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                MyDatabaseHelper myDB2 = new MyDatabaseHelper(MainActivity.this);
                myDB2.deleteOneRow(fridge_ingredients.get(i));
                fridge_ingredients.remove(i);
                fridge_type.remove(i);
                fridge_date.remove(i);
                //fridge_adapter.removeItem(i);
                //fridge_listView.setAdapter(fridge_adapter);
                restart(MainActivity.this);
                return false;
            }
        });
        myFridge=(TextView)findViewById(R.id.myFridge);

        //레시피 추천 함수
        getVal();

        //레시피 상세보기 페이지로
        recipe_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), RecipeActivity.class);
                intent.putExtra("recipe_name", recipe_name.get(i));
                intent.putExtra("recipe_image",recipe_image.get(i));
                startActivityForResult(intent,1);
            }
        });

        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            restart(MainActivity.this);
        }


    public void getVal() {

        DataBaseHelper dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StarDatabaseHelper dbHelper2 = new StarDatabaseHelper(this);
        SQLiteDatabase db2 = dbHelper2.getReadableDatabase();
        int star=0;
        String[] strData=fridge_ingredients.toArray(new String[fridge_ingredients.size()]);

        recipe_name=new ArrayList<>();
        recipe_info=new ArrayList<>();
        recipe_image=new ArrayList<>();
        recipe_star=new ArrayList<>();

        if(strData.length>0){
            //현재 냉장고 안에 있는 재료 출력
            String addIngredients=strData[0];
            for(int i=1;i<strData.length;i++){
                addIngredients+=", "+strData[i];
            }
            myFridge.setText("재료: "+addIngredients);

            // 냉장고 속 식재료로 만들 수 있는 레시피 조회(가장 많이 겹치는 상위 10개 출력)
            String addSql="A.IRDNT_NM=? ";
            for(int i=0;i<strData.length-1;i++){
                addSql+="OR A.IRDNT_NM=? ";
            }
            Cursor cursor = db.rawQuery("SELECT B.RECIPE_NM_KO, B.SUMRY, B.IMG_URL, C.RECIPE_COUNT" +
                    " FROM recipe_basic B,(SELECT A.RECIPE_ID, count(A.IRDNT_NM) AS RECIPE_COUNT" +
                    " FROM recipe_ingredient A" +
                    " WHERE " +addSql+
                    " GROUP BY A.RECIPE_ID" +
                    " ORDER BY count(A.IRDNT_NM) DESC) C" +
                    " WHERE B.RECIPE_ID=C.RECIPE_ID" +
                    " ORDER BY C.RECIPE_COUNT DESC limit 10", strData);

            while (cursor.moveToNext())
            {
                val += cursor.getString(0)+", ";
                recipe_name.add(cursor.getString(0));
                recipe_info.add(cursor.getString(1));
                recipe_image.add(cursor.getString(2));
                Cursor cursor2 = db2.rawQuery("SELECT ID" +
                        " FROM my_star" +
                        " WHERE ID =?;", new String[]{cursor.getString(0)});
                if(cursor2.moveToNext()){
                    recipe_star.add("1");
                }else{
                    recipe_star.add("0");
                }
                cursor2.close();
            }
            //namename.setText("요리 목록: "+val);
            cursor.close();
        }
        displayRecipe(recipe_name, recipe_info, recipe_image, recipe_star, 1);
        dbHelper2.close();
        dbHelper.close();
    }

    public void getStar() {
        DataBaseHelper dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StarDatabaseHelper dbHelper2 = new StarDatabaseHelper(this);
        SQLiteDatabase db2 = dbHelper2.getReadableDatabase();

        Cursor cursor2 = dbHelper2.readAllData();

        star_name=new ArrayList<>();
        star_info=new ArrayList<>();
        star_image=new ArrayList<>();
        star_star=new ArrayList<>();

        while (cursor2.moveToNext()) {
            Cursor cursor = db.rawQuery("SELECT RECIPE_NM_KO, SUMRY, IMG_URL" +
                    " FROM recipe_basic" +
                    " WHERE RECIPE_NM_KO=?;", new String[]{cursor2.getString(0)});
            if (cursor.moveToNext()) {
                star_name.add(cursor.getString(0));
                star_info.add(cursor.getString(1));
                star_image.add(cursor.getString(2));
            }
            cursor.close();
        }
        cursor2.close();
        displayRecipe(star_name, star_info, star_image, star_star, 2);
        dbHelper.close();
        dbHelper2.close();
    }

    void storeDataInArrays(){
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
        }else{
            while (cursor.moveToNext()){
                fridge_ingredients.add(cursor.getString(0));
                fridge_type.add(cursor.getString(1));
                fridge_date.add(cursor.getString(2));
                fridge_adapter.notifyDataSetChanged();
            }
        }
    }

    void displayData(){
        for(int i=0;i<fridge_ingredients.size();i++){
            int image=0;
            switch (fridge_type.get(i)){
                case "육류":
                    image=R.drawable.meats;
                    break;
                case "채소류":
                    image=R.drawable.vegetables;
                    break;
                case "과일류":
                    image=R.drawable.fruits;
                    break;
                case "수산물":
                    image=R.drawable.fishes;
                    break;
                case "곡물/견과류":
                    image=R.drawable.grains;
                    break;
                case "가공/유제품":
                    image=R.drawable.dairy;
                    break;
                default:
                    image=R.drawable.msg;
                    break;
            }
            fridge_adapter.addItem(ContextCompat.getDrawable(this, image), fridge_ingredients.get(i),fridge_date.get(i));
        }
        fridge_adapter.notifyDataSetChanged();
    }

    void displayRecipe( ArrayList<String> recipe_name, ArrayList<String> recipe_info, ArrayList<String> recipe_image, ArrayList<String> recipe_star, int type){

        if(type==1) {
            view_type=1;
            for (int i = 0; i < recipe_name.size(); i++) {
                new DownloadFilesTask().execute(recipe_image.get(i));
            }
            recipe_adapter.notifyDataSetChanged();
        }else{
            view_type=2;
            for (int i = 0; i < recipe_name.size(); i++) {
                new DownloadFilesTask().execute(recipe_image.get(i));
            }
            star_adapter.notifyDataSetChanged();
        }
    }
    private void restart(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    private class DownloadFilesTask extends AsyncTask<String,Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            bmp = null;
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
            if(view_type==1) {
                int star;
                if (recipe_star.get(count).equals("1")) {
                    star = R.drawable.star;
                    Log.v("MYTAG", recipe_name.get(count));
                    star_adapter.addItem(result, recipe_name.get(count), recipe_info.get(count), ContextCompat.getDrawable(MainActivity.this, star));
                } else {
                    star = R.drawable.no_star;
                }
                recipe_adapter.addItem(result, recipe_name.get(count), recipe_info.get(count), ContextCompat.getDrawable(MainActivity.this, star));
                count++;
            }else if(view_type ==2){
                int star;
                star = R.drawable.star;
                star_adapter.addItem(result, star_name.get(count2), star_info.get(count2), ContextCompat.getDrawable(MainActivity.this, star));
                count2++;
            }
        }
    }

}

