package com.example.photosandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Slide;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photosandroid.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Slideshow extends AppCompatActivity {

    User user;
    int photoindex;
    int albumindex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);
        Intent in = getIntent();
        photoindex = in.getIntExtra("photoindex",0);
        albumindex = in.getIntExtra("albumindex", 0);
        user = loadData();

        TextView caption = findViewById(R.id.caption);
        ImageView image = findViewById(R.id.imageView);
        String display = "Caption: " + user.albums.get(albumindex).photos.get(photoindex).caption;
        for(String tag: user.albums.get(albumindex).photos.get(photoindex).tags) {
            display += "\n" + tag;
        }

        caption.setText(display);
        image.setImageURI(Uri.parse(user.albums.get(albumindex).photos.get(photoindex).itemPath));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveData(user);
        Intent i = new Intent(Slideshow.this, PhotoDisplay.class);
        i.putExtra("albumindex", albumindex);
        i.putExtra("photoindex", photoindex);
        startActivity(i);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData(user);
    }

    public void next(View view) {
        if(photoindex + 1 >= user.albums.get(albumindex).photos.size()) {
            photoindex = 0;
        } else {
            photoindex++;
        }
        TextView caption = findViewById(R.id.caption);
        ImageView image = findViewById(R.id.imageView);
        String display = "Caption: " + user.albums.get(albumindex).photos.get(photoindex).caption;
        for(String tag: user.albums.get(albumindex).photos.get(photoindex).tags) {
            display += "\n" + tag;
        }

        caption.setText(display);
        image.setImageURI(Uri.parse(user.albums.get(albumindex).photos.get(photoindex).itemPath));
    }

    public void previous(View view) {
        if(photoindex - 1 <= 0) {
            photoindex = user.albums.get(albumindex).photos.size() -1;
        } else {
            photoindex--;
        }
        TextView caption = findViewById(R.id.caption);
        ImageView image = findViewById(R.id.imageView);
        String display = "Caption: " + user.albums.get(albumindex).photos.get(photoindex).caption;
        for(String tag: user.albums.get(albumindex).photos.get(photoindex).tags) {
            display += "\n" + tag;
        }

        caption.setText(display);
        image.setImageURI(Uri.parse(user.albums.get(albumindex).photos.get(photoindex).itemPath));
    }

    public void saveData(User person)
    {
        PackageManager m = getPackageManager();
        String str = getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(str, 0);
            str = p.applicationInfo.dataDir;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            System.out.println("Package error: " + e);
        }

        String userDir = str + "/admin.ser";
        File userFil = new File(userDir);
        try
        {
            FileOutputStream fo = new FileOutputStream(userFil);
            ObjectOutputStream oo = new ObjectOutputStream(fo);

            oo.writeObject(person);

            oo.close();
            fo.close();
        }
        catch(IOException e)
        {
            System.out.println("Error in saveData");
            System.out.println(e);
        }
    }

    public User loadData()
    {
        PackageManager m = getPackageManager();
        String str = getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(str, 0);
            str = p.applicationInfo.dataDir;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            System.out.println("Package error: " + e);
        }

        String userDir = str + "/admin.ser";
        File userFil = new File(userDir);
        User person = null;
        try
        {
            FileInputStream fi = new FileInputStream(userFil);
            ObjectInputStream oi = new ObjectInputStream(fi);

            person = (User)oi.readObject();

            oi.close();
            fi.close();
        }
        catch (IOException e)
        {
            System.out.println("Error in loadData");
            System.out.println(e);
        }
        catch (ClassNotFoundException ex)
        {
            System.out.println("Error in loadData");
            System.out.println(ex);
        }

        return person;
    }
}
