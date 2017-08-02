package cuiweiyou.wifisinfo;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

	/**
	 * 第一次扫描wifi
	 **/
	private boolean isFirstScanWif = true;
	/** 全部的wifi **/
	private ArrayList<String> mAllWifis;
	/** 已选择作为参照物的wifi **/
	private JSONArray mSelectedJA;
	/** 临时，获取的索引 **/
	private int tIndex = 0;
	/**
	 * 解读Service发来的消息
	 **/
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if ("wifis".equals(msg.obj.toString())) {
				Bundle data = msg.getData();
				mAllWifis = data.getStringArrayList("wifis");
				mCurrDegree = data.getFloat("degree");

				if(!isFirstScanWif) {
					updateLocal();
				} else {
					isFirstScanWif = false;

					// 选择定位wifi
					Intent i = new Intent(MainActivity.this, WifiActivity.class);
					i.putExtra("wifis", mAllWifis);
					startActivityForResult(i, 100);

					dialog.hide();
				}
			}
		}
	};

	/**
	 * 前往Service的信使
	 **/
	private Messenger msgerInActivity = new Messenger(handler);
	/**
	 * 从Service拿到的信使
	 **/
	private Messenger msgerFromService;
	/**
	 * 可收到拖放控件的容器型控件。最底是地图
	 **/
	private WifiRelativeLayout mMap;
	/**
	 * 朝向指标
	 **/
	private View mSorrow;
	/**
	 * 上次朝向
	 **/
	private float mLastDegree = 0f;
	/**
	 * 当前朝向
	 **/
	private float mCurrDegree = 0f;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
	}

	public void initView() {
		mMap = (WifiRelativeLayout) findViewById(R.id.map);
		mMap.setConfirmBack(confirmLocalBack); // 定位完成回调
		mSorrow = findViewById(R.id.sorrow);

		dialog = new ProgressDialog(this);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage("稍等");
	}

	@Override
	public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
		msgerFromService = new Messenger(iBinder);

		Message msg = new Message();
		msg.obj = "startWatchWifiList";
		msg.replyTo = msgerInActivity; // 指定回复器

		// 向服务发消息
		try {
			msgerFromService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {
		Intent it = new Intent(MainActivity.this, SensorAndWifiService.class);
		bindService(it, MainActivity.this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 解除绑定的服务
		unbindService(MainActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(1, 1, 1, "1.加载地图").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { // 添加选中事件
			@Override
			public boolean onMenuItemClick(MenuItem item) {

				// 从文件选择地图图片，设置显示
				mMap.setBackgroundResource(R.mipmap.map);

				return true;
			}
		});
		menu.add(1, 2, 2, "2.扫描wifi").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { // 添加选中事件
			@Override
			public boolean onMenuItemClick(MenuItem item) {

				dialog.show();

				/** 绑定服务 **/
				Intent it = new Intent(MainActivity.this, SensorAndWifiService.class);
				bindService(it, MainActivity.this, Context.BIND_AUTO_CREATE);

				return true;
			}
		});
		menu.add(1, 3, 3, "3.定位").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { // 添加选中事件
			@Override
			public boolean onMenuItemClick(MenuItem item) {

				mSorrow.setVisibility(View.VISIBLE);

				return true;
			}
		});

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(100 == requestCode && 100 == resultCode) {
			String json = data.getStringExtra("selected");

			try {
				mSelectedJA = new JSONArray(json);

				setLocal();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void setLocal() {
		try {
			JSONObject jo = mSelectedJA.getJSONObject(tIndex);
			String name = jo.getString("ssid");
			String level = jo.getString("level");

			// 拖放wifi位置
			mMap.addChildView( name, level); // 子控件名，信号强度

			Toast.makeText(MainActivity.this, "设置位置", Toast.LENGTH_SHORT).show();
		}catch (JSONException e){
			Toast.makeText(this, "设置完毕", Toast.LENGTH_SHORT).show();
		}
	}

	private WifiRelativeLayout.ConfirmLocalBack confirmLocalBack = new WifiRelativeLayout.ConfirmLocalBack(){
		@Override
		public void confirmBack() {
			tIndex += 1;
			setLocal();
		}
	};

	/**
	 * 更新箭头
	 **/
	private void updateLocal() {
		updateSorrowLocal();
		updateSorrowForwad();
	}

	/**
	 * 更新位置
	 **/
	private void updateSorrowLocal() {
		//mSelectedJA
		//mAllWifis
		//mCurrDegree

		mMap.drawCircle(mAllWifis);
	}

	/**
	 * 更新方向
	 **/
	private void updateSorrowForwad() {
		// float fromDegrees：旋转的开始角度。
		// float toDegrees：旋转的结束角度。
		// int pivotXType：X轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
		// float pivotXValue：X坐标的伸缩值。
		// int pivotYType：Y轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
		// float pivotYValue：Y坐标的伸缩值。
		RotateAnimation anim = new RotateAnimation(mLastDegree, mCurrDegree, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(100);//设置动画持续时间
		anim.setFillAfter(true);//动画执行完后是否停留在执行完的状态
		mSorrow.startAnimation(anim);

		mLastDegree = mCurrDegree;
	}
}

