package com.chen.wayne.myfillmemory;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class FillMemoryActivity extends AppCompatActivity {
    private final long mTotal = getTotalMemory();
    private long mFree;
    private EditText mNewSize;
    private TextView mFreeSize;
    private TextView mLabelFree;
    private TextView mTipsSize;
    private TextView mTipsPercent;
    private Button mFill;
    private Button mSize;
    private Button mPercent;
    private TextView mUnit;
    private TextView mUnit1;
    private TextView mLabelNew;
    private Handler mHandler;
    private TextView mTotalSize;
    private TextView mUsedSize;
    private TableRow mRow2;
    private TableRow mRow3;
    private ImageView mProgressBar;
    private static final int NOTIFICATION_ID = 20171;
    public BroadcastReceiver mQueryResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int size = intent.getIntExtra(FillMemoryService.EXTRA_FILL_SIZE, -1);
            if (size == -1) {
                mProgressBar.clearAnimation();
                Toast.makeText(getApplicationContext(), "执行失败", Toast.LENGTH_LONG).show();
            } else {
                Log.i("size:", String.valueOf(size));
                long when = System.currentTimeMillis();
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder noticeBuilder = new Notification.Builder(getApplicationContext());
                noticeBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("内存填充：操作已完成")
                        .setWhen(when);
                Notification notification = createNotification(noticeBuilder);
                // NotificationC notification = new Notification(R.drawable.logo, "内存填充：操作已完成", when);
                Intent intent1 = new Intent(getApplicationContext(), FillMemoryActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, 0);
                if (mFill.getText().toString().equals(getString(R.string.btn_fill))) {
                    Log.i("==toast==", "fill");
                    // notification.setLatestEventInfo(getApplicationContext(), "内存填充成功！", "实际填充了："
                    //        + String.valueOf(size) + "MB", pendingIntent);
                    noticeBuilder.setContentTitle("内存填充成功！")
                            .setContentText("实际填充了：" + String.valueOf(size) + "MB")
                            .setContentIntent(pendingIntent);
                    notification = createNotification(noticeBuilder);
                    mProgressBar.clearAnimation();
                    Toast.makeText(getApplicationContext(), "填充成功", Toast.LENGTH_SHORT).show();
                    MainActivity.sFill = getString(R.string.btn_release);
                    MainActivity.sUnit = mUnit.getText().toString();
                    MainActivity.sNewSize = mNewSize.getText().toString().replaceFirst("^0*", "");
                    // refresh UI
                    mNewSize.setEnabled(false);
                    mNewSize.setText(MainActivity.sNewSize);
                } else {
                    Log.i("==toast==", "release");
                    notification.flags = Notification.FLAG_AUTO_CANCEL;
                    // notification.setLatestEventInfo(getApplicationContext(), "内存释放成功！", "已经释放了上次填充的内存", pendingIntent);
                    noticeBuilder.setContentTitle("内存释放成功！")
                            .setContentText("已经释放了上次填充的内存")
                            .setContentIntent(pendingIntent);
                    notification = createNotification(noticeBuilder);
                    mProgressBar.clearAnimation();
                    Toast.makeText(getApplicationContext(), "释放成功", Toast.LENGTH_SHORT).show();
                    MainActivity.sFill = getString(R.string.btn_fill);
                    MainActivity.sUnit = getString(R.string.unit_size);
                    MainActivity.sNewSize = "";
                    // refresh UI
                    mNewSize.setEnabled(true);
                    mNewSize.setText("");
                }
                mNotificationManager.notify(NOTIFICATION_ID, notification);
                mFill.setText(MainActivity.sFill);
                // 解锁
                mFill.setEnabled(true);
            }
        }
    };
    // 轮询空闲内存
    private Runnable runnable = new Runnable() {
        public void run() {
            this.update();
            mHandler.postDelayed(this, 2000); // 间隔2秒
        }
        void update() {
            mFree = getAvailableMemory() / 1024;
            long usedSize = mTotal - mFree;
            if (mUnit.getText().equals(getString(R.string.unit_size))) {
                mFreeSize.setText(String.valueOf(mFree / 1024));
                mUsedSize.setText(String.valueOf(usedSize / 1024));
            } else {
                mFreeSize.setText(String.valueOf(usedSize * 100 / mTotal));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_memory);
        mNewSize = (EditText) findViewById(R.id.new_size);
        mFreeSize = (TextView) findViewById(R.id.free_size);
        mLabelFree = (TextView) findViewById(R.id.label_free_size);
        mLabelNew = (TextView) findViewById(R.id.label_new_size);
        mTipsSize = (TextView) findViewById(R.id.tips_size);
        mTipsPercent = (TextView) findViewById(R.id.tips_percent);
        mFill = (Button) findViewById(R.id.fill);
        mSize = (Button) findViewById(R.id.size);
        mPercent = (Button) findViewById(R.id.percent);
        mUnit = (TextView) findViewById(R.id.unit);
        mUnit1 = (TextView) findViewById(R.id.unit1);
        mProgressBar = (ImageView) findViewById(R.id.progress_bar);
        mTotalSize = (TextView) findViewById(R.id.total_size);
        mUsedSize = (TextView) findViewById(R.id.used_size);
        mRow2 = (TableRow) findViewById(R.id.row2);
        mRow3 = (TableRow) findViewById(R.id.row3);
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 200); // 延迟200毫秒
        mSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.sFill.equals(getString(R.string.btn_release))) {
                    if (MainActivity.sUnit.equals(getString(R.string.unit_percent))) {
                        Toast.makeText(getApplicationContext(), getString(R.string.clickable_tip),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setUI(1);
                }
            }
        });
        mPercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.sFill.equals(getString(R.string.btn_release))) {
                    if (MainActivity.sUnit.equals(getString(R.string.unit_size))) {
                        Toast.makeText(getApplicationContext(), getString(R.string.clickable_tip),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setUI(2);
                }
            }
        });
        mFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 不允许重复点击按钮，锁定
                mFill.setEnabled(false);
                if (MainActivity.sFill.equals(getString(R.string.btn_fill))) {
                    onFill();
                } else {
                    onRelease();
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        setUI(0);
        // query result result
        IntentFilter filter = new IntentFilter();
        filter.addAction(FillMemoryService.ACTION_QUERY_RESULT);
        filter.addAction(FillMemoryService.ACTION_FILL);
        filter.addAction(FillMemoryService.ACTION_RELEASE);
        registerReceiver(mQueryResultReceiver, filter);
        //StatService.onResume(this);
        //UserVisitReport.pageReport("内存填充");
    }
    @Override
    protected void onPause() {
        super.onPause();
        //StatService.onPause(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // 取消注册
        unregisterReceiver(mQueryResultReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(runnable);
    }
    private void onRelease() {
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        mProgressBar.setAnimation(rotate);
        mProgressBar.startAnimation(rotate);
        Intent intent = new Intent(this, FillMemoryService.class);
        intent.setAction(FillMemoryService.ACTION_RELEASE);
        startService(intent);
    }
    private void onFill() {
        if (TextUtils.isEmpty(mNewSize.getText())) {
            // 空值
            Toast.makeText(getApplicationContext(), "请填入数值", Toast.LENGTH_SHORT).show();
            // 解锁
            mFill.setEnabled(true);
        } else {
            // 非空
            long newSize = Long.valueOf(mNewSize.getText().toString());
            boolean flag = false;
            if (mUnit.getText().toString().equals(getString(R.string.unit_percent))) {
                // 比例填充
                if (validate(newSize, 2)) {
                    newSize = (mTotal * (newSize - Integer.parseInt(mFreeSize.getText().toString())) / 100) / 1024;
                    flag = true;
                }
            } else {
                // 数值填充
                if (validate(newSize, 1)) {
                    flag = true;
                }
            }
            if (flag) {
                // start animation
                Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                mProgressBar.setAnimation(rotate);
                mProgressBar.startAnimation(rotate);
                Intent intent = new Intent(this, FillMemoryService.class);
                intent.setAction(FillMemoryService.ACTION_FILL);
                intent.putExtra(FillMemoryService.EXTRA_FILL_SIZE, (int) newSize);
                startService(intent);
            } else {
                // 解锁
                mFill.setEnabled(true);
            }
        }
    }
    public boolean validate(long size, int type) {
        boolean flag = false;
        if (size == 0) {
            Toast.makeText(getApplicationContext(), "数值不能为0", Toast.LENGTH_SHORT).show();
        } else {
            switch (type) {
                case 1:
                    if (size >= Integer.parseInt(mFreeSize.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "数值只能小于空闲内存", Toast.LENGTH_SHORT).show();
                    } else {
                        flag = true;
                    }
                    break;
                case 2:
                    if (size <= Integer.parseInt(mFreeSize.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "数值只能大于内存占用比", Toast.LENGTH_SHORT).show();
                    } else {
                        flag = true;
                    }
                    break;
                default:
                    break;
            }
        }
        return flag;
    }
    private void setUI(int status) {
        final int colorSelected = 0xfffd8502;
        final int colorUnselected = 0xffffffff; // 0xff727273;
        final int colorSelectedBg = 0x00ffffff;
        final int colorUnselectedBg = 0xffcccccc;
        final int sizeSelected = 18;
        final int sizeUnselected = 16;
        switch (status) {
            case 1: // 界面可进行填充操作,点击大小按钮后
                mSize.setTextColor(colorSelected);
                mSize.setBackgroundColor(colorSelectedBg);
                mSize.setTextSize(sizeSelected);
                mSize.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                mPercent.setTextColor(colorUnselected);
                mPercent.setBackgroundColor(colorUnselectedBg);
                mPercent.setTextSize(sizeUnselected);
                mPercent.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mNewSize.setText("");
                mFreeSize.setText(R.string.default_text);
                mUsedSize.setText(R.string.default_text);
                mLabelFree.setText(R.string.label_free_size);
                mLabelNew.setText(R.string.label_new_size);
                mNewSize.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)}); // 最多填4位
                mUnit.setText(R.string.unit_size);
                mUnit1.setText(R.string.unit_size);
                mRow2.setVisibility(View.VISIBLE);
                mRow3.setVisibility(View.VISIBLE);
                mTipsSize.setVisibility(View.VISIBLE);
                mTipsPercent.setVisibility(View.GONE);
                break;
            case 2:  // 界面可进行填充操作,点击比例按钮后
                mSize.setTextColor(colorUnselected);
                mSize.setBackgroundColor(colorUnselectedBg);
                mSize.setTextSize(sizeUnselected);
                mSize.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mPercent.setTextColor(colorSelected);
                mPercent.setBackgroundColor(colorSelectedBg);
                mPercent.setTextSize(sizeSelected);
                mPercent.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                mNewSize.setText("");
                mFreeSize.setText(R.string.default_text);
                mLabelFree.setText(R.string.label_free_percent);
                mLabelNew.setText(R.string.label_new_percent);
                mUnit.setText(R.string.unit_percent);
                mUnit1.setText(R.string.unit_percent);
                mNewSize.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)}); // 最多填2位
                mRow2.setVisibility(View.GONE);
                mRow3.setVisibility(View.GONE);
                mTipsSize.setVisibility(View.GONE);
                mTipsPercent.setVisibility(View.VISIBLE);
                break;
            default:  // 加载界面
                mFill.setText(MainActivity.sFill);
                mUnit.setText(MainActivity.sUnit);
                mUnit1.setText(MainActivity.sUnit);
                mTotalSize.setText(String.valueOf(mTotal / 1024));
                if (MainActivity.sUnit.equals(getString(R.string.unit_size))) {
                    mNewSize.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)}); // 最多填4位
                    mSize.setTextColor(colorSelected);
                    mSize.setBackgroundColor(colorSelectedBg);
                    mSize.setTextSize(sizeSelected);
                    mSize.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    mPercent.setTextColor(colorUnselected);
                    mPercent.setBackgroundColor(colorUnselectedBg);
                    mPercent.setTextSize(sizeUnselected);
                    mPercent.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    mLabelNew.setText(R.string.label_new_size);
                    mLabelFree.setText(R.string.label_free_size);
                    mRow2.setVisibility(View.VISIBLE);
                    mRow3.setVisibility(View.VISIBLE);
                    mTipsSize.setVisibility(View.VISIBLE);
                    mTipsPercent.setVisibility(View.GONE);
                } else {
                    mNewSize.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)}); // 最多填2位
                    mSize.setTextColor(colorUnselected);
                    mSize.setBackgroundColor(colorUnselectedBg);
                    mSize.setTextSize(sizeUnselected);
                    mSize.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    mPercent.setTextColor(colorSelected);
                    mPercent.setBackgroundColor(colorSelectedBg);
                    mPercent.setTextSize(sizeSelected);
                    mPercent.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    mLabelFree.setText(R.string.label_free_percent);
                    mLabelNew.setText(R.string.label_new_percent);
                    mRow2.setVisibility(View.GONE);
                    mRow3.setVisibility(View.GONE);
                    mTipsSize.setVisibility(View.GONE);
                    mTipsPercent.setVisibility(View.VISIBLE);
                }
                mNewSize.setText(MainActivity.sNewSize);
                if (MainActivity.sFill.equals(getString(R.string.btn_fill))) {
                    mNewSize.setEnabled(true);
                } else {
                    mNewSize.setEnabled(false);
                }
                break;
        }
    }
    public long getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi); // 传入参数，将获得数据保存在memInfo对象中
        return mi.availMem; // 单位是Byte
    }
    private long getTotalMemory() {
        String str1 = "/proc/meminfo"; // 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initialMemory;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine(); // 读取meminfo第一行，系统总内存大小
            Log.i(str2, "");
            arrayOfString = str2.split("\\s+");
            initialMemory = Integer.valueOf(arrayOfString[1]); // 获得系统总内存，单位是KB
            localBufferedReader.close();
        } catch (IOException ignored) {
            initialMemory = 0;
        }
        return initialMemory;
    }
    public void back() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void onClick(View view) {
        finish();
    }
    public void buttonClick(View view) {
        finish();
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    private Notification createNotification(Notification.Builder builder) {
        if (builder == null) {
            return null;
        }
        if (android.os.Build.VERSION.SDK_INT > 11 & android.os.Build.VERSION.SDK_INT <= 15) {
            return builder.getNotification();
        } else if (android.os.Build.VERSION.SDK_INT > 15) {
            return builder.build();
        }
        return null;
    }
}
