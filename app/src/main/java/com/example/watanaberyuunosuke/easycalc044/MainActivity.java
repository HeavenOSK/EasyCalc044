package com.example.watanaberyuunosuke.easycalc044;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

//    **EasyCalc044(簡易電卓アプリ)**
//    Mac OS X El Capitanの標準電卓を参考に作成。
//
//    ユーザーの入力は、ボタンタップのみ。
//
//    **ボタンの種類**
//    0.整数入力(0~9)
//    1.記号入力(小数点、マイナス記号)
//    2.演算子入力(+、-、×、÷)
//    3.パーセント入力(%)
//    4.イコール入力(=)
//    5.オールクリア/クリア入力(AC/C)
//
//    **電卓の処理上、４つの場面を設定する**
//    0.数値１の編集中(NUM01_EDIT)
//    1.演算子入力後、入力待ち(WAIT_ON_CALC_SIGN)
//    2.数値２の編集中(NUM02_EDIT)
//    3.計算結果を表示中(VIEW_RESULT)
//
//    ユーザー入力前にどの場面に属しているかによって、ユーザーの入力で行う処理を変える。
//    また、必要に応じて、処理の後、場面を切り替える。
//
//    数値入力、また表示中の数値は全て文字列で反映させる。
//    計算を行う際は、一旦、数値をDouble型にキャストし計算を行い、文字列型に変換し直して表示する。
//
//    *数値１が入力された後、演算子が入力され、その後イコールが押された際は、
//     現在表示されている数値をコピーして、その数値同士を計算した値を表示する。

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    View v01;
    TextView tv01;
    Button bt01;

    String current_num; //現在表示中の数値を格納するString型変数
    String num01_st;    //一時保存用のString型変数
    String instant_num_st = null;

    //計算用BigDecimalクラス
    BigDecimal num01_dec, num02_dec, result_num_dec;
    //整数型にキャストするための変数
    long result_num;

    //小数点の計算を行うかどうかのフラグ
    //デフォルトは整数計算で、false
    boolean double_calc_flag = false;

    //現在、どの計算記号が押されているかを保存するint型変数
    //calc_sign_flagにはボタンIDを保存する(+の例:calc_sign_flag = R.id.plus;)
    int calc_sign_flag = 0;

    //文字列処理をするため、記号用の定数を用意する
    final String POINT_NUM = ".";
    final String MINUS_NUM = "-";

    //場面設定用のint型変数
    int mCalc_Scene;
    //各代入用の定数を用意する
    final int NUM01_EDIT = 0;       //数値１の編集中
    final int WAIT_ON_CALC_SIGN = 1;//計算記号入力後、入力待ち
    final int NUM02_EDIT = 2;       //数値２の編集中
    final int VIEW_RESULT = 3;      //計算結果を表示中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCalc_Scene = NUM01_EDIT;

        tv01 = (TextView) findViewById(R.id.number_view);

        //全てのボタンのクリックリスナーの初期化
        int number_buttons[] = {R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.zero, R.id.dot};
        for (int i = 0; i < number_buttons.length; i++) {
            v01 = findViewById(number_buttons[i]);
            v01.setOnClickListener(this);
        }
        int sign_buttons[] = {R.id.plus, R.id.minus, R.id.div, R.id.multip, R.id.equal};
        for (int i = 0; i < sign_buttons.length; i++) {
            v01 = findViewById(sign_buttons[i]);
            v01.setOnClickListener(this);
        }
        int special_buttons[] = {R.id.plus_minus, R.id.clear_or_all_clear, R.id.percent};
        for (int i = 0; i < special_buttons.length; i++) {
            v01 = findViewById(special_buttons[i]);
            v01.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        bt01 = (Button) v;
        //現在表示中の数値を取得
        current_num = tv01.getText().toString();
        switch (v.getId())
        {
            //数値入力
            case R.id.one:
            case R.id.two:
            case R.id.three:
            case R.id.four:
            case R.id.five:
            case R.id.six:
            case R.id.seven:
            case R.id.eight:
            case R.id.nine:
            case R.id.zero:
                editNumber(bt01);
                break;
            case R.id.dot:
                setDot();
                break;
            //演算子入力
            case R.id.plus:
            case R.id.minus:
            case R.id.multip:
            case R.id.div:
                setCalc_Sign(bt01);
                break;
            case R.id.equal:
                doEqual();
                break;
            case R.id.plus_minus:
                setPlusMinus();
                break;
            case R.id.percent:
                doPercent();
                break;
            case R.id.clear_or_all_clear:
                clear();
                break;
            default:
                break;
        }
        //イベント発生後、mCalc_Sceneによって、表示(AC / C)を切り替える
        bt01 = (Button)findViewById(R.id.clear_or_all_clear);
        switch(mCalc_Scene){
            case NUM01_EDIT:
                bt01.setText("AC");
                break;
            default:
                bt01.setText("C");
                break;
        }
    }

    //数値入力メソッド
    void editNumber(Button bt01){
        //NUM01_EDITかNUM02_EDIT以外の場合、
        //数値入力を行う前に、表示中の数値を削除する
        //その後、mCalc_SceneをNUM01_EDITかNUM02_EDITを代入する
        switch(mCalc_Scene){
            case WAIT_ON_CALC_SIGN:
                num01_st = current_num; //削除前に数値を保存
                current_num = "";
                mCalc_Scene = NUM02_EDIT;
                break;
            case VIEW_RESULT:
                current_num = "";
                mCalc_Scene = NUM01_EDIT;
                double_calc_flag = false;
                break;
            default:
                break;
        }
        //数値を編集する
        //現在表示中の数値が0の場合、一度削除して入力する
        switch(mCalc_Scene){
            case NUM01_EDIT:
            case NUM02_EDIT:
                if(current_num.equals("0")){
                    current_num = "";
                }
                current_num += bt01.getText();
                break;
            default:
                break;
        }
        tv01.setText(current_num);
    }

    //小数点の入力メソッド
    //入力中の場合は小数点を表示中の数値の文字列の末尾に付け足す。
    //それ以外は、表示中の数値の文字列を削除して"0."を代入する。
    void setDot(){
        double_calc_flag = true;
        switch(mCalc_Scene){
            case NUM01_EDIT:
            case NUM02_EDIT:
                //表示中の数値にすでに小数点が含まれている場合は、処理を行わずreturnする
                for(int i = 0;i < current_num.length();i++){
                    if(current_num.charAt(i) == POINT_NUM.charAt(0)){
                        return;
                    }
                }
                current_num += POINT_NUM;
                break;
            case WAIT_ON_CALC_SIGN:
                num01_st = current_num;
                current_num = "0.";
                break;
            case VIEW_RESULT:
                current_num = "0.";
                break;
        }
        tv01.setText(current_num);
    }

    //入力がされた演算子をcalc_sign_flagに保存するメソッド
    void setCalc_Sign(Button bt01){
        switch(mCalc_Scene){
            case NUM01_EDIT:
            case WAIT_ON_CALC_SIGN:
            case VIEW_RESULT:
                calc_sign_flag = bt01.getId();
                break;
            //NUM02_EDIT時のみ、計算を行う。
            case NUM02_EDIT:
                if(doCalc(num01_st ,current_num) < 0){
                    mCalc_Scene = NUM01_EDIT;
                    tv01.setText("0");
                    return;
                }
                break;
            default:
                break;
        }
        calc_sign_flag = bt01.getId();
        mCalc_Scene = WAIT_ON_CALC_SIGN;
    }

    //イコール入力の際、mCalc_Sceneの状態によって、
    //計算を行うのか、
    //計算する場合はどのような計算を行うかの処理を変化させるメソッド
    void doEqual(){
        switch(mCalc_Scene){
            case NUM01_EDIT:
                break;
            case NUM02_EDIT:
                if(doCalc(num01_st, current_num) < 0){
                    mCalc_Scene = NUM01_EDIT;
                    tv01.setText("0");
                    return;
                }
                mCalc_Scene = VIEW_RESULT;
                break;
            case VIEW_RESULT:
                break;
            case WAIT_ON_CALC_SIGN:
                //num01_st
                //current_num <- (num01_st)
                //数値１と数値１、同一のものを演算する
                if(instant_num_st == null){
                    instant_num_st = current_num;
                }else if(!instant_num_st.equals(current_num)){
                    instant_num_st = current_num;
                }
                if(doCalc(current_num, instant_num_st) < 0){
                    mCalc_Scene = NUM01_EDIT;
                    tv01.setText("0");
                    return;
                };
                break;
        }
    }

    //計算メソッド
    //通常、戻り値=0;
    //零割りがあった場合のみ、戻り値=-1;
    int doCalc(String value01, String value02){
        num01_dec = new BigDecimal(value01);
        num02_dec = new BigDecimal(value02);
        switch(calc_sign_flag){
            case R.id.plus:
                result_num_dec = num01_dec.add(num02_dec);
                break;
            case R.id.minus:
                result_num_dec = num01_dec.subtract(num02_dec);
                break;
            case R.id.multip:
                result_num_dec = num01_dec.multiply(num02_dec);
                break;
            case R.id.div:
                try {
                    result_num_dec = num01_dec.divide(num02_dec, 8, BigDecimal.ROUND_HALF_UP);
                }catch(ArithmeticException e){
                    return -1;
                }
                break;
            default:
                break;
        }
        //小数点計算と割り算以外は、
        //整数型にキャストして表示する
        if(double_calc_flag || (!double_calc_flag && calc_sign_flag == R.id.div)){
            tv01.setText(String.valueOf(result_num_dec));
        }else{
            try {
                result_num = Long.parseLong(result_num_dec.toString());
            }catch(NumberFormatException e){
                return -1;
            }
            tv01.setText(String.valueOf(result_num));
        }
        return 0;
    }

    //パーセント計算メソッド
    void doPercent(){
        switch(mCalc_Scene){
            //表示中の数値を(1/100)したものを表示する
            case NUM01_EDIT:
                double_calc_flag = true;
                result_num_dec = new BigDecimal(current_num);
                result_num_dec = result_num_dec.divide(new BigDecimal(100));
                tv01.setText(String.valueOf(result_num_dec));
                mCalc_Scene = VIEW_RESULT;
                break;
            //表示中の数値を(1/100)したものを数値2として、乗算を行う
            case NUM02_EDIT:
                double_calc_flag = true;
                num01_dec = new BigDecimal(num01_st);
                num02_dec = new BigDecimal(current_num);
                result_num_dec = num01_dec.multiply(num02_dec.divide(new BigDecimal(100)));
                tv01.setText(String.valueOf(result_num_dec));
                mCalc_Scene = VIEW_RESULT;
                break;
            default:
                break;
        }
    }

    //マイナス記号入力メソッド
    void setPlusMinus(){
        StringBuilder builder = new StringBuilder(current_num);
        //数値の頭に-がある場合は-を削除し、
        //ない場合は-を頭につけ加える
        if(!current_num.equals("0")){
            if(current_num.charAt(0) == MINUS_NUM.charAt(0)){
                builder.deleteCharAt(0);
            }else{
                builder.insert(0, "-");
            }
        }
        tv01.setText(builder.toString());
    }

    //クリアメソッド
    //mCalc_Sceneの値を巻き戻す
    void clear(){
        switch(mCalc_Scene){
            //これのみ、ACの動作
            case NUM01_EDIT:
                tv01.setText("0");
                break;
            case WAIT_ON_CALC_SIGN:
                calc_sign_flag = 0;
                mCalc_Scene--;
                break;
            case NUM02_EDIT:
                tv01.setText("0");
                mCalc_Scene--;
                break;
            case VIEW_RESULT:
                tv01.setText("0");
                mCalc_Scene = NUM01_EDIT;
                break;
            default:
                break;
        }
    }
}