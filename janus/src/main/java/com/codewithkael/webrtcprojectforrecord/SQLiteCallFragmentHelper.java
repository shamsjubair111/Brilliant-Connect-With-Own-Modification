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
    private static final String tableName = "callList";



    private static final int databaseVersion = 1;

    private  static  final String id = "_id";
    private  static  final String contactsName = "contactName";
    private  static  final String contactsNumber = "contactNumber";

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
                    contactsName + " VARCHAR(255), " +
                    contactsNumber + " VARCHAR(255));");

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

        SQLiteDatabase sqliteDatabase = this.getWritableDatabase();
        ContentValues contentValues  = new ContentValues();
        contentValues.put(contactsName,contactName);
        contentValues.put(contactsNumber,contactNumber);
        long rowId = sqliteDatabase.insert(tableName,null,contentValues);
        return rowId;
    }


    public List<CallRecords> getAllRecodrs(){
        List<CallRecords> returnList  = new ArrayList<>();

        String query = "SELECT * FROM " +   tableName + " ORDER BY " + id + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery(query, null);

        if(cursor.moveToFirst()){

            do{
                String userId = cursor.getString(0);
                String userName = cursor.getString(1);
                String userNumber = cursor.getString(2);

                CallRecords callRecords = new CallRecords(userId,userName,userNumber);
                returnList.add(callRecords);

            }
            while(cursor.moveToNext());
        }
        else{

        }
//        cursor.close();
//        db.close();
        return returnList;
    }
}

