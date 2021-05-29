package com.example.yummyfridge;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class RecipeListItem {
    private Bitmap image;
    private String text1;
    private String text2;
    private Drawable star;

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public Drawable getStar() {
        return star;
    }

    public void setStar(Drawable star) {
        this.star = star;
    }

}

