package com.wxq.findjob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {
	
	EditText keywordsEt ;
	EditText pagesEt;
	Button searchBtn;
	
	ListView lv;
	SimpleAdapter mAdapter;
	
	Handler handler = new Handler();
	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
	
	Runnable searchRunnable = new Runnable() {
		
		@Override
		public void run() {
			//搜索
			//查询一次
			String [] pages = pagesEt.getText().toString().split(",");
			int [] pageInts = new int[pages.length];
			for (int i = 0; i < pages.length; i++) {
				pageInts[i] = Integer.parseInt(pages[i]);
			}
			List<Map<String, String>> newList = JobSpider.getUrlHrefs(
				      new String[] { "http://m.byr.cn", "http://m.newsmth.net" }, 
				      new String[] { "http://m.byr.cn/board/JobInfo", "http://m.newsmth.net/board/Career_Campus" }, 
				      keywordsEt.getText().toString().split(","), 
				      pageInts);
			System.out.println("total items count:"+list.size());
			list.clear();
			list.addAll(newList);

			handler.post(new Runnable() {
				
				@Override
				public void run() {
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		keywordsEt = (EditText) findViewById(R.id.etxt_keywords);
		pagesEt = (EditText) findViewById(R.id.etxt_pages);
		searchBtn = (Button) findViewById(R.id.btn_search);
		lv = (ListView) findViewById(R.id.list);
		
		mAdapter = new SimpleAdapter(
				this, 
				list, 
				R.layout.list_item, 
				new String[]{"title","time"}, 
				new int []{android.R.id.text1,android.R.id.text2});
		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 点击打开网页
				String url = list.get(position).get("url");
				Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(it);
			}
		});
		
		searchBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Thread thread = new Thread(searchRunnable);
				thread.start();
			}
		});
		
	}

}
