package com.proxy.service;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Build;

import com.proxy.*;
import com.proxy.bean.GameInfo;
import com.proxy.bean.GameUser;
import com.proxy.bean.KnPayInfo;
import com.proxy.bean.Result;
import com.proxy.bean.User;
import com.proxy.listener.BaseListener;
import com.proxy.listener.LoginListener;
import com.proxy.net.RestClient;
import com.proxy.net.callback.IError;
import com.proxy.net.callback.IFailure;
import com.proxy.net.callback.ISuccess;
import com.proxy.sdk.channel.SDKConfig;
import com.proxy.sdk.module.GeTuiPushModule;
import com.proxy.task.CommonAsyncTask;
import com.proxy.util.DeviceUtil;
import com.proxy.util.LoadingDialog;
import com.proxy.util.LogUtil;
import com.proxy.util.Util;
import com.proxy.util.Md5Util;
import com.yunva.im.sdk.lib.model.troops.WheatInfo;

public class HttpService {


	public static void doLogin(Activity activity,
							   String content , final BaseListener listener) {

			GameInfo gameInfo = Data.getInstance().getGameInfo();
			WeakHashMap<String , Object> params = getCommonParamsMap();
			LogUtil.e("params:"+params.toString());
			String game_id = gameInfo.getGameId();
			String platform = gameInfo.getPlatform();
			String channel = gameInfo.getChannel();
			params.put("content", content);
			params.put(
					"sign",
					Md5Util.getMd5(game_id + channel
							+ platform + content.toString()
							+ gameInfo.getAppKey()));
			LogUtil.e("AppKey"+gameInfo.getAppKey());
			LogUtil.e("game_id"+Data.getInstance().getGameInfo().getGameId());
			LogUtil.e("game_id = "+game_id+"  channel="+channel+"  AppKey="+gameInfo.getAppKey());


		//网络请求
		RestClient.builder()
				.url( Constants.URLS.LOGIN)
				.loader(activity)
				.params(params)
				.success(new ISuccess() {
					@Override
					public void onSuccess(String response) {

						LogUtil.log("登录成功="+response.toString());
						listener.onSuccess(response.toString());
					}
				})
				.error(new IError() {
					@Override
					public void onError(int code, String msg) {
						listener.onFail(code+" msg="+msg);
					}
				})
				.failure(new IFailure() {
					@Override
					public void onFailure() {

					}
				})
				.build()
				.post();

	}


	public static void applyOrder(Activity activity , KnPayInfo knPayInfo, final BaseListener listener) {
		try {

			User userInfo = Data.getInstance().getUser();
			GameInfo gameInfo = Data.getInstance().getGameInfo();
			GameUser gamuser = Data.getInstance().getGameUser();

			WeakHashMap<String,Object> params = getCommonParamsMap();

			String open_id = userInfo != null ? userInfo.getOpenId():"";

			String uid = gamuser!=null ? gamuser.getUid():"";
			int server_id = gamuser!=null ?  gamuser.getServerId() : 0;

			String game_id = gameInfo != null ? gameInfo.getGameId() : "";
			String platform = gameInfo != null ? gameInfo.getPlatform():"";
			String channel = gameInfo != null ? gameInfo.getChannel():"";
			LogUtil.e("=======uid"+uid+"::::server_id"+server_id+"------gameInfo"+gameInfo);



			if(Util.getGameName(activity).equals("fmsg")){

				params.put("extra_info", knPayInfo.getExtraInfo());

				LogUtil.log("======封魔三国的订单号："+knPayInfo.getExtraInfo());

			}else {


				params.put("extra_info", knPayInfo.getOrderNo());



				if (Util.getChannle(activity).equals("nubia")) {


					params.put("productName", knPayInfo.getProductName());


				}if (Util.getChannle(activity).equals("dalv")) {

					params.put("productName",(knPayInfo.getPrice()/10+"元宝"));
					params.put("amount",String.valueOf((knPayInfo.getPrice()/100)));
					params.put("extend","充值元宝");
					params.put("appid","et51ba58d87527a539");
					params.put("gameArea",Data.getInstance().getGameUser().getServerName());
					params.put("gameAreaId",String.valueOf(Data.getInstance().getGameUser().getServerId()));
					params.put("roleId",Data.getInstance().getGameUser().getUid());
					params.put("userRole",Data.getInstance().getGameUser().getUsername());
					params.put("gameLevel",String.valueOf(Data.getInstance().getGameUser().getUserLevel()));

				}if (Util.getChannle(activity).equals("uc")) {

					double price=knPayInfo.getPrice();
					params.put("amount",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100));
					params.put("accountId",Data.getInstance().getGameUser().getExtraInfo());
					params.put("callbackInfo","自定义信息");

					LogUtil.log("支付请求参数accountId====="+Data.getInstance().getGameUser().getExtraInfo()+"amount="+String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100));

				}

				if (Util.getChannle(activity).equals("mz")) {


					double price=knPayInfo.getPrice();
					params.put("mztotal_price",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100)); //金额总数
					params.put("mzproduct_per_price",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100));//游戏道具单价，默认值：总金额
					params.put("mzapp_id",knPayInfo.getExtraInfo());//appid

					LogUtil.log("appid= "+knPayInfo.getExtraInfo());
					params.put("mzuid",gamuser.getExtraInfo()); //sdk登录成功后的 uid
					params.put("mzproduct_id",knPayInfo.getProductId());//CP 游戏道具 ID,
					params.put("mzproduct_subject",knPayInfo.getProductName());//订单标题,格式为：”购买 N 枚金币”
					params.put("mzproduct_unit","元");//游戏道具的单位，默认值：””
					params.put("mzproduct_body","元宝");//游戏道具说明，默认值：””
					params.put("mzbuy_amount",String.valueOf(1));//道具购买的数量，默认值：”1”
					params.put("mzcreate_time",userInfo.getExtenInfo());//创建时间戳
					params.put("mzpay_type",String.valueOf(0));//支付方式，默认值：”0”（即定额支付）
					params.put("mzuser_info","");//CP 自定义信息，默认值：””
					params.put("mzsign_type","md5");//签名算法，默认值：”md5”(不能为空)


					LogUtil.log("支付请求参数mzuid====="+gamuser.getExtraInfo()+" 创建时间戳="+userInfo.getExtenInfo()+"订单名称="+knPayInfo.getProductName()+" 总额="
							+String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100)+" product_id="+knPayInfo.getProductId()+
							" product_subject="+knPayInfo.getProductName()+" pay_type"+String.valueOf(0));


				}if (Util.getChannle(activity).equals("duoku")) {


					params.put("dkuid",gamuser.getExtraInfo());//支付传递的uid
					params.put("dkextraInfo",userInfo.getExtenInfo());//透传

					LogUtil.log("下单发送游戏服务器dkuid="+gamuser.getExtraInfo()+"透传="+userInfo.getExtenInfo());

				}if (Util.getChannle(activity).equals("sanxing")) { //三星

					double price=knPayInfo.getPrice(); //金额
					params.put("SxPrice", String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100));
					params.put("Sxappuserid",gamuser.getUid()); //用户在商户应用的唯一标识，建议为用户帐号。
					params.put("SxCurrency","RMB"); //货币类型
					params.put("SxWaresid",knPayInfo.getProductId()); //商品编号

				}if(Util.getChannle(activity).equals("jinli")){//金立

					double price=knPayInfo.getPrice();
					params.put("jluid",gamuser.getExtraInfo()); //sdk登录成功返回的user_id。
					params.put("jlsubject",knPayInfo.getProductName()); //商品名称
					params.put("jltotal_fee",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100)); //需支付金额
					params.put("jldeliver_type","1"); //付款方式：1为立即付款，2为货到付款
					params.put("jldeal_price",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100)); //商品总金额
					//params.put("jladChannel","2802003"); //后台兼容新老支付接口判断标识

				}
			}

			params.put("price" , String.valueOf(knPayInfo.getPrice()));
			params.put("extraInfo", knPayInfo.getExtraInfo());


			params.put(
					"sign",
					Md5Util.getMd5(game_id + channel + platform + uid + open_id
							+ server_id + gameInfo.getAppKey()));

			if(!Util.isNetWorkAvailable(activity)){
				LoadingDialog.dismiss();
				Util.ShowTips(activity,"请检查网络是否连接");
				return ;
			}

			//游戏服务器下单
			RestClient.builder()
					.url( Constants.URLS.APPLY_ORDER)
					//.loader(activity)
					.params(params)
					.success(new ISuccess() {
						@Override
						public void onSuccess(String response) {

							LogUtil.log("下单成功="+response.toString());
                           listener.onSuccess(response.toString());
						}
					})
					.error(new IError() {
						@Override
						public void onError(int code, String msg) {
							listener.onFail(code+","+msg);
						}
					})
					.failure(new IFailure() {
						@Override
						public void onFailure() {

						}
					})
					.build()
					.post();


		} catch (Exception e) {
			LoadingDialog.dismiss();
			listener.onFail(new Result(ResultCode.FAIL, "申请订单号失败"));
			e.printStackTrace();
		}
	}




	//上报游戏数据接口(retrofit2.0 传递的参数不能为null,比如：channelVersion= null)
	public static void enterGame(Activity activity,ISuccess iSuccess,IError iError,IFailure iFailure){
		GameUser gameUser = Data.getInstance().getGameUser();
		WeakHashMap<String,Object> params = getCommonParamsMap();
		if(gameUser!=null){
			params.put("lv", String.valueOf(gameUser.getUserLevel()));
			params.put("extraInfo", gameUser.getExtraInfo());

		}
		params.put("getuiClientId", GeTuiPushModule.getInstance().getClientId());
		LogUtil.log("上报游戏数据接口========"+ params.toString());

		//网络请求
		RestClient.builder()
				.url( Constants.URLS.ENTER_GAME)
				.params(params)
				.success(iSuccess)
				.error(iError)
				.failure(iFailure)
				.build()
				.post();


	}












/*
	public static void applyOrder( Activity activity ,KnPayInfo knPayInfo, BaseListener listener) {
		try {

			User userInfo = Data.getInstance().getUser();
			GameInfo gameInfo = Data.getInstance().getGameInfo();
			GameUser gamuser = Data.getInstance().getGameUser();

			HashMap<String,String> params = getCommonParams();

			String open_id = userInfo != null ? userInfo.getOpenId():"";
			
			String uid = gamuser!=null ? gamuser.getUid():"";
			int server_id = gamuser!=null ?  gamuser.getServerId() : 0;
			
			String game_id = gameInfo != null ? gameInfo.getGameId() : "";
			String platform = gameInfo != null ? gameInfo.getPlatform():"";
			String channel = gameInfo != null ? gameInfo.getChannel():"";
            LogUtil.e("=======uid"+uid+"::::server_id"+server_id+"------gameInfo"+gameInfo);

			
			
			if(Util.getGameName(activity).equals("fmsg")){
				
			params.put("extra_info", knPayInfo.getExtraInfo());
			
			LogUtil.log("======封魔三国的订单号："+knPayInfo.getExtraInfo());
				
			}else {
				
				
				params.put("extra_info", knPayInfo.getOrderNo());
				
			
			
			 if (Util.getChannle(activity).equals("nubia")) {
				
				
				params.put("productName", knPayInfo.getProductName());
				
				
			}if (Util.getChannle(activity).equals("dalv")) {
				
				params.put("productName",(knPayInfo.getPrice()/10+"元宝"));
				params.put("amount",String.valueOf((knPayInfo.getPrice()/100)));
				params.put("extend","充值元宝");
				params.put("appid","et51ba58d87527a539");
				params.put("gameArea",Data.getInstance().getGameUser().getServerName());
				params.put("gameAreaId",String.valueOf(Data.getInstance().getGameUser().getServerId()));
				params.put("roleId",Data.getInstance().getGameUser().getUid());
				params.put("userRole",Data.getInstance().getGameUser().getUsername());
				params.put("gameLevel",String.valueOf(Data.getInstance().getGameUser().getUserLevel()));
								
			}if (Util.getChannle(activity).equals("uc")) {
				
				double price=knPayInfo.getPrice();
				params.put("amount",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100));				
				params.put("accountId",Data.getInstance().getGameUser().getExtraInfo());
				params.put("callbackInfo","自定义信息");
				
				LogUtil.log("支付请求参数accountId====="+Data.getInstance().getGameUser().getExtraInfo()+"amount="+String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100));
			
			}
			
			if (Util.getChannle(activity).equals("mz")) {
					
				
				double price=knPayInfo.getPrice();
				params.put("mztotal_price",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100)); //金额总数			
				params.put("mzproduct_per_price",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100));//游戏道具单价，默认值：总金额
				params.put("mzapp_id",knPayInfo.getExtraInfo());//appid
				
				LogUtil.log("appid= "+knPayInfo.getExtraInfo());
				params.put("mzuid",gamuser.getExtraInfo()); //sdk登录成功后的 uid
				params.put("mzproduct_id",knPayInfo.getProductId());//CP 游戏道具 ID,
				params.put("mzproduct_subject",knPayInfo.getProductName());//订单标题,格式为：”购买 N 枚金币”
				params.put("mzproduct_unit","元");//游戏道具的单位，默认值：””
				params.put("mzproduct_body","元宝");//游戏道具说明，默认值：””
				params.put("mzbuy_amount",String.valueOf(1));//道具购买的数量，默认值：”1”
    			params.put("mzcreate_time",userInfo.getExtenInfo());//创建时间戳
				params.put("mzpay_type",String.valueOf(0));//支付方式，默认值：”0”（即定额支付）
				params.put("mzuser_info","");//CP 自定义信息，默认值：””
				params.put("mzsign_type","md5");//签名算法，默认值：”md5”(不能为空)
				
				
				LogUtil.log("支付请求参数mzuid====="+gamuser.getExtraInfo()+" 创建时间戳="+userInfo.getExtenInfo()+"订单名称="+knPayInfo.getProductName()+" 总额="
				+String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100)+" product_id="+knPayInfo.getProductId()+
				" product_subject="+knPayInfo.getProductName()+" pay_type"+String.valueOf(0));
			
			
			}if (Util.getChannle(activity).equals("duoku")) {
				
				
				params.put("dkuid",gamuser.getExtraInfo());//支付传递的uid
				params.put("dkextraInfo",userInfo.getExtenInfo());//透传
				
				LogUtil.log("下单发送游戏服务器dkuid="+gamuser.getExtraInfo()+"透传="+userInfo.getExtenInfo());
				
			}if (Util.getChannle(activity).equals("sanxing")) { //三星
				
				double price=knPayInfo.getPrice(); //金额					
				params.put("SxPrice", String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100));
				params.put("Sxappuserid",gamuser.getUid()); //用户在商户应用的唯一标识，建议为用户帐号。
				params.put("SxCurrency","RMB"); //货币类型
				params.put("SxWaresid",knPayInfo.getProductId()); //商品编号
				
			}if(Util.getChannle(activity).equals("jinli")){//金立
				
				double price=knPayInfo.getPrice();
				params.put("jluid",gamuser.getExtraInfo()); //sdk登录成功返回的user_id。
				params.put("jlsubject",knPayInfo.getProductName()); //商品名称
				params.put("jltotal_fee",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100)); //需支付金额
				params.put("jldeliver_type","1"); //付款方式：1为立即付款，2为货到付款
				params.put("jldeal_price",String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(price))/100)); //商品总金额
				//params.put("jladChannel","2802003"); //后台兼容新老支付接口判断标识
                
			  }
			}
			
			params.put("price" , String.valueOf(knPayInfo.getPrice()));						
			params.put("extraInfo", knPayInfo.getExtraInfo());
			
			
			params.put(
					"sign",
					Md5Util.getMd5(game_id + channel + platform + uid + open_id
							+ server_id + gameInfo.getAppKey()));
			
			if(!Util.isNetWorkAvailable(activity)){
				LoadingDialog.dismiss();
				Util.ShowTips(activity,"请检查网络是否连接");
				return ;
			}
			
			new CommonAsyncTask(activity , Constants.URL.APPLY_ORDER, listener)
					.execute(new Map[] { params, null, null });
		} catch (Exception e) {
			LoadingDialog.dismiss();
			listener.onFail(new Result(ResultCode.FAIL, "申请订单号失败"));
			e.printStackTrace();
		}
	}
	*/


	//发送等级url
	/*public static void enterGame(BaseListener listener) {
		try {

			GameUser gameUser = Data.getInstance().getGameUser();

			HashMap<String,String> params = getCommonParams();
			
			if(gameUser!=null){
				params.put("lv", String.valueOf(gameUser.getUserLevel()));
				params.put("extraInfo", gameUser.getExtraInfo());
			}
			
			params.put("getuiClientId", GeTuiPushModule.getInstance().getClientId());
			
			LogUtil.e("params="+params.toString());
			LogUtil.e("game_id"+Data.getInstance().getGameInfo().getGameId());
			LogUtil.e("enter_url"+Constants.URL.ENTER_GAME);
			new CommonAsyncTask(null , Constants.URL.ENTER_GAME, listener)
					.execute(new Map[] { params, null, null });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/






















	
	public static HashMap<String, String> getCommonParams(){
		HashMap<String, String> params = new HashMap<String, String>();
		
		User userInfo = Data.getInstance().getUser();
		GameInfo gameInfo = Data.getInstance().getGameInfo();
		GameUser gamuser = Data.getInstance().getGameUser();
		
		String open_id="",game_id="",channel="",ad_channel="",platform="",gid="";
		String uid="",server_id="";
		
		String msi = DeviceUtil.getDeviceId();
		
		if(gameInfo!=null){
			game_id = gameInfo.getGameId();
			channel = gameInfo.getChannel();
			LogUtil.e("ad_channel="+ad_channel);
			ad_channel = gameInfo.getAdChannel();
			platform = gameInfo.getPlatform();
		}
		
		if(userInfo!=null){
			open_id = userInfo.getOpenId();
		}
		
		if(gamuser!=null){
			uid = gamuser.getUid();
			server_id = String.valueOf(gamuser.getServerId());
		}
		
		params.put("gid", gameInfo.getGid());
		params.put("game_id", game_id);
		params.put("channel", channel);
		params.put("ad_channel", ad_channel);
		params.put("uid", uid+"");
		params.put("open_id", open_id);
		params.put("server_id", server_id);
		params.put("mac", DeviceUtil.getMacAddress());
		params.put("platform", platform);
		params.put("phoneType", DeviceUtil.getPhoneType());
		params.put("netType", DeviceUtil.getNetWorkType());
		
		params.put("msi", msi );
		
		params.put("channelVersion", OpenSDK.getInstance().getChannelVersion());
		params.put("proxyVersion", OpenSDK.getInstance().getProxyVersion());
		
		String appInfo = Util.getAppInfo( Data.getInstance().getGameActivity() );
		params.put("packageName", Util.getJsonStringByName(appInfo, "packageName") );
		params.put("versionName", Util.getJsonStringByName(appInfo, "versionName") );
		params.put("versionCode", Util.getJsonStringByName(appInfo, "versionCode") );
		
		return params;
		
	}


	public static WeakHashMap<String, Object> getCommonParamsMap(){
		WeakHashMap<String, Object> params = new WeakHashMap<>();

		User userInfo = Data.getInstance().getUser();
		GameInfo gameInfo = Data.getInstance().getGameInfo();
		GameUser gamuser = Data.getInstance().getGameUser();

		String open_id="",game_id="",channel="",ad_channel="",platform="",gid="";
		String uid="",server_id="";

		String msi = DeviceUtil.getDeviceId();

		if(gameInfo!=null){
			game_id = gameInfo.getGameId();
			channel = gameInfo.getChannel();
			LogUtil.e("ad_channel="+ad_channel);
			ad_channel = gameInfo.getAdChannel();
			platform = gameInfo.getPlatform();
		}

		if(userInfo!=null){
			open_id = userInfo.getOpenId();
		}

		if(gamuser!=null){
			uid = gamuser.getUid();
			server_id = String.valueOf(gamuser.getServerId());
		}

		params.put("gid", gameInfo.getGid());
		params.put("game_id", game_id);
		params.put("channel", channel);
		params.put("ad_channel", ad_channel);
		params.put("uid", uid+"");
		params.put("open_id", open_id);
		params.put("server_id", server_id);
		params.put("mac", DeviceUtil.getMacAddress());
		params.put("platform", platform);
		params.put("phoneType", DeviceUtil.getPhoneType());
		params.put("netType", DeviceUtil.getNetWorkType());

		params.put("msi", msi );

		String channelversion = OpenSDK.getInstance().getChannelVersion();

		if( channelversion == null){
			params.put("channelVersion","");
		}else {

			params.put("channelVersion", channelversion);
		}


		params.put("proxyVersion", OpenSDK.getInstance().getProxyVersion());

		String appInfo = Util.getAppInfo( Data.getInstance().getGameActivity() );
		params.put("packageName", Util.getJsonStringByName(appInfo, "packageName") );
		params.put("versionName", Util.getJsonStringByName(appInfo, "versionName") );
		params.put("versionCode", Util.getJsonStringByName(appInfo, "versionCode") );

		return params;

	}





	public static void loginVerfiy(Activity activity , String content , final LoginListener loginListener){
		LoadingDialog.show(activity, "正在登录...", false);
		HttpService.doLogin(activity,content,new BaseListener() {
			@Override
			public void onSuccess(Object result) {
					LoadingDialog.dismiss();
					JSONObject obj = null;
    				User user = null;
    				try {
    					obj = new JSONObject(result.toString());
    					user = new User();
    					user.setOpenId( obj.getString("open_id") );
    					user.setSid( obj.getString("sid") );
    					user.setSign( obj.getString("sign") );
    					user.setIsIncompany( Integer.parseInt(obj.getString("iscompany")) );
    					user.setLogin(true);
    					Data.getInstance().setUser(user);
    				}catch(Exception e){
    					e.printStackTrace();
    				}
    				loginListener.onSuccess(user);
			}
			@Override
			public void onFail(Object result) {
				LoadingDialog.dismiss();
				loginListener.onFail(result.toString());
			}
		});
	}

	
	//游戏开始push数据地址
	public static void doPushData( final  Activity activity, Map<String,Object> data , BaseListener listener){
		
		try{
			
			HashMap<String , String> params = new HashMap<String, String>();
			
			params.put("game_id",(String) data.get("game_id"));
			params.put("app_key",(String) data.get("app_key"));
			params.put("imei",(String) data.get("imei"));
			params.put("platform",(String) data.get("platform"));
			params.put("ad_channel",(String) data.get("ad_channel"));
			params.put("channel",(String) data.get("channel"));
			params.put("ip",(String) data.get("ip"));
			params.put("phone_type",data.get("phone_type").toString());
			
			String game_id = (String) data.get("game_id");
			String appkey = (String) data.get("app_key");
			String imei    = (String) data.get("imei");
	
			
			params.put("sign",Md5Util.getMd5(game_id+appkey+imei));
			
			new CommonAsyncTask(null, Constants.URL.PUSH_DATA, listener).execute(new Map[] { params, null, null });
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	//游戏邀请码数据请求地址
	public static void doPushActivation(final Activity activity , Map<String,Object> data ,  BaseListener listener){
		
		try{
				
//				KnLog.e(data.toString());
		
				HashMap<String , String> params = new HashMap<String, String>();
				
				params.put("m",(String) data.get("m"));
				params.put("a",(String) data.get("a"));
				params.put("uid",(String) data.get("uid"));
				params.put("game",(String) data.get("game"));
				params.put("channel",(String) data.get("channel"));
				params.put("zone",(String)data.get("zone"));
				params.put("server_id",(String)data.get("server_id"));
				params.put("cdkey",(String)data.get("cdkey"));
				params.put("vip",(String)data.get("vip"));
				params.put("level",(String)data.get("level"));
				
				params.put("app_id",(String)data.get("app_id"));
				params.put("open_id",(String)data.get("open_id"));
				params.put("send",(String)data.get("send"));
				params.put("sign",(String)data.get("sign"));
				
				
				LogUtil.e("params:"+params.toString());
				
				new CommonAsyncTask(activity, Constants.URL.ACTIVATION, listener).execute(new Map[] { params, null, null });
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
		}
	
	public static void payData(Activity activity,  
			String content , BaseListener listener) {

		try {
			
			HashMap<String , String> params = new HashMap<String, String>();
			LogUtil.e("params:"+params.toString());
			
			params.put("content", content);
				
			new CommonAsyncTask(activity , Constants.URL.PAYDATAURL, listener).execute(new Map[] { params, null, null });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
