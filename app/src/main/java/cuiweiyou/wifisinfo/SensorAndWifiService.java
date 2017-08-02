package cuiweiyou.wifisinfo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 类的说明：
 *
 * @author：崔维友
 * @phone：13241701472
 * @version：1.0.0
 * @created：2016/12/009,16/12/9
 */

public class SensorAndWifiService extends Service implements SensorEventListener {

	private Messenger msgerInActivity;

	/** 处理aty发来的消息 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if ("startWatchWifiList".equals(msg.obj.toString())) {
				// 得到aty中的信使
				msgerInActivity = msg.replyTo;

				Message message = new Message();
				message.obj = "子线程启动了";
				try {
					msgerInActivity.send(message);
				} catch (RemoteException e) {
					e.printStackTrace();
				}

				runnable = true;

				startWatchSensor(); // 开启传感器定向
			}
		}
	};

	/** 用于提供一个绑定器，和Aty里的Mesger关联 **/
	private Messenger msgerFromService = new Messenger(handler);
	/** 获取wifi状态条件。在while内执行 **/
	private boolean runnable = false;
	/** while死循环条件 **/
	private boolean canRun = true;
	/** 传感器 **/
	private SensorManager mSensorMng;
	/** 当前朝向 **/
	private float mCurrentDegree;

	@Override
	public void onCreate() {
		super.onCreate();

		startWatchWifiList(); // 开启wifi信号监听
	}

	/**
	 * 执行绑定的方法
	 * Activity就靠这个方法和Service联络了
	 */
	@Override
	public IBinder onBind(Intent intent) {

		return msgerFromService.getBinder();
	}

	/**
	 * 再次绑定
	 */
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	/**
	 * 解除绑定
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		runnable = false;
		canRun = false;

		//return true; // 在仅bindService方式，返回值无所谓
		return super.onUnbind(intent); // false。结合startService时须区别
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mSensorMng.unregisterListener(this);

		runnable = false;
		canRun = false;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			mCurrentDegree = event.values[0];
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {}

	public void startWatchSensor() {
		mSensorMng = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensorMng.registerListener(this, mSensorMng.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void startWatchWifiList() {
		new Thread(
				new Runnable() {
					@Override
					public void run() {
						while (canRun) {

							if (runnable) {
								getwifilist();
							}

							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
		).start();
	}

	private void getwifilist() {
		WifiManager wifi_service = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		List<ScanResult> results = wifi_service.getScanResults();

		sortByLevel(results);
		ArrayList<String> alist = getStringArrayList(results);

		Bundle data = new Bundle();
		data.putStringArrayList("wifis", alist);
		data.putFloat("degree", mCurrentDegree);

		try {
			Message rs = new Message();
			rs.obj = "wifis";
			rs.setData(data);

			if (null != msgerInActivity)
				msgerInActivity.send(rs);                // 向aty回复消息

		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("ard", "发出异常：" + e.toString());
		}
	}

	private ArrayList<String> getStringArrayList(List<ScanResult> results) {
		ArrayList<String> list = new ArrayList<String>();

		for (int i = 0; i < results.size(); i++) {
			ScanResult re = results.get(i);

			String bssid = re.BSSID; //
			String ssid = re.SSID; //
			int level = re.level; // 信号强度
//			int channelWidth = re.channelWidth;// mi2pad没有此属性

			StringBuilder sb = new StringBuilder();
			sb.append("{");

			sb.append("\"");
			sb.append("bssid");
			sb.append("\"");
			sb.append(":");
			sb.append("\"");
			sb.append(bssid);
			sb.append("\"");
			sb.append(",");

			sb.append("\"");
			sb.append("ssid");
			sb.append("\"");
			sb.append(":");
			sb.append("\"");
			sb.append(ssid);
			sb.append("\"");
			sb.append(",");

			sb.append("\"");
			sb.append("level");
			sb.append("\"");
			sb.append(":");
			sb.append("\"");
			sb.append(level);
			sb.append("\"");
//			sb.append(",");
//
//			sb.append("\"");
//			sb.append("channelWidth");
//			sb.append("\"");
//			sb.append(":");
//			sb.append("\"");
//			sb.append(channelWidth);
//			sb.append("\"");

			sb.append("}");

			list.add(sb.toString());
		}

		return list;
	}

	private void sortByLevel(List<ScanResult> list) {
		Collections.sort(list, new Comparator<ScanResult>() {
			public int compare(ScanResult o1, ScanResult o2) {
				if (o1.level < o2.level) {
					return 1;
				} else if (o1.level == o2.level) {
					return 0;
				}
				return -1;
			}
		});
	}
}
