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
    private List<String> strCityName=new ArrayList<>();
    private List<String> strCityNumber=new ArrayList<>();
    private List<String> strCityInfo=new ArrayList<>();
    private String selectedCityID;
    private MyApplication app;

    private TextView mTextView;
    private EditText mEditText;
    private ArrayAdapter<String> adapter;

    List<String> strCityInfo_full=new ArrayList<>();
    List<String> strCityInfo_full_pinyin=new ArrayList<>();
    private Editable editableTemp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn  = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        mEditText = (EditText) findViewById(R.id.search_edit);
        mTextView = (TextView)findViewById(R.id.t);


        //注意是MyApplication类型，而不能是Application类型，否则下面getCityList方法会找不到
        app = (MyApplication) getApplicationContext();
        mCityList = app.getCityList();
        int cnt=0;
        for(City city:mCityList)
        {
            strCityInfo.add("NO."+cnt+":"+city.getNumber()+"-"+city.getProvince()+"-"+ city.getCity());//这个则是保持匹配edittex中字符串的item的信息
            strCityInfo_full.add("NO."+cnt+":"+city.getNumber()+"-"+city.getProvince()+"-"+ city.getCity()); //这个一直保持listview中所有item的字符串信息
            strCityInfo_full_pinyin.add(Trans2PinYin.trans2PinYin(strCityInfo_full.get(cnt)));
            strCityName.add(city.getCity());
            strCityNumber.add(city.getNumber());
            cnt++;
        }

        ListView list_view = (ListView) findViewById(R.id.list_view);
        //第三个参数类型变了，则ArrayAdapter
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strCityInfo);
        //后面的泛型也要相应改变
        list_view.setAdapter(adapter);
        list_view.setOnItemClickListener(this);


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
                        for(String str2:strCityInfo_full_pinyin) {  //Trans2PinYin.trans2PinYin(str).indexOf(editableTemp.toString())!=-1 每次都转换会卡到不行，即使开线程也卡得不行
                            if (str2.indexOf(editable.toString()) != -1) {
                                strCityInfo.add(strCityInfo_full.get(cnt2));

                            }
                            cnt2++;  //千万不要放在if中
                        }
                adapter.notifyDataSetChanged();

            }
        });
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
