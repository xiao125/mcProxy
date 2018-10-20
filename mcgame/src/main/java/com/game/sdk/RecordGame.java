package com.game.sdk;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.game.sdk.bean.GameUser;
import com.game.sdk.service.HttpService;
import com.game.sdk.util.KnLog;

/**
 * 上报数据（暂时没用，使用中间键的上报接口，都一样）
 */

public class RecordGame {

    private static RecordGame inst=null;

    public static RecordGame getInstance(){
        if (inst==null){
            inst = new RecordGame();
        }
        return inst;
    }

    public void roleInfo(final Activity activity, GameUser gameUser){
        HttpService.enterGame(activity,gameUser,handler);


    }

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){

                case ResultCode.SUCCESS:

                    if (GameSDK.getInstance().getmReportListener() != null) {
                        GameSDK.getInstance().getmReportListener().onSuccess(msg.obj.toString());

                        KnLog.log("上报数据成功"+msg.obj.toString());


                    }


                    break;

                case ResultCode.FAIL:

                    if (GameSDK.getInstance().getmReportListener() != null) {
                        GameSDK.getInstance().getmReportListener().onFail(msg.obj.toString());

                        KnLog.log("上报数据失败"+msg.obj.toString());

                    }


                    break;

                default:
                    break;
            }
        }
    };

}
