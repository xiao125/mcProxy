package com.game.sdk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.sdk.Constants;
import com.game.sdk.GameSDK;
import com.game.sdk.ResultCode;
import com.game.sdk.service.HttpService;
import com.game.sdk.util.DBHelper;
import com.game.sdk.util.KnLog;
import com.game.sdk.util.LoadingDialog;
import com.game.sdk.util.TodayTimeUtils;
import com.game.sdk.util.Util;
import com.game.sdkproxy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 快速注册
 */
public class FastLoginActivity extends Activity {


    private Activity m_activity = null ;
    private ImageView select_close,m_phone_ks_close;
    private Button user_register,phone_register,phone_ks_code,kn_user_zc;
    private LinearLayout user_layout,phone_layout;
    private TextView masscount;
    private EditText ks_user,kn_password,phone_ks_register,phone_ks_register_code,phone_ks_register_password;
    private boolean isVISIBLE=false;
    private String newSdk="1";
    public  static   String    m_userName ;
    public  static   String    m_passWord ;
    public String m_phone;
    public String m_pw;
    public String randName;
    private  boolean isCountDown=false; //倒计时标识
    public static final String allChar = "0123456789";
    //声明一个SharedPreferences对象和一个Editor对象
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Boolean iscb=false;
    private String lastTime; //退出日期
    private String todayTime;//当前日期
    private String lastName; //最后退出名字
    private  String Spname; //存入sp中的key名


    /**
     * 倒计时秒数
     */
    private int mCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        m_activity = this ;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mc_fast_login);
        initView();
        //进入快速界面，默认用户名注册按钮不可点击
        user_register.setEnabled(false);
        RandName();
        initLinster();
    }

    //获取是否提醒信息
    private void remind(String name){
        Spname = name;
        lastTime =String.valueOf(TodayTimeUtils.LastTime(m_activity));
        lastName = String.valueOf(TodayTimeUtils.LastName(m_activity,name));
        todayTime = TodayTimeUtils.TodayTime();
        KnLog.log("==========lastTime========"+lastName+"  ============lastName="+lastName);
    }

    //退出保存提醒信息
    private void exitsave(String spname,String extname){
        TodayTimeUtils.saveExitTime(m_activity);
        TodayTimeUtils.saveExitName(m_activity,spname,extname);
    }


    private void TomastUser(){
        Util.ShowTips(m_activity,m_userName+",已登录成功");
    }


    private void initLinster() {
        KSUser();
        phone_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_register.setBackgroundColor(getResources().getColor(R.color.mc_kn_text));
                phone_register.setBackgroundColor(getResources().getColor(R.color.mc_Kn_Username));
                user_register.setEnabled(true);
                user_layout.setVisibility(View.INVISIBLE); //隐藏
                phone_layout.setVisibility(View.VISIBLE);//显示
                isVISIBLE=true;
                phone_ks_code.setVisibility(View.VISIBLE); //显示倒计时
                KnLog.log("手机注册。。。。。，isVISIBLE="+isVISIBLE);
            }
        });

        user_register.setOnClickListener(new View.OnClickListener() { //用户名注册
            @Override
            public void onClick(View view) {
                //随机参数一组数字
                user_register.setBackgroundColor(getResources().getColor(R.color.mc_Kn_Username));
                phone_register.setBackgroundColor(getResources().getColor(R.color.mc_kn_text));
                user_layout.setVisibility(View.VISIBLE);//显示
                phone_layout.setVisibility(View.INVISIBLE); //隐藏
                isVISIBLE=false;
                //如果用户注册界面显示时且手机注册界面倒计时正在进行时，隐藏倒计时
                if(isCountDown && (user_layout.getVisibility()==View.VISIBLE)){
                    phone_ks_code.setVisibility(View.INVISIBLE);
                }
                KnLog.log("用户名注册。。。。。，isVISIBLE="+isVISIBLE);
            }
        });

        kn_user_zc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_layout.getVisibility()==View.VISIBLE){
                    KnLog.log("开始用户名注册");
                    //用户名注册
                    UserRegister();
                }else if ( phone_layout.getVisibility()==View.VISIBLE){
                    KnLog.log("开始手机注册");
                    //手机注册
                    MobileRegister();
                }
            }
        });

        //验证码倒计时
        phone_ks_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cell_Num = phone_ks_register.getText().toString().trim();
                if (isPhone(cell_Num)){
                    return;
                }else {
                    countdownTimer();
                    sendcod();
                }
            }
        });

        select_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ks_user.setText("");
            }
        });

        m_phone_ks_close.setOnClickListener(new View.OnClickListener() { //清除验证码
            @Override
            public void onClick(View view) {
                phone_ks_register_code.setText("");
            }
        });

        masscount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO 跳转保存账号界面
               Intent intent = new Intent(FastLoginActivity.this, AutoLoginActivity.class);
                intent.putExtra("selectLogin", "selectLogin");
                startActivity(intent);
                m_activity.finish();
                m_activity = null ;
            }
        });


    }



    private  void RandName(){
        //获取随机有户名
        SimpleDateFormat formatter   =   new   SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate =  new Date(System.currentTimeMillis());
        String   time  =   formatter.format(curDate);
        KnLog.log("获取时间："+time);
        HttpService.RandUserName(m_activity,handler,String.valueOf(time));

    }



    //随机生成一组字符串
    public void  generateMixString(int length)

    {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++)
        {
            sb.append(allChar.charAt(random.nextInt(allChar.length())));
        }
       // String userpasword ="mc"+sb.toString();
        kn_password.setText(sb.toString()); //默认填写密码
    }


    //判断手机号是否正确
    private boolean isPhone(String phone) {
        if(TextUtils.isEmpty(phone)){
            Util.ShowTips(m_activity,getResources().getString(R.string.mc_tips_58));

            return true;
        }
        if(!Util.isMobileNO(phone)){
            Util.ShowTips(m_activity,getResources().getString(R.string.mc_tips_57));
            return true;
        }
        if(!Util.isNetWorkAvailable(getApplicationContext())){
            Util.ShowTips(getApplicationContext(),getResources().getString(R.string.mc_tips_34).toString());

            return true;
        }
        return false;
    }

    //输入用户名与密码监听
    private void KSUser(){

        ks_user.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v ) {
                // TODO Auto-generated method stub
                ks_user.setCursorVisible(true);
            }
        } );

        ks_user.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v , int actionId, KeyEvent event ) {
                // TODO Auto-generated method stub
                if(EditorInfo.IME_ACTION_DONE==actionId){ // 按下完成按钮
                    ks_user .clearFocus(); //清除光标，也就是失去焦点
                    kn_password.requestFocus();
                    ks_user.setCursorVisible(false); //让EditText不出现光标
                }
                return false;
            }
        } );

        kn_password.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                kn_password.setCursorVisible(true);
            }
        } );


        kn_password.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v , int actionId , KeyEvent event ) {
                // TODO Auto-generated method stub
                if(EditorInfo.IME_ACTION_DONE==actionId){
                    kn_password.clearFocus();
                    ks_user.clearFocus();
                    kn_password.requestFocus();
                    kn_password.setCursorVisible(false);
                    Util.hideEditTextWindow(m_activity, kn_password);
                    Util.hideEditTextWindow(m_activity, ks_user);
                    //显示密码
                    kn_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                return false;
            }
        } );


    }




    private void initView() {
        user_register = (Button) findViewById(R.id.kn_selecte_user_register); //用户名注册
        ks_user= (EditText) findViewById(R.id.ks_user); //用户名
        kn_password = (EditText) findViewById(R.id.kn_password); //用户名密码
        select_close = (ImageView) findViewById(R.id.select_close); //清除账号
        phone_register = (Button) findViewById(R.id.kn_selecte_phone_register);//手机注册
        phone_ks_register= (EditText) findViewById(R.id.phone_ks_register); //手机号
        phone_ks_register_code= (EditText) findViewById(R.id.phone_ks_register_code); //获取到的验证码
        phone_ks_register_password= (EditText) findViewById(R.id.phone_ks_register_password);//输入的密码
        phone_ks_code = (Button) findViewById(R.id.phone_ks_code); //验证码
        m_phone_ks_close = (ImageView) findViewById(R.id.phone_ks_close); //清除验证码
        user_layout = (LinearLayout) findViewById(R.id.user_register_layout); //用户名注册view
        phone_layout = (LinearLayout) findViewById(R.id.phone_register_layout); //手机号注册view
        kn_user_zc = (Button) findViewById(R.id.kn_user_zc); //注册按钮
        masscount =(TextView)findViewById(R.id.yy_username); //已有账号
    }

    /**
     * 发送验证码倒计时
     */
    private void countdownTimer(){

        phone_ks_code.setEnabled(false);
        mCount = 60;
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCount--;
                        phone_ks_code.setText(String.valueOf(mCount)+"秒");
                        phone_ks_code.setBackgroundColor(getResources().getColor(R.color.mc_kn_text));

                        isCountDown = true;

                        if (mCount<=0){


                            phone_ks_code.setText("重新发送");
                            phone_ks_code.setEnabled(true);
                            timer.cancel();
                        }
                    }
                });
            }
        };

        timer.schedule(task,1000,1000);
    }


    /**
     * 发送验证码
     */

    private void sendcod(){
        String cell_Num = phone_ks_register.getText().toString().trim();
        if (isPhone(cell_Num)){
            return;
        }else {
            LoadingDialog.show(m_activity, "获取验证码中...", true);
            HttpService.getSecCode(m_activity, handler,cell_Num,newSdk);
        }

    }


    //判断手机号是否正确
    private  void isphone( String cell_Num){

        if(TextUtils.isEmpty(cell_Num)){
            Util.ShowTips(m_activity,getResources().getString(R.string.mc_tips_58));
            return ;
        }
        if(!Util.isMobileNO(cell_Num)){
            Util.ShowTips(m_activity,getResources().getString(R.string.mc_tips_57));
            return ;
        }
        if(!Util.isNetWorkAvailable(getApplicationContext())){
            Util.ShowTips(getApplicationContext(),getResources().getString(R.string.mc_tips_34).toString());
            return ;
        }
    }

    //用户名与密码，注册
    private void UserRegister(){
        Util.hideEditTextWindow(this,kn_password);
        checkRegisterParams(m_activity,ks_user,kn_password);
    }

    //手机号注册
    private void MobileRegister(){
        Util.hideEditTextWindow(this,phone_ks_register_password);
        checkRegisterParams(m_activity,phone_ks_register,phone_ks_register_password,phone_ks_register_code);
    }

    //判断手机号，验证码，密码
    private void checkRegisterParams(Activity context, EditText phone, EditText passWordEt,EditText code) {
        String userphone = phone.getText().toString().trim(); //手机号
        String security_code = code.getText().toString().trim();//验证码
        String password = passWordEt.getText().toString().trim();//密码

        if (!Util.isUserPhone(context,userphone)){
            return;
        }

        if (!Util.isUserCode(context,security_code)){
            return;
        }

        if (!Util.isUserPassword(context,password)){
            return;
        }

       // String pw = Md5Util.getMd5(password);
        m_phone = userphone ;
        m_pw = password ;
        LoadingDialog.show(m_activity, "注册中...",true);
        //手机注册
        HttpService.doMobileRegister(getApplicationContext(), handler, userphone,security_code, password);
    }


    //判断用户名与密码输入格式
    private void checkRegisterParams(Activity context, EditText userNameEt, EditText passWordEt) {

        //注意：判断顺序
        String username = userNameEt.getText().toString();
        if(!Util.isName(context,username)){
            return;
        }
        String password = passWordEt.getText().toString();
        if (!Util.isUserPassword(context,password)){
            return;
        }

     //  String  pw = Md5Util.getMd5(password);
        m_userName = username ;
        m_passWord = password ;
        KnLog.log("用户名注册的密码="+password);
        LoadingDialog.show(m_activity, "注册中...",true);
        HttpService.doRegister(getApplicationContext(), handler, username, password);
    }


    private  Handler loghandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoadingDialog.dismiss();
            String msg_content = msg.obj.toString();
            int resultCode = msg.what;
            Intent intent = null;

            switch (resultCode) {
                case ResultCode.SUCCESS: //登录成功
                    if(msg.obj!=null){
                        if(GameSDK.getInstance().getmLoginListener()!=null){
                            GameSDK.getInstance().getmLoginListener().onSuccess( msg.obj.toString() );
                            //登录成功之后就保存账号密码
                            DBHelper.getInstance().insertOrUpdateUser( m_userName , m_passWord );

                            KnLog.log("==============sdk登录成功了");
                            //查询账号是否绑定手机号
                            HttpService.queryBindAccont(m_activity, handler, m_userName);




                        }
                    }


                    break;


                case ResultCode.FAIL:
                    if(msg.obj!=null)
                    {
                        if(GameSDK.getInstance().getmLoginListener()!=null){
                            GameSDK.getInstance().getmLoginListener().onFail(  msg.obj.toString() );
                            if(null==m_activity){

                            }else{
                                try {
                                    JSONObject jsons = new JSONObject(msg.obj.toString());
                                    String reason= jsons.getString("reason");
                                    Util.ShowTips(m_activity,reason);
                                    m_activity.finish();
                                    m_activity = null ;

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }



                            }
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    };



    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String msg_content = msg.obj.toString();
            int resultCode = msg.what;
            Intent intent = null;
            LoadingDialog.dismiss();
            Log.e("sadaaaawdawf", msg_content+"////"+resultCode);
            switch (resultCode) {
               /* case ResultCode.PASSWORD_NEW_SUCCESS:

                    if(msg.obj!=null) {
                                                   if (GameSDK.getInstance().getmLoginListener() != null) {
                                GameSDK.getInstance().getmLoginListener().onSuccess(msg.obj.toString());


                            KnLog.log("修改密码成功++");
                            try {
                                KnLog.log("msg_content:"+msg_content);
                                JSONObject json = new JSONObject( msg_content );
                                KnLog.log("json:"+json.toString());
                                String reason = json.getString("reason");
                                String user_name = json.getString("user_name");
                                KnLog.log("reason:"+reason);
                                KnLog.log("user_name:"+user_name);
                                Util.ShowTips(m_activity,reason);
                                intent = new Intent(m_activity.getApplicationContext(), AutoLoginActivity.class); //跳转到免输入密码登录界面
                                intent.putExtra("userName", user_name);
                                if(null==intent){

                                }else{
                                    if(null==m_activity){

                                    }else{

                                        m_activity.startActivity(intent);
                                        m_activity.finish();
                                        m_activity = null ;

                                    }
                                }

                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }



                    break;*/
                case ResultCode.PASSWORD_NEW_FAIL:

                    if(msg.obj!=null) {
                        if (GameSDK.getInstance().getmLoginListener() != null) {
                            GameSDK.getInstance().getmLoginListener().onFail(msg.obj.toString());

                            Util.ShowTips(m_activity,msg_content);

                        }
                    }


                    break;
                case ResultCode.SECURITY_SUCCESS: //验证码获取成功

                    break;
                case ResultCode.SECURITY_FAIL:

                    Util.ShowTips(m_activity,msg_content);
                    break;

                case ResultCode.SUCCESS: //注册成功

                    if(msg.obj!=null) {
                        if (GameSDK.getInstance().getmLoginListener() != null) {
                            GameSDK.getInstance().getmLoginListener().onSuccess(msg.obj.toString());
                            LoadingDialog.dismiss();
                            setResult(Activity.RESULT_OK);
                            DBHelper.getInstance().insertOrUpdateUser( m_userName , m_passWord );
                            remind(m_userName);
                            //账号登录
                            HttpService.doLogin(getApplicationContext(), loghandler,m_userName ,m_passWord);
                            //TODO
                            //查询账号是否绑定手机号
                          //  HttpService.queryBindAccont(m_activity, handler, m_userName);
                            //  GameSDK.instance.login(FastLoginActivity.this); //跳转到免密码登录
                            //	执行自动登录
                        }
                    }
                    break;
                case ResultCode.FAIL: ////注册失败

                    if(msg.obj!=null) {
                        if (GameSDK.getInstance().getmLoginListener() != null) {
                            GameSDK.getInstance().getmLoginListener().onFail(msg.obj.toString());

                                Util.ShowTips(FastLoginActivity.this,  Util.getJsonStringByName( msg.obj.toString() , "reason" ) );
                        }
                    }

                    break;

                case ResultCode.MOBILE_REG_SUCCRESS: //手机注册成功
                    if(msg.obj!=null) {
                        if (GameSDK.getInstance().getmLoginListener() != null) {
                            GameSDK.getInstance().getmLoginListener().onSuccess(msg.obj.toString());
                            setResult(Activity.RESULT_OK);
                            KnLog.log("手机注册成功1");
                            //添加手机账号
                            DBHelper.getInstance().insertOrUpdateUser( m_phone ,m_pw );
                            Util.ShowTips(FastLoginActivity.this, getResources().getString(R.string.mc_tips_15) );
                            GameSDK.instance.login(FastLoginActivity.this); //跳转到免密码登录
                        }
                    }

                    break;

                case ResultCode.MOBILE_REG_FAIL: //手机注册失败

                    if(msg.obj!=null) {
                        if (GameSDK.getInstance().getmLoginListener() != null) {
                            GameSDK.getInstance().getmLoginListener().onFail(msg.obj.toString());

                            Util.ShowTips(FastLoginActivity.this,  Util.getJsonStringByName( msg.obj.toString() , "reason" ) );
                        }
                    }

                    break;

                case ResultCode.RANDUSERNAME_SUCCESS: //获取分配用户名成功

                    if(msg.obj!=null) {
                        if (GameSDK.getInstance().getmLoginListener() != null) {
                          //  GameSDK.getInstance().getmLoginListener().onSuccess(msg.obj.toString());
                            KnLog.log("获取分配用户名成功========"+msg.obj.toString());
                            randName =msg.obj.toString();
                            ks_user.setText(randName); //显示用户名
                            generateMixString(6);//密码客户端随机生成
                        }
                    }
                    break;

                case ResultCode.RANDUSERNAME_FAIL: //获取分配用户名失败

                    if(msg.obj!=null) {
                        if (GameSDK.getInstance().getmLoginListener() != null) {
                            GameSDK.getInstance().getmLoginListener().onFail(msg.obj.toString());


                            KnLog.log("服务器生成失败，那就客户端生成");
                           // generateMixString(7);//客户端生成

                        }
                    }



                    break;

                case ResultCode.QUERY_ACCOUNT_BIND_SUCCESS: //绑定了手机号

                    if(msg.obj!=null) {
                        String mobile= msg.obj.toString() ;
                        TomastUser();
                        if(null==m_activity){
                        }else{
                            m_activity.finish();
                            m_activity = null ;
                        }
                    }
                    break;
                case ResultCode.QUERY_ACCOUNT_BIND_FAIL: //没有绑定手机号

                    if(msg.obj!=null) {
                            KnLog.log("没有绑定手机");
                            if (null == m_activity) {

                            } else {

                                if(lastTime.equals(todayTime) && lastName.equals( m_userName )){ //如果两个时间段相等

                                    KnLog.log("今天不提醒,今天日期"+todayTime+" 最后保存日期:"+lastTime+" 现在登录的账号:"+m_userName+" 最后保存的账号:"+lastName);

                                    TomastUser();

                                    GameSDK.getInstance().getmLoginListener().onSuccess(msg.obj.toString());
                                    m_activity.finish();
                                    m_activity = null;


                                }else {


                                    LayoutInflater inflater = LayoutInflater.from(m_activity);
                                    View v = inflater.inflate(R.layout.mc_bind_mobile_dialog_ts, null); //绑定手机
                                    LinearLayout layout = (LinearLayout) v.findViewById(R.id.visit_dialog);
                                    final AlertDialog dia = new AlertDialog.Builder(m_activity).create();
                                    Button bind = (Button) v.findViewById(R.id.visit_bind_account); //下次再说
                                    Button cont = (Button) v.findViewById(R.id.visit_continue);//立刻绑定
                                    TextView ts = (TextView) v.findViewById(R.id.ts);
                                    ImageView close = (ImageView)v.findViewById(R.id.mc_da_lose);//关闭
                                    CheckBox mcheckBox = (CheckBox)v.findViewById(R.id.mc_tx);//选择今日不提醒
                                    dia.show();
                                    dia.setContentView(v);
                                    dia.setCancelable(false);//返回键时 也不消失dismiss
                                    cont.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            // TODO Auto-generated method stub

                                            DBHelper.getInstance().insertOrUpdateUser(m_userName,m_passWord);
                                            Intent intent = new Intent(m_activity, BindCellActivity.class);
                                            intent.putExtra("userName", m_userName);
                                            startActivity(intent);
                                            if (null == m_activity) {

                                            } else {
                                                dia.dismiss();
                                                m_activity.finish();
                                                m_activity=null;

                                            }

                                        }
                                    });

                                    //稍后绑定
                                    bind.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            // TODO Auto-generated method stub

                                            //	DBHelper.getInstance().insertOrUpdateUser( name , m_passwords );
                                            if (null == m_activity) {

                                            } else {

                                                if(iscb){


                                                    //保存勾选后的日期
                                                    exitsave(Spname,m_userName);

                                                    TomastUser();

                                                    dia.dismiss();
                                                    m_activity.finish();
                                                    m_activity = null;

                                                    KnLog.log("勾选了今天不提醒,今天日期"+todayTime+" 最后保存日期:"+lastTime+" 现在登录的账号:"+m_userName+" 最后保存的账号:"+lastName);
                                                }else {

                                                    TomastUser();
                                                    dia.dismiss();

                                                    m_activity.finish();
                                                    m_activity = null;

                                                }





                                            }


                                        }
                                    });


                                    //关闭AlertDialog
                                    close.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            TomastUser();
                                            dia.dismiss();
                                            m_activity.finish();
                                            m_activity = null;
                                        }
                                    });


                                    //选择提醒
                                    mcheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                            if (isChecked){
                                                iscb= true;

                                            }else {

                                                iscb = false;

                                            }
                                        }
                                    });
                                }
                            }
                    }

                    break;

                default:
                    Util.ShowTips(m_activity,msg_content);
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        /*if(GameSDK.getInstance().getGameInfo().getOrientation() == Constants.LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }*/
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
            return true;//不执行父类点击事件
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
}
