package com.zv.downloader.netroid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.excellence.basetoolslibrary.baseadapter.CommonAdapter;
import com.excellence.basetoolslibrary.baseadapter.ViewHolder;
import com.excellence.basetoolslibrary.utils.FileUtils;
import com.vincestyling.netroid.Listener;
import com.vincestyling.netroid.NetroidError;
import com.vincestyling.netroid.toolbox.FileDownloader.DownloadController;
import com.zv.downloader.DownloadActivity;
import com.zv.downloader.R;
import com.zv.downloader.bean.NetroidTask;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class SingleThreadActivity extends DownloadActivity
{

	private static final String TAG = SingleThreadActivity.class.getSimpleName();

	protected DownloadAdapter mDownloadAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	protected void initDownloader()
	{
//		Netroid.initFileDownload(null, Runtime.getRuntime().availableProcessors() - 1);
		Netroid.initFileDownload(null, 1);
	}

	private void init()
	{
		List<NetroidTask> netroidTasks = new ArrayList<>();
		netroidTasks.add(new NetroidTask("QQ_Netroid.apk", QQ_URL));
		netroidTasks.add(new NetroidTask("AngryBirds_Netroid.apk", ANGRYBIRDS_URL));
		netroidTasks.add(new NetroidTask("Filmon_Netroid.apk", FILMON_URL));
		netroidTasks.add(new NetroidTask("RomUpdate_Netroid.bin", ROMUPDATE_URL));
		mDownloadAdapter = new DownloadAdapter(this, netroidTasks, R.layout.download_item);
		mDownloadListView.setAdapter(mDownloadAdapter);
	}

	@Override
	protected void destroyDownloader()
	{
		Netroid.destroy();
	}

	private class DownloadAdapter extends CommonAdapter<NetroidTask>
	{

		public DownloadAdapter(Context context, List<NetroidTask> datas, @LayoutRes int layoutId)
		{
			super(context, datas, layoutId);
		}

		@Override
		public void convert(ViewHolder viewHolder, NetroidTask item, int position)
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
			private NetroidTask mNetroidTask = null;

			public TaskClick(NetroidTask item)
			{
				mNetroidTask = item;
			}

			@Override
			public void onClick(View v)
			{
				if (mNetroidTask == null)
					return;

				DownloadController controller = mNetroidTask.getDownloadController();
				switch (v.getId())
				{
				case R.id.start_btn:
					if (controller == null)
					{
						// 建立下载任务
						buildTask();
					}
					else
					{
						// 已经建立下载任务
						switch (controller.getStatus())
						{
						case DownloadController.STATUS_DOWNLOADING:
						case DownloadController.STATUS_WAITING:
							controller.pause();
							break;

						case DownloadController.STATUS_PAUSE:
							controller.resume();
							break;
						}
					}
					break;

				case R.id.delete_btn:
					if (controller != null)
						controller.discard();

					// 重新初始化下载任务
					mNetroidTask.setDownloadController(null);
					FileUtils.deleteFile(new File(DOWNLOAD_PATH, mNetroidTask.getFileName()).getPath());
					// Netroid下载过程中临时文件后缀是.tmp
					FileUtils.deleteFile(new File(DOWNLOAD_PATH, mNetroidTask.getFileName() + ".tmp").getPath());
					break;
				}
				mNetroidTask.invalidateTask();
			}

			private void buildTask()
			{
				File storeFile = new File(DOWNLOAD_PATH, mNetroidTask.getFileName());
				mNetroidTask.setDownloadController(Netroid.getFileDownloader().add(storeFile, mNetroidTask.getFileUrl(), new Listener<Void>()
				{
					@Override
					public void onPreExecute()
					{
						super.onPreExecute();
						Log.e(TAG, "Pre Execute");
					}

					@Override
					public void onProgressChange(long fileSize, long downloadedSize)
					{
						super.onProgressChange(fileSize, downloadedSize);
						mNetroidTask.setDownloadLength(downloadedSize);
						mNetroidTask.setFileSize(fileSize);
						mNetroidTask.invalidateTask();
					}

					@Override
					public void onSuccess(Void response)
					{
						super.onSuccess(response);
						// 下载完成，但是status状态还是downloading
						mNetroidTask.invalidateTask();
						Log.e(TAG, "Success");
					}

					@Override
					public void onFinish()
					{
						super.onFinish();
						// 不论下载完成或者失败，最后都会执行Finish，判断下载完成需要判断文件长度与下载文件长度
						mNetroidTask.invalidateTask();
						Log.e(TAG, "Finish");
					}

					@Override
					public void onError(NetroidError error)
					{
						super.onError(error);
						mNetroidTask.setDownloadController(null);
						mNetroidTask.invalidateTask();
						Log.e(TAG, "Error");
						if (error != null)
							Log.w(TAG, error.getMessage());
					}

					@Override
					public void onCancel()
					{
						super.onCancel();
						Log.e(TAG, "Cancel");
					}

				}));
				mNetroidTask.setDownloadLength(0);
				mNetroidTask.invalidateTask();
			}
		}
	}

}
