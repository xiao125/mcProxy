package com.game.sdk;

//import com.UCMobile.PayPlugin.PayInterface;
//import com.iapppay.mpay.ifmgr.IPayResultCallback;
//import com.iapppay.mpay.ifmgr.SDKApi;
//import com.iapppay.mpay.tools.PayRequest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;

import com.game.sdk.activity.AutoLoginActivity;
import com.game.sdk.activity.AutomaticLoginActivity;
import com.game.sdk.activity.FastLoginActivity;
import com.game.sdk.activity.ForgotPasswordActivity;
import com.game.sdk.activity.StartWebView;
import com.game.sdk.bean.Data;
import com.game.sdk.bean.GameInfo;
import com.game.sdk.bean.GameUser;
import com.game.sdk.bean.PayInfo;
import com.game.sdk.bean.UserInfo;
import com.game.sdk.floatmenu.SusViewMager;
import com.game.sdk.listener.BaseListener;
import com.game.sdk.listener.InitListener;
import com.game.sdk.listener.LoginListener;
import com.game.sdk.listener.PayListener;
import com.game.sdk.listener.ReportListener;
import com.game.sdk.service.HttpService;
import com.game.sdk.task.SDK;
import com.game.sdk.util.DBHelper;
import com.game.sdk.util.KnLog;
import com.game.sdk.util.TodayTimeUtils;
import com.game.sdk.util.Util;
import com.game.sdk.util.WxTools;
import com.game.sdk_project.SelecteLoginActivity;
import com.game.sdkproxy.R;

public class GameSDK {

	public static GameSDK instance = null;

	private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; //横竖屏
	private boolean isInited = false;
	private Activity activity = null;
	private LoginListener mLoginListener = null;
	private PayListener mPayListener = null;
	private ReportListener mReportListener =null;
	private InitListener mInitListener= null;
	private UserInfo userInfo = null;
	private GameInfo gameInfo = null;
	private GameUser gameUser = null;
	private boolean  mScreenSensor = false ;

	private SusViewMager mSusViewMager;

	public boolean isAuot=false;

	public boolean ismScreenSensor() {
		return mScreenSensor;
	}

	public void setmScreenSensor(boolean mScreenSensor) {
		this.mScreenSensor = mScreenSensor;
	}

	public static GameSDK getInstance() {
		if (instance == null) {
			instance = new GameSDK();
		}
		return instance;
	}

	/**
	 * @param activity
	 * @param
	 * *  0为横屏 ， 1为竖屏
	 */
	public void initSDK(Activity activity, GameInfo gameInfo) {
		this.activity = activity;
		this.isInited = true;
		setmOrientation(gameInfo.getOrientation());
		setGameInfo(gameInfo);
		SDK.changeConfig(gameInfo.getAdChannelTxt());
		KnLog.setLogEnable(true);

		//	读取activity中manifest.xml中某个键值对是否支持横竖屏切换
		ApplicationInfo ai;
		String adChannel = null ;
		try {
			ai = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			if(null==bundle){
				KnLog.e("bundle is null");
			}else{
				if(bundle.containsKey("ScreenSendor")){
					mScreenSensor = true ;
				}else{
					
				}
			}
		    
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		/*Data.getInstance().setGameActivity(activity);
		//读取ass文件参数
		Data.getInstance().setGameInfo(gameInfo);
*/

		//上报数据
		//RecordActivate.getInstance().init(activity);
	//	mInitListener.onSuccess(0);


	}

	/**
	 * 
	 * @param activity
	 * @param appid
	 *            爱贝appid
	 * @param appkey
	 *            爱贝appkey
	 */
	public void initIappaySDK(Activity activity,String appid, String appkey , String publicKey) {
		//PAY_API.getInstance().init(activity, getmOrientation(), appid , appkey  , publicKey);
	}

	/**
	 * 跳转到登陆页面
	 * 
	 * @param
	 * @param
	 */
	public void login( Activity activity ){
		KnLog.log(" login ccc");
		if (!isInited()) {
			Util.ShowTips(activity, activity.getResources().getString(R.string.mc_tips_16) );
			return;
		}

		String[] usernames = DBHelper.getInstance().findAllUserName();
		Intent intent = null;
		//	数据库中获取用户数据量
		if (usernames.length == 0 ) {
			intent = new Intent(activity.getApplicationContext(), SelecteLoginActivity.class);
			intent.putExtra("selectLogin", "selectLogin");
		} else {

			String  lastUserName = usernames[0];
			KnLog.log("lastUserName:"+lastUserName);
			intent = new Intent(activity.getApplicationContext(), AutoLoginActivity.class);
			KnLog.log("lastUserName="+lastUserName);
			intent.putExtra("userName",lastUserName);
		}
		
		if( null == intent ){
			return ;
		}
		
		activity.startActivity(intent);
		activity.finish();
		activity = null ;
		
	}
	
	public void login(Activity activity, LoginListener listener,SusViewMager.OnLogoutListener logoutListener) {

		if (listener == null) {
			KnLog.e("请先设置登录监听");
			Util.ShowTips(activity, "请先设置登录监听");
		}
		setmLoginListener(listener);
		if (!isInited()) {
			Util.ShowTips(activity, activity.getResources().getString(R.string.mc_tips_16));
			return;
		}

		String[] usernames = DBHelper.getInstance().findAllUserName();
		Intent intent = null;
		//	数据库中获取用户数据量
		if (usernames.length == 0) {
			intent = new Intent(activity.getApplicationContext(), FastLoginActivity.class);
			intent.putExtra("selectLogin", "selectLogin");
			activity.startActivity(intent);
			isAuot=true;
		}else {
			Object isLogout = TodayTimeUtils.getLogout(activity);
			String result = Util.getAssetsFileContent(activity, "SDKFile/adChannel.png");
			String game = Util.getJsonStringByName(result, "game");
			if (game.equals("p3")) {
				KnLog.log("=========p3渠道配置,注销状态=======" + isLogout.toString());
				if (isLogout.equals("true")) {
					KnLog.log("=========显示选择账号-=======");
					intent = new Intent(activity, AutoLoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("logout", "logout");
					activity.startActivity(intent);
					TodayTimeUtils.setLogout(activity, "false");
					KnLog.log("sdk注销账号了2");
				} else {
					KnLog.log("=========自动登录1-=======" + isAuot);
					if (!isAuot) {
						intent = new Intent(activity, AutomaticLoginActivity.class);
						activity.startActivity(intent);
						isAuot = false;
					} else {
						intent = new Intent(activity, AutomaticLoginActivity.class);
						activity.startActivity(intent);
						KnLog.log("=========11111========");
						isAuot = false;
					}
				}
			}
		}

		KnLog.log("=========自动登录2-======="+isAuot);
       //开启悬浮窗
		mSusViewMager = SusViewMager.getInstance();
		if (mSusViewMager !=null){
			mSusViewMager.setOnLogoutListener(logoutListener);
		}
		mSusViewMager.showWithCallback(activity);
	}

	//游戏内切换账号接口
	public  void McLogout(Activity activity){
		Intent intent1 = new Intent(activity, AutoLoginActivity.class);
		// intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent1.putExtra("logout","logout");
		activity.startActivity(intent1);

		//注销
		HttpService.doCancel("2",new BaseListener() {
			@Override
			public void onSuccess(Object result) {
			}

			@Override
			public void onFail(Object result) {

			}
		});
	}

	//退出
	public void  McQuit(){
		//注销
		KnLog.log("sdk退出");
		HttpService.doCancel("1",new BaseListener() {
			@Override
			public void onSuccess(Object result) {
				KnLog.log("退出返回数据"+result);
			}

			@Override
			public void onFail(Object result) {

			}
		});
	}

	public void McLogout(SusViewMager.OnLogoutListener logoutListener){
		mSusViewMager = SusViewMager.getInstance();
		if (mSusViewMager !=null){
			mSusViewMager.setOnLogoutListener(logoutListener);
		}
		logoutListener.onExitFinish();
		RecordActivate.getInstance().init(activity);


		//延迟1.5S游戏跳转到登录界面后弹出登录框
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				//防止第一没有账号就点击注销
				Intent intent1 = new Intent(activity, AutoLoginActivity.class);
				// intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent1.putExtra("logout","logout");
				activity.startActivity(intent1);
				KnLog.log("sdk注销账号了2");
			}
		},1000);
	}

	//悬浮窗注销功能
	public void setLogoutListener(SusViewMager.OnLogoutListener onLogoutListener){

		if(mSusViewMager!=null){
			mSusViewMager.setOnLogoutListener(onLogoutListener);
			KnLog.log("注销回调2");
		}else {

		}
	}


	/**
	 * 上报游戏信息
	 * @param activity
	 * @param gameUser
	 * @param listener
	 */
	public void reportGameRole(Activity activity, GameUser gameUser ,ReportListener listener){
		setmReportListener(listener);
		RecordGame.getInstance().roleInfo(activity,gameUser);
	}

	// 跳转到快速注册页面
	public void KsRegister(Activity activity, boolean hasResult) {
		if (hasResult)
		{
			Intent intent = new Intent(activity.getApplicationContext(),FastLoginActivity.class);
			activity.startActivityForResult(intent, SDK.REQUESTCODE_REG);
			activity.finish();
		}
	}

	// 跳转到修改密码
	public void Update_password(Activity activity, boolean hasResult) {

		if (hasResult)
		{
			Intent intent = new Intent(activity.getApplicationContext(),ForgotPasswordActivity.class);
			activity.startActivityForResult(intent, SDK.UPDATE_PASSWORD);
			activity.finish();
		/*	Intent intent = new Intent(activity.getApplicationContext(),PasswordUpdateActivity.class);
			activity.startActivityForResult(intent, SDK.UPDATE_PASSWORD);
			activity.finish();*/
		}
		/*else
		{
			Intent intent = new Intent(activity.getApplicationContext(), RegisterActivity.class);
			activity.startActivity(intent);
		}*/
	}

	/**
	 * 
	 * @param activity
	 * @param
	 * @param
	 */
	public void pay(final Activity activity, final PayInfo payInfo,
			final PayListener payListener) {
		if (userInfo == null || !userInfo.isLogin()) {
			Util.ShowTips(activity,  activity.getResources().getString(R.string.mc_tips_17) );
			return;
		}
		setmPayListener(payListener);
		//PAY_API.getInstance().pay(activity, payInfo, payListener);
	}

	//打开web， 参数： web支付总界面url  与 单独 微信支付url
	public void openWeb(Activity act , String url ,String wxUrl ){
		Intent intent = new Intent(act,StartWebView.class);
		intent.putExtra("url",  url );
		intent.putExtra("wxUrl",  wxUrl );
		act.startActivity( intent );
	}


	//关闭
	public void hideFloat(){

		if (mSusViewMager!=null){
			mSusViewMager.hideFloat();
		}
	}

	//移除所有悬浮窗
	public void destoryFloat(){
		if (mSusViewMager!=null){
			mSusViewMager.destroyFloat();
		}
	}


	public int getmOrientation() {
		return mOrientation;
	}

	private void setmOrientation(int mOrientation) {
		this.mOrientation = mOrientation;
	}

	public Activity getActivity() {
		return this.activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public LoginListener getmLoginListener() {
		return mLoginListener;
	}

	private void setmLoginListener(LoginListener mLoginListener) {
		this.mLoginListener = mLoginListener;
	}

	public PayListener getmPayListener() {
		return mPayListener;
	}

	public void setmPayListener(PayListener mPayListener) {
		this.mPayListener = mPayListener;
	}

	public ReportListener getmReportListener() {
		return mReportListener;
	}

	public void setmReportListener(ReportListener mReportListener) {
		this.mReportListener = mReportListener;
	}

	public boolean isInited() {
		return isInited;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public GameInfo getGameInfo() {
		return gameInfo;
	}

	public void setGameInfo(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
	}

	public GameUser getGameUser() {
		return gameUser;
	}

	public void setGameUser(GameUser gameUser) {
		this.gameUser = gameUser;
	}

	public InitListener getmInitListener() {
		return mInitListener;
	}

	public void setmInitListener(InitListener mInitListener) {
		this.mInitListener = mInitListener;
	}
}
