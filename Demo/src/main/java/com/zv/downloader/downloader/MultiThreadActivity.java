package com.zv.downloader.downloader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.excellence.basetoolslibrary.baseadapter.CommonAdapter;
import com.excellence.basetoolslibrary.baseadapter.ViewHolder;
import com.excellence.basetoolslibrary.utils.FileUtils;
import com.excellence.downloader.DownloaderListener;
import com.excellence.downloader.DownloaderManager;
import com.excellence.downloader.FileDownloader;
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
		DownloaderManager.init(Runtime.getRuntime().availableProcessors() - 1);
	}

	@Override
	protected void destroyDownloader()
	{
		DownloaderManager.destroy();
	}

	private void init()
	{
		mDownloaderTasks = new ArrayList<>();
		mDownloaderTasks.add(new DownloaderTask("AngryBirds.apk", APPMARKET_URL));
		mDownloadListView.setAdapter(new DownloaderAdapter(this, mDownloaderTasks, R.layout.download_item));
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
			viewHolder.setText(R.id.task_textview, item.getFileName());
			Button startBtn = viewHolder.getView(R.id.start_btn);
			Button deleteBtn = viewHolder.getView(R.id.delete_btn);
			ProgressBar progressBar = viewHolder.getView(R.id.task_progressbar);
			startBtn.setOnClickListener(new TaskClick(item));
			deleteBtn.setOnClickListener(new TaskClick(item));
			item.setStartBtn(startBtn);
			item.setDeleteBtn(deleteBtn);
			item.setProgressBar(progressBar);
			item.invalidateTask();
		}

		private class TaskClick implements View.OnClickListener
		{
			private DownloaderTask mDownloaderTask = null;

			public TaskClick(DownloaderTask item)
			{
				mDownloaderTask = item;
			}

			@Override
			public void onClick(View v)
			{
				if (mDownloaderTask == null)
					return;

				FileDownloader fileDownloader = mDownloaderTask.getFileDownloader();
				switch (v.getId())
				{
				case R.id.start_btn:
					if (fileDownloader == null)
					{
						// 建立下载任务
						buildTask();
					}
					else
					{
						switch (fileDownloader.getState())
						{
						case FileDownloader.STATE_DOWNLOADING:
							fileDownloader.setPause();
							break;

						case FileDownloader.STATE_PAUSE:
							buildTask();
//							fileDownloader.schedule();
							break;
						}
					}
					break;

				case R.id.delete_btn:
					if (fileDownloader != null)
						fileDownloader.setStop();

					FileUtils.deleteFile(new File(DOWNLOAD_PATH, mDownloaderTask.getFileName()));
					mDownloaderTask.setFileDownloader(null);
					break;
				}
				updateUI();
			}

			private void buildTask()
			{
				File file = new File(DOWNLOAD_PATH, mDownloaderTask.getFileName());
				mDownloaderTask.setFileDownloader(DownloaderManager.addTask(MultiThreadActivity.this, file, mDownloaderTask.getFileUrl(), new DownloaderListener()
				{
					@Override
					public void onDownloadStartListener(String filename, int fileLength)
					{
						super.onDownloadStartListener(filename, fileLength);
						mDownloaderTask.setFileSize(fileLength);
						updateUI();
						System.out.println("start: " + filename + " : " + fileLength);
					}

					@Override
					public void onDownloadingListener(String filename, long downloadedLength)
					{
						super.onDownloadingListener(filename, downloadedLength);
						mDownloaderTask.setDownloadLength(downloadedLength);
						updateUI();
						System.out.println("downloading: " + filename + " : " + downloadedLength);
					}

					@Override
					public void onDownloadFinishListener(String filename)
					{
						super.onDownloadFinishListener(filename);
						updateUI();
						System.out.println("finish: " + filename);
					}

					@Override
					public void onDownloadFailListener(String filename, int result)
					{
						super.onDownloadFailListener(filename, result);
						updateUI();
						System.out.println("failed: " + filename + " : " + result);
					}
				}));
			}

			private void updateUI()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						mDownloaderTask.invalidateTask();
					}
				});
			}
		}
	}
}