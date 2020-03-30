package com.example.photosandroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;



import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;



import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class AlbumDisplay extends AppCompatActivity {

    int albumindex;
    int index;
    User user;
    private static final int PICK_IMAGE = 100;

    Uri imageUri;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_display);
        Intent in = getIntent();
        albumindex = in.getIntExtra("index", 0);
        user = (User) in.getSerializableExtra("User");
        user = loadData();
        System.out.println("Album index: " + albumindex);

        //user.albums.get(albumindex).photos.add(new Photo("new", "first photo"));
        //user.albums.get(albumindex).photos.add(new Photo("ye", "firso"));

        list = findViewById(R.id.listview);

        CustomAdapter adapter = new CustomAdapter();
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                index = i;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData(user);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveData(user);
        startActivity(new Intent(AlbumDisplay.this, MainActivity.class));
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

    public void add(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);

    }

    public void loadList() {
        CustomAdapter adapter = new CustomAdapter();
        list.setAdapter(adapter);
    }


    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        if(resultcode == RESULT_OK && requestcode == PICK_IMAGE) {
            imageUri = data.getData();

            getApplicationContext().getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            AlertDialog.Builder rename = new AlertDialog.Builder(this);
            rename.setTitle("Photo caption");
            rename.setMessage("Enter of a caption for the photo: ");
            final EditText in = new EditText(this);
            rename.setView(in);

            rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    user.albums.get(albumindex).photos.add(new Photo(imageUri.toString(), in.getText().toString()));
                }
            });

            rename.show();
            loadList();
        }
    }

    public void delete(View view) {
        if(index > -1 && !user.albums.get(albumindex).photos.isEmpty()) {
            AlertDialog.Builder delete = new AlertDialog.Builder(this);
            delete.setTitle("Delete Confirmation");
            delete.setMessage("Are you sure you would like to delete?");

            delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    user.albums.get(albumindex).photos.remove(index);
                    loadList();
                }
            });

            delete.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeContextMenu();
                }
            });

            delete.show();
        }
        else {
            AlertDialog.Builder error = new AlertDialog.Builder(this);
            error.setTitle("Selection error");
            error.setMessage("Please select a photo to delete");

            error.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeContextMenu();
                }
            });

            error.show();
        }
    }

    public void display(View view) {
        if(!user.albums.get(albumindex).photos.isEmpty()) {
            Intent i = new Intent(AlbumDisplay.this, PhotoDisplay.class);
            i.putExtra("albumindex", albumindex);
            i.putExtra("photoindex", index);
            i.putExtra("User", user);
            startActivity(i);
        }
        else {
            AlertDialog.Builder error = new AlertDialog.Builder(this);
            error.setTitle("Selection error");
            error.setMessage("Please select a photo to display ");

            error.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeContextMenu();
                }
            });

            error.show();
        }

    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return user.albums.get(albumindex).photos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = getLayoutInflater().inflate(R.layout.listview, null);

            ImageView image = view.findViewById(R.id.imageView);
            TextView text = view.findViewById(R.id.text);


            Uri path = Uri.parse(user.albums.get(albumindex).photos.get(position).itemPath);

            String provider = "com.android.providers.media.MediaProvider";

            grantUriPermission(provider, path, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            grantUriPermission(provider, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(provider, path, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);


            getApplicationContext().getContentResolver().takePersistableUriPermission(path, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);



            image.setImageURI(path);


            if(user.albums.get(albumindex).photos.get(position).caption != null) {
                text.setText(user.albums.get(albumindex).photos.get(position).caption);
            }




            return view;
        }
    }
}
