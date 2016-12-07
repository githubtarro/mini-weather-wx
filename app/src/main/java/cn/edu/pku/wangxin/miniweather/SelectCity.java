package cn.edu.pku.wangxin.miniweather;



import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator_x.myapplication_real.R;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.wangxin.app.MyApplication;
import cn.edu.pku.wangxin.bean.City;
import cn.edu.pku.wangxin.util.Trans2PinYin;

/**
 * Created by Administrator_x on 2016/10/18.
 */
public class SelectCity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener {
    private static final int UPDATE_LISTVIEW = 1;
    private ImageView mBackBtn;
    private List<City> mCityList;
    /*private List<String> strCityName=new ArrayList<>();
    private List<String> strCityNumber=new ArrayList<>();*/
    private  List<String> strCityInfo=new ArrayList<>(); //没有 static
    private String selectedCityID;
    private  MyApplication app;

    private TextView mTextView;
    private EditText mEditText;
    private ArrayAdapter<String> adapter;

    private static List<String> strCityInfo_full=new ArrayList<>();
    private static  List<String> strCityInfo_full_pinyin=new ArrayList<>();
    private static int once=0;
    private Editable editableTemp;
    private ListView list_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn  = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        mEditText = (EditText) findViewById(R.id.search_edit);
        mTextView = (TextView)findViewById(R.id.t);
        app = (MyApplication) getApplicationContext();
        mCityList = app.getCityList();
        if(once==0)  //第一次进入城市选择界面的时候就把数据库中所有城市的必要信息加载进static成员变量中，并放在内存，方便后续的模糊查找
        {
            once = 1;
            int cnt = 0;
            for (City city : mCityList) {
                strCityInfo_full.add("NO." + cnt + ":" + city.getNumber() + "-" + city.getProvince() + "-" + city.getCity()); //这个一直保持listview中所有item的字符串信息
                strCityInfo_full_pinyin.add(Trans2PinYin.trans2PinYin(strCityInfo_full.get(cnt)));
                cnt++;
            }
        }
        strCityInfo.clear();  //每次进入城市选择界面，都要先清空一下当前可选城市列表集合
        int cnt=0;
        for (City city : mCityList) {
            strCityInfo.add("NO." + cnt + ":" + city.getNumber() + "-" + city.getProvince() + "-" + city.getCity());
            cnt++;
        }
           list_view = (ListView) findViewById(R.id.list_view);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strCityInfo);
            list_view.setAdapter(adapter);
            list_view.setOnItemClickListener(this);


        //EditText控件与ListView控件实现模糊查找
        mEditText.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int editStart ;
            private int editEnd ;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                temp = charSequence;
                Log.d("myapp","beforeTextChanged:"+temp) ;
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
               // mTextView.setText(charSequence);
                Log.d("myapp","onTextChanged:"+charSequence) ;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editStart = mEditText.getSelectionStart();
                editEnd = mEditText.getSelectionEnd();
                if (temp.length() > 10) {
                    Toast.makeText(SelectCity.this, "你输入的字数已经超过了限制！", Toast.LENGTH_SHORT).show();
                    editable.delete(editStart-1, editEnd);
                    int tempSelection = editStart;
                  //  mEditText.setText(editable);
                    mEditText.setSelection(tempSelection);
                }
                Log.d("myapp","afterTextChanged:") ;

                /*每次都根据edittex中的内容更新adapter绑定的集合的内容*/
                strCityInfo.clear();
                        for(String str:strCityInfo_full) {
                            if (str.indexOf(editable.toString()) != -1) {
                                strCityInfo.add(str);
                            }
                        }
                        int cnt2=0;
                        for(String str2:strCityInfo_full_pinyin) {
                            if (str2.indexOf(editable.toString()) != -1) {
                                strCityInfo.add(strCityInfo_full.get(cnt2));
                            }
                            cnt2++;
                        }
                adapter.notifyDataSetChanged();  //适配器通知ListView说strCityInfo已经改变
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){ //如果点击返回键，表明不选中任何城市信息
            case R.id.title_back:
                Intent i = new Intent();
                if(selectedCityID==null) {
                    SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                    selectedCityID=sp.getString("main_city_code","101040100");
                }
                i.putExtra("cityCode", selectedCityID);
                setResult(RESULT_OK, i); //向上一个活动返回数据的。
                finish(); //由于当前活动是mainactivity通过startactivityforresult方法调用的，所以这个方法销毁之后会回调上一个活动的onActivityResult()方法。
                break;
            default:
                break;
        }
    }

    //单击ListView中的某个item后表明选中了一个城市，于是返回主界面显示这个城市的天气信息
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(SelectCity.this, "你选择了:"+strCityInfo.get(position), Toast.LENGTH_SHORT).show();
        String[] a=strCityInfo.get(position).split(":");  //因为listview中每个item的形式是NO.xxx:城市Number-省份-城市
        String[] b=a[1].split("-");
        selectedCityID=b[0];
        Log.d("selectedCityID",b[0]);

        Intent i = new Intent();
        i.putExtra("cityCode", selectedCityID);
        setResult(RESULT_OK, i); //向上一个活动返回数据的。
        finish(); //由于当前活动是mainactivity通过startactivityforresult方法调用的，所以这个方法销毁之后会回调上一个活动的onActivityResult()方法。
    }
}
