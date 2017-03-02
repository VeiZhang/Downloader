package com.excellence.downloader.utils;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * Created by MinGuo on 15-7-20.
 */
public class MemorySpaceCheck
{
	public static long getAvailableSize(String path)
	{
		StatFs fileStats = new StatFs(path);
		fileStats.restat(path);
		return (long) fileStats.getAvailableBlocks() * fileStats.getBlockSize();
	}

	public static long getTotalSize(String path)
	{
		StatFs fileStats = new StatFs(path);
		fileStats.restat(path);
		return (long) fileStats.getBlockCount() * fileStats.getBlockSize();
	}

	public static long getSDAvailableSize()
	{
		// if
		// (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		// {
		Log.e("path", Environment.getExternalStorageDirectory().toString());
		return getAvailableSize(Environment.getExternalStorageDirectory().toString());
		// }
		// return 0;
	}

	public static long getSystemAvailableSize()
	{
		return getAvailableSize("/data");
	}

	public static boolean hasEnoughMemory(String filePath)
	{
		File file = new File(filePath);
		long length = file.length();
		if (filePath.startsWith("/sdcard") || filePath.startsWith("/mnt/sdcard"))
		{
			return getSDAvailableSize() > length;
		}
		else
		{
			return getSystemAvailableSize() > length;
		}
	}

	public static boolean hasSDEnoughMemory(String filePath, long length)
	{
		Log.d("hasSD", getAvailableSize(filePath) + "::" + length);
		return getAvailableSize(filePath) > length;
	}

	public static long getSDTotalSize()
	{
		// if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		// {
		return getTotalSize(Environment.getExternalStorageDirectory().toString());
		// }
		// return 0;
	}

	public static long getSysTotalSize()
	{
		return getTotalSize("/data");
	}
}
