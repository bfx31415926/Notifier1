package com.example.notifier;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import static com.example.notifier.Sounds.*;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MainActivity extends AppCompatActivity implements DoForPositive1 {
    public static final String LOG_TAG = "mainActivity";
    private TextView selectMinutesTV;
    private RadioGroup radioGroup;
    private Integer[] radioButtonsID;
    private int selectedRBNumber = 0;
    private int selectedMinutes;
    private Button actionBtn;
    private TextView timerTV;
    private Boolean bAction = false; //true - старт, false - стоп
    private Timer timer;
    private final static int bigMS = 3600000; //кол-во мс в сутках
    private final static int timerInterval = 1000;// (1 сек) Интервал таймера (тик)
    private int currSec; //текущая секунда, выводимая в timerTV
    private SharedPreferences sPref;
    final private String SAVED_INT = "saved_int";
//    protected PowerManager.WakeLock mWakeLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        loadInt();


        selectMinutesTV = findViewById(R.id.selectMinutesTV);
        radioGroup = findViewById(R.id.radioGroup);

        actionBtn  = findViewById(R.id.actionBtn);
        timerTV = findViewById(R.id.timerTV);

        fillRadioButtonsID();

        radioGroup.check(radioButtonsID[selectedRBNumber]);
        selectedMinutes = getSelectedMinutes(selectedRBNumber);

        adjustWidgets();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectedRBNumber = getSelectedRBNumber(checkedId);
                radioGroup.check(checkedId);
                selectedMinutes = getSelectedMinutes(selectedRBNumber);
            }
        });

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionBtnClick();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "MainActivity: onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "MainActivity: onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "MainActivity: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "MainActivity: onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(LOG_TAG, "MainActivity: onDestroy()");
    }
    private void fillRadioButtonsID(){
        radioButtonsID = new Integer[5];
        radioButtonsID[0] = R.id.radioButton1;
        radioButtonsID[1] = R.id.radioButton2;
        radioButtonsID[2] = R.id.radioButton3;
        radioButtonsID[3] = R.id.radioButton4;
        radioButtonsID[4] = R.id.radioButton5;
    }
    private int getSelectedRBNumber(int checkedId){
        for (int i = 0; i < radioButtonsID.length; ++i){
            if(radioButtonsID[i] == checkedId)
                return i;
        }
        return 0;
    }
    private int getSelectedMinutes(int rBNumber){
        RadioButton rb = findViewById(radioButtonsID[rBNumber]);
        return Integer.valueOf(rb.getText().toString());
    }
    private void onActionBtnClick() {
        bAction = !bAction;
        adjustWidgets();
    }
    private void adjustWidgets(){
        selectMinutesTV.setVisibility(bAction ? View.INVISIBLE : View.VISIBLE);
        radioButtonsSetEnabled(!bAction);
//        radioGroup.setEnabled(bAction ? false : true);
        radioGroup.check(radioButtonsID[selectedRBNumber]);
        actionBtn.setText(bAction ? R.string.stop : R.string.start);
        timerTV.setVisibility(bAction ? View.VISIBLE : View.INVISIBLE);
        if(bAction) { //стартовали таймер
            timer = new Timer(bigMS, timerInterval);
            timer.myStart();
        } else {
            if(timer != null) {
                timer.myCancel();
            }
        }
    }
    private void radioButtonsSetEnabled(boolean b){
        for (int i = 0; i < radioButtonsID.length; ++i){
            RadioButton rb = findViewById(radioButtonsID[i]);
            rb.setEnabled(b);
        }
    }
    private class Timer extends CountDownTimer {
        private boolean started;
        public Timer(long millisInFuture, long interval) {
            super(millisInFuture, interval);
        }
        public boolean isStarted() {
            return started;
        }

        @Override
        public void onFinish() {
            Log.d(LOG_TAG, "Timer(onFinish)");
            myCancel();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            ++currSec;
            Log.d(LOG_TAG, "onTick currSec = [" + String.valueOf(currSec) + "] millisUntilFinished = " + String.valueOf(millisUntilFinished));
            timerTV.setText(String.valueOf(currSec));
            if(currSec == selectedMinutes * 60){
                currSec = 0;
                //todo Запуск звукового файла
                Log.d(LOG_TAG, "BEFORE startPlaying");
                startPlaying(MainActivity.this, R.raw.gong);
                Log.d(LOG_TAG, "AFTER startPlaying");
            }
        }

        public void myStart() {
            timerTV.setText("0");
            timerTV.setVisibility(View.VISIBLE);
            start();
            started = true;
            currSec = 0;

            Log.d(LOG_TAG, "BEFORE startPlaying");
            startPlaying(MainActivity.this, R.raw.gong);
            Log.d(LOG_TAG, "AFTER startPlaying");

            Log.d(LOG_TAG, "Timer(myStart)");
        }

        public void myCancel() {
            cancel();
            started = false;
            timerTV.setText("0");
            timerTV.setVisibility(View.INVISIBLE);
            Log.d(LOG_TAG, "Timer(myCancel)");
        }
    }
    public void fDo () {
        //вызывается из диалога DlgWithTwoButtons
        if(timer != null)
            timer.myCancel();

        saveInt();

        finish(); //покидаем эту Activity (дестроим)
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            DlgWithTwoButtons myDialogFragment = new DlgWithTwoButtons("", getString(R.string.exitMainActivity),
                    getString(R.string.yes_option), getString(R.string.no_option), this);
            myDialogFragment.show(getSupportFragmentManager(), "myDialog");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void loadInt(){
        sPref = getPreferences(MODE_PRIVATE);
        selectedRBNumber = sPref.getInt(SAVED_INT, 0);
    }
    private void saveInt(){
        sPref = getPreferences(MODE_PRIVATE);
        Editor ed = sPref.edit();
        ed.putInt(SAVED_INT, selectedRBNumber);
        ed.commit();
    }
}