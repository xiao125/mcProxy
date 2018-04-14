package com.mc.game;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;

import com.proxy.Constants;
import com.proxy.OpenSDK;
import com.proxy.bean.GameInfo;
import com.proxy.bean.KnPayInfo;
import com.proxy.bean.User;
import com.proxy.listener.ExitListener;
import com.proxy.listener.InitListener;
import com.proxy.listener.LoginListener;
import com.proxy.listener.LogoutListener;
import com.proxy.listener.PayListener;
import com.proxy.listener.WeixinListener;
import com.proxy.net.RestClient;
import com.proxy.net.callback.ISuccess;
import com.proxy.service.HttpService;
import com.proxy.util.LogUtil;
import com.proxy.util.Md5Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/10 0010.
 */

public class TestActivity extends Activity {

    public static int       WIDTH = 0 ;
    public static int       HEIGHT = 0 ;




    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if(m_proxy.onKeyUp(keyCode, event)){
            LogUtil.e("Web可以响应了++");
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                LogUtil.e("Web可以响应了但是不退出");
                return false ;
            }
        }else if(m_proxy.hasThirdPartyExit()){
            LogUtil.e("SDK退出");
            m_proxy.onThirdPartyExit();

        }else{
            LogUtil.e("游戏自己退出");
            // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

                // 确认对话框
                final AlertDialog isExit = new AlertDialog.Builder(this).create();
                // 对话框标题
                isExit.setTitle("提示");
                // 对话框消息
                isExit.setMessage("是否要退出游戏？");
                // 实例化对话框上的按钮点击事件监听
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case AlertDialog.BUTTON1:// "确认"按钮退出程序
                                System.exit(0);
                                finish();
                                //  玩家登出
                                m_proxy.logOut();
                                break;
                            case AlertDialog.BUTTON2:// "取消"第二个按钮取消对话框
                                isExit.cancel();
                                break;
                            default:
                                break;
                        }
                    }
                };
                // 注册监听
               /* isExit.setButton("确定", listener);
                isExit.setButton2("取消", listener);*/
                // 显示对话框
                isExit.show();
                return false;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        m_proxy.onDestroy();
    }

    OpenSDK m_proxy = OpenSDK.getInstance();
    private String m_appKey = "tkvXAqJlLSewyd2h7WgjRZibaMFHIKBp";
    private String m_gameId = "fmsg";
    private String m_gameName = "fmsg";
    private int m_screenOrientation = 0;
    private static Activity m_activity  = null ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);

        m_activity = this ;

        //		酷牛SDK初始化游戏信息
        m_proxy.isSupportNew(true);
        m_proxy.doDebug(true);

        LogUtil.e("md5值："+ Md5Util.getMd5("tlwz_android"+"#"+"e7a4ea49b6c61f80c844bf2fbda4d8d3"));



        this.runOnUiThread(new Runnable() {


            public void run() {
                GameInfo m_gameInfo = new GameInfo(m_gameName, m_appKey, m_gameId,m_screenOrientation);

                // TODO Auto-generated method stub
                m_proxy.init(m_activity, m_gameInfo , new InitListener() {


                    public void onSuccess(Object arg0) {
                        // TODO Auto-generated method stub
                        LogUtil.log("游戏初始化成功");
                    }

                    public void onFail(Object arg0) {
                        // TODO Auto-generated method stub
                        LogUtil.log("游戏初始化失败");
                    }
                });
            }
        });


        //  设置SDK登出监听
        m_proxy.setLogoutListener(new LogoutListener() {

            public void onSuccess(Object result) {


                if(result.equals(1)){
                    LogUtil.log("悬浮窗注销成功");
                    //游戏账号注销，返回到登录界面
                }else if(result.equals(2)) {
                    LogUtil.log("游戏内切换账号登出成功");
                    //游戏账号注销，返回到登录界面

                }


            }

            public void onFail(Object result) {

            }
        });

        //	设置SDK退出监听
        m_proxy.setExitListener(new ExitListener() {

            public void onConfirm() {

            }

            public void onCancel() {

            }
        });

        //	设置SDK登录监听
        m_proxy.setLogoinListener( new LoginListener() {


            public void onSuccess(User user) {
                // TODO Auto-generated method stub
                String open_id = user.getOpenId();
                String sId	   = user.getSid();
                LogUtil.log("登录成功");
//					KnUtil.ShowTips(m_activity, "登录成功");
            }


            public void onFail(String result) {
                // TODO Auto-generated method stub
                LogUtil.log("登录失败+result:"+result);

            }
        } );

        //	设置SDK支付监听
        m_proxy.setPayListener(new PayListener() {


            public void onSuccess(Object result) {
                LogUtil.log("setPayListener 支付回调成功"+result.toString());
            }


            public void onFail(Object result) {
            }
        });

        m_proxy.setWeixinListener(new WeixinListener() {


            public void onSuccess(Object result) {
                // TODO Auto-generated method stub

            }


            public void onFail(Object result) {
                // TODO Auto-generated method stub

            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        WIDTH = dm.widthPixels;
        HEIGHT = dm.heightPixels;

    }

    public void onLogin( View vt ){
        m_proxy.login(m_activity);
    }


    public void onEnterGame( View vt ){
        String  userId="1001";         	   				//游戏玩家ID
        String  serverId = "10021";      				//游戏玩家所在服ID
        String  userLv = "100";          				//游戏玩家等级Lv
        String  extraInfo = "expendinfo";      			//玩家信息拓展字段
        String  serverName = "神龙在手"; 					//玩家所在服区名称
        String roleName = "玩家角色名称";					//玩家角色名称
        String vipLevel ="10";          				//玩家VIP等级
        String factionName="虎头帮";     					//用户所在帮派名称
        int senceType = 1 ;           					//场景ID;//(值为1则是进入游戏场景，值为2则是创建角色场景，值为4则是提升等级场景)
        String role_id = "1001" ;       				//角色ID
        String  diamondLeft = "100";        			//玩家货币余额
        String  roleCTime = String.valueOf(System.currentTimeMillis()); //角色创建时间（需要从游戏服务端获取）

        Map<String, Object> data = new HashMap<String, Object>();
        data.put(Constants.USER_ID, userId);			//游戏玩家ID
        data.put(Constants.SERVER_ID, serverId);		//游戏玩家所在的服务器ID
        data.put(Constants.USER_LEVEL, userLv);		//游戏玩家等级
        data.put(Constants.EXPEND_INFO, extraInfo);	//扩展字段
        data.put(Constants.SERVER_NAME, serverName);	//所在服务器名称data.put(Constants.ROLE_NAME,roleName);//角色名称
        data.put(Constants.VIP_LEVEL, vipLevel);		//VIP等级
        data.put(Constants.FACTION_NAME, factionName);//帮派名称
        data.put(Constants.SCENE_ID, senceType);		//场景ID
        data.put(Constants.ROLE_ID, userId);			//角色ID
        data.put(Constants.ROLE_CREATE_TIME,roleCTime);//角色创建时间
        data.put(Constants.BALANCE,diamondLeft);		//剩余货币data.put(Constants.IS_NEW_ROLE,senceType==2?true:false);	//是否是新角色
        data.put(Constants.USER_ACCOUT_TYPE,"1");		//玩家账号类型账号类型，0:未知用于来源1:游戏自身注册用户2:新浪微博用户3:QQ用户4:腾讯微博用户5:91用户(String)
        data.put(Constants.USER_SEX,"0");				//玩家性别，0:未知性别1:男性2:女性；(String)
        data.put(Constants.USER_AGE,"25");			//玩家年龄；(String)
        m_proxy.onEnterGame(data);
    }

    public void onPay( View vt ){
        final KnPayInfo payInfo = new KnPayInfo();
        payInfo.setProductName("传送阵");					//商品名字
        payInfo.setCoinName("黄金");						//货币名称	如:元宝
        payInfo.setCoinRate(10);						//游戏货币的比率	如:1元=10元宝 就传10
        payInfo.setPrice(600);							//商品价格   			分
        payInfo.setProductId("10001");					//商品Id,没有可以填null
        payInfo.setOrderNo("10003");					//支付接口//很特殊的接口就不做统一了
        payInfo.setExtraInfo("ExtraInfo++");
        m_activity.runOnUiThread(new Runnable() {


            public void run() {
                // TODO Auto-generated method stub
                m_proxy.pay(m_activity, payInfo);
            }
        });

    }



    public void onLogout( View vt ){
        m_proxy.logOut();
        finish();
        System.exit(0);
    }



    //游戏内切换账号接口
    public void onSwitch(View vt){
       m_proxy.switchAccount();




    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        m_proxy.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        m_proxy.onResume();
    }



    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        m_proxy.onRestart();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        m_proxy.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        m_proxy.onStop();
    }



    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        m_proxy.onBackPressed();
    }


}
