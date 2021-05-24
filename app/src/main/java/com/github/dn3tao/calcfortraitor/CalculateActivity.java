package com.github.dn3tao.calcfortraitor;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class CalculateActivity extends AppCompatActivity {

    // идентификатор диалогового окна AlertDialog с кнопками
    private final int IDD_TWO_BUTTONS = 0;

    private TextView mTextViewResult;
    private EditText mEditAltOnTarget;
    private EditText mEditAltOnMortar;
    private EditText mEditx1;
    private EditText mEditx2;
    private EditText mEdity1;
    private EditText mEdity2;
    private int mCaliber;
    private TextView mResultText1;
    private TextView mResultText2;
    private TextView mResultText3;
    private TextView mResultText4;
    private TextView mResultText5;
    private TextView mResultText6;
    private TextView mResultText7;
    private TextView mResultText8;
    private TextView mResultRange;
    private TextView mResultAzimute;
    private ArrayList<TextView> mResultTextViews;
    private HashMap<String, Float> resultHash;
    private int mRange;
    private float mAzimute;
    private View mDivider;
    private SharedPreferences mSettings;
    private boolean mIsDisplayOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);

        FloatingActionButton fab = findViewById(R.id.fab);

        // обработчики нажатий floating button
        fab.setOnLongClickListener(v -> {
            onClickClearAll();
            Toast toast = Toast.makeText(CalculateActivity.this, getString(R.string.all_clear), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 15);
            toast.show();
            return true;
        });
        fab.setOnClickListener(view -> {
            onClickClearTarget();
            Toast toast = Toast.makeText(CalculateActivity.this, getString(R.string.hold_3_sec), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 15);
            toast.show();
        });

        // вотчеры для текстовых полей
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                onEventCalculate();
            }
        };

        mCaliber = getIntent().getIntExtra("CALIBER", 120); //получение выбранного калибра из интента

        mTextViewResult = findViewById(R.id.resultHeaderText);
        mEditAltOnMortar = findViewById(R.id.editAltOnMortar);
        mEditAltOnTarget = findViewById(R.id.editAltOnTarget);
        mEditx1 = findViewById(R.id.editx1);
        mEdity1 = findViewById(R.id.edity1);
        mEditx2 = findViewById(R.id.editx2);
        mEdity2 = findViewById(R.id.edity2);
        mResultAzimute = findViewById(R.id.azimute);
        mResultRange = findViewById(R.id.range);
        mDivider = findViewById(R.id.divider_results);
        // создаем массив текстовых полей для результатов
        mResultTextViews = createTextViewList();
        // навешиваем обработчики изменений текста
        mEditAltOnMortar.addTextChangedListener(textWatcher);
        mEditAltOnTarget.addTextChangedListener(textWatcher);
        mEditx1.addTextChangedListener(textWatcher);
        mEditx2.addTextChangedListener(textWatcher);
        mEdity1.addTextChangedListener(textWatcher);
        mEdity2.addTextChangedListener(textWatcher);

        mEditx1.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // отвечает за показ клавиатуры
        imm.showSoftInput(mEditAltOnTarget, InputMethodManager.SHOW_IMPLICIT);

        mChangeTitle();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //включение стрелки назад в экшнбаре

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // установка флага для всегда включенного экрана

        if (savedInstanceState != null) {
            resultHash = (HashMap<String, Float>) savedInstanceState.getSerializable("resultHash");
            if (resultHash != null) {
                setResultViewsVisible(resultHash);
                Log.d("resultHash", resultHash.toString());
                setResultText(resultHash, chargeListCheck(resultHash));
            }

        }
        setAllTextGone();

        // проверяем хранится ли значение lightsOn в настройках
        mSettings = getSharedPreferences("calc_for_traitor_settings", Context.MODE_PRIVATE);
        if (mSettings.contains("lightsOn")) {
            mIsDisplayOn = mSettings.getBoolean("lightsOn", true);
        } else {
            changeSettings(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * привязываем xml файл меню к конкретной активити
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculate_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.d("lightIsOn-prepare", Boolean.toString(mIsDisplayOn));
        if (mIsDisplayOn) {
            menu.findItem(R.id.action_dont_off_screen).setChecked(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            menu.findItem(R.id.action_dont_off_screen).setChecked(false);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        return true;
    }

    @Override //переопределение стандартного поведения стрелки назад в экшнбаре
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * изменяет настройку "держать экран включенным" на переданное значение
     * @param value
     */
    private void changeSettings(Boolean value) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("lightsOn", value);
        editor.apply();
        mIsDisplayOn = value;
    }

    /**
     * делает невидимыми все текстовые поля
     */
    private void setAllTextInvisible() {

        mResultAzimute.animate().alpha(0f).setDuration(300);
        mResultRange.animate().alpha(0f).setDuration(300);
        mDivider.animate().alpha(0f).setDuration(300);
        for (int i = 0; i < mResultTextViews.size(); i++) {
            mResultTextViews.get(i).animate().alpha(0f).setDuration(300);
        }
    }

    /**
     * убирает все текстовые поля в карточке результатов
     */
    private void setAllTextGone() {
        setAllTextInvisible();
        mResultAzimute.setVisibility(View.GONE);
        mResultRange.setVisibility(View.GONE);
        mDivider.setVisibility(View.GONE);
        for (int i = 0; i < mResultTextViews.size(); i++) {
            mResultTextViews.get(i).setVisibility(View.GONE);
        }
    }


    /**
     * в зависимости от длины результата вычислений делает видимым нужное количество
     * полей текста
     *
     * @param inputHash - массив результатов из метода калькулятора
     */
    private void setResultViewsVisible(HashMap<String, Float> inputHash) {
        int hashLength = inputHash.size();
        mResultRange.animate().alpha(1f).setDuration(300);
        mResultAzimute.animate().alpha(1f).setDuration(300);
        mDivider.animate().alpha(1f).setDuration(300);

        mResultRange.setVisibility(View.VISIBLE);
        mResultAzimute.setVisibility(View.VISIBLE);
        mDivider.setVisibility(View.VISIBLE);
        for (int i = 0; i < hashLength - 2; i++) {
            mResultTextViews.get(i).animate().alpha(1f).setDuration(300);
            mResultTextViews.get(i).setVisibility(View.VISIBLE);
        }
    }

    /**
     * формирует список используемых charge в данный момент для правильного отображение результатов
     *
     * @param inputHash - массив результатов из метода калькулятора
     * @return ArrayList с названиями charge которые есть в массиве результатов
     */
    private ArrayList<String> chargeListCheck(HashMap<String, Float> inputHash) {
        ArrayList<String> resultList = new ArrayList<>();
        if (inputHash.containsKey("Charge 0")) {
            resultList.add("Charge 0");
        }
        if (inputHash.containsKey("Charge 1")) {
            resultList.add("Charge 1");
        }
        if (inputHash.containsKey("Charge 2")) {
            resultList.add("Charge 2");
        }
        if (inputHash.containsKey("Charge 3")) {
            resultList.add("Charge 3");
        }
        if (inputHash.containsKey("Charge 4")) {
            resultList.add("Charge 4");
        }
        if (inputHash.containsKey("Charge 5")) {
            resultList.add("Charge 5");
        }
        if (inputHash.containsKey("Charge 6")) {
            resultList.add("Charge 6");
        }
        return resultList;
    }

    /**
     * выводит результат вычислений в подготовленные текстовые поля
     *
     * @param inputHash - массив результатов из метода калькулятора
     * @param inputList - массив используемых charge
     */
    private void setResultText(HashMap<String, Float> inputHash, ArrayList<String> inputList) {
        String resultText;
        setAllTextGone();
        setResultViewsVisible(resultHash);
        mRange = Math.round(resultHash.get("range"));
        mAzimute = resultHash.get("azimute");
        mTextViewResult.setVisibility(View.GONE);
        String resultTextAzimute = getString(R.string.azimute) + " " + String.format(Locale.getDefault(), "%.2f", mAzimute);
        // соединяем строку с результатом азимута с ограничением на количество символов после запятой
        String resultTextRange = getString(R.string.range) + " " + mRange;
        mResultRange.setText(resultTextRange);
        mResultAzimute.setText(resultTextAzimute);

        for (int i = 0; i < inputList.size(); i++) {
            resultText = (inputList.get(i) + " : " + String.format(Locale.getDefault(), "%.2f", inputHash.get(inputList.get(i))));
            mResultTextViews.get(i).setText(resultText);
        }

    }

    /**
     * @return список всех текстовых полей для результатов
     */
    private ArrayList<TextView> createTextViewList() {
        ArrayList<TextView> result = new ArrayList<>();
        result.add(mResultText1 = findViewById(R.id.result1));
        result.add(mResultText2 = findViewById(R.id.result2));
        result.add(mResultText3 = findViewById(R.id.result3));
        result.add(mResultText4 = findViewById(R.id.result4));
        result.add(mResultText5 = findViewById(R.id.result5));
        result.add(mResultText6 = findViewById(R.id.result6));
        result.add(mResultText7 = findViewById(R.id.result7));
        result.add(mResultText8 = findViewById(R.id.result8));
        return result;
    }

    /**
     * метод забирает из текстовых полей значения и отправляет их в метод подсчетов
     */
    public void onEventCalculate() {
        // если поля ввода не пустые
        if (mCheckEdit()) {
            mTextViewResult.animate().alpha(1).setDuration(300);
            mTextViewResult.setVisibility(View.VISIBLE);
            setAllTextGone();
            mTextViewResult.setText(R.string.empty_fields);
        } else if (!mCheckRange()) { // проверяет попадает ли range в необходимый промежуток в выбранном калибре
            mChangeResultText();
        } else if (mCheckRange()) {
            int mSelectAltOnTarget = Integer.parseInt(mEditAltOnTarget.getText().toString()); // получаем текст из EditText
            int mSelectAltOnMortar = Integer.parseInt(mEditAltOnMortar.getText().toString());
            int mSelectx1 = Integer.parseInt(mEditx1.getText().toString());
            int mSelecty1 = Integer.parseInt(mEdity1.getText().toString());
            int mSelectx2 = Integer.parseInt(mEditx2.getText().toString());
            int mSelecty2 = Integer.parseInt(mEdity2.getText().toString());

            resultHash = Calculator.calculate(mCaliber, mSelectAltOnTarget, mSelectAltOnMortar, mSelectx1, mSelecty1, mSelectx2, mSelecty2);

//            mRange = Calculator.range_calc(mSelectx1, mSelecty1, mSelectx2, mSelecty2);
//            mAxisAngle = Calculator.axis_angle_calc(mSelectx1, mSelecty1, mSelectx2, mSelecty2);
//            mAzimute = Calculator.azimute_calc(mSelectx1, mSelecty1, mSelectx2, mSelecty2, mAxisAngle);
            setResultText(resultHash, chargeListCheck(resultHash));
        }

    }


    /**
     * при долгом нажатии на флоатинг баттон очищает все поля ввода
     */
    public void onClickClearAll() {
        mEditAltOnTarget.setText("");
        mEditAltOnMortar.setText("");
        mEditx1.setText("");
        mEdity1.setText("");
        mEditx2.setText("");
        mEdity2.setText("");
        mEditx1.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // показ клавиатуры
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * при одиночном коротком нажатии на флоатинг баттон очищает все поля цели
     */
    public void onClickClearTarget() {
        mEditAltOnTarget.setText("");
        mEditx2.setText("");
        mEdity2.setText("");
        mEditx2.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * @return проверяет пустые ли поля ввода
     */
    private boolean mCheckEdit() {
        return mEditx1.getText().toString().equals("") || mEditx2.getText().toString().equals("") || mEdity1.getText().toString().equals("") || mEdity2.getText().toString().equals("") || mEditAltOnMortar.getText().toString().equals("") || mEditAltOnTarget.getText().toString().equals("");
    }

    /**
     * изменяет заголовок в зависимости от выбранного калибра
     */
    private void mChangeTitle() {
        if (mCaliber == 120) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.mm120_description); //изменение текста заголовка
        } else if (mCaliber == 82) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.mm82_description);
        } else if (mCaliber == 30) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.d30a_description);
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.mm120_description);
        }
    }

    /**
     * если range не попадает ни в один из charge или он равен нулю выводится сообщение об ошибке
     */
    private void mChangeResultText() {
        int mSelectx1;
        int mSelecty1;
        int mSelectx2;
        int mSelecty2;
        mSelectx1 = Integer.parseInt(mEditx1.getText().toString());
        mSelecty1 = Integer.parseInt(mEdity1.getText().toString());
        mSelectx2 = Integer.parseInt(mEditx2.getText().toString());
        mSelecty2 = Integer.parseInt(mEdity2.getText().toString());
        int mRange = Calculator.range_calc(mSelectx1, mSelecty1, mSelectx2, mSelecty2);
        String mText;
        setAllTextGone();
        mTextViewResult.animate().alpha(1).setDuration(300);
        mTextViewResult.setVisibility(View.VISIBLE);
        if (mRange == 0) {
            mTextViewResult.setText(R.string.range_not_null);
        } else if (mCaliber == 120) {
            mText = (getString(R.string.wrong_range) + " " + mRange + " \n" + getString(R.string.mm120_text));
            mTextViewResult.setText(mText);
        } else if (mCaliber == 82) {
            mText = (getString(R.string.wrong_range) + " " + mRange + " \n" + getString(R.string.mm82_text));
            mTextViewResult.setText(mText);
        } else if (mCaliber == 30) {
            mText = (getString(R.string.wrong_range) + " " + mRange + " \n" + getString(R.string.d30a_text));
            mTextViewResult.setText(mText);
        } else {
            mText = (getString(R.string.wrong_range) + " " + mRange + " \n" + getString(R.string.mm120_text));
            mTextViewResult.setText(mText);
        }
    }

    /**
     * @return лежит ли range в пределах выбранного charge
     */
    private boolean mCheckRange() {
        int mSelectx1;
        int mSelecty1;
        int mSelectx2;
        int mSelecty2;
        if (!mCheckEdit()) {
            mSelectx1 = Integer.parseInt(mEditx1.getText().toString());
            mSelecty1 = Integer.parseInt(mEdity1.getText().toString());
            mSelectx2 = Integer.parseInt(mEditx2.getText().toString());
            mSelecty2 = Integer.parseInt(mEdity2.getText().toString());
        } else return false;
        return Calculator.check(mSelectx1, mSelecty1, mSelectx2, mSelecty2, mCaliber);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (resultHash != null) {
            outState.putSerializable("resultHash", resultHash);
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        //TODO переписать на использование фрагментов
        switch (id) {
            case IDD_TWO_BUTTONS:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.youtube_confirm_text)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes_button),
                                (dialog, id1) -> {
                                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.youtube_URL)));
                                    try {
                                        CalculateActivity.this.startActivity(webIntent);
                                    } catch (ActivityNotFoundException ex) {
                                    }
                                })
                        .setNegativeButton(getString(R.string.no_button),
                                (dialog, id12) -> dialog.cancel());

                return builder.create();
            default:
                return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intent = new Intent(CalculateActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_info:
                if (mCaliber == 120) {
                    Toast.makeText(CalculateActivity.this, getString(R.string.mm120_text), Toast.LENGTH_LONG).show();
                } else if (mCaliber == 82) {
                    Toast.makeText(CalculateActivity.this, getString(R.string.mm82_text), Toast.LENGTH_LONG).show();
                } else if (mCaliber == 30) {
                    Toast.makeText(CalculateActivity.this, getString(R.string.d30a_text), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_dont_off_screen:
                if (item.isChecked() && mIsDisplayOn) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // очистка флага всегда включенного экрана
                    mIsDisplayOn = false;
                    changeSettings(mIsDisplayOn);
                    item.setChecked(false);
                } else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    mIsDisplayOn = true;
                    changeSettings(mIsDisplayOn);
                    item.setChecked(true);
                }
                break;
            case R.id.action_faq:
                showDialog(IDD_TWO_BUTTONS); //TODO переписать на фрагменты
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


}
