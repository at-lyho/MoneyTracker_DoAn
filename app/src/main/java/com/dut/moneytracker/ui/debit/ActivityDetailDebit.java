package com.dut.moneytracker.ui.debit;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.moneytracker.R;
import com.dut.moneytracker.constant.DebitType;
import com.dut.moneytracker.currency.CurrencyUtils;
import com.dut.moneytracker.dialogs.DialogCalculator;
import com.dut.moneytracker.dialogs.DialogConfirm;
import com.dut.moneytracker.dialogs.DialogInput;
import com.dut.moneytracker.dialogs.DialogInput_;
import com.dut.moneytracker.dialogs.DialogPickAccount;
import com.dut.moneytracker.models.realms.AccountManager;
import com.dut.moneytracker.models.realms.DebitManager;
import com.dut.moneytracker.objects.Account;
import com.dut.moneytracker.objects.Debit;
import com.dut.moneytracker.utils.DateTimeUtils;
import com.dut.moneytracker.view.DayPicker;
import com.dut.moneytracker.view.DayPicker_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.Date;
import java.util.Locale;

/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 13/04/2017.
 */
@EActivity(R.layout.activity_add_debit)
@OptionsMenu(R.menu.menu_detail_exchange)
public class ActivityDetailDebit extends AppCompatActivity {
    @ViewById(R.id.toolbar)
    Toolbar mToolbar;
    @ViewById(R.id.tvName)
    TextView mTvName;
    @ViewById(R.id.tvAmount)
    TextView mTvAmount;
    @ViewById(R.id.tvDescriptionDebit)
    TextView tvDescriptionDebit;
    @ViewById(R.id.tvAccount)
    TextView mTvAccountName;
    @ViewById(R.id.tvStartDate)
    TextView mTvStartDate;
    @ViewById(R.id.tvEndDate)
    TextView mTvEndDate;
    @ViewById(R.id.spinnerDebit)
    AppCompatSpinner mSpinnerDebit;
    @ViewById(R.id.tvType)
    TextView mTvTypeDebit;
    @Extra
    Debit mDebit;
    private int idLastDebit;
    private DialogPickAccount mDialogPickAccount;
    private DialogCalculator mDialogCalculator;
    private DialogInput mDialogInput;

    @AfterViews
    void init() {
        idLastDebit = mDebit.getId();
        innitDialog();
        initToolbar();
        loadView();
    }

