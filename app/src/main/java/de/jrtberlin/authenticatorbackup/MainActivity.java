package de.jrtberlin.authenticatorbackup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;
import com.topjohnwu.superuser.io.SuFileOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the reason why you should call it in a static block
         * The followings are some examples, check Javadoc for more details */
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    int MY_WRITE_EXTERNAL_STORAGE = 0;
    EditText editText;

    int WRITE_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
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
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_STORAGE);

            }
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                }
                break;
        }
    }

    public void backitup(View v) {
        openDB();
    }

    private void openDB() {
        File dbfile = SuFile.open("/data/data/com.google.android.apps.authenticator2/databases/databases");
        if (dbfile.exists()) {
            try (InputStream in = new SuFileInputStream(dbfile);
                 OutputStream out = new SuFileOutputStream("/sdcard/sucopied")) {
                ShellUtils.pump(in, out);
                execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void execute() {
        //editText.setText("");
        SQLiteDatabase myDB = null;
        try {
            myDB = SQLiteDatabase.openDatabase("/sdcard/sucopied", null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        String select = "SELECT * FROM accounts";
        try {
            Cursor dbCursor = myDB.rawQuery(select, null);

            dbCursor.moveToFirst();
            String json, type;
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");
            boolean firstloop = true;
            while (dbCursor.isAfterLast() == false) {
                if (!firstloop) {
                    jsonBuilder.append(",");
                } else firstloop = false;
                if (dbCursor.getString(4).equals("0")) {
                    type = "TOTP";
                } else {
                    type = "HOTP";
                }
                jsonBuilder.append("{\"secret\":\"" + dbCursor.getString(2) + "\""
                        + ",\"label\":\"" + dbCursor.getString(7) + "\""
                        + ",\"digits\":6"
                        + ",\"type\":\"" + type + "\""
                        + ",\"algorithm\":\"SHA1\""
                        + ",\"thumbnail\":\"Default\""
                        + ",\"period\":30");
                if (type.equals("HOTP")) {
                    jsonBuilder.append(",\"counter\":" + dbCursor.getString(3));
                }
                jsonBuilder.append(",\"tags\":[\"G Authenticator import\"]}");
                dbCursor.moveToNext();
            }
            jsonBuilder.append("]");
            editText.setText(jsonBuilder.toString());
            dbCursor.close();
            Shell.su("rm /sdcard/sucopied*").submit();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/sdcard/andOTP_import.json", false));
            writer.append(jsonBuilder.toString());

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
