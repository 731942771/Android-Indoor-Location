package cuiweiyou.wifisinfo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 类的说明：
 *
 * @author：崔维友
 * @phone：13241701472
 * @version：1.0.0
 * @created：2016/12/012,16/12/12
 */

public class WifiRelativeLayout extends RelativeLayout {

	/** 手指按下滑动时的x坐标 **/
	private int currentX;
	/** 手指按下滑动时的y坐标 **/
	private int currentY;
	/** 可否拖动 **/
	private boolean canTouchDown;
	/** wifi实例 **/
	private View vWifi;
	/** wifi实例宽 **/
	private int mViewWidth;
	/** wifi实例高 **/
	private int mViewHeight;
	/** 定位完成回调 **/
	private ConfirmLocalBack back;
	private Paint paint;
	private int radis;

	public WifiRelativeLayout(Context context) {
		super(context);
	}

	public WifiRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WifiRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(null == paint){
			paint = new Paint();
		}

		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			drawCircle(canvas, getChildAt(i));
		}
	}

	private void drawCircle(Canvas canvas, View view) {
		paint.setStyle(Paint.Style.STROKE); // 设置空心
		paint.setColor(Color.parseColor("#0066ff")); // 设置圆环轨道颜色
		paint.setStrokeWidth(5); // 设置圆环的宽度
		paint.setAntiAlias(true); // 消除锯齿

		int c = Math.abs(Integer.valueOf(((TextView)view.findViewById(R.id.level)).getText().toString()));

		// 画出圆形（圆心x、圆心y，半径，画笔），如果paint为空心模式，画出来的就是圆环；实心模式画出的是圆饼
		canvas.drawCircle(view.getLeft() + mViewWidth / 2, view.getTop() + mViewHeight / 2, c * radis / 500, paint);
	}

	public void drawCircle(ArrayList<String> mAllWifis) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			TextView name = (TextView) v.findViewById(R.id.name);
			TextView level = (TextView) v.findViewById(R.id.level);

			for (int j = 0; j < mAllWifis.size(); j++) {
				String s = mAllWifis.get(j);
				try {
					JSONObject jo = new JSONObject(s);
					String ssid = jo.getString("ssid");
					if(name.getText().toString().equals(ssid)){
						level.setText(jo.getString("level"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		postInvalidate();
	}

	/**
	 * 设置子元素的布局位置
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount(); // 子元素数量

		for (int i = 0; i < count; i++) {// 遍历子元素
			View child = this.getChildAt(i);

			int left = child.getLeft(); // 子元素定位，相对于viewgroup左上角的位置
			int top = child.getTop();   // 子元素的4个边，都相对于viewgroup的左上角来确定

			int childWidth = child.getMeasuredWidth();   // 获取子元素自身内容的实际宽高
			int childHeight = child.getMeasuredHeight();

			//            // 测量子元素内容占用的实际宽高
			//            child.measure(
			//                    MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
			//                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));

			// 确定子元素的布局位置
			child.layout(left, top, left + childWidth, top + childHeight);
		}

		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();

		radis = measuredHeight > measuredWidth ? measuredHeight : measuredWidth;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!canTouchDown)
			return super.onTouchEvent(event);

		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动对应的事件
				currentX = (int) event.getRawX();
				currentY = (int) event.getRawY();

				// 得到imageView最开始的各顶点的坐标
				int l = currentX - mViewWidth / 2;
				int r = currentX + mViewWidth / 2;
				int t = currentY - mViewHeight * 2;
				int b = currentY + mViewHeight * 2;

				vWifi.layout(l, t, r, b);

				postInvalidate(); // 重绘UI

				break;
			case MotionEvent.ACTION_UP:// 手指离开屏幕对应事件

				confirmLocal();

				break;
		}

		return true;// 不会中断触摸事件的返回
	}

	private void confirmLocal() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMessage("确定此位置吗");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				//vWifi = null;

				canTouchDown = false;

				back.confirmBack();
			}
		});
		builder.setNegativeButton("继续", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void setConfirmBack(ConfirmLocalBack confirmLocalBack){
		back = confirmLocalBack;
	}

	public void addChildView(String jsonn, String leveln){
		canTouchDown = true;

		vWifi = View.inflate(getContext(), R.layout.view_wifi, null);
		TextView name = (TextView) vWifi.findViewById(R.id.name);
		TextView level = (TextView) vWifi.findViewById(R.id.level);
		name.setText(jsonn);
		level.setText(leveln);

		vWifi.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mViewWidth = vWifi.getMeasuredWidth();
		mViewHeight = vWifi.getMeasuredHeight();

		addView(vWifi); // 会调用容器的onlayout方法
	}

	public interface ConfirmLocalBack{
		public void confirmBack();
	}
}
