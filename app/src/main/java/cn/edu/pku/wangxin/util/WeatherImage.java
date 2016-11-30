package cn.edu.pku.wangxin.util;

import com.example.administrator_x.myapplication_real.R;

/**
 * Created by Administrator_x on 2016/11/29.
 */
public class WeatherImage {
    public static int transToImage(String type){
        switch (type) {
            case "小雨":
                return R.drawable.biz_plugin_weather_xiaoyu;
            case "阴":
                return R.drawable.biz_plugin_weather_yin;
            case "多云":
                return R.drawable.biz_plugin_weather_duoyun;
            case "暴雪":
                return R.drawable.biz_plugin_weather_baoxue;
            case "暴雨":
                return R.drawable.biz_plugin_weather_baoyu;
            case "大暴雨":
                return R.drawable.biz_plugin_weather_dabaoyu;
            case "大雪":
                return R.drawable.biz_plugin_weather_daxue;
            case "大雨":
                return R.drawable.biz_plugin_weather_dayu;
            case "雷阵雨":
                return R.drawable.biz_plugin_weather_leizhenyu;
            case "雷阵雨冰雹":
                return R.drawable.biz_plugin_weather_leizhenyubingbao;
            case "沙尘暴":
                return R.drawable.biz_plugin_weather_shachenbao;
            case "特大暴雨":
                return R.drawable.biz_plugin_weather_tedabaoyu;
            case "雾":
                return R.drawable.biz_plugin_weather_wu;
            case "小雪":
                return R.drawable.biz_plugin_weather_xiaoxue;
            case "雨加雪":
                return R.drawable.biz_plugin_weather_yujiaxue;
            case "阵雪":
                return R.drawable.biz_plugin_weather_zhenxue;
            case "阵雨":
                return R.drawable.biz_plugin_weather_zhenyu;
            case "中雪":
                return R.drawable.biz_plugin_weather_zhongxue;
            case "中雨":
                return R.drawable.biz_plugin_weather_zhongyu;
            default:               //默认就返回晴
                return R.drawable.biz_plugin_weather_qing;
        }
    }

    public static int transToImage_PM25(String type) {
        if(type==null)  //如果不事先判断，碰到没有pm2.5数据的，下面parseInt就会崩溃。
            return R.drawable.biz_plugin_weather_0_50;
        int i=Integer.parseInt(type);
        if(i<=50){
            return R.drawable.biz_plugin_weather_0_50;
        }else if (i<=100){
            return R.drawable.biz_plugin_weather_51_100;
        }else if (i<=150){
            return R.drawable.biz_plugin_weather_101_150;
        }else if (i<=200){
            return R.drawable.biz_plugin_weather_151_200;
        }else if (i<=300){
            return R.drawable.biz_plugin_weather_201_300;
        }else {
            return R.drawable.biz_plugin_weather_greater_300;
        }
    }
}
