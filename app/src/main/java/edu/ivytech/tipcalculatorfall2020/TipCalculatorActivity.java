package edu.ivytech.tipcalculatorfall2020;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import static android.content.SharedPreferences.*;
import static android.widget.TextView.*;


public class TipCalculatorActivity extends AppCompatActivity { //implements RadioGroup.OnCheckedChangeListener {

    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;

    private EditText billAmountEditText;
    private EditText tipEditText;
    private EditText totalEditText;
    private EditText percentEditText;
    private float tipPercent = .15f;
    private String billAmountString;
    private SharedPreferences savedValues;
    private SharedPreferences prefs;
    private AutoCompleteTextView splitSpinner;
    private String[] s;
    private ArrayAdapter<String> adapter;
    private int rounding = ROUND_NONE;
    private boolean remember;
    //private RadioGroup roundingRadioGroup;
    private int split = 1;
    private EditText eachPaysEditText;
    private TextInputLayout eachPays;
    private SeekBar tipSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipcalculator);

        billAmountEditText = findViewById(R.id.billAmountEditText);
        tipEditText = findViewById(R.id.tipEditText);
        totalEditText = findViewById(R.id.totalEditText);
        percentEditText = findViewById(R.id.percentEditText);

        billAmountEditText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                if (actionID == EditorInfo.IME_ACTION_DONE || actionID == EditorInfo.IME_ACTION_UNSPECIFIED
                        || actionID == EditorInfo.IME_ACTION_NEXT) {
                    calculateAndDisplay();
                }

                return false;
            }
        });
        s = getResources().getStringArray(R.array.split_array);
        splitSpinner = findViewById(R.id.splitBill);
        adapter = new ArrayAdapter<String>(this, R.layout.dropdown_menu_popup_item,s);
        splitSpinner.setAdapter(adapter);
        splitSpinner.setText(s[0],false);
        splitSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                split = position + 1;
                calculateAndDisplay();
            }
        });

        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
        eachPays = findViewById(R.id.eachPays);
       // roundingRadioGroup = findViewById(R.id.roundingRadioGroup);
       // roundingRadioGroup.setOnCheckedChangeListener(this);
       // roundingRadioGroup.check(R.id.noRoundingRadioButton);
        //eachPays = findViewById(R.id.eachPays);
        eachPaysEditText = findViewById(R.id.eachPaysEditText);
        tipSeekBar = findViewById(R.id.tipSeekBar);
        tipSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percentEditText.setText(String.format("%d", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                tipPercent = (float) progress / 100;
                calculateAndDisplay();
            }
        });
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onPause() {
        Editor editor = savedValues.edit();
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.putInt("rounding", rounding);
        editor.putInt("split", split);
        editor.apply();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        remember = prefs.getBoolean(getResources().getString(R.string.remember_key),false);
        rounding = Integer.parseInt(prefs.getString(getResources().getString(R.string.rounding_key), "0"));
        if (remember) {
            billAmountString = savedValues.getString("billAmountString","");
            billAmountEditText.setText(billAmountString);
            tipPercent = savedValues.getFloat("tipPercent", 0.15f);
            split = savedValues.getInt("split", 1);
            if (split > 1) {
                eachPays.setVisibility(View.VISIBLE);
            }
            splitSpinner.setText(s[split - 1], false);
        } else {
            tipPercent = .15f;
        }



        int tipProgress = (int) (tipPercent * 100);
        tipSeekBar.setProgress(tipProgress);
        //rounding = savedValues.getInt("rounding", ROUND_NONE);
      /*  if (rounding == ROUND_NONE) {
            roundingRadioGroup.check(R.id.noRoundingRadioButton);
        } else if (rounding == ROUND_TIP) {
            roundingRadioGroup.check(R.id.roundTipRadioButton);
        } else if (rounding == ROUND_TOTAL) {
            roundingRadioGroup.check(R.id.roundTotalRadioButton);
        }*/

        calculateAndDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_about:
                //Toast.makeText(this, R.string.about, Toast.LENGTH_LONG).show();
                //Snackbar.make(totalEditText, R.string.about, Snackbar.LENGTH_LONG).show();
                Log.i("TipCalculator", "Clicked about button");
                //Intent about = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                break;
            case R.id.menu_settings:
                //Toast.makeText(this, R.string.about, Toast.LENGTH_LONG).show();
                //Snackbar.make(totalEditText, R.string.about, Snackbar.LENGTH_LONG).show();
                Log.i("TipCalculator", "Clicked settings button");
                //Intent about = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }
        return true;
    }

    public void calculateAndDisplay() {
        billAmountString = billAmountEditText.getText().toString();
        float billAmount;
        if (billAmountString.isEmpty()) {
            billAmount = 0;
        } else {
            billAmount = Float.parseFloat(billAmountString);
        }

        float tipAmount = 0;
        float totalAmount = 0;
        if (rounding == ROUND_NONE) {
            tipAmount = billAmount * tipPercent;
            totalAmount = billAmount + tipAmount;
        } else if (rounding == ROUND_TIP) {
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = billAmount + tipAmount;
        } else if (rounding == ROUND_TOTAL) {
            tipAmount = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipAmount);
            tipAmount = totalAmount - billAmount;
        }


        if (split == 1) {
            eachPays.setVisibility(View.GONE);

        } else {
            eachPays.setVisibility(View.VISIBLE);
            float splitAmount = totalAmount / split;
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        NumberFormat percent = NumberFormat.getPercentInstance();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        symbols.setPercent('\0');
        ((DecimalFormat) currency).setDecimalFormatSymbols(symbols);
        ((DecimalFormat) percent).setDecimalFormatSymbols(symbols);

        if (split == 1) {
            eachPays.setVisibility(View.GONE);
        } else {
            eachPays.setVisibility(View.VISIBLE);
            float splitAmount = totalAmount / split;
            eachPaysEditText.setText(currency.format(splitAmount));
        }

        tipEditText.setText(currency.format(tipAmount));
        totalEditText.setText(currency.format(totalAmount));
        percentEditText.setText(percent.format(tipPercent));
    }

   public void changePercent(View v) {
        switch(v.getId()) {
            case R.id.calculateButton:
                calculateAndDisplay();
                break;
            case R.id.clearButton:
                billAmountEditText.setText("");
                tipPercent = .15f;
                percentEditText.setText(R.string.default_percent);
                tipSeekBar.setProgress(15);
              //  roundingRadioGroup.clearCheck();
            //    rounding = ROUND_NONE;
              //  roundingRadioGroup.check(R.id.noRoundingRadioButton);
                tipEditText.setText(R.string.default_number);
                splitSpinner.setText(s[0], false);
                split = 1;
                eachPaysEditText.setText(R.string.default_number);
                eachPays.setVisibility(View.GONE);
                totalEditText.setText(R.string.default_number);
        }
    }
/*
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.noRoundingRadioButton: rounding = ROUND_NONE; break;
            case R.id.roundTipRadioButton: rounding = ROUND_TIP; break;
            case R.id.roundTotalRadioButton: rounding = ROUND_TOTAL; break;
        }

        calculateAndDisplay();
    }*/
}