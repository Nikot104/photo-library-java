package com.example.photosandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class PhotoDisplay extends AppCompatActivity {

    public int albumindex;
    public int photoindex;
    public int selection;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        Intent in = getIntent();
        photoindex = in.getIntExtra("photoindex",0);
        albumindex = in.getIntExtra("albumindex", 0);
        user = (User) in.getSerializableExtra("User");
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

    public void addtag(View view) {

        AlertDialog.Builder tags = new AlertDialog.Builder(this);
        final String[] items = {"Location", "Person"};

        tags.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                selection = n;
            }

        });

        tags.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                closeContextMenu();
                androidx.appcompat.app.AlertDialog.Builder rename = new androidx.appcompat.app.AlertDialog.Builder(PhotoDisplay.this);
                rename.setTitle("Tag value");
                rename.setMessage("Enter the tag value: ");
                final EditText in = new EditText(PhotoDisplay.this);
                rename.setView(in);


                rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        user.albums.get(albumindex).photos.get(photoindex).addTag(items[selection], in.getText().toString());
                        TextView caption = findViewById(R.id.caption);
                        String display = "Caption: " + user.albums.get(albumindex).photos.get(photoindex).caption;
                        for(String tag: user.albums.get(albumindex).photos.get(photoindex).tags) {
                            display += "\n" + tag;
                        }

                        caption.setText(display);
                        closeContextMenu();

                    }
                });

                rename.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeContextMenu();

                    }
                });

                rename.show();

            }
        });

        tags.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                closeContextMenu();

            }
        });

        tags.setTitle("What tag type?");
        tags.show();


    }

    public void slideshow(View view) {
        Intent i = new Intent(PhotoDisplay.this, Slideshow.class);
        i.putExtra("albumindex", albumindex);
        i.putExtra("photoindex", photoindex);
        saveData(user);
        startActivity(i);
    }

    public void move(View view) {
        AlertDialog.Builder tags = new AlertDialog.Builder(this);
        ArrayList<String> strings = new ArrayList<>(user.albums.size());
        for (Album a: user.albums) {
            strings.add(a.toString());
        }
        String[] items = strings.toArray(new String[strings.size()]);


        tags.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                selection = n;
            }

        });
        tags.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.albums.get(selection).addPhoto(user.albums.get(albumindex).photos.get(photoindex));
                user.albums.get(albumindex).photos.remove(photoindex);
                closeContextMenu();
            }
        });

        tags.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                closeContextMenu();
            }
        });
        tags.setTitle("What album to move photo to?");
        tags.show();
    }

    public void deleteTag(View view) {
        AlertDialog.Builder tags = new AlertDialog.Builder(this);
        String[] items = user.albums.get(albumindex).photos.get(photoindex).tags.toArray(new String[user.albums.get(albumindex).photos.get(photoindex).tags.size()]);


        tags.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                selection = n;

            }

        });
        tags.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.albums.get(albumindex).photos.get(photoindex).tags.remove(selection);
                TextView caption = findViewById(R.id.caption);
                String display = "Caption: " + user.albums.get(albumindex).photos.get(photoindex).caption;
                for(String tag: user.albums.get(albumindex).photos.get(photoindex).tags) {
                    display += "\n" + tag;
                }

                caption.setText(display);
                closeContextMenu();
            }
        });

        tags.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                closeContextMenu();
            }
        });

        tags.setTitle("Which one?");
        tags.show();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveData(user);
        Intent i = new Intent(PhotoDisplay.this, AlbumDisplay.class);
        i.putExtra("index", this.albumindex);

        startActivity(i);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData(user);
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
