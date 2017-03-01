package com.zv.downloader;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.excellence.basetoolslibrary.baseadapter.CommonAdapter;
import com.excellence.basetoolslibrary.baseadapter.ViewHolder;
import com.excellence.basetoolslibrary.utils.ActivityUtils;
import com.zv.downloader.bean.ActivityInfo;
import com.zv.downloader.downloader.MultiThreadActivity;
import com.zv.downloader.netroid.SingleThreadActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
	}

	private void init()
	{
		ListView listView = (ListView) findViewById(R.id.function_list_view);

		List<ActivityInfo> activityInfos = new ArrayList<>();
		activityInfos.add(new ActivityInfo("单线程断点", SingleThreadActivity.class));
		activityInfos.add(new ActivityInfo("多线程断点", MultiThreadActivity.class));
		listView.setAdapter(new ActivityAdapter(this, activityInfos, android.R.layout.simple_expandable_list_item_1));
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		ActivityInfo activityInfo = (ActivityInfo) parent.getItemAtPosition(position);
		ActivityUtils.startAnotherActivity(this, activityInfo.getActivityCls());
	}

	private class ActivityAdapter extends CommonAdapter<ActivityInfo>
	{
		public ActivityAdapter(Context context, List<ActivityInfo> activityInfos, int layoutId)
		{
			super(context, activityInfos, layoutId);
		}

		@Override
		public void convert(ViewHolder viewHolder, ActivityInfo item, int position)
		{
			viewHolder.setText(android.R.id.text1, item.getActivityName());
		}
	}
}
