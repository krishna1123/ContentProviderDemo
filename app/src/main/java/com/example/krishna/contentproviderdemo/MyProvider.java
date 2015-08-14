package com.example.krishna.contentproviderdemo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by krishna on 14/8/15.
 */
public class MyProvider extends ContentProvider {

    private static final String AUTHORITY="com.example.krishna.contentproviderdemo";
    public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/items");
    private static final UriMatcher uriMatcher;
    private static final int ITEMS=1;
    private static final int ITEM_ID=2;
    private MyDatabase myDatabase;

    static {
        uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "items", ITEMS);
        uriMatcher.addURI(AUTHORITY, "items/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        Log.d("MyProvider", "onCreate (Line:37) :");
        myDatabase=new MyDatabase(getContext());
        Log.d("MyProvider", "onCreate (Line:39) :");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(uriMatcher.match(uri)!=ITEMS && uriMatcher.match(uri)!=ITEM_ID ){
            throw new IllegalArgumentException(
                    "Unsupported URI for selection: " + uri);
        }

        SQLiteDatabase database=myDatabase.getReadableDatabase();

        SQLiteQueryBuilder builder=new SQLiteQueryBuilder();
        builder.setTables(MyDatabase.TAB_ITEM);

       if(uriMatcher.match(uri)==ITEM_ID){
            String id=uri.getLastPathSegment();
            String where=MyDatabase.ITEM_ID+" = "+id;
            builder.appendWhere(where);
        }

        Cursor cursor=builder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case ITEMS:
                return "vnd.android.cursor.dir/vnd.com.example.krishna";

            case ITEM_ID:
                return "vnd.android.cursor.item/vnd.com.example.krishna";
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if(uriMatcher.match(uri)!=ITEMS){
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

        SQLiteDatabase database = myDatabase.getWritableDatabase();
        long id = database.insert(MyDatabase.TAB_ITEM, null, values);
        return getUriForId(id, uri);

    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
                // notify all listeners of changes:
                getContext().
                        getContentResolver().
                        notifyChange(itemUri, null);
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException(
                "Problem while inserting into uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(uriMatcher.match(uri)!=ITEMS && uriMatcher.match(uri)!=ITEM_ID ){
            throw new IllegalArgumentException(
                    "Unsupported URI for deletion: " + uri);
        }

        int deleteCount=0;
        SQLiteDatabase database=myDatabase.getWritableDatabase();
        if(uriMatcher.match(uri) == ITEMS){
            deleteCount = database.delete(MyDatabase.TAB_ITEM, selection, selectionArgs);
        }else if(uriMatcher.match(uri)==ITEM_ID){
            String id=uri.getLastPathSegment();
            String where=MyDatabase.ITEM_ID+" = "+id;
            if(!TextUtils.isEmpty(selection)){
                where+=" AND "+selection;
            }
            deleteCount=database.delete(MyDatabase.TAB_ITEM, where, selectionArgs);
        }
        if(deleteCount>0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if(uriMatcher.match(uri)!=ITEMS && uriMatcher.match(uri)!=ITEM_ID ){
            throw new IllegalArgumentException(
                    "Unsupported URI for updation: " + uri);
        }

        int updateCount=0;
        SQLiteDatabase database=myDatabase.getWritableDatabase();
        if(uriMatcher.match(uri)==ITEMS){
            updateCount=database.update(MyDatabase.TAB_ITEM, values, selection, selectionArgs);
        }else if(uriMatcher.match(uri)==ITEM_ID){
            String id=uri.getLastPathSegment();
            String where=MyDatabase.ITEM_ID+" = "+id;
            if(!TextUtils.isEmpty(selection)){
                where+=" AND "+selection;
            }
            updateCount=database.update(MyDatabase.TAB_ITEM, values, where, selectionArgs);
        }
        if(updateCount>0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    public static final class MyDatabase extends SQLiteOpenHelper{
        private static final String DBName="mydatabase";
        private static final String TAB_ITEM="items";
        public static final String ITEM_ID="id";
        public static final String ITEM_NAME="name";
        private static final String TABLE_QUERY="create table "+TAB_ITEM+" ( "+ITEM_ID+" integer primary key autoincrement, "+ITEM_NAME+" text )";

        public MyDatabase(Context context) {
            super(context, DBName, null, 1);
            Log.d("MyDatabase", "MyDatabase (Line:166) :"+TABLE_QUERY);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("MyDatabase", "onCreate (Line:168) :" + TABLE_QUERY);
            db.execSQL(TABLE_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists "+TAB_ITEM);
            db.execSQL(TABLE_QUERY);
        }
    }
}
