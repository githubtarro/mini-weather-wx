package cn.edu.pku.wangxin.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
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

import cn.edu.pku.wangxin.bean.TodayWeather;
import cn.edu.pku.wangxin.util.NetUtil;

/**
 * Created by zhangqixun on 16/7/4.
 */
public class MainActivity extends Activity implements View.OnClickListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);//更新按钮
        mUpdateBtn.setOnClickListener(this);

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
        initView();
    }

    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality
        );
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature
        );
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
    }


    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);
            //startActivity(i);
            startActivityForResult(i,1); //则下面要重写onAcitivityResult方法
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
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
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
                    } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                        eventType = xmlPullParser.next();
                        todayWeather.setFengli(xmlPullParser.getText());
                        fengliCount++;
                    } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                        eventType = xmlPullParser.next();
                        todayWeather.setDate(xmlPullParser.getText());
                        dateCount++;
                    } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                        eventType = xmlPullParser.next();

                        todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                        highCount++;
                    } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                        eventType = xmlPullParser.next();
                        todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                        lowCount++;
                    } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                        eventType = xmlPullParser.next();
                        todayWeather.setType(xmlPullParser.getText());
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
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }


    /**
     *
     * @param cityCode
     */
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
                    con = (HttpURLConnection)url.openConnection(
                    );
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in)); //BufferedReader (Reader  in)创建一个使用默认大小输入缓冲区
                    // 的缓冲字符输入流。  InputStreamReader (InputStream  in)创建一个使用默认字符集的 InputStreamReader。
                    StringBuilder response = new StringBuilder()
                            ;

                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);  //这一行行输出xml文件中的内容与下面一次性输出的效果是一样的
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);  //这是一次性输出xml文件中的内容与上面一行行输出的效果是一样的。

                    todayWeather=parseXML(responseStr);  //parseXML是对xml文件的内容进行pull解析。
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
}




