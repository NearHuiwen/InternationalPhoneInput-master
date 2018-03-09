package com.yiming.android.internationalphoneinput;

import android.app.Activity;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yimingyu.android.ui.phoneinput.IntlPhoneInput;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,IntlPhoneInput.IntlPhoneInputListener{
    private static final String TAG ="手机号输入控件" ;
    IntlPhoneInput phoneInputView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("测试");
        phoneInputView = (IntlPhoneInput) findViewById(R.id.input_phone);
        phoneInputView.setOnValidityChange(this);
        findViewById(R.id.btn_test).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_test:
                Log.e(TAG,phoneInputView.getText()+" "+phoneInputView.getUstr());
                break;
        }
    }

    @Override
    public void onValidityChange(boolean isValid) {
        if(!isValid){
            Toast.makeText(MainActivity.this,"手机号格式不正确",Toast.LENGTH_LONG).show();
        }
    }
}
