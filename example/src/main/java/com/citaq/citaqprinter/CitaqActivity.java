package com.citaq.citaqprinter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.github.lzyzsd.jsbridge.example.R;

import java.io.IOException;
import java.security.InvalidParameterException;

public class CitaqActivity extends SerialPortActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (Application) getApplication();
        try {
            mSerialPort = mApplication.getPrintSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }

    }


    @Override
    protected void onDataReceived(byte[] buffer, int size) {

    }

}
