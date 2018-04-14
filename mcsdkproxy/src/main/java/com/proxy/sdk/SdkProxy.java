package com.proxy.sdk;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View.OnClickListener;

import com.proxy.Data;
import com.proxy.Listener;
import com.proxy.activity.settingActivity;
import com.proxy.bean.FuncButton;
import com.proxy.bean.GameInfo;
import com.proxy.bean.GameUser;
import com.proxy.bean.KnPayInfo;
import com.proxy.listener.BaseListener;
import com.proxy.listener.InitListener;
import com.proxy.listener.LoginListener;
import com.proxy.listener.PayListener;
import com.proxy.listener.PushActivationListener;
import com.proxy.listener.PushDataListener;
import com.proxy.net.callback.IError;
import com.proxy.net.callback.IFailure;
import com.proxy.net.callback.ISuccess;
import com.proxy.service.HttpService;
import com.proxy.util.LogUtil;

public class SdkProxy {
	
	protected Data knData = Data.getInstance();
	protected Listener kNListener = Listener.getInstance();
	
	protected LoginListener mLoginListener =  null;
	protected PayListener mPayListener = null;
	protected PushDataListener mPushDataListener = null ;
	protected PushActivationListener   mPushActivationListener = null ;
	protected InitListener mInitListener = null;
	
	protected GameInfo mGameInfo = null;
	protected GameUser mGameUser = null;
	
	protected Activity mActivity = null;
	public static OnClickListener onclickwx = null;
	public static OnClickListener onclickqt = null;
	
	protected void onCreate(Activity activity){
		LogUtil.i("<============onCreate================>");
		mActivity = activity;
		mGameInfo = knData.getGameInfo();
		
		mInitListener = kNListener.getInitListener();
		
		if(mInitListener==null){
			
			LogUtil.e("没有设置初始化回调");
			
		}else{
			
			LogUtil.e("设置初始化回调==");
			
		}
		
		mLoginListener =  kNListener.getLoginListener();


	
	}
	
	protected boolean canEnterGame() {
		return true;
	}

	protected void onEnterGame(Map<String, Object> data) {
		LogUtil.i("<============onEnterGame================>");
		
		setUserData(data);

		//TODO 上报游戏数据接口
		/*HttpService.enterGame(new BaseListener() {
			@Override
			public void onSuccess(Object result) {
				LogUtil.i("上报成功=="+result.toString());
			}
			
			@Override
			public void onFail(Object result) {
				LogUtil.i("send enterGame data is failed!");
			}
		});
*/

		HttpService.enterGame(mActivity, new ISuccess() {
			@Override
			public void onSuccess(String response) {
				LogUtil.log("=======上报数据成功======="+response.toString());

			}
		}, new IError() {
			@Override
			public void onError(int code, String msg) {

				LogUtil.i("=======上报数据失败=======code="+code+" msg"+msg);
			}
		}, new IFailure() {
			@Override
			public void onFailure() {

				LogUtil.i("=======返回上报数据失败=======");
			}
		});

		
	}
	
	private void setUserData(Map<String, Object> data) {

		mGameUser = new GameUser();

		LogUtil.e("setUserData func is entergame data222: " + data);
//		String sceneId = "0";
//		int userLv = 1;
//		try {
//			sceneId = data.get("scene_id") == null ? "0" : data.get("scene_id").toString();
//			userLv = data.get("userLv") == null ? 0 : Integer.parseInt(data.get("userLv").toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (sceneId == "4") {
//			try {
//				knData.getGameUser().setUserLevel(userLv);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
			try {
				String userId = data.get("userId") == null ? "none" : data.get("userId").toString();
				int serverId = data.get("serverId") == null ? 0 : Integer.parseInt(data.get("serverId").toString());
				LogUtil.e("entergame : userId=" + userId + " serverId =  "+ serverId);

				int userLevel = data.get("userLv") == null ? 0 : Integer.parseInt(data.get("userLv").toString());
				LogUtil.e("userLevel = " + userLevel);
				mGameUser.setUid(userId);
				mGameUser.setServerId(serverId);
				mGameUser.setUserLevel(userLevel);
				mGameUser.setExtraInfo(data.get("extraInfo") == null ? "none": data.get("extraInfo").toString());
				mGameUser.setServerName(data.get("serverName") == null ? "none": data.get("serverName").toString());
				mGameUser.setUsername(data.get("roleName") == null ? "none": data.get("roleName").toString());
				mGameUser.setVipLevel(data.get("vipLevel") == null ? "0" : data.get("vipLevel").toString());
				mGameUser.setFactionName(data.get("factionName") == null ? "none": data.get("factionName").toString());
				mGameUser.setNewRole(data.get("isNewRole") == null ? false: (Boolean) data.get("isNewRole"));
				mGameUser.setRoleId(data.get("roleId") == null ? "0" : data.get("roleId").toString());
				mGameUser.setScene_id(data.get("scene_id") == null ? "0" : data.get("scene_id").toString());
				mGameUser.setBalance(data.get("balance") == null ? "0" : data.get("balance").toString());
				mGameUser.setRoleCTime(data.get("roleCTime") == null ? "0": data.get("roleCTime").toString());

			} catch (Exception e) {
				e.printStackTrace();
			}

			knData.setGameUser(mGameUser);
//		}
	}
	
