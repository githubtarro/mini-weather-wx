package cn.edu.pku.wangxin.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.wangxin.bean.City;
import cn.edu.pku.wangxin.db.CityDB;

/**
 * Created by Administrator_x on 2016/10/18.
 */
public class MyApplication extends Application {
    private static final String TAG="MyAPP";
    private static MyApplication mApplication;

    private CityDB mCityDB;  //手握一个数据库对象的引用

    private List<City> mCityList;  //这是得到数据库中的数据后，把每条记录（即每个城市）的信息存储在集合变量中。
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");

        mApplication = this;
        mCityDB = openCityDB();  //1.用自己的成员函数opencitydb得到citydb类对象，并让自己的一个属性成员指向它。 opencitydb()函数的作用是把assets中的city.db
        //文件内容存储到手机上的/data/data/包名/databases目录中

        initCityList(); //2.调用citydb的getAllcity方法，初始化mCityList这个集合成员的值。使mcitylist这个集合变量中每个元素都是个city对象。
    }

    private void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
// TODO Auto-generated method stub
                prepareCityList();  //.这里就让那个mCityList成员成功成为有数据的集合。
            }
        }).start();
    }

    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();  //mcitylist这个集合变量中每个元素都是个city对象。
        int i=0;
        for (City city : mCityList) {  //这是是检验mcitylist中每个city对象是否已经有数据了。city对象有很多方法，下面只是输出两个做为测试。
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }
    public List<City> getCityList() {
        return mCityList;  //3.提供这个公有方法，能让其他外部组件得到 app的mCityList
    }

    public static MyApplication getInstance(){
        return mApplication;
    }

    private CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()  //这个说白了返回的是"/data"
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;  //其实就是CityDB类的静态成员CITY_DB_NAME="city.db"
        File db = new File(path);  //path是city.db的绝对路径，与File类型的相关联
        Log.d(TAG,path);
        if (!db.exists()) {  //如果File类对象db关联的city.db文件不存在的话
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;

            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){  //虽然已经知道city.db不存在，但是city.db所在层次目录可能存在了，所以这里判断一下，不存在目录的话，那就创建。
                dirFirstFolder.mkdirs(); //创建这个的目录的原因是因为下面new FileOutputStream(db)中如果city.db文件不存在，自动创建成功要求这个文件所在目录都已经存在
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");//因为这是在上面那条if(!db.exitst())函数体内，即city.db不存在才能执行到这里。
            try {
                InputStream is = getAssets().open("city.db");  //打开res文件夹中的asset文件中的city.db文件，返回的是个字节流对象。用来读取
                FileOutputStream fos = new FileOutputStream(db); //我们要往我们上面设置的city.db中写入数据，如果city.db不存在，则会创建一个。
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) { //   int read(byte b[]) 读取多个字节，放置到字节数组b中，通常读取的字节数量为b的长度，返回值为实际独取的字节的数量。
                    fos.write(buffer, 0, len);  //从buffer中写到fos流对象对应的文件中。
                    fos.flush(); //强行将缓冲区中的内容输出，否则直到缓冲区满后才会一次性的将内容输出
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path); //调用citydb类的构造函数，返回一个打开的数据库对象
    }
}
