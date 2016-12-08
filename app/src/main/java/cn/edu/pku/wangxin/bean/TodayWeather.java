package cn.edu.pku.wangxin.bean;

/**
 * Created by Administrator_x on 2016/10/11.
 */
public class TodayWeather {
    private String city;
    private String updatetime;
    private String wendu;
    private String shidu;
    private String pm25;
    private String quality;
    private String fengxiang;
    private String[] fengli=new String[6];  //风力
    private String[] date=new String[6];   //星期几
    private String[] high=new String[6];  //最高温
    private String[] low=new String[6];   //最低温
    private String[] type=new String[6];  //天气类型决定天气图片  在最近6天天气中决定两个控件

    public String getGuide() {
        return guide;
    }

    public void setGuide(String guide) {
        this.guide = guide;

    }

    private String guide;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getWendu() {
        return wendu;
    }

    public void setWendu(String wendu) {
        this.wendu = wendu;
    }

    public String getShidu() {
        return shidu;
    }

    public void setShidu(String shidu) {
        this.shidu = shidu;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getFengxiang() {
        return fengxiang;
    }

    public void setFengxiang(String fengxiang) {
        this.fengxiang = fengxiang;
    }

    public String getFengli(int i) {
        return fengli[i];
    }

    public void setFengli(int i,String fengli) {
        this.fengli[i] = fengli;
    }

    public String getDate(int i) {  //最近6天天气存储
        return date[i];
    }

    public void setDate(int i,String date) {
        this.date[i] = date;
    }

    public String getHigh(int i) {
        return high[i];
    }

    public void setHigh(int i,String high) {
        this.high[i] = high;
    }

    public String getLow(int i) {
        return low[i];
    }

    public void setLow(int i,String low) {
        this.low[i] = low;
    }

    public String getType(int i) {
        return type[i];
    }

    public void setType(int i,String type) {
        this.type[i] = type;
    }


    @Override
    public String toString() {
        return "TodayWeather{"+
                "city='"+city+'\''+
                ",updatetime='"+updatetime+'\''+
                ",wendu='"+wendu+'\''+
                ",shidu='"+shidu+'\''+
                ",pm25='"+pm25+'\''+
                ",quality='"+quality+'\''+
                ",fengxiang='"+fengxiang+'\''+
                ",fengli='"+fengli+'\''+
                ",date='"+date+'\''+
                ",high='"+high+'\''+
                ",low='"+low+'\''+
                ",type='"+type+'\''+
                '}';
    }
}
