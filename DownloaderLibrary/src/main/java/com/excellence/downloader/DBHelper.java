package com.excellence.downloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ZhangWei on 2016/2/19.
 */

/**
 * 多线程断点数据库
 */
public class DBHelper extends SQLiteOpenHelper
{
	private static final String DB_NAME = "download.db";
	private static final int VERSION = 1;
	private static final String DOWNLOAD_TBL_NAME = "DownloadInfo";
	private static final String KEY_NAME = "name";
	private static final String KEY_ID = "threadid";
	private static final String KEY_LENGTH = "downloadsize";
	private static final String CREATE_DOWNLOAD_TBL = String.format("create table %1$s(%2$s VARCHAR(1024), %3$s INTEGER, %4$s INTEGER, PRIMARY KEY(%5$s, %6$s))", DOWNLOAD_TBL_NAME, KEY_NAME, KEY_ID,
			KEY_LENGTH, KEY_NAME, KEY_ID);

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

		if (mInstance.mDatabase != null && !mInstance.mDatabase.isOpen())
		{
			mInstance = new DBHelper(context.getApplicationContext());
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(CREATE_DOWNLOAD_TBL);
	}

	/**
	 * 新建单线程下载记录
	 *
	 * @param name 文件名
	 * @param threadId 线程Id
	 * @param downloadSize 下载长度
     */
	protected synchronized void insertDownloadSize(String name, int threadId, int downloadSize)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_ID, threadId);
		values.put(KEY_LENGTH, downloadSize);
		mDatabase.insert(DOWNLOAD_TBL_NAME, null, values);
	}

	/**
	 * 更新单线程下载记录
	 *
	 * @param name 文件名
	 * @param threadId 线程Id
	 * @param downloadSize 下载长度
     */
	protected synchronized void updateDownloadSize(String name, int threadId, int downloadSize)
	{
		mDatabase.execSQL(String.format("update %1$s set %2$s = ? where %3$s = ? and %4$s = ?", DOWNLOAD_TBL_NAME, KEY_LENGTH, KEY_NAME, KEY_ID), new Object[] { downloadSize, name, threadId });
	}

	/**
	 * 查询下载总长度
	 *
	 * @param name 文件名
	 * @return 下载总长度
     */
	public synchronized int queryDownloadSize(String name)
	{
		int length = 0;
		Cursor cursor = mDatabase.rawQuery(String.format("select * from %1$s where %2$s = ?", DOWNLOAD_TBL_NAME, KEY_NAME), new String[] { name });
		if (cursor != null)
		{
			while (cursor.moveToNext())
			{
				length += cursor.getInt(cursor.getColumnIndex(KEY_LENGTH));
			}
			cursor.close();
		}
		return length;
	}

	/**
	 * 查询单线程下载长度
	 *
	 * @param name 文件名
	 * @param threadId 线程Id
     * @return 下载长度
     */
	public synchronized int queryDownloadSize(String name, int threadId)
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

	/**
	 * 获取单个下载记录任务的并发线程数
	 *
	 * @param name 文件名
	 * @return 单任务下载线程数
     */
	protected synchronized int queryDownloadThreadCount(String name)
	{
		int count = 0;
		Cursor cursor = mDatabase.rawQuery(String.format("select count(*) from %1$s where %2$s = ?", DOWNLOAD_TBL_NAME, KEY_NAME), new String[] { name });
		if (cursor != null && cursor.moveToFirst())
		{
			count = cursor.getInt(0);
			cursor.close();
		}
		return count;
	}

	/**
	 * 删除下载表中的文件记录
	 *
	 * @param name 文件名
     */
	protected synchronized void deleteDownloadInfo(String name)
	{
		delete(DOWNLOAD_TBL_NAME, name);
	}

	/**
	 * 删除某数据表中的记录
	 *
	 * @param tableName 数据表名
	 * @param name 文件名
     */
	private synchronized void delete(String tableName, String name)
	{
		mDatabase.execSQL(String.format("delete from %1$s where %2$s = ?", tableName, KEY_NAME), new Object[] { name });
	}

	/**
	 * 关闭数据连接
	 */
	protected synchronized void closeDB()
	{
		if (mDatabase != null)
		{
			mDatabase.close();
		}
	}

	/**
	 * 升级数据库
	 *
	 * @param db 数据库索引
	 * @param oldVersion 旧版本号
	 * @param newVersion 新版本号
     */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("drop table if exists " + DOWNLOAD_TBL_NAME);
		onCreate(db);
	}

}
