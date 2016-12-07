package cn.edu.pku.wangxin.miniweather;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.example.administrator_x.myapplication_real.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator_x on 2016/11/29.
 */
public class Guide extends Activity implements ViewPager.OnPageChangeListener {

    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;
    private Button btn;

    private ImageView[] dots;
    private int[] ids={R.id.iv1, R.id.iv2,R.id.iv3};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        initViews();
        initDots();
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String isFirst = sharedPreferences.getString("isFirst","yes");
        if(isFirst.equals("no")){
            Log.d("Guide","nonono");
            startActivity(new Intent(Guide.this,MainActivity.class));
            finish();
        }else{
            Log.d("Guide","First");
            SharedPreferences.Editor sp = getSharedPreferences("config", MODE_PRIVATE).edit();
            sp.putString("isFirst","no");
            sp.commit();
        }
        btn= (Button)views.get(2).findViewById(R.id.btn); //要加views.get(2)前缀是因为这个控件不在与当前活动绑定的布局文件中，否则虽然编译通过，但是运行起来要崩溃。可能因为这不是与activity通过setcontentview绑定的布局文件。
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Guide.this,MainActivity.class));
                finish();
            }
        });
    }

    private void initDots(){
        dots=new ImageView[views.size()];
        for(int i=0;i<views.size();i++){
            dots[i]= (ImageView) findViewById(ids[i]);
        }
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.page1,null));//inflate()方法一般接收两个参数，第一个参数就是要加载的布局文件的id，
        views.add(inflater.inflate(R.layout.page2,null));
        views.add(inflater.inflate(R.layout.page3,null));
        vpAdapter=new ViewPagerAdapter(views,this);
        vp= (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        for(int i=0;i<ids.length;i++){
            if(i==position)
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            else
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
