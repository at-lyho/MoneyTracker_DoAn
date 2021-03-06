package com.dut.moneytracker.dialogs;

import android.support.v4.app.DialogFragment;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.moneytracker.R;
import com.dut.moneytracker.utils.DateTimeUtils;
import com.dut.moneytracker.view.DayPicker;
import com.dut.moneytracker.view.DayPicker_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 27/03/2017.
 */
@EFragment(R.layout.dialog_custom_filter)
public class DialogCustomFilter extends DialogFragment {
    @ViewById(R.id.tvToDate)
    TextView mTvToDate;
    @ViewById(R.id.tvFromDate)
    TextView mTvFromDate;
    FilterListener mFilterListener;
    private DayPicker mDayPicker;
    private Date mFromDate;
    private Date mToDate;

    @AfterViews
    void init() {
        if (mFromDate == null || mToDate == null) {
            mFromDate = DateTimeUtils.getInstance().getStartTimeOfDay(new Date());
            mToDate = DateTimeUtils.getInstance().getEndTimeOfDay(mFromDate);
        }
        mTvFromDate.setText(DateTimeUtils.getInstance().getStringFullDateVn(mFromDate));
        mTvToDate.setText(DateTimeUtils.getInstance().getStringFullDateVn(mToDate));
    }

    public interface FilterListener {
        void onResultDate(Date fromDate, Date toDate);
    }

    public void registerFilterListener(FilterListener filterListener, Date lastFromDate, Date lastToDate) {
        mFilterListener = filterListener;
        mFromDate = lastFromDate;
        mToDate = lastToDate;
    }

    @Click(R.id.tvFromDate)
    void onCLickFromDate() {
        mDayPicker = DayPicker_.builder().build();
        mDayPicker.show(getChildFragmentManager(), null);
        mDayPicker.registerPicker(new DayPicker.DatePickerListener() {
            @Override
            public void onResultDate(Date date) {
                mFromDate = DateTimeUtils.getInstance().getStartTimeOfDay(date);
                mTvFromDate.setText(DateTimeUtils.getInstance().getStringFullDateVn(mFromDate));
            }
        });

    }

    @Click(R.id.tvToDate)
    void onCLickToDate() {
        mDayPicker = DayPicker_.builder().build();
        mDayPicker.show(getChildFragmentManager(), null);
        mDayPicker.registerPicker(new DayPicker.DatePickerListener() {
            @Override
            public void onResultDate(Date date) {
                mToDate = DateTimeUtils.getInstance().getEndTimeOfDay(date);
                mTvToDate.setText(DateTimeUtils.getInstance().getStringFullDateVn(mToDate));
            }
        });
    }

    @Click(R.id.tvConfirm)
    void onClickConfirm() {
        if (!DateTimeUtils.getInstance().isValidateFromDateToDate(mFromDate, mToDate)) {
            Toast.makeText(getContext(), "Date not validate!", Toast.LENGTH_SHORT).show();
            return;
        }
        mFilterListener.onResultDate(mFromDate, mToDate);
        dismiss();
    }

    @Click(R.id.tvCancel)
    void onClickCancel() {
        dismiss();
    }
}
