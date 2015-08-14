package com.example.krishna.contentproviderdemo;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getCount(View view){
        Cursor cursor = getContentResolver().query(MyProvider.CONTENT_URI, null, null, null, null);

        int count=cursor.getCount();
        Log.d("MainActivity", "getCount (Line:29) :"+count);
    }

    public void getItems(View view){
        Log.d("MainActivity", "getItems (Line:33) :");
        Cursor cursor=getContentResolver().query(MyProvider.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()){
            long id=cursor.getLong(cursor.getColumnIndex(MyProvider.MyDatabase.ITEM_ID));
            String name=cursor.getString(cursor.getColumnIndex(MyProvider.MyDatabase.ITEM_NAME));
            Log.d("MainActivity", "getItems (Line:36) :"+id+" "+name);
        }
        cursor.close();
    }

    public void insertItems(View view){
        ContentValues values=new ContentValues();
        values.put(MyProvider.MyDatabase.ITEM_NAME, "krishna");
        Uri insertUri = getContentResolver().insert(MyProvider.CONTENT_URI, values);
        Log.d("MainActivity", "insertItems (Line:43) :"+insertUri);
    }

    public void updateItems(View view){
        ContentValues values=new ContentValues();
        values.put(MyProvider.MyDatabase.ITEM_NAME, "krishna");
        int updateCount = getContentResolver().update(MyProvider.CONTENT_URI, values, null, null);
        Log.d("MainActivity", "updateItems (Line:49) :"+updateCount);
    }

    public void deleteItems(View view){
        int deleteCount=getContentResolver().delete(MyProvider.CONTENT_URI, MyProvider.MyDatabase.ITEM_ID+" = 2 ", null);
        Log.d("MainActivity", "deleteItems (Line:54) :"+deleteCount);
    }
}