    private void innitDialog() {
        mDialogCalculator = DialogCalculator.getInstance();
        mDialogPickAccount = DialogPickAccount.getInstance();
        mDialogInput = DialogInput.getInstance();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_close_white);
        setTitle(getString(R.string.toobar_detail_debit));
    }

    private void loadView() {
        mSpinnerDebit.setVisibility(View.GONE);
        mTvTypeDebit.setVisibility(View.VISIBLE);
        mTvAccountName.setText(AccountManager.getInstance().getAccountNameById(mDebit.getIdAccount()));
        mTvStartDate.setText(DateTimeUtils.getInstance().getStringDateUs(mDebit.getStartDate()));
        mTvEndDate.setText(DateTimeUtils.getInstance().getStringDateUs(mDebit.getEndDate()));
        mTvName.setText(mDebit.getName());
        tvDescriptionDebit.setText(mDebit.getDescription());
        mTvAmount.setText(CurrencyUtils.getInstance().getStringMoneyFormat(mDebit.getAmount(), CurrencyUtils.DEFAULT_CURRENCY_CODE));
        onChangeAmount(mDebit.getTypeDebit());
        if (mDebit.getTypeDebit() == DebitType.LEND) {
            mTvTypeDebit.setText(R.string.debit_lend);
        } else {
            mTvTypeDebit.setText(R.string.debit_broved);
        }

    }

    @OptionsItem(android.R.id.home)
    void onCLickClose() {
        finish();
    }

    @OptionsItem(R.id.actionDelete)
    void onClickDelete() {
        DialogConfirm.getInstance().setMessage(getString(R.string.dialog_delete_debit));
        DialogConfirm.getInstance().registerClickListener(new DialogConfirm.ClickListener() {
            @Override
            public void onClickResult(boolean value) {
                if (value) {
                    DebitManager.getInstance().deleteDebitById(mDebit.getId());
                    finish();
                }
            }
        });
        DialogConfirm.getInstance().show(getSupportFragmentManager(), null);
    }

    @OptionsItem(R.id.actionSave)
    void onClickSave() {
        if (TextUtils.isEmpty(mDebit.getAmount())) {
            Toast.makeText(this, "Nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mDebit.getName())) {
            Toast.makeText(this, "Nhhập tên", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mDebit.getIdAccount())) {
            Toast.makeText(this, "Chọn tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!DateTimeUtils.getInstance().isSameDate(mDebit.getStartDate(), mDebit.getEndDate())) {
            if (mDebit.getStartDate().after(mDebit.getEndDate())) {
                Toast.makeText(this, "Nhập sai ngày", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        //Debit
        if (idLastDebit != mDebit.getId()) {
            DebitManager.getInstance().updateDebitIfAccountChange(mDebit);
        } else {
            DebitManager.getInstance().insertOrUpdateDebit(mDebit);
        }
        DebitManager.getInstance().genExchangeFromDebit(mDebit, null);
        finish();
    }

    @Click(R.id.rlName)
    void onClickName() {
        if (mDebit.isClose()) {
            return;
        }
        mDialogInput.register(new DialogInput.DescriptionListener() {
            @Override
            public void onResult(String content) {
                mDebit.setName(content);
                mTvName.setText(content);
            }
        });
        mDialogInput.initValue(mDebit.getName());
        mDialogInput.show(getSupportFragmentManager(), null);
    }

    @Click(R.id.rlAmount)
    void onCLickAmount() {
        if (mDebit.isClose()) {
            return;
        }
        String amount = mDebit.getAmount();
        if (amount.startsWith("-")) {
            amount = amount.substring(1);
        }
        mDialogCalculator.show(getFragmentManager(), null);
        mDialogCalculator.setAmount(amount);
        mDialogCalculator.registerResultListener(new DialogCalculator.ResultListener() {
            @Override
            public void onResult(String amount) {
                if (mDebit.getTypeDebit() == DebitType.BORROWED) {
                    mDebit.setAmount(amount);
                } else {
                    mDebit.setAmount(String.format(Locale.US, "-%s", amount));
                }
                mTvAmount.setText(CurrencyUtils.getInstance().getStringMoneyFormat(mDebit.getAmount(), CurrencyUtils.DEFAULT_CURRENCY_CODE));
            }
        });
    }

    private void onChangeAmount(int type) {
        String amount = mDebit.getAmount();
        if (TextUtils.isEmpty(amount)) {
            return;
        }
        if (amount.startsWith("-")) {
            amount = amount.substring(1);
        }
        if (type == DebitType.BORROWED) {
            mDebit.setAmount(amount);
            mTvAmount.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            mTvAmount.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            mDebit.setAmount(String.format(Locale.US, "-%s", amount));
        }
        mTvAmount.setText(CurrencyUtils.getInstance().getStringMoneyFormat(mDebit.getAmount(), CurrencyUtils.DEFAULT_CURRENCY_CODE));
    }

    @Click(R.id.rlDescription)
    void onCLickDescription() {
        if (mDebit.isClose()) {
            return;
        }
        DialogInput dialogInput = DialogInput_.builder().build();
        dialogInput.register(new DialogInput.DescriptionListener() {
            @Override
            public void onResult(String content) {
                mDebit.setDescription(content);
                tvDescriptionDebit.setText(content);
            }
        });
        dialogInput.show(getSupportFragmentManager(), null);
    }

    @Click(R.id.rlAccount)
    void onCLickAccount() {
        if (mDebit.isClose()) {
            return;
        }
        mDialogPickAccount.registerPickAccount(new DialogPickAccount.AccountListener() {
            @Override
            public void onResultAccount(Account account) {
                mDebit.setIdAccount(account.getId());
                mTvAccountName.setText(account.getName());
            }
        }, false, mDebit.getIdAccount());
        mDialogPickAccount.show(getFragmentManager(), null);
    }

    @Click(R.id.rlStartDate)
    void onClickDate() {
        if (mDebit.isClose()) {
            return;
        }
        DayPicker dayPicker = DayPicker_.builder().build();
        dayPicker.registerPicker(new DayPicker.DatePickerListener() {
            @Override
            public void onResultDate(Date date) {
                mDebit.setStartDate(date);
                mTvStartDate.setText(DateTimeUtils.getInstance().getStringDateUs(date));
            }
        });
        dayPicker.show(getSupportFragmentManager(), null);
    }

    @Click(R.id.rlEndDate)
    void onClickEndDate() {
        if (mDebit.isClose()) {
            return;
        }
        DayPicker dayPicker = DayPicker_.builder().build();
        dayPicker.registerPicker(new DayPicker.DatePickerListener() {
            @Override
            public void onResultDate(Date date) {
                mDebit.setEndDate(date);
                mTvEndDate.setText(DateTimeUtils.getInstance().getStringDateUs(date));
            }
        });
        dayPicker.show(getSupportFragmentManager(), null);
    }
}
