package com.codewithkael.webrtcprojectforrecord;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SQLiteCallFragmentHelper extends SQLiteOpenHelper {

    private static final String databaseName = "CallDetails.db";
    private final String tableName = "callList";
    private static final int databaseVersion = 1;

    private final String id = "_id";
    private final String contactName = "contactName";
    private final String contactNumber = "contactNumber";
    private SQLiteDatabase sqliteDatabase;

    private Context context;

    public SQLiteCallFragmentHelper(@Nullable Context context) {
        super(context != null ? context.getApplicationContext() : null, databaseName, null, databaseVersion);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (context == null) {
            Log.e("SQLiteCallFragmentHelper", "Context is null in onCreate");
            return;
        }

        try {
            db.execSQL("CREATE TABLE " + tableName + " (" +
                    id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    contactName + " VARCHAR(255), " +
                    contactNumber + " VARCHAR(255));");
            showToast("Table Created Successfully");
        } catch (SQLException e) {
            Log.e("SQLiteCallFragmentHelper", "Error creating table", e);
            showToast("Exception Caught: " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // db.execSQL("DROP TABLE IF EXISTS " + tableName);
            // onCreate(db);
        } catch (SQLException e) {
            Log.e("SQLiteCallFragmentHelper", "Error upgrading table", e);
            showToast("Exception Caught onUpgrade");
        }
    }

    public long insertData(String contactName, String contactNumber) {
        long rowId = -1;
        try {
            sqliteDatabase = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(this.contactName, contactName);
            contentValues.put(this.contactNumber, contactNumber);
            rowId = sqliteDatabase.insert(tableName, null, contentValues);
        } catch (SQLException e) {
            Log.e("SQLiteCallFragmentHelper", "Error inserting data", e);
            showToast("Error inserting data: " + e);
        } finally {
            if (sqliteDatabase != null && sqliteDatabase.isOpen()) {
                sqliteDatabase.close();
            }
        }
        return rowId;
    }

    public List<CallRecords> getAllRecords() {
        List<CallRecords> returnList = new ArrayList<>();
        Cursor cursor = null;

        try {
            sqliteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM " + tableName + " ORDER BY " + id + " DESC";
            cursor = sqliteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(id);
                    int nameIndex = cursor.getColumnIndex(contactName);
                    int numberIndex = cursor.getColumnIndex(contactNumber);

                    String contactId = cursor.getString(idIndex);
                    String contactName = cursor.getString(nameIndex);
                    String contactNumber = cursor.getString(numberIndex);

                    CallRecords callRecords = new CallRecords(contactId, contactName, contactNumber);
                    returnList.add(callRecords);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            Log.e("SQLiteCallFragmentHelper", "Error reading data", e);
            showToast("Error reading data: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqliteDatabase != null && sqliteDatabase.isOpen()) {
                sqliteDatabase.close();
            }
        }

        return returnList;
    }

    private void showToast(String message) {
        if (context != null) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}
