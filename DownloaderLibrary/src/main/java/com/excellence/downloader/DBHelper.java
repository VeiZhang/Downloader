package com.excellence.downloader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ZhangWei on 2016/2/19.
 */
public class DBHelper extends SQLiteOpenHelper
{
	public static final String DOWNLOAD_TBL_NAME = "DownloadInfo";
	public static final String FLAG_TBL_NAME = "FlagInfo";
	public static final String TBL_KEYNAME = "name";
	public static final String TBL_FLAG = "flag";
	public static final String TBL_ID = "threadid";
	public static final String TBL_LENGTH = "downloadlength";
	public static final String lock = "Visit";
	private static final int VERSION = 1;
	private static final String DB_NAME = "download.db";
	private static final String CREATE_DOWNLOAD_TBL = "create table DownloadInfo(name VARCHAR(1024), threadid INTEGER, downloadlength INTEGER, PRIMARY KEY(name,threadid))";
	private static final String CREATE_FLAG_TBL = "create table FlagInfo(name VARCHAR(1024), flag INTEGER, downloadlength INTEGER, PRIMARY KEY(name))";
	private static DBHelper mInstance = null;
	private SQLiteDatabase mDatabase = null;

	private DBHelper(Context context) throws SQLiteException
	{
		super(context, DB_NAME, null, VERSION);
		try
		{
			mDatabase = getWritableDatabase();
		}
		catch (Exception e)
		{
			mDatabase = getReadableDatabase();
		}
	}

	public static synchronized DBHelper getInstance(Context context)
	{
		if (mInstance == null)
			mInstance = new DBHelper(context.getApplicationContext());
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(CREATE_FLAG_TBL);
		db.execSQL(CREATE_DOWNLOAD_TBL);
	}

	public void insertDownload(String name, int threadId, int downloadlength)
	{
		ContentValues values = new ContentValues();
		values.put(TBL_KEYNAME, name);
		values.put(TBL_ID, threadId);
		values.put(TBL_LENGTH, downloadlength);
		mDatabase.insert(DOWNLOAD_TBL_NAME, null, values);
	}

	public void updateDownloadId(String name, int threadId, int downloadLength)
	{
		mDatabase.execSQL("update DownloadInfo set downloadlength = ? where name = ? and threadid = ?", new Object[] { downloadLength, name, threadId });
	}

	public void insertFlag(String name, int flag, int size)
	{
		ContentValues values = new ContentValues();
		values.put(TBL_KEYNAME, name);
		values.put(TBL_FLAG, flag);
		// default value
		values.put(TBL_LENGTH, 0);
		mDatabase.insert(FLAG_TBL_NAME, null, values);
	}

	public SQLiteDatabase getDatabase()
	{
		return mDatabase;
	}

	public int queryDownloadedLength(String name)
	{
		int length = -1;
		Cursor cursor = mDatabase.rawQuery("select * from FlagInfo where name = ?", new String[] { name });
		if (cursor != null && cursor.moveToNext())
		{
			length = cursor.getInt(cursor.getColumnIndex(TBL_LENGTH));
			cursor.close();
		}
		return length;
	}

	public int queryDownloadedLength(String name, int threadId)
	{
		int length = -1;
		Cursor cursor = mDatabase.rawQuery("select * from DownloadInfo where name = ? and threadid = ?", new String[] { name, String.valueOf(threadId) });
		if (cursor != null && cursor.moveToNext())
		{
			length = cursor.getInt(cursor.getColumnIndex(TBL_LENGTH));
			cursor.close();
		}
		return length;
	}

	public void updateFlag(String name, int flag)
	{
		mDatabase.execSQL("update FlagInfo set flag = ? where name = ?", new Object[] { flag, name });
	}

	public void updateDownloadLength(String name, int downloadLength)
	{
		mDatabase.execSQL("update FlagInfo set downloadlength = ? where name = ?", new Object[] { downloadLength, name });
	}

	public void delete(String name, String tableName)
	{
		mDatabase.execSQL("delete from " + tableName + " where name = ?", new Object[] { name });
	}

	public void closeDB()
	{
		if (mDatabase != null)
		{
			synchronized (DBHelper.lock)
			{
				mDatabase.close();
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("drop table if exists " + DOWNLOAD_TBL_NAME);
		db.execSQL("drop table if exists " + FLAG_TBL_NAME);
		onCreate(db);
	}

	public List<HistoryFileInfo> queryFlagAll()
	{
		List<HistoryFileInfo> apkInfos = new ArrayList<>();
		Cursor cursor = mDatabase.rawQuery("select * from FlagInfo", null);
		if (cursor != null)
		{
			while (cursor.moveToNext())
			{
				HistoryFileInfo apkInfo = new HistoryFileInfo();
				apkInfo.setName(cursor.getString(0));
				apkInfo.setFlag(cursor.getInt(1));
				apkInfo.setDownloadLength(cursor.getInt(2));
				apkInfos.add(apkInfo);
			}
			cursor.close();
		}
		return apkInfos;
	}

	public HistoryFileInfo queryFlag(String name)
	{
		Cursor cursor = mDatabase.rawQuery("select * from FlagInfo where name = ?", new String[] { name });
		HistoryFileInfo apkInfo = null;
		if (cursor != null && cursor.moveToNext())
		{
			apkInfo = new HistoryFileInfo();
			apkInfo.setName(cursor.getString(cursor.getColumnIndex(TBL_KEYNAME)));
			apkInfo.setFlag(cursor.getInt(cursor.getColumnIndex(TBL_FLAG)));
			apkInfo.setDownloadLength(cursor.getInt(cursor.getColumnIndex(TBL_LENGTH)));
			cursor.close();
		}
		return apkInfo;
	}

	public HistoryFileInfo queryDownload(String name, int threadId)
	{
		Cursor cursor = mDatabase.rawQuery("select * from DownloadInfo where name = ? and threadid = ?", new String[] { name, String.valueOf(threadId) });
		HistoryFileInfo apkInfo = null;
		if (cursor != null && cursor.moveToNext())
		{
			apkInfo = new HistoryFileInfo(cursor.getString(cursor.getColumnIndex(TBL_KEYNAME)), cursor.getInt(cursor.getColumnIndex(TBL_ID)), cursor.getInt(cursor.getColumnIndex(TBL_LENGTH)));
			cursor.close();
		}
		return apkInfo;
	}
}
