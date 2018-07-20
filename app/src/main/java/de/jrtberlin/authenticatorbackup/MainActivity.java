package de.jrtberlin.authenticatorbackup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

int MY_WRITE_EXTERNAL_STORAGE = 0;
EditText editText;

    int WRITE_REQUEST_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_WRITE_EXTERNAL_STORAGE);

            }
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else{
                }
                break;
        }
    }
    public void backitup(View v){
        copydb();
        //execute();
    }

    private void copydb(){
        try {
            Runtime.getRuntime().exec("su -c cp /data/data/com.google.android.apps.authenticator2/databases/databases /sdcard/authdb").waitFor();
        } catch (IOException e) {
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        execute();

    }

    public void execute(){
        //editText.setText("");
        SQLiteDatabase myDB = null;
try {
    myDB = SQLiteDatabase.openDatabase("/sdcard/authdb", null, SQLiteDatabase.OPEN_READONLY);
}catch (Exception e){
    System.out.println(e);
}
        String select = "select * from accounts";
        Cursor dbCursor = myDB.rawQuery(select, null);

        dbCursor.moveToFirst();
        while (dbCursor.isAfterLast() == false)
        {
            editText.setText(editText.getText() + "\n\n" + dbCursor.getString(1) + "\n" + dbCursor.getString(2));
            dbCursor.moveToNext();
        }
        dbCursor.close();
        try {
            Runtime.getRuntime().exec("rm /sdcard/authdb");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
