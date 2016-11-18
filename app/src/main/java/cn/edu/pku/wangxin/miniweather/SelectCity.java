package cn.edu.pku.wangxin.miniweather;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.administrator_x.myapplication_real.R;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.wangxin.app.MyApplication;
import cn.edu.pku.wangxin.bean.City;

/**
 * Created by Administrator_x on 2016/10/18.
 */
public class SelectCity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener {
    private ImageView mBackBtn;
    private List<City> mCityList;
    private List<String> strCityName=new ArrayList<>();
    private List<String> strCityNumber=new ArrayList<>();
    private String selectedCityID;
    private MyApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn  = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);


        //注意是MyApplication类型，而不能是Application类型，否则下面getCityList方法会找不到
        app = (MyApplication) getApplicationContext();
        mCityList = app.getCityList();
        for(City city:mCityList)
        {
            strCityName.add(city.getCity());
            strCityNumber.add(city.getNumber());
        }
        ListView list_view = (ListView) findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strCityName); //第三个参数类型变了，则ArrayAdapter
        //后面的泛型也要相应改变
        list_view.setAdapter(adapter);
        list_view.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_back:
                Intent i = new Intent();  //注意不是用getIntent获取intent，因为当前活动不是要从mainactivity创建的Intent中获取什么数据。毕竟mainactvitiy中的intent也没有
                //putExtra操作。 恰恰是当前activity要创建intent并写入东西，传递给mainactivity
                if(selectedCityID==null) {
                    SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                    selectedCityID=sp.getString("main_city_code","101040100");
                }
                i.putExtra("cityCode", selectedCityID); //利用Intent传递数据  两个参数 键值对
                setResult(RESULT_OK, i); //这个方法专门用于向上一个活动返回数据的。 所以不用startActivity
                finish(); //由于当前活动是mainactivity通过startactivityforresult方法调用的，所以这个方法销毁之后会回调上一个活动的onActivityResult()方法。
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(SelectCity.this, "你选择了:"+strCityName.get(position), Toast.LENGTH_SHORT).show();
        selectedCityID=strCityNumber.get(position);

        Intent i = new Intent();  //注意不是用getIntent获取intent，因为当前活动不是要从mainactivity创建的Intent中获取什么数据。毕竟mainactvitiy中的intent也没有
        //putExtra操作。 恰恰是当前activity要创建intent并写入东西，传递给mainactivity
        i.putExtra("cityCode", selectedCityID); //利用Intent传递数据  两个参数 键值对
        setResult(RESULT_OK, i); //这个方法专门用于向上一个活动返回数据的。 所以不用startActivity
        finish(); //由于当前活动是mainactivity通过startactivityforresult方法调用的，所以这个方法销毁之后会回调上一个活动的onActivityResult()方法。
    }
}
