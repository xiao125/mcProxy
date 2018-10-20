package com.game.sdk.activity;

import com.game.sdk.util.DeviceUtil;
import com.game.sdk.util.JsInterface;
import com.game.sdk.util.KnLog;
import com.game.sdk.util.LoadingDialog;
import com.game.sdkproxy.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class StartWebView extends Activity implements OnClickListener {

	private WebView webView;
	private static String m_url = null;
	private com.game.sdk.util.JsInterface JsInterface = new JsInterface();
	private static Activity m_activity = null;
	private ProgressDialog waitdialog = null;
	private static long mTime = System.currentTimeMillis();
	private static String m_wxUrl;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_activity = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉应用标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.mc_activity_startweb_layout);

		m_url = getIntent().getExtras().getString("url");
		m_wxUrl = getIntent().getExtras().getString("wxUrl");
		KnLog.e("url:" + m_url+"  wxUrl="+m_wxUrl);

		LoadingDialog.show(m_activity, "请稍等...", true);

		if (DeviceUtil.isFastMobileNetwork(m_activity)
				|| DeviceUtil.isWifi(m_activity)) {
			// 高速网络忽略
			KnLog.e("高速网络");
		} else {
			// 延时三秒停止
			KnLog.e("非高速网络");
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					waitdialog.dismiss();
				}

			}, 3000);
		}
		initView();
		initwebView();
	}

	private void initView(){
		webView = (WebView) findViewById(R.id.webView);
	}

	private void initwebView(){

		WebSettings ws = webView.getSettings();
		ws.setBuiltInZoomControls(true);// 隐藏缩放按钮
		ws.setUseWideViewPort(true);// 可任意比例缩放
		ws.setSavePassword(true);
		ws.setSaveFormData(true);// 保存表单数据
		ws.setJavaScriptEnabled(true);
		ws.setDomStorageEnabled(true);
		ws.setSupportMultipleWindows(true);// 新加
		// 如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
		ws.setJavaScriptEnabled(true);
		//ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		webView.setWebViewClient(new myWebViewClient()); //webView加载支付总页面
		webView.addJavascriptInterface(JsInterface, "JsInterface");
		JsInterface.setWvClientClickListener(new MyWebViewClient());// 这里就是js调用java端的具体实现
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.loadUrl(m_url);
	}

	@Override
	public void onClick(View v) {

	}


	//webView加载总支付界面
	public class myWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			KnLog.e("shouldOverrideUrlLoading  url = " + url);
			 view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			// waitdialog.dismiss();
			LoadingDialog.dismiss();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (webView != null) {
			webView.onResume();
			webView.resumeTimers();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (webView != null) {
			webView.onPause();
			webView.pauseTimers();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		webView.loadUrl("about:blank");
		webView.stopLoading();
		webView.setWebChromeClient(null);
		webView.setWebViewClient(null);
		webView.destroy();
		webView = null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			KnLog.e("返回++");
			StartWebView.this.finish();
		}
		return false;
	}

	class MyWebViewClient implements com.game.sdk.util.JsInterface.wvClientClickListener {

		@Override
		public void wvHasClickEnvent(String title, String content, String imageUrl, String url) {

		}

		@Override
		public void wvCloseWebEvent() {
			KnLog.log("关闭支付页面");
			m_activity.finish();
			m_activity = null;
		}

		@Override
		public void wvWxWebPayEvent() {
			KnLog.e("浏览器打开微信支付页面");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(m_wxUrl));
			startActivity(intent);
		}
	}







}
