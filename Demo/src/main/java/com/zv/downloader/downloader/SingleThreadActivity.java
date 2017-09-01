package com.zv.downloader.downloader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.excellence.annotations.Download;
import com.excellence.basetoolslibrary.baseadapter.CommonAdapter;
import com.excellence.basetoolslibrary.baseadapter.ViewHolder;
import com.excellence.downloader.Downloader;
import com.excellence.downloader.FileDownloader.DownloadTask;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.utils.IListener;
import com.zv.downloader.DownloadActivity;
import com.zv.downloader.R;
import com.zv.downloader.bean.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.excellence.downloader.entity.TaskEntity.STATUS_DOWNLOADING;
import static com.excellence.downloader.entity.TaskEntity.STATUS_ERROR;
import static com.excellence.downloader.entity.TaskEntity.STATUS_PAUSE;

public class SingleThreadActivity extends DownloadActivity
{
	private static final String TAG = SingleThreadActivity.class.getSimpleName();

	private List<Task> mTasks = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	protected void initDownloader()
	{
		Downloader.init(this);
		Downloader.register(this);
	}

	@Override
	protected void destroyDownloader()
	{
		Downloader.destroy();
	}

	private void init()
	{
		mTasks = new ArrayList<>();
		mTasks.add(new Task("AngryBirds.apk", ANGRYBIRDS_URL));
		mTasks.add(new Task("QQ.apk", QQ_URL));
		mTasks.add(new Task("RomUpdate.bin", ROMUPDATE_URL));
		mTasks.add(new Task("Filmon.apk", FILMON_URL));
		mDownloadListView.setAdapter(new DownloaderAdapter(this, mTasks, R.layout.download_item));
	}

	@Download.onPre
	public void onPre(DownloadTask task)
	{

	}

	private class DownloaderAdapter extends CommonAdapter<Task>
	{
		public DownloaderAdapter(Context context, List<Task> datas, @LayoutRes int layoutId)
		{
			super(context, datas, layoutId);
		}

		@Override
		public void convert(ViewHolder viewHolder, Task item, int position)
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
			private Task mTask = null;

			public TaskClick(Task item)
			{
				mTask = item;
			}

			@Override
			public void onClick(View v)
			{
				if (mTask == null)
					return;

				DownloadTask downloadTask = mTask.getDownloadTask();

				switch (v.getId())
				{
				case R.id.start_btn:
					if (downloadTask == null)
					{
						// 建立下载任务
						buildTask();
					}
					else
					{
						switch (downloadTask.getStatus())
						{
						case STATUS_DOWNLOADING:
							downloadTask.pause();
							break;

						case STATUS_PAUSE:
						case STATUS_ERROR:
							downloadTask.resume();
							break;
						}
					}
					break;

				case R.id.delete_btn:
					if (downloadTask != null)
						downloadTask.discard();

					mTask.setDownloadTask(null);
					break;
				}
				mTask.invalidateTask();
			}

			private void buildTask()
			{
				File file = new File(DOWNLOAD_PATH, mTask.getFileName());
				mTask.setDownloadTask(Downloader.addTask(file, mTask.getFileUrl(), new IListener()
				{

					@Override
					public void onPreExecute(long fileSize)
					{
						mTask.setDownloadLength(0);
						mTask.setFileSize(fileSize);
						mTask.invalidateTask();
						System.out.println("pre " + fileSize);
					}

					@Override
					public void onProgressChange(long fileSize, long downloadedSize)
					{
						mTask.setDownloadLength(downloadedSize);
						mTask.invalidateTask();
					}

					@Override
					public void onProgressChange(long fileSize, long downloadedSize, long speed)
					{

					}

					@Override
					public void onCancel()
					{
						System.out.println("cancel");
					}

					@Override
					public void onError(DownloadError error)
					{
						error.printStackTrace();
						mTask.invalidateTask();
					}

					@Override
					public void onSuccess()
					{
						mTask.invalidateTask();
						System.out.println("success");
					}

				}));

			}

		}
	}
}