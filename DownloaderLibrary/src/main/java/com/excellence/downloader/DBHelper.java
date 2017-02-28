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
	private static final String DB_NAME = "download.db";
	private static final int VERSION = 1;
	private static final String DOWNLOAD_TBL_NAME = "DownloadInfo";
	private static final String FLAG_TBL_NAME = "FlagInfo";
	private static final String KEY_NAME = "name";
	private static final String KEY_ID = "threadid";
	private static final String KEY_FLAG = "flag";
	private static final String KEY_LENGTH = "downloadlength";
	private static final String CREATE_DOWNLOAD_TBL = String.format("create table %1$s(%2$s VARCHAR(1024), %3$s INTEGER, %4$s INTEGER, PRIMARY KEY(%5$s, %6$s))", DOWNLOAD_TBL_NAME, KEY_NAME, KEY_ID,
			KEY_LENGTH, KEY_NAME, KEY_ID);
	private static final String CREATE_FLAG_TBL = String.format("create table %1$s(%2$s VARCHAR(1024), %3$s INTEGER, %4$s INTEGER, PRIMARY KEY(%5$s))", FLAG_TBL_NAME, KEY_NAME, KEY_FLAG, KEY_LENGTH,
			KEY_NAME);
	public static final String lock = "Visit";

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
		values.put(KEY_NAME, name);
		values.put(KEY_ID, threadId);
		values.put(KEY_LENGTH, downloadlength);
		mDatabase.insert(DOWNLOAD_TBL_NAME, null, values);
	}

	public void updateDownloadId(String name, int threadId, int downloadLength)
	{
		mDatabase.execSQL(String.format("update %1$s set %2$s = ? where %3$s = ? and %4$s = ?", DOWNLOAD_TBL_NAME, KEY_LENGTH, KEY_NAME, KEY_ID), new Object[] { downloadLength, name, threadId });
	}

	public void insertFlag(String name, int flag, int size)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_FLAG, flag);
		// default value
		values.put(KEY_LENGTH, 0);
		mDatabase.insert(FLAG_TBL_NAME, null, values);
	}

	public SQLiteDatabase getDatabase()
	{
		return mDatabase;
	}

	public int queryDownloadedLength(String name)
	{
		int length = -1;
		Cursor cursor = mDatabase.rawQuery(String.format("select * from %1$s where %2$s = ?", FLAG_TBL_NAME, KEY_NAME), new String[] { name });
		if (cursor != null && cursor.moveToNext())
		{
			length = cursor.getInt(cursor.getColumnIndex(KEY_LENGTH));
			cursor.close();
		}
		return length;
	}

	public int queryDownloadedLength(String name, int threadId)
	{
		int length = -1;
		Cursor cursor = mDatabase.rawQuery(String.format("select * from %1$s where %2$s = ? and %3$s = ?", DOWNLOAD_TBL_NAME, KEY_NAME, KEY_ID), new String[] { name, String.valueOf(threadId) });
		if (cursor != null && cursor.moveToNext())
		{
			length = cursor.getInt(cursor.getColumnIndex(KEY_LENGTH));
			cursor.close();
		}
		return length;
	}

	public void updateFlag(String name, int flag)
	{
		mDatabase.execSQL(String.format("update %1$s set %2$s = ? where %3$s = ?", FLAG_TBL_NAME, KEY_FLAG, KEY_NAME), new Object[] { flag, name });
	}

	public void updateDownloadLength(String name, int downloadLength)
	{
		mDatabase.execSQL(String.format("update %1$s set %2$s = ? where %3$s = ?", FLAG_TBL_NAME, KEY_LENGTH, KEY_NAME), new Object[] { downloadLength, name });
	}

	public void deleteDownloadInfo(String name)
	{
		delete(DOWNLOAD_TBL_NAME, name);
	}

	public void deleteFlagInfo(String name)
	{
		delete(FLAG_TBL_NAME, name);
	}

	public void delete(String tableName, String name)
	{
		mDatabase.execSQL(String.format("delete from %1$s where %2$s = ?", tableName, KEY_NAME), new Object[] { name });
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
		Cursor cursor = mDatabase.rawQuery(String.format("select * from %1$s", FLAG_TBL_NAME), null);
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
		Cursor cursor = mDatabase.rawQuery(String.format("select * from %1$s where %2$s = ?", FLAG_TBL_NAME, KEY_NAME), new String[] { name });
		HistoryFileInfo apkInfo = null;
		if (cursor != null && cursor.moveToNext())
		{
			apkInfo = new HistoryFileInfo();
			apkInfo.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
			apkInfo.setFlag(cursor.getInt(cursor.getColumnIndex(KEY_FLAG)));
			apkInfo.setDownloadLength(cursor.getInt(cursor.getColumnIndex(KEY_LENGTH)));
			cursor.close();
		}
		return apkInfo;
	}

	public HistoryFileInfo queryDownload(String name, int threadId)
	{
		Cursor cursor = mDatabase.rawQuery(String.format("select * from %1$s where %2$s = ? and %3$s = ?", DOWNLOAD_TBL_NAME, KEY_NAME, KEY_ID), new String[] { name, String.valueOf(threadId) });
		HistoryFileInfo apkInfo = null;
		if (cursor != null && cursor.moveToNext())
		{
			apkInfo = new HistoryFileInfo(cursor.getString(cursor.getColumnIndex(KEY_NAME)), cursor.getInt(cursor.getColumnIndex(KEY_ID)), cursor.getInt(cursor.getColumnIndex(KEY_LENGTH)));
			cursor.close();
		}
		return apkInfo;
	}
}