	protected void showUserCenter() {
		LogUtil.i("<============showUserCenter================>");
	}
	
	protected void onEnterGame(Map<String, Object> data , BaseListener baseListener) {
		LogUtil.i("<============onEnterGame================>");
		
		setUserData(data);
		
		//HttpService.enterGame(baseListener);
	}


	private void setUserLv(int userLevel){
		
				
		LogUtil.e("userLevel: "+userLevel);			
		Data.getInstance().getGameUser().setUserLevel(userLevel);
		//mGameUser.setUserLevel(userLevel);
		//knData.setGameUser(mGameUser);
	}

	protected void onGameLevelChanged(int newlevel) {
		LogUtil.i("<============onGameLevelChanged================>");
		setUserLv(newlevel);
	}

	protected void onResume() {
		LogUtil.i("<============onResume================>");
	}

	protected void onPause() {
		LogUtil.i("<============onPause================>");
	}

	protected void onStop() {
		LogUtil.i("<============onStop================>");
	}

	protected void onDestroy() {
		LogUtil.i("<============onDestroy================>");
	}
	
	protected void onRestart() {
		LogUtil.i("<============onRestart================>");
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.i("<============onActivityResult================>");
	}
	
	protected void login(Activity activity , Map<String, Object> params) {
		LogUtil.i("<============login================>");
		if(mLoginListener == null){
			mLoginListener =  kNListener.getLoginListener(); 
		}
		
		
		if(mLoginListener == null){
			LogUtil.e("没有设置登陆回调");
		}
		
	}
	
	protected void pay( Activity activity ,KnPayInfo knPayInfo) {
		LogUtil.i("<============pay================>");
		mPayListener = kNListener.getPayListener();
		if(mPayListener == null){
			LogUtil.e("没有设置支付回调");
		}
	}

	protected boolean hasThirdPartyExit() {
		return false;
	}

	protected boolean hasSwitchUserView() {
		return false;
	}

	protected void onThirdPartyExit() {
		
		LogUtil.i("<============onThirdPartyExit================>");
		
	}
	
	protected String getChannelVersion() {
		return "1.0.0";
	}

	public String getChannelName() {
		return mGameInfo.getChannel();
	}

	protected void onNewIntent(Intent intent) {
		
		LogUtil.i("<============onNewIntent================>");
		
	}
	
	protected void onConfigurationChanged(Configuration newConfig){
		
		LogUtil.i("<============onConfigurationChanged================>");
		
	}

	protected void onStart() {
		
		LogUtil.i("<============onStart================>");
		
	}
	
	protected FuncButton[] getSettingItems(){
		/*return  new FuncButton[]{
				new FuncButton("账号注销",new FuncButton.OnClickListener() {
					@Override
					public void onClick() {
						System.err.println("账号注销");
					}
				}),
			};*/
		return null;
	}
	
	protected void callSettingView() {
		
		if(this.getSettingItems()!=null)
		{
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mActivity.startActivity(new Intent(mActivity , settingActivity.class));
				}
			});
		}
		else
			LogUtil.e("No SDK settings page");
		
	}

	protected void onSaveInstanceState(Bundle outState) {
		
		LogUtil.i("<============onSaveInstanceState================>");
		
	}

	public String getAdChannel() {
		return mGameInfo.getAdChannel();
	}
	
	protected void  pushData( final Activity activity , Map<String,Object> data  ) {
		
		LogUtil.e("<============pushData================>");
		mPushDataListener = kNListener.getPushDataListener();
		
		if(mPushDataListener == null ){
			LogUtil.e("没有设置推送回调");
			return ;
		}
		
	}
	
	public void  pushActivation( final  Activity activity , Map<String,Object> data ) {
		
		LogUtil.e("<============pushActivation================>");
		mPushActivationListener = kNListener.getPushActivationListener();
		if(mPushActivationListener == null ){
			LogUtil.e("没有设置激活码回调");
			return ;
		}
		
	}
	
	protected void activation( final Activity activity ) {
		
		
		
	}
	
	protected void finish(){
		
		LogUtil.i("<============finish================>");
		
	}
	
	protected void onWindowFocusChanged(boolean hasFocus) {
		
		LogUtil.i("<============onWindowFocusChanged================>");
		
	}
	
	protected void switchAccount() {
		LogUtil.i("<============switchAccount================>");
	}
	
	protected void logout(){
		LogUtil.i("<============logout================>");
	}
	
	protected void onBackPressed() {
		// TODO Auto-generated method stub
		LogUtil.i("<============onBackPressed================>");
	}
	
	
}
