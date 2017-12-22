package com.topic.coffeeownner;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    FirebaseDatabase db;
    DatabaseReference myRef;

    String[] date,preiod,things;
    int year,momth,day;
    String phone , code ,dayofYear;
    ListView listView;
    Switch mSwitch;
    boolean isOpen ;
    private String SEND_SMS;
    TextView tvDate;
    Button btnLast,btnNext;
    SimpleAdapter simpleAdapter;
    HashMap<String,Object> m;
    int count,k=0;
    ArrayList<Map<String,Object>> myList;
    ArrayList<Object> orderList;
    ArrayList<HashMap<String, ArrayList<Object>>> timeList;
    ArrayList<HashMap<String,ArrayList<HashMap<String, ArrayList<Object>>>>> dayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        setListener();
        db = FirebaseDatabase.getInstance();
        isOpening();
        myList = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this,dayList,R.layout.mycoffeeitem,new String[]{"coffee","手機","time"}
                ,new int[]{R.id.textView1,R.id.textView2,R.id.textView3});

    }
    void findView(){
        mSwitch = (Switch)findViewById(R.id.switch1);
        listView = (ListView)findViewById(R.id.listView);
        tvDate = (TextView)findViewById(R.id.tvDate);
        btnLast = (Button)findViewById(R.id.btnLast);
        btnNext = (Button)findViewById(R.id.btnNext);
    }
    void setListener(){

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isOpen = b;
                Log.d(TAG,"switch:"+b);
                myRef = db.getReference();
                myRef.child("營業").setValue(b);
            }
        });
        btnLast.setOnClickListener(onClickListener);
        btnNext.setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.btnLast:
                    day-=1;
                    break;
                case R.id.btnNext:
                    day+=1;
                    break;
            }
            dayofYear=countingDate(year,momth,day);
            changeDate();
        }
    };
    void setText(){
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        momth = c.get(Calendar.MONTH)+1;
        day = c.get(Calendar.DAY_OF_MONTH);
        dayofYear = countingDate(year,momth,day);
        changeDate();
    }
    void changeDate(){
        myRef=db.getReference("order");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot db1:dataSnapshot.getChildren()){
                    if(db1.getKey().equals(dayofYear)){
                        Log.d(TAG,"d:"+db1.getKey());
                        tvDate.setText(db1.getKey().substring(0,4)+"/"+db1.getKey().substring(4,6)+"/"+db1.getKey().substring(6,8));
                        for (DataSnapshot db2:db1.getChildren()){
                            Log.d(TAG,"db2:"+db2.getKey());
                            for(DataSnapshot db3:db2.getChildren()){
                                String coffee = new String();
                                m = new HashMap<>();
                                coffee = db3.child("咖啡").getValue()+""+db3.child("冷熱").getValue()+
                                        db3.child("大小").getValue()+db3.child("甜度").getValue()+
                                        db3.child("數量").getValue()+db3.child("總價").getValue();
                                m.put("coffee",coffee);
                                m.put("手機",db3.child("手機").getValue());
                                m.put("time",db2.getKey());
                                myList.add(m);
                                Log.d(TAG,"db3:"+db3.child("咖啡").getValue());
                                Log.d(TAG,"db3:"+db3.child("冷熱").getValue());
                                Log.d(TAG,"db3:"+db3.child("大小").getValue());
                                Log.d(TAG,"db3:"+db3.child("甜度").getValue());
                                Log.d(TAG,"db3:"+db3.child("數量").getValue());
                                Log.d(TAG,"db3:"+db3.child("單價").getValue());
                                Log.d(TAG,"db3:"+db3.child("總價").getValue());
                                if(myList.size()!=0){
                                    listView.setAdapter(simpleAdapter);
                                }

                            }
                        }
                    }else {
                        tvDate.setText(dayofYear.substring(0,4)+"/"+dayofYear.substring(4,6)+"/"+dayofYear.substring(6,8));
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        myRef=db.getReference(dayofYear);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"ss:"+dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public String countingDate(int tYear,int tMomth,int tDay){
        Log.d(TAG,"counting"+tYear+","+tMomth+","+tDay);
        if (tYear % 4 == 0) {
            if (tDay == 30 && tMomth == 2) {
                tDay = 1;
                tMomth += 1;
            }
            if (tYear % 100 == 0) {
                if (tDay == 29 && tMomth == 2) {
                    tDay = 1;
                    tMomth += 1;
                }
                if (tYear % 400 == 0) {
                    if (tDay == 30 && tMomth == 2) {
                        tDay = 1;
                        tMomth += 1;
                    }
                    if (tYear % 4000 == 0) {
                        if (tDay == 29 && tMomth == 2) {
                            tDay = 1;
                            tMomth += 1;
                        }
                    }
                }
            }
        }
            if((tDay==0)&&(tMomth==2||tMomth==4||tMomth==6||tMomth==8||tMomth==9||tMomth==11)) {
                tDay=31;
                tMomth-=1;
            }
            if ((tDay==0)&&(tMomth==1||tMomth==3||tMomth==5||tMomth==7||tMomth==10||tMomth==12)){
                if(tMomth==1){
                    tYear-=1;
                    tMomth=13;
                }
                tDay=30;
                tMomth-=1;
            }
            if ((tDay == 31 && (tMomth == 4 || tMomth == 6 || tMomth == 9 || tMomth == 11))||(tDay ==29 && tMomth==2)) {
                tDay = 1;
                tMomth += 1;
            }
            if (tDay == 32 && (tMomth == 1 || tMomth == 3 || tMomth == 5 || tMomth == 7 || tMomth == 8 || tMomth == 10 || tMomth == 12)) {
                if (tMomth == 12) {
                    tDay = 1;
                    tMomth = 1;
                    tYear += 1;
                    Log.d("TAGT", "1月1日");
                } else {
                    tDay = 1;
                    tMomth += 1;
                }
            }
        day=tDay;
        momth=tMomth;
        year=tYear;
        String m =tMomth>9?""+tMomth:"0"+tMomth;
        String d =tDay>9?""+tDay:"0"+tDay;
        return tYear+""+m+""+d;
    }
    @Override
    protected void onResume() {
        super.onResume();
        setText();
//        Runnable r1 = new Runnable() {
//            @Override
//            public void run() {
//                getData();
//            }
//        };
//        Thread t1 = new Thread(r1);
//        t1.start();
//        try {
//            t1.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,1,"新增").setIcon(android.R.drawable.ic_menu_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0,1,1,"修改").setIcon(android.R.drawable.ic_menu_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
    void setArrayList(){
        for(int k=0 ;k<dayList.get(0).get(0).size();k++){
            Log.d(TAG,"deyLits:"+dayList.get(0).get(0).get(k).toString());
        }

    }
    void isOpening(){
        myRef = db.getReference();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSwitch.setChecked((dataSnapshot.child("營業").getValue().toString().equals("true")?true:false));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    void getData(){

        myRef = db.getReference("order");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int i=0;
                for (DataSnapshot db1 :dataSnapshot.getChildren()){
                    date = new String[(int)db1.getChildrenCount()];
                    date[i]= db1.getKey().toString();

                    i++;
                    HashMap<String,ArrayList<HashMap<String, ArrayList<Object>>>> m3 =new HashMap<String,ArrayList<HashMap<String, ArrayList<Object>>> >();
                    Log.d(TAG,"days:"+db1.getKey().toString());
                    timeList = new ArrayList<HashMap<String, ArrayList<Object>>>();
                    for (DataSnapshot db2:db1.getChildren()){
                        HashMap<String,ArrayList<Object>> m2 =new HashMap<String, ArrayList<Object>>();
                        Log.d(TAG,"times:"+db2.getKey());
                        for (DataSnapshot db3:db2.getChildren()){
                            Log.d(TAG,"數:"+db3.getKey());
                            orderList = new ArrayList<Object>();
                            for (DataSnapshot db4:db3.getChildren()){
                                Map<String,String> m1 =new HashMap<String, String>();
                                m1.put(db4.getKey(),db4.getValue().toString());
                                Log.d(TAG,"details:"+db4.getKey()+"/"+db4.getValue());
                               orderList.add(m1);
                            }
                        }
                        m2.put(db2.getKey(),orderList);
                        timeList.add(m2);
                    }
                    m3.put(db1.getKey(),timeList);
                    dayList.add(m3);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setSendCode(){      //        傳送簡訊 驗證碼
        myRef = db.getReference("users");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count=0;
                for (DataSnapshot db1 : dataSnapshot.getChildren()){
                    count++;
                    if (db1.child("sendCode").getValue().toString().equals("false")){
                        Log.d(TAG,"phone:"+ db1.child("phone").getValue().toString());
                        Log.d(TAG,"code:"+ db1.child("code").getValue().toString());
                        phone = db1.child("phone").getValue().toString();
                        code = db1.child("code").getValue().toString();
                        sendSMS(phone,code);
                        myRef.child(count+"").child("sendCode").setValue(true);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void sendSMS(String phone,String code){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone,
                null,
                "驗證碼 : "+code,
                PendingIntent.getBroadcast(getApplicationContext(),
                0,
                new Intent(),0),
                null);
    }



}
