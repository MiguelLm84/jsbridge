package com.github.lzyzsd.jsbridge.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class MainActivity extends CitaqActivity implements OnClickListener {

	private static final String TAG = "MainActivity";

	private static final String REDLIGHT = "RED";
	private static final String BLUELIGHT = "BLUE";

	private static final String PRINTSTRFROMWEB = "printStrFromWeb";
	private static final String PRINTCMDFROMWEB = "printCmdFromWeb";
	private static final String LIGHTON = "lightOn";
	private static final String LIGHTOFF = "lightOff";
	private static final String FUNCTIONINJS = "functionInJs";

	Context mContext;

	BridgeWebView webView;

	Button bt_setUrl, bt_callWeb;

	EditText et_url;

	String url = null;

	int RESULT_CODE = 0;

	Type listType;

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
		et_url = (EditText) findViewById(R.id.et_url);

		bt_callWeb = (Button) findViewById(R.id.button);
		bt_callWeb.setOnClickListener(this);

		bt_setUrl = (Button) findViewById(R.id.bt_seturl);
		bt_setUrl.setOnClickListener(this);

		listType=new TypeToken<Map<String,String>>(){}.getType();//TypeToken内的泛型就是Json数据中的类型

		//DefaultHandler接收全部来自web的数据,DefaultHandler有回调
		webView.setDefaultHandler(new DefaultHandler(){
			@Override
			public void handler(String data, CallBackFunction function) {
				if(function != null){
					function.onCallBack("DefaultHandler response" + data);

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

	//	webView.loadUrl("file:///android_asset/demo.html");

//		webView.loadUrl("http://192.168.1.115:8080/test/demo.html");

		//add wuf
		//          实现js回调的handler
		//必须和js同名函数，注册具体执行函数，类似java实现类。
		//第一参数是订阅的java本地函数名字 第二个参数是回调Handler , 参数返回js请求
		webView.registerHandler(PRINTSTRFROMWEB, new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = printStrFromWeb, data from web = " + data);

				String dataOK =getData(data);
				try {
					dataOK =dataOK+"\n";
					mOutputStream.write(dataOK.getBytes("GBK"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					function.onCallBack("printStrFromWeb:Print data form Js failure.（test Chinese：打印失败）");  //指定Handler收到Web发来的数据，回传数据
				}

				function.onCallBack("printStrFromWeb:Print data form Js OK.（test Chinese：打印成功）");  //指定Handler收到Web发来的数据，回传数据
			}

		});

		webView.registerHandler(PRINTCMDFROMWEB, new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = printCmdFromWeb, data from web = " + data);

				try {
					mOutputStream.write(hexStringToByteArray(getData(data)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					function.onCallBack("printCmdFromWeb:Print data form Js failure.（test Chinese：打印失败）");  //指定Handler收到Web发来的数据，回传数据
				}

				function.onCallBack("printCmdFromWeb:Print data form Js OK.（test Chinese：打印成功）");  //指定Handler收到Web发来的数据，回传数据
			}

		});

		webView.registerHandler(LIGHTON, new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = redLightOn, data from web = " + data);


				if(REDLIGHT.equals(getData(data))){
					LEDControl.trunOnRedRight(true);
					function.onCallBack("redLightOn:Print data form Js OK.（light on）");  //指定Handler收到Web发来的数据，回传数据
				}else if(BLUELIGHT.equals(getData(data))){
					LEDControl.trunOnBlueRight(true);
					function.onCallBack("lightOn:Print data form Js OK.（light on）");  //指定Handler收到Web发来的数据，回传数据
				}

			}

		});

		webView.registerHandler(LIGHTOFF, new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = redLightOff, data from web = " + data);

				if(REDLIGHT.equals(getData(data))){
					LEDControl.trunOnRedRight(false);
					function.onCallBack("redLightOn:Print data form Js OK.（light off）");  //指定Handler收到Web发来的数据，回传数据
				}else if(BLUELIGHT.equals(getData(data))){
					LEDControl.trunOnBlueRight(false);
					function.onCallBack("lightOn:Print data form Js OK.（light off）");  //指定Handler收到Web发来的数据，回传数据
				}
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
		if (bt_callWeb.equals(v)) {
            webView.callHandler(FUNCTIONINJS, "data from Java", new CallBackFunction() {

				@Override
				public void onCallBack(String data) {
					// TODO Auto-generated method stub
					Log.i(TAG, "reponse data from js " + data);
				}

			});
		}else if(bt_setUrl.equals(v)){
			String  c_url =et_url.getText().toString().trim();
			if("".equals(c_url)){
				webView.loadUrl("file:///android_asset/demo.html");
			}else{
				url = et_url.getText().toString();
				webView.loadUrl(url);

			}

		}

	}

	public byte[] hexStringToByteArray(String s) {
		s = s.replaceAll(" ", "");
		int len = s.length();
		byte[] b = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			// 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
			b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return b;
	}


	private String getData(String data){
		Map<String,String> map=new Gson().fromJson(data, listType);
		String dataOK = map.get("param");
    	return dataOK;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode==KeyEvent.KEYCODE_BACK && webView.canGoBack()){
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);


	}

}
