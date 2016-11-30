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

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);  //函数作用是设置各个控件的text属性。使界面显示内容。
                    break;
                default:
                    break;
            }
            //上面都执行完了才是整个界面更新完毕，才执行下面的
             title_update_progress.setVisibility(View.GONE);
            mUpdateBtn.setVisibility(View.VISIBLE);  //

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
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);//更新按钮
        mtitle_share = (ImageView) findViewById(R.id.title_share);  //得到更新按钮左边的按钮，因为他toleft更新按钮。
        mUpdateBtn.setOnClickListener(this);

        vp_6days = (ViewPager) findViewById(R.id.vp_6days);

        title_update_progress = (ProgressBar) findViewById(R.id.title_update_progress); //更新按钮旋转控件 progressbar
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

    //实现最近6天信息的pagerview界面初始化
    private void init6day() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.weather_6day,null));//inflate()方法一般接收两个参数，第一个参数就是要加载的布局id，
        views.add(inflater.inflate(R.layout.weather_6day2,null));
        vpAdapter=new ViewPagerAdapter(views,this);
        vp= (ViewPager) findViewById(R.id.vp_6days);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this); //关键代码。设置监听器，就是传入一个实现那个监听功能的对象。一般都是拿activity对象。
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
            startActivityForResult(i,1); //要求selectcity.java界面要返回一个城市ID信息。         则下面要重写onAcitivityResult方法
        }
        if (view.getId() == R.id.title_update_btn){
            view.setVisibility(View.GONE);  //一点击更新按钮(实则是imageview的onclick事件)，就使这个背景为静态图片的Imageview控件隐藏。
            title_update_progress.setVisibility(View.VISIBLE);

            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101040100"); //一般不会显示第二个参数代表城市天气，因为那是缺省值。
            Log.d("myWeather",cityCode);  //虽然上面那个config文件不存在，但是这里仍然能显示,因为cityCode是用的是上面getString的第二个参数作为缺省值。
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);  //这条语句中会调用子线程执行，子线程执行需要时间，但是这个函数在主线程中执行几乎不需要时间，所以马上就能看到下面Log输出我醒了
                  //  Thread.sleep(8000); //这里不行是因为上面的控件setvisibility的生效相对于这条语句要慢。而这条语句一旦执行，整个界面要保持静止状态。
            }else
            {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
            Log.d("哈哈","我醒了"); //这条语句表明子线程sleep，但是这里没有sleep，即主线程没有sleep
            //上面执行完毕不代表界面都把更新完的数据显示了，因为数据是从子线程传到主线程的handlmessage中的那个updateTodayweather方法
           // title_update_progress.setVisibility(View.GONE);
            //view.setVisibility(View.VISIBLE);  //不能放在上面if后面，否则else情况的话，即网络断的时候，就会一直转，而且网突然有了之后也不会回来
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //从select_city.java返回的信息，决定选择显示哪个城市
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            SharedPreferences.Editor sp = getSharedPreferences("config", MODE_PRIVATE).edit();  //将最近一次选择城市的天气信息暂存到sharedpreference中。
            //注意上面的文件名不要写后缀，因为默认就是xml格式的。
            sp.putString("main_city_code",newCityCode);
            sp.commit();

            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode); //这个函数的作用是从网络中把xml数据拿下来，再经过pull解析时打印log信息
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =1;  //每天的fengli有两个，白天，和夜晚
        int dateCount=1;
        int highCount =1;
        int lowCount=1;
        int typeCount =1;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata)); //xmldata就是responseStr,即xml文件中的内容
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
                        todayWeather.setFengli(fengliCount/2,xmlPullParser.getText()); //注意有11个fengli，不是10个，因为那个一开始有个总fengli,幸好用查找统计了一下
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

        weatherImg.setImageResource(WeatherImage.transToImage(todayWeather.getType(1))); //定义一个WeatherImage类的静态方法transToIamge
        pmImg.setImageResource(WeatherImage.transToImage_PM25(todayWeather.getPm25()));


        for(int i=0;i<6;i++) {  //不要用.length方法，因为这是数组，不是容器，数组的大小不是里面有效元素的大小。
            week_today_arr[i].setText(todayWeather.getDate(i));
            weather_img_ar[i].setImageResource(WeatherImage.transToImage(todayWeather.getType(i)));
            temperature_arr[i].setText(todayWeather.getHigh(i)+"~"+todayWeather.getLow(i));
            climate_arr[i].setText(todayWeather.getType(i));
            wind_arr[i].setText(todayWeather.getFengli(i));
        }
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }

    private void queryWeatherCode(String cityCode) {

        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);  //输出上面的网址+城市编号
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
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in)); //BufferedReader (Reader  in)创建一个使用默认大小输入缓冲区
                    // 的缓冲字符输入流。  InputStreamReader (InputStream  in)创建一个使用默认字符集的 InputStreamReader。
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);  //这一行行输出xml文件中的内容与下面一次性输出的效果是一样的
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);  //这是一次性输出xml文件中的内容与上面一行行输出的效果是一样的。

                    todayWeather=parseXML(responseStr);  //parseXML是对xml文件的内容进行pull解析。  todayWeather是个对象，里面已经写好toString方法。
                    if(todayWeather!=null) {
                        Log.d("myWeather", todayWeather.toString());  //将todayWeather对象的各个成员值输出来。

                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;  //.what属性是一个数字，数字用宏定义来直观表明这个Message是携带什么东西
                        msg.obj=todayWeather;  //obj的值才是真正要传递的信息
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




