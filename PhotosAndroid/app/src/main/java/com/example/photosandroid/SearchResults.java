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


import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class SearchResults extends AppCompatActivity {

    int index;
    User user;
    private static final int PICK_IMAGE = 100;
    ArrayList<Photo> photos;

    Uri imageUri;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_results);
        Intent in = getIntent();
        String input = in.getStringExtra("text");
        user = (User) in.getSerializableExtra("User");
        user = loadData();
        photos = search(user, input);


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
        startActivity(new Intent(SearchResults.this, MainActivity.class));
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


    public void loadList() {
        CustomAdapter adapter = new CustomAdapter();
        list.setAdapter(adapter);
    }


    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return photos.size();
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


            Uri path = Uri.parse(photos.get(position).itemPath);

            String provider = "com.android.providers.media.MediaProvider";

            grantUriPermission(provider, path, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            grantUriPermission(provider, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(provider, path, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);


            getApplicationContext().getContentResolver().takePersistableUriPermission(path, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);



            image.setImageURI(path);


            if(photos.get(position).caption != null) {
                text.setText(photos.get(position).caption);
            }




            return view;
        }
    }

    public ArrayList<Photo> search(User per, String tagTypeVal)
    {
        //Starting Search, creating arraylist for return values
        System.out.println("Activated Search: " + tagTypeVal);
        ArrayList<Photo> fin = new ArrayList<Photo>();

        //Taking the searched term which is "<tagType> : <tagValue>" and splitting it
        String[] tagUnder = tagTypeVal.split(" :  ", 2);

        System.out.println("Split: " + tagUnder[0]);
        System.out.println("Split: " + tagUnder[1]);

        for(Album al : per.albums)
        {
            for(Photo ph : al.photos)
            {
                for(String ta : ph.tags)
                {
                    System.out.println("Working with tag: " + ta);
                    String[] tagOver = ta.split(":  ", 2);
                    System.out.println("which then became: " + tagOver[0] + tagOver[1]);

                    //If the photo tagType matches the search tagType && if the photo tagValue contains the search tagValue
                    if(tagOver[0].equals(tagUnder[0]) && tagOver[1].contains(tagUnder[1]))
                    {
                        System.out.println("Found substring");
                        fin.add(ph);
                    }
                }
            }
        }
        //Prints the caption for testing
        for(Photo phi : fin) {
            System.out.println("found caption: " + phi.caption);
        }
        System.out.println("Finished Search");
        return fin;
    }

}
