package com.proxy.net.callback;

import android.os.Handler;

import com.proxy.Loader.LatteLoader;
import com.proxy.Loader.LoaderStyle;
import com.proxy.configurator.ConfigKeys;
import com.proxy.configurator.Proxys;
import com.proxy.net.RestCreator;
import com.proxy.util.LogUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created
 */

public final class RequestCallbacks implements Callback<String> {

    private final IRequest REQUEST;
    private final ISuccess SUCCESS;
    private final IFailure FAILURE;
    private final IError ERROR;
    private final LoaderStyle LOADER_STYLE;
    private static final Handler HANDLER = Proxys.getHandler(); //获取hander

    public RequestCallbacks(IRequest request, ISuccess success, IFailure failure, IError error, LoaderStyle style ) {
        this.REQUEST = request;
        this.SUCCESS = success;
        this.FAILURE = failure;
        this.ERROR = error;
        this.LOADER_STYLE = style;

    }

    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        LogUtil.log("==============jjj"+response.body());
        if (response.isSuccessful()) {
            if (call.isExecuted()) {
                if (SUCCESS != null) {

                    SUCCESS.onSuccess(response.body());
                }
            }
        } else {
            if (ERROR != null) {
                ERROR.onError(response.code(), response.message());
            }
        }

        onRequestFinish();
    }

    @Override
    public void onFailure(Call<String> call, Throwable t) {

        LogUtil.log("==============call======="+call.toString()+"   t="+t.toString());
        if (FAILURE != null) {
            FAILURE.onFailure();
        }
        if (REQUEST != null) {
            REQUEST.onRequestEnd();
        }

        onRequestFinish();
    }

    private void onRequestFinish() {
        final long delayed = Proxys.getConfiguration(ConfigKeys.LOADER_DELAYED); //获取网络延迟时间，默认0
        if (LOADER_STYLE != null) {
            HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    RestCreator.getParams().clear();
                    LatteLoader.stopLoading();
                }
            }, delayed);
        }
    }
}
