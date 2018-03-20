package com.github.lzyzsd.jsbridge.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.Toast;

import com.citaq.citaqprinter.CitaqActivity;
import com.citaq.citaqprinter.Command;
import com.citaq.citaqprinter.LEDControl;
import com.citaq.citaqprinter.SerialPortActivity;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.google.gson.Gson;

import java.io.IOException;

public class MainActivity extends CitaqActivity implements OnClickListener {

	private final String TAG = "MainActivity";
	Context mContext;

	BridgeWebView webView;

	Button button;

	int RESULT_CODE = 0;

	ValueCallback<Uri> mUploadMessage;

    static class Location {
        String address;
    }

    static class User {
        String name;
        Location location;
        String testStr;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext =this;

        webView = (BridgeWebView) findViewById(R.id.webView);

		button = (Button) findViewById(R.id.button);

		button.setOnClickListener(this);

		//DefaultHandler接收全部来自web的数据,DefaultHandler有回调
		webView.setDefaultHandler(new DefaultHandler(){
			@Override
			public void handler(String data, CallBackFunction function) {
				if(function != null){
					function.onCallBack("DefaultHandler response" + data);
					Toast.makeText(mContext,data,Toast.LENGTH_LONG).show();
				}
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
				this.openFileChooser(uploadMsg);
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
				this.openFileChooser(uploadMsg);
			}

			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				mUploadMessage = uploadMsg;
				pickFile();
			}
		});

		webView.loadUrl("file:///android_asset/demo.html");

//		webView.loadUrl("http://192.168.1.115:8080/test/demo.html");

		//add wuf
		//          实现js回调的handler
		//必须和js同名函数，注册具体执行函数，类似java实现类。
		//第一参数是订阅的java本地函数名字 第二个参数是回调Handler , 参数返回js请求
		webView.registerHandler("submitFromWeb", new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = submitFromWeb, data from web = " + data);

				try {
					data =data+"\n";
					mOutputStream.write(data.getBytes("GBK"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					function.onCallBack("submitFromWeb:Print data form Js failure.（test Chinese：打印失败）");  //指定Handler收到Web发来的数据，回传数据
				}

				function.onCallBack("submitFromWeb:Print data form Js OK.（test Chinese：打印成功）");  //指定Handler收到Web发来的数据，回传数据
			}

		});

		webView.registerHandler("lightOn", new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = lightOn, data from web = " + data);

				LEDControl.trunOnBlueRight(true);

				function.onCallBack("lightOn:Print data form Js OK.（light on）");  //指定Handler收到Web发来的数据，回传数据
			}

		});

		webView.registerHandler("lightOff", new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = lightOff, data from web = " + data);

				LEDControl.trunOnBlueRight(false);

				function.onCallBack("lightOff:Print data form Js OK.（light off）");  //指定Handler收到Web发来的数据，回传数据
			}

		});

        User user = new User();
        Location location = new Location();
        location.address = "Shantou";
        user.location = location;
        user.name = "Citaq";

        webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {

            }
        });

        webView.send("hello");

	}

	@Override
	protected void onDataReceived(byte[] buffer, int size) {

	}

	public void pickFile() {
		Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
		chooserIntent.setType("image/*");
		startActivityForResult(chooserIntent, RESULT_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == RESULT_CODE) {
			if (null == mUploadMessage){
				return;
			}
			Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		}
	}

	@Override
	public void onClick(View v) {
		if (button.equals(v)) {
            webView.callHandler("functionInJs", "data from Java", new CallBackFunction() {

				@Override
				public void onCallBack(String data) {
					// TODO Auto-generated method stub
					Log.i(TAG, "reponse data from js " + data);
				}

			});
		}

	}

}
