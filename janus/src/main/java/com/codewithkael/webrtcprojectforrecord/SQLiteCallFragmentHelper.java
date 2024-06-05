package com.codewithkael.webrtcprojectforrecord;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
        super(context, databaseName, null, databaseVersion);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try{
            db.execSQL("CREATE TABLE " + tableName + " (" +
                    id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    contactName + " VARCHAR(255), " +
                    contactNumber + " VARCHAR(255));");

            Toast.makeText(context, "Data Inserted Successfully", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(context, " Exception Caught "+e, Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        try{
//            db.execSQL("DROP TABLE IF EXISTS " + tableName);
//            onCreate(db);

        }
        catch(Exception e){
            Toast.makeText(context, "Exception Caught onUpgrade", Toast.LENGTH_SHORT).show();
        }

    }


    public long insertData (String contactName,  String contactNumber){

        sqliteDatabase = this.getWritableDatabase();
        ContentValues contentValues  = new ContentValues();
        contentValues.put(this.contactName,contactName);
        contentValues.put(this.contactNumber,contactNumber);
        long rowId = sqliteDatabase.insert(tableName,null,contentValues);
        sqliteDatabase.close();
        return rowId;
    }


    public List<CallRecords> getAllRecodrs(){
        List<CallRecords> returnList  = new ArrayList<>();

        String query = "SELECT * FROM " +   tableName + " ORDER BY " + id + " DESC";
        sqliteDatabase = this.getReadableDatabase();
        Cursor cursor =  sqliteDatabase.rawQuery(query, null);

        if(cursor.moveToFirst()){

            do{
                int idIndex = cursor.getColumnIndex(id);
                int nameIndex = cursor.getColumnIndex(contactName);
                int numberIndex = cursor.getColumnIndex(contactNumber);

                String contactId = cursor.getString(idIndex);
                String contactName = cursor.getString(nameIndex);
                String contactNumber = cursor.getString(numberIndex);

                CallRecords callRecords = new CallRecords(contactId,contactName,contactNumber);
                returnList.add(callRecords);

            }
            while(cursor.moveToNext());
        }
        cursor.close();
        sqliteDatabase.close();
        return returnList;
    }
}

