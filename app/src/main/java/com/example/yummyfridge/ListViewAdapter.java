package com.example.yummyfridge;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ListItem> listItems = new ArrayList<ListItem>();
    /*
    private OnDeleteClickListener mListener;

    public interface OnDeleteClickListener{ // 인터페이스 정의
        void onDelete(View v, int pos);
    }
    public ListViewAdapter(Context context, OnDeleteClickListener listener){
        this.mContext = context;
        this.mListener = listener;
    }
    */
    public ListViewAdapter(Context context){
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
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        // listitem.xml 의 참조 획득
        ImageView list_image=(ImageView)convertView.findViewById(R.id.list_image);
        TextView list_ingredients = (TextView)convertView.findViewById(R.id.list_ingredients);
        TextView list_date = (TextView)convertView.findViewById(R.id.list_date);
        ListItem listItem = listItems.get(position);

        // 가져온 데이터를 텍스트뷰에 입력
        list_image.setImageDrawable(listItem.getImage());
        list_ingredients.setText(listItem.getText1());
        list_date.setText(listItem.getText2());

        // 리스트 아이템 삭제
        //btn_delete.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        listItems.remove(position);
        //        notifyDataSetChanged();
        //    }
        //});

        return convertView;
    }
    public void addItem(Drawable image, String text1, String text2){
        ListItem listItem = new ListItem();

        listItem.setImage(image);
        listItem.setText1(text1);
        listItem.setText2(text2);

        listItems.add(listItem);
    }

    //public void removeItem(int pos){
      //  listItems.remove(pos);
        //notifyDataSetChanged();
    //}

}
