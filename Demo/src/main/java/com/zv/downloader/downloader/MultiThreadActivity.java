package com.zv.downloader.downloader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;

import com.excellence.basetoolslibrary.baseadapter.CommonAdapter;
import com.excellence.basetoolslibrary.baseadapter.ViewHolder;
import com.excellence.downloader.DownloaderListener;
import com.excellence.downloader.DownloaderUtils;
import com.zv.downloader.DownloadActivity;
import com.zv.downloader.R;
import com.zv.downloader.bean.DownloaderTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadActivity extends DownloadActivity
{
	private static final String TAG = MultiThreadActivity.class.getSimpleName();

	private List<DownloaderTask> mDownloaderTasks = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	protected void initDownloader()
	{
		DownloaderUtils.init(Runtime.getRuntime().availableProcessors() - 1);
	}

	@Override
	protected void destroyDownloader()
	{
		DownloaderUtils.destroy();
	}

	private void init()
	{
		mDownloaderTasks = new ArrayList<>();
		mDownloaderTasks.add(new DownloaderTask("app.apk", APPMARKET_URL));
		mDownloadListView.setAdapter(new DownloaderAdapter(this, mDownloaderTasks, R.layout.download_item));

		File file = new File(DOWNLOAD_PATH, "app.apk");
		DownloaderUtils.addTask(this, file, APPMARKET_URL,new DownloaderListener()
		{
			@Override
			public void onDownloadStartListener(String filename, int fileLength)
			{
				super.onDownloadStartListener(filename, fileLength);
				System.out.println("start: " + filename + " : " + fileLength);
			}

			@Override
			public void onDownloadingListener(String filename, long downloadedLength)
			{
				super.onDownloadingListener(filename, downloadedLength);
				System.out.println("downloading: " + filename + " : " + downloadedLength);
			}

			@Override
			public void onDownloadFinishListener(String filename)
			{
				super.onDownloadFinishListener(filename);
				System.out.println("finish: " + filename);
			}

			@Override
			public void onDownloadFailListener(String filename, int result)
			{
				super.onDownloadFailListener(filename, result);
				System.out.println("failed: " + filename + " : " + result);
			}
		});
	}

	private class DownloaderAdapter extends CommonAdapter<DownloaderTask>
	{
		public DownloaderAdapter(Context context, List<DownloaderTask> datas, @LayoutRes int layoutId)
		{
			super(context, datas, layoutId);
		}

		@Override
		public void convert(ViewHolder viewHolder, DownloaderTask item, int position)
		{

		}
	}
}