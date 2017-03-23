package com.dut.moneytracker.activities;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dut.moneytracker.R;

/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 16/03/2017.
 */

public class SpinnerTypeLoopManger implements Spinner.OnItemSelectedListener {
    private AppCompatSpinner mSpinner;
    private Context mContext;
    private String[] items = new String[]{"Day", "Week", "Month", "Year"};
    private int[] listId = new int[]{0, 1, 2, 3};

    public interface ItemSelectedListener {
        void onResultTypeLoop(int type);
    }

    private ItemSelectedListener itemSelectedListener;

    public void registerSelectedItem(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    public SpinnerTypeLoopManger(Context context, AppCompatSpinner spinner) {
        mSpinner = spinner;
        mContext = context;
        onLoadItemSpinner();
    }


    private void onLoadItemSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, R.layout.support_simple_spinner_dropdown_item, items);
        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        itemSelectedListener.onResultTypeLoop(listId[position]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setSelectItem(int type) {
        mSpinner.setSelection(type);
    }
}
