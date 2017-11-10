package com.yw.android.aoptest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yw.android.aoptest.aop.CheckLogin;

public class MainActivity extends AppCompatActivity {

    private Button btnAop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAop = (Button) findViewById(R.id.btn_aop);
        btnAop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAop();
            }
        });
    }

    @CheckLogin
    public void onAop(){
        Log.d("tag","执行方法参数");
    }
}
