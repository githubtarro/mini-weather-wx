package cn.edu.pku.wangxin.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator_x.myapplication_real.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.wangxin.bean.TodayWeather;
import cn.edu.pku.wangxin.util.NetUtil;
import cn.edu.pku.wangxin.util.WeatherImage;

/**
 * Created by zhangqixun on 16/7/4.
 */
public class MainActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {
    private static final int UPDATE_TODAY_WEATHER = 1;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    //UI线程处理从子线程传过来的更新UI的任务
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
            //上面都执行完了才是整个界面更新完毕，才执行下面的
             title_update_progress.setVisibility(View.GONE);
            mUpdateBtn.setVisibility(View.VISIBLE);

        }
    };
    private ProgressBar title_update_progress;
    private ImageView mtitle_share;
    private ViewPager vp_6days;
    private List<View> views;
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private ImageView[] dots;
    private int[] ids={R.id.iv1, R.id.iv2};

    //最近6天的天气信息数组
   private TextView[] week_today_arr=new TextView[6];
   private ImageView[] weather_img_ar=new ImageView[6];
   private TextView[] temperature_arr=new TextView[6];
    private TextView[] climate_arr=new TextView[6];
    private TextView[] wind_arr=new TextView[6];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mtitle_share = (ImageView) findViewById(R.id.title_share);
        mUpdateBtn.setOnClickListener(this);

        vp_6days = (ViewPager) findViewById(R.id.vp_6days);

        title_update_progress = (ProgressBar) findViewById(R.id.title_update_progress);
        title_update_progress.setVisibility(View.GONE);//初始化为看不见
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this,"网络OK！", Toast.LENGTH_LONG).show();
        }else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！", Toast.LENGTH_LONG).show();
        }

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        init6day();
        initDots();
        initView();
    }

    //清空界面上各控件的信息
    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        //得到最近6天天气信息控件的实例
        int[] tempArr1={R.id.week_today1,R.id.week_today2,R.id.week_today3};
        for(int i=0;i<3;i++) {
            week_today_arr[i] = (TextView) views.get(0).findViewById(tempArr1[i]);
            week_today_arr[i+3] = (TextView) views.get(1).findViewById(tempArr1[i]);
        }
        int[] tempArr2={R.id.weather_img1,R.id.weather_img2,R.id.weather_img3};
        for(int i=0;i<3;i++) {
            weather_img_ar[i] = (ImageView) views.get(0).findViewById(tempArr2[i]);
            weather_img_ar[i+3] = (ImageView) views.get(1).findViewById(tempArr2[i]);
        }
        int[] tempArr3={R.id.temperature1,R.id.temperature2,R.id.temperature3};
        for(int i=0;i<3;i++) {
            temperature_arr[i] = (TextView) views.get(0).findViewById(tempArr3[i]);
            temperature_arr[i+3] = (TextView) views.get(1).findViewById(tempArr3[i]);
        }
        int[] tempArr4={R.id.climate1,R.id.climate2,R.id.climate3};
        for(int i=0;i<3;i++) {
            climate_arr[i] = (TextView) views.get(0).findViewById(tempArr4[i]);
            climate_arr[i+3] = (TextView) views.get(1).findViewById(tempArr4[i]);
        }
        int[] tempArr5={R.id.wind1,R.id.wind2,R.id.wind3};
        for(int i=0;i<3;i++) {
            wind_arr[i] = (TextView) views.get(0).findViewById(tempArr5[i]);
            wind_arr[i+3] = (TextView) views.get(1).findViewById(tempArr5[i]);
        }

        for(int i=0;i<6;i++) {
            week_today_arr[i].setText("N/A");
            weather_img_ar[i].setImageResource(R.drawable.biz_plugin_weather_qing);
            temperature_arr[i].setText("N/A");
            climate_arr[i].setText("N/A");
            wind_arr[i].setText("N/A");
        }
    }

    //实现最近6天天气信息的界面初始化
    private void init6day() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.weather_6day,null));
        views.add(inflater.inflate(R.layout.weather_6day2,null));
        vpAdapter=new ViewPagerAdapter(views,this);
        vp= (ViewPager) findViewById(R.id.vp_6days);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
    }


    private void initDots(){
        dots=new ImageView[views.size()];
        for(int i=0;i<views.size();i++){
            dots[i]= (ImageView) findViewById(ids[i]);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);
            startActivityForResult(i,1); //要求selectcity.java界面要返回一个城市ID信息。下面要重写onAcitivityResult方法
        }
        if (view.getId() == R.id.title_update_btn){
            view.setVisibility(View.GONE);
            title_update_progress.setVisibility(View.VISIBLE);

            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101040100");
            Log.d("myWeather",cityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            }else
            {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }

    //从select_city.java返回的信息，决定选择显示哪个城市
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            SharedPreferences.Editor sp = getSharedPreferences("config", MODE_PRIVATE).edit();
            sp.putString("main_city_code",newCityCode);//将最近一次选择城市编码暂存到sharedpreference中。
            sp.commit();

            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    //用pull进行xml文件的解析，返回一个存储有选定城市天气信息的对象
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =1;
        int dateCount=1;
        int highCount =1;
        int lowCount=1;
        int typeCount =1;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
// 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
// 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp"
                        )){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                    eventType = xmlPullParser.next();
                            todayWeather.setCity(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("updatetime")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setUpdatetime(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("shidu")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setShidu(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("wendu")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setWendu(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("pm25")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setPm25(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("quality")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setQuality(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                        eventType = xmlPullParser.next();
                        todayWeather.setFengxiang(xmlPullParser.getText());
                        fengxiangCount++;
                    } else if (xmlPullParser.getName().equals("fl_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(0,xmlPullParser.getText());
                      } else if (xmlPullParser.getName().equals("fengli")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setFengli(fengliCount/2,xmlPullParser.getText());
                        fengliCount++;
                    }else if (xmlPullParser.getName().equals("date_1") ) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(0,xmlPullParser.getText());
                   } else if (xmlPullParser.getName().equals("date") ) {
                        eventType = xmlPullParser.next();
                        todayWeather.setDate(dateCount,xmlPullParser.getText());
                        dateCount++;
                    }else if (xmlPullParser.getName().equals("high_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(0,xmlPullParser.getText().substring(2).trim());
                    } else if (xmlPullParser.getName().equals("high")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setHigh(highCount,xmlPullParser.getText().substring(2).trim());
                        highCount++;
                    } else if (xmlPullParser.getName().equals("low_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(0,xmlPullParser.getText().substring(2).trim());
                     } else if (xmlPullParser.getName().equals("low")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setLow(lowCount,xmlPullParser.getText().substring(2).trim());
                        lowCount++;
                    } else if (xmlPullParser.getName().equals("type_1") ) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(0,xmlPullParser.getText());

                   } else if (xmlPullParser.getName().equals("type") ) {
                        eventType = xmlPullParser.next();
                        todayWeather.setType((typeCount+1)/2,xmlPullParser.getText());
                        typeCount++;
                    }
                }
                break;
// 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                    break;
            }
// 进入下一个元素并触发相应事件
            eventType = xmlPullParser.next();
        }
    } catch (XmlPullParserException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    return todayWeather;
}
    //更新主界面选择的城市的天气信息
    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate(1));  //下标为1代表今天，下标为0代表昨天
        temperatureTv.setText(todayWeather.getHigh(1)+"~"+todayWeather.getLow(1));
        climateTv.setText(todayWeather.getType(1));  //因为yesterday中有两个
        windTv.setText("风力:"+todayWeather.getFengli(1));

        weatherImg.setImageResource(WeatherImage.transToImage(todayWeather.getType(1)));
        pmImg.setImageResource(WeatherImage.transToImage_PM25(todayWeather.getPm25()));

        //显示近6天天气信息
        for(int i=0;i<6;i++) {
            week_today_arr[i].setText(todayWeather.getDate(i));
            weather_img_ar[i].setImageResource(WeatherImage.transToImage(todayWeather.getType(i)));
            temperature_arr[i].setText(todayWeather.getHigh(i)+"~"+todayWeather.getLow(i));
            climate_arr[i].setText(todayWeather.getType(i));
            wind_arr[i].setText(todayWeather.getFengli(i));
        }
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }

    //这个函数的作用是从网络中把xml数据拿下来
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection con=null;
                TodayWeather todayWeather=null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);

                    todayWeather=parseXML(responseStr);  //经过parseXML解析后返回一个拥有特定城市天气信息的对象
                    if(todayWeather!=null) {
                        Log.d("myWeather", todayWeather.toString());

                        //子线程向UI线程传递消息
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
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




