package com.example.photosandroid;

import com.example.photosandroid.model.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.transition.Slide;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    int state = 1;

    User user;

    ListView list;
    ArrayAdapter<Album> array;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = startup();
        if(user == null) {
            System.out.println("error");

        }
        else {
            list = findViewById(R.id.listview);


            array = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, user.albums);
            list.setAdapter(array);
            System.out.println("First");
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    index = i;
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData(user);
    }

    public void open(View view) {
        if(index > -1 && !user.albums.isEmpty()) {
            Intent i = new Intent(MainActivity.this, AlbumDisplay.class);
            i.putExtra("index", index);
            i.putExtra("User", user);
            startActivity(i);
        }
        else {
            AlertDialog.Builder error = new AlertDialog.Builder(this);
            error.setTitle("Selection error");
            error.setMessage("Please select an album to open");

            error.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeContextMenu();
                }
            });

            error.show();
        }

    }

    public void create(View view) {
        AlertDialog.Builder add = new AlertDialog.Builder(this);
        add.setTitle("New Album");
        add.setMessage("Enter name of album: ");
        final EditText in = new EditText(this);
        add.setView(in);

        add.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String input = in.getText().toString();
                user.albums.add(new Album(input));
                loadList();

            }
        });

        add.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                closeContextMenu();
            }
        });

        add.show();
    }

    public void rename(View view) {
        if(index > -1 && !user.albums.isEmpty()) {
            AlertDialog.Builder rename = new AlertDialog.Builder(this);
            rename.setTitle("Rename album");
            rename.setMessage("Enter new name of album: ");
            final EditText in = new EditText(this);
            rename.setView(in);

            rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    user.albums.get(index).albumName = in.getText().toString();
                    loadList();

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
        else {
            AlertDialog.Builder error = new AlertDialog.Builder(this);
            error.setTitle("Selection error");
            error.setMessage("Please select an album to rename");

            error.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeContextMenu();
                }
            });

            error.show();
        }
    }

    public void delete(View view) {
        if(index > -1 && !user.albums.isEmpty()) {
            AlertDialog.Builder delete = new AlertDialog.Builder(this);
            delete.setTitle("Delete Confirmation");
            delete.setMessage("Are you sure you would like to delete?");

            delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    user.albums.remove(index);
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
            error.setMessage("Please select an album to delete");

            error.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeContextMenu();
                }
            });

            error.show();
        }
    }

    public void loadList() {
        ArrayAdapter<Album> array = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, user.albums);
        list.setAdapter(array);
    }

    public User startup()
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
        User person = null;
        File userFile = new File(userDir);

        if(userFile.exists())
        {
            //Read it
            System.out.println("File exists, reading in");
            person = loadData();
        }
        else
        {
            //Create it and read it
            System.out.println("File doesn't exist, creating");
            User person1 = new User();
            saveData(person1);
            person = loadData();

        }

        if (person == null)
        {
            System.out.println("Failed to create person in startup method");
        }
        else {
            System.out.println("Successfully read in user data");
        }

        return person;
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

    public void search(View view) {
        AlertDialog.Builder search = new AlertDialog.Builder(this);
        search.setTitle("Search");
        search.setMessage("Enter value or part of value to search ");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText type = new EditText(getApplicationContext());
        type.setHint("Tag Type: ");
        layout.addView(type); // Notice this is an add method

        final EditText value = new EditText(getApplicationContext());
        value.setHint("Tag Value: ");
        layout.addView(value); // Another add method

        search.setView(layout);

        search.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String combined = type.getText().toString() + " :  " + value.getText().toString();
                if(type.getText().toString().isEmpty() || value.getText().toString().isEmpty()) {
                    dialogInterface.dismiss();
                    AlertDialog.Builder empty = new AlertDialog.Builder(MainActivity.this);
                    empty.setTitle("Error");
                    empty.setMessage("Cannot search for empty type or value");

                    empty.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            closeContextMenu();
                        }
                    });
                    empty.show();
                }
                else if(search(user, combined) == null || search(user, combined).isEmpty()) {
                    dialogInterface.dismiss();
                    AlertDialog.Builder empty = new AlertDialog.Builder(MainActivity.this);
                    empty.setTitle("No matches");
                    empty.setMessage("No photos matched the search input");

                    empty.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            closeContextMenu();
                        }
                    });
                    empty.show();
                } else {
                    saveData(user);
                    state = 1;
                    Intent a = new Intent(MainActivity.this, SearchResults.class);

                    a.putExtra("text", combined);
                    startActivity(a);
                }

            }
        });

        search.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                closeContextMenu();
            }
        });


        search.show();

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