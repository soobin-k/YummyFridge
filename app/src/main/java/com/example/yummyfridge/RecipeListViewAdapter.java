package com.example.yummyfridge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecipeListViewAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<RecipeListItem> listItems = new ArrayList<RecipeListItem>();

    public RecipeListViewAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int i) {
        return listItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // listitem.xml 레이아웃을 inflate해서 참조획득
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.recipe_list, parent, false);
        }

        // listitem.xml 의 참조 획득
        ImageView list_image=(ImageView)convertView.findViewById(R.id.recipe_image);
        TextView list_ingredients = (TextView)convertView.findViewById(R.id.recipe_name);
        TextView list_date = (TextView)convertView.findViewById(R.id.recipe_info);
        ImageView list_star=(ImageView)convertView.findViewById(R.id.recipe_star);
        RecipeListItem listItem = listItems.get(position);

        // 가져온 데이터를 텍스트뷰에 입력
        list_image.setImageBitmap(listItem.getImage());
        list_ingredients.setText(listItem.getText1());
        list_date.setText(listItem.getText2());
        list_star.setImageDrawable(listItem.getStar());

        return convertView;
    }
    public void addItem(Bitmap image, String text1, String text2, Drawable star){
        RecipeListItem listItem = new RecipeListItem();

        listItem.setImage(image);
        listItem.setText1(text1);
        listItem.setText2(text2);
        listItem.setStar(star);

        listItems.add(listItem);
    }
}
