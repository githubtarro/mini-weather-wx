package cn.edu.pku.wangxin.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.wangxin.bean.City;

/**
 * Created by Administrator_x on 2016/10/18.
 */
public class CityDB {
    public static final String CITY_DB_NAME = "city.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;
    public CityDB(Context context, String path) {//MyApplication.java中的那个openCityDB方法的返回值就是用这个构造函数构造的一个对象
        db = context.openOrCreateDatabase(path, Context.MODE_PRIVATE, null);  //path就是/data/data/包名/databases/city.db。application对象中手握一个
        // 这个citydb对象的引用
    }
    public List<City> getAllCity() {  //这个方法返回一个list链表，里面存储所有的city对象。
        List<City> list = new ArrayList<City>();
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME, null);
        while (c.moveToNext()) {
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, firstPY,allPY,allFirstPY);
            list.add(item);  //所以list这个集合类型对象中存储的每个元素都是个City对象
        }
        return list;
    }
}
