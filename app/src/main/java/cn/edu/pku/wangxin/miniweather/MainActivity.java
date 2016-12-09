package cn.edu.pku.wangxin.miniweather;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
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

import cn.edu.pku.wangxin.app.MyApplication;
import cn.edu.pku.wangxin.bean.City;
import cn.edu.pku.wangxin.bean.TodayWeather;


import cn.edu.pku.wangxin.service.AutoUpdateService;
import cn.edu.pku.wangxin.util.NetUtil;
import cn.edu.pku.wangxin.util.Trans2PinYin;
import cn.edu.pku.wangxin.util.WeatherImage;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
import com.baidu.location.Poi;
/**
 * Created by zhangqixun on 16/7/4.
 */
public class MainActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {
    private  MyApplication app;
    private List<City> mCityList;

    private static final int UPDATE_TODAY_WEATHER = 1;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;
    private String weatherAdvice;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    //UI线程处理从子线程传过来的更新UI的任务
   public  Handler mHandler = new Handler() {
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
    private TextView mTv_advice;
    private ImageView title_location;
    private TextView tv_testGPS;
    private TextView temperature_current;
    private AutoUpdateReceiver autoUpdateReceiver;

    class MyLocationListener implements BDLocationListener {
        @Override

        public void onReceiveLocation(BDLocation location) {

            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                String baiduAddr=location.getAddrStr();
                sb.append(baiduAddr);

                app = (MyApplication) getApplicationContext();
                mCityList = app.getCityList();

                    for (City city : mCityList) {
                        if(baiduAddr.indexOf(city.getCity())!= baiduAddr.indexOf(city.getProvince()) &&  baiduAddr.indexOf(city.getCity())!=-1&& baiduAddr.indexOf(city.getProvince())!=-1 ) {//省名与城市名相同的时候则这个逻辑会一直是北京-北京，而不会是北京-大兴

                            SharedPreferences.Editor sp = getSharedPreferences("config", MODE_PRIVATE).edit();
                            sp.putString("main_city_code", city.getNumber());//将最近一次选择城市编码暂存到sharedpreference中。
                            sp.commit();

                            Log.d("myWeather", "选择的城市代码为" + city.getNumber());
                            if (NetUtil.getNetworkState(MainActivity.this) != NetUtil.NETWORN_NONE) {
                                Log.d("myWeather", "网络OK");
                                queryWeatherCode(city.getNumber());
                                Toast.makeText(MainActivity.this, "你目前位于："+city.getProvince()+"-"+city.getCity(), Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("myWeather", "网络挂了");
                                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
                            }
                            break;
                        }

                    }

                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
            tv_testGPS.setText(""); //在界面上展示百度定位API的信息，测试时用，实际则清空
            mLocationClient.stop();   //否则会不断执行这个百度定位API监听器，就会不断执行updateweather中的Toast
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation(); //勿忘

        //更新“按钮”
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        //定位“按钮”
        title_location = (ImageView) findViewById(R.id.title_location);
        title_location.setOnClickListener(this);
        tv_testGPS = (TextView) findViewById(R.id.tv_testGPS);

        //分享“按钮”
        mtitle_share = (ImageView) findViewById(R.id.title_share);
        mtitle_share.setOnClickListener(this);

        mTv_advice = (TextView) findViewById(R.id.tv_advice);//温馨提示
        mTv_advice.setOnClickListener(this);
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

        //动态注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Weather_CHANGED_ACTION");
        autoUpdateReceiver = new AutoUpdateReceiver();
        registerReceiver(autoUpdateReceiver,intentFilter);

        Intent intent=new Intent("Weather_CHANGED_ACTION");
        sendBroadcast(intent);

        //startService(new Intent(this,AutoUpdateService.class));
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,AutoUpdateService.class));
        unregisterReceiver(autoUpdateReceiver);
        super.onDestroy();
    }

    //清空界面上各控件的信息
    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        temperature_current = (TextView) findViewById(R.id.temperature_current);
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
        temperature_current.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        mTv_advice.setText("");

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
        if(view.getId()==R.id.title_city_manager){        //单击选择城市“按钮”
            Intent i=new Intent(this,SelectCity.class);
            startActivityForResult(i,1); //要求selectcity.java界面要返回一个城市ID信息。下面要重写onAcitivityResult方法
        }
        if (view.getId() == R.id.title_update_btn){  //单击更新“按钮”
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
        if(view.getId()==R.id.tv_advice){          //单击温馨提示“按钮”
            Toast.makeText(MainActivity.this,weatherAdvice,Toast.LENGTH_SHORT).show();
        }
        if(view.getId()==R.id.title_location){     //单击定位“按钮”
            mLocationClient.start();
        }
        if(view.getId()==R.id.title_share){
            Intent intent=new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
            intent.putExtra(Intent.EXTRA_TEXT, "I have successfully share my message through my app");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, getTitle()));
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
                    }else if (xmlPullParser.getName().equals("name") ) {
                                eventType = xmlPullParser.next();
                                if(xmlPullParser.getText().equals("运动指数")) {
                                    eventType = xmlPullParser.next();
                                    eventType = xmlPullParser.next();
                                    eventType = xmlPullParser.next();
                                    eventType = xmlPullParser.next();
                                    eventType = xmlPullParser.next();
                                    eventType = xmlPullParser.next();
                                    todayWeather.setGuide(xmlPullParser.getText());
                                }
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
        temperature_current.setText("温度："+todayWeather.getWendu()+"℃");
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate(1));  //下标为1代表今天，下标为0代表昨天
        temperatureTv.setText(todayWeather.getHigh(1)+"~"+todayWeather.getLow(1));
        climateTv.setText(todayWeather.getType(1));  //因为yesterday中有两个
        windTv.setText("风力:"+todayWeather.getFengli(1));

        mTv_advice.setText("温馨提示(点击我)");
        weatherAdvice=todayWeather.getGuide();
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
    public void queryWeatherCode(String cityCode) {
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

    //百度GPS
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public class AutoUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(MainActivity.this, "后台天气自动更新中……", Toast.LENGTH_SHORT).show();
            Log.d("Receiver","haha");
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101040100");
            Log.d("myWeather",cityCode);
            if (NetUtil.getNetworkState(MainActivity.this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            }else
            {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
            Intent i=new Intent(context, AutoUpdateService.class);
            context.startService(i); //会调用service的onStartCommand方法，再设置定时器。
        }
    }
}




