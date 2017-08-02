package cuiweiyou.wifisinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 类的说明：
 *
 * @author：崔维友
 * @phone：13241701472
 * @version：1.0.0
 * @created：2016/12/012,16/12/12
 */
public class WifiActivity extends Activity {

	private ListView mWifis;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);

		mWifis = (ListView) findViewById(R.id.wifis);

		final ArrayList<String> wifis = (ArrayList<String>) getIntent().getSerializableExtra("wifis");

		final Map<String, Boolean> selected = new HashMap<String, Boolean>();

		final BaseAdapter adapter =  new BaseAdapter(){

			@Override
			public int getCount() {
				return wifis.size();
			}

			@Override
			public Object getItem(int i) {
				return wifis.get(i);
			}

			@Override
			public long getItemId(int i) {
				return i;
			}

			@Override
			public View getView(int i, View view, ViewGroup viewGroup) {
				Holder holder;
				if(null == view) {
					holder = new Holder();

					view = View.inflate(WifiActivity.this, R.layout.item_wifi, null);
					holder.name = (TextView) view.findViewById(R.id.name);
					holder.level = (TextView) view.findViewById(R.id.level);
					holder.select = (ImageView) view.findViewById(R.id.select);
					holder.item = view.findViewById(R.id.item);

					holder.item.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View view) {
							String s = view.getTag().toString();

							if(selected.containsKey(s)){
								selected.put(s, !selected.get(s));
							} else {
								selected.put(s, true);
							}

							notifyDataSetChanged();
						}
					});

					view.setTag(holder);
				} else {
					holder = (Holder) view.getTag();
				}

				String s = wifis.get(i);

				try {
					JSONObject jo = new JSONObject(s);
					holder.name.setText(jo.getString("ssid"));
					holder.level.setText(jo.getString("level"));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if(selected.containsKey("" + i)){

					if(selected.get("" + i)){
						holder.select.setImageResource(R.mipmap.checked);
					} else {
						holder.select.setImageResource(R.mipmap.uncheck);
					}
				} else {
					holder.select.setImageResource(R.mipmap.uncheck);
				}

				holder.item.setTag(i);

				return view;
			}

			public Map<String, Boolean> getSelected(){
				return selected;
			}

			class Holder {
				TextView name;
				TextView level;
				ImageView select;
				View item;
			}
		};

		mWifis.setAdapter(adapter);

		findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");

				Iterator<String> keys = selected.keySet().iterator();
				while (keys.hasNext()){
					String key = keys.next();
					Boolean value = selected.get(key);

					if(value){
						try {
							String s = wifis.get(Integer.valueOf(key));
							sb.append(s);
							sb.append(",");
						} catch (NumberFormatException e){}
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("]");

				Intent idata = new Intent();
				idata.putExtra("selected", sb.toString());
				setResult(100, idata);
				finish();
			}
		});
	}
}
