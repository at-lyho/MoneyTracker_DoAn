package com.dut.moneytracker.ui.exchanges;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.moneytracker.R;
import com.dut.moneytracker.constant.ExchangeType;
import com.dut.moneytracker.constant.IntentCode;
import com.dut.moneytracker.currency.CurrencyExpression;
import com.dut.moneytracker.dialogs.DialogCalculator;
import com.dut.moneytracker.dialogs.DialogPickAccount;
import com.dut.moneytracker.maps.GoogleLocation;
import com.dut.moneytracker.models.realms.AccountManager;
import com.dut.moneytracker.models.realms.ExchangeManger;
import com.dut.moneytracker.objects.Account;
import com.dut.moneytracker.objects.Category;
import com.dut.moneytracker.objects.Exchange;
import com.dut.moneytracker.ui.category.ActivityPickCategory_;
import com.dut.moneytracker.ui.interfaces.AddListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 23/02/2017.
 */
@EActivity(R.layout.activity_add_exchange)
@OptionsMenu(R.menu.menu_add_exchange)
public class ActivityAddExchange extends AppCompatActivity implements AddListener {
    @ViewById(R.id.toolbar)
    Toolbar mToolbar;
    @ViewById(R.id.tvAmount)
    TextView tvAmount;
    @ViewById(R.id.btnTransfer)
    Button btnTransfer;
    @ViewById(R.id.btnIncome)
    Button btnIncome;
    @ViewById(R.id.btnExpenses)
    Button btnExpenses;
    @ViewById(R.id.tvAccountName)
    TextView tvAccountName;
    @ViewById(R.id.tvCategoryName)
    TextView tvCategoryName;
    @ViewById(R.id.tvStatus)
    TextView tvStatus;
    @ViewById(R.id.tvTitleFromAccount)
    TextView tvTitleFromAccount;
    @ViewById(R.id.tvTitleToAccount)
    TextView tvTitleToAccount;
    //Model
    @Extra
    Account mAccount;
    private DialogPickAccount mDialogPickAccount;
    private Exchange mExchange;
    private String mNameAccountTransfer;
    private GoogleLocation mGoogleLocation;
    private boolean isClickTabIncome;
    private boolean isClickTabExpense;

    @AfterViews
    void init() {
        mDialogPickAccount = DialogPickAccount.getInstance();
        initDataExchange();
        initView();
        initGoogleLocation();
    }

    private void initGoogleLocation() {
        mGoogleLocation = new GoogleLocation(getApplicationContext());
        mGoogleLocation.connectApiGoogle();
    }

    private void initView() {
        mToolbar.setNavigationIcon(R.drawable.ic_close_white);
        setTitle(getString(R.string.activity_add_exchange_title));
        setSupportActionBar(mToolbar);
        btnExpenses.setAlpha(1f);
        btnIncome.setAlpha(0.5f);
        btnTransfer.setAlpha(0.5f);
        tvAccountName.setText(mAccount.getName());
        mNameAccountTransfer = getString(R.string.unknown);
    }

    private void initDataExchange() {
        mExchange = new Exchange();
        mExchange.setId(UUID.randomUUID().toString());
        mExchange.setTypeExchange(ExchangeType.EXPENSES);
        mExchange.setIdAccount(mAccount.getId());
    }

    @OptionsItem(android.R.id.home)
    void onClose() {
        finish();
    }

    @OptionsItem(R.id.actionAdd)
    void onSave() {
        switch (mExchange.getTypeExchange()) {
            case ExchangeType.INCOME:
            case ExchangeType.EXPENSES:
                onAddExpensesOrIncome();
                break;
            case ExchangeType.TRANSFER:
                onAddTransfer();
                break;
        }
    }

    @Click(R.id.tvCal0)
    void onClickTvCalZero() {
        onSetValueAmount("0");
    }

    @Click(R.id.tvCal1)
    void onClickTvCalOne() {
        onSetValueAmount("1");
    }

    @Click(R.id.tvCal2)
    void onClickTvCalTwo() {
        onSetValueAmount("2");
    }

    @Click(R.id.tvCal3)
    void onClickTvCalThree() {
        onSetValueAmount("3");
    }

    @Click(R.id.tvCal4)
    void onClickTvCalFour() {
        onSetValueAmount("4");
    }

    @Click(R.id.tvCal5)
    void onClickTvCalFive() {
        onSetValueAmount("5");
    }

    @Click(R.id.tvCal6)
    void onClickTvCalSix() {
        onSetValueAmount("6");
    }

    @Click(R.id.tvCal7)
    void onClickTvCalSeven() {
        onSetValueAmount("7");
    }

    @Click(R.id.tvCal8)
    void onClickTvCalEight() {
        onSetValueAmount("8");
    }

    @Click(R.id.tvCal9)
    void onClickTvCalNight() {
        onSetValueAmount("9");
    }

    @Click(R.id.tvCalDot)
    void onClickTvCalDot() {
        onSetValueAmount(".");
    }

    @Click(R.id.imgCalBack)
    void onClickBackNumber() {
        String current = tvAmount.getText().toString();
        if (!TextUtils.isEmpty(current)) {
            tvAmount.setText(current.substring(0, current.length() - 1));
        }
    }

    @Click(R.id.llAccount)
    void onClickPickAccount() {
        mDialogPickAccount.show(getFragmentManager(), null);
        mDialogPickAccount.registerPickAccount(new DialogPickAccount.AccountListener() {
            @Override
            public void onResultAccount(Account account) {
                mExchange.setIdAccount(account.getId());
                tvAccountName.setText(account.getName());
            }
        }, false, mExchange.getIdAccountTransfer());
    }

    @Click(R.id.llCategory)
    void onClickPickCategory() {
        if (mExchange.getTypeExchange() == ExchangeType.TRANSFER) {
            showDialogPickAccountReceive();
        } else {
            showActivityPickCategory();
        }
    }

    @Click(R.id.tvMoreAdd)
    void onClickMoreAddExchange() {
        String textAmount = tvAmount.getText().toString().trim();
        if (TextUtils.isEmpty(textAmount)) {
            textAmount = "0";
        }
        if (mExchange.getTypeExchange() == ExchangeType.EXPENSES) {
            mExchange.setAmount(String.format(Locale.US, "-%s", textAmount));
        } else {
            mExchange.setAmount(textAmount);
        }
        ActivityAddMoreExchange_.intent(this).mExchange(mExchange).startForResult(IntentCode.MORE_ADD);
    }

    @Click(R.id.btnIncome)
    void onClickTabIncome() {
        if (isClickTabIncome) {
            return;
        }
        isClickTabIncome = true;
        isClickTabExpense = false;
        mExchange.setTypeExchange(ExchangeType.INCOME);
        mExchange.setIdCategory(null);
        tvTitleFromAccount.setText(getString(R.string.name_wallet));
        tvTitleToAccount.setText(getString(R.string.category_name));
        tvCategoryName.setText(getString(R.string.unknown));
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("+");
        btnExpenses.setAlpha(0.5f);
        btnIncome.setAlpha(1f);
        btnTransfer.setAlpha(0.5f);
    }

    @Click(R.id.btnExpenses)
    void onClickTabExpenses() {
        if (isClickTabExpense) {
            return;
        }
        isClickTabExpense = true;
        isClickTabIncome = false;
        mExchange.setTypeExchange(ExchangeType.EXPENSES);
        mExchange.setIdCategory(null);
        tvTitleFromAccount.setText(getString(R.string.name_wallet));
        tvTitleToAccount.setText(getString(R.string.category_name));
        tvCategoryName.setText(getString(R.string.unknown));
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("-");
        btnExpenses.setAlpha(1f);
        btnIncome.setAlpha(0.5f);
        btnTransfer.setAlpha(0.5f);
    }

    @Click(R.id.btnTransfer)
    void onClickTabTransfer() {
        isClickTabExpense = false;
        isClickTabIncome = false;
        mExchange.setTypeExchange(ExchangeType.TRANSFER);
        tvStatus.setVisibility(View.GONE);
        tvTitleFromAccount.setText(getString(R.string.account_send));
        tvTitleToAccount.setText(getString(R.string.account_receive));
        tvCategoryName.setText(mNameAccountTransfer);
        btnExpenses.setAlpha(0.5f);
        btnIncome.setAlpha(0.5f);
        btnTransfer.setAlpha(1f);
    }


    @Override
    public void onAddExpensesOrIncome() {
        if (!isAvailableIncomeAndExpense()) {
            return;
        }
        String textAmount = tvAmount.getText().toString().trim();
        if (mExchange.getTypeExchange() == ExchangeType.EXPENSES) {
            mExchange.setAmount(String.format(Locale.US, "-%s", textAmount));
        } else {
            mExchange.setAmount(textAmount);
        }
        onRequestExchangePlace();
    }

    @Override
    public void onAddTransfer() {
        if (!isAvailableTransfer()) {
            return;
        }
        onRequestExchangePlace();
    }

    @Override
    public void onSaveDataBase() {
        if (null == mExchange.getCreated()) {
            mExchange.setCreated(new Date());
        }
        if (mExchange.getTypeExchange() == ExchangeType.TRANSFER) {
            String codeTransfer = UUID.randomUUID().toString();
            // Them giao dich account gui
            String amount = String.format(Locale.US, "-%s", tvAmount.getText().toString());
            mExchange.setAmount(amount);
            mExchange.setCodeTransfer(codeTransfer);
            ExchangeManger.getInstance().insertOrUpdate(mExchange);
            // Them giao dich account nhan
            String idTransfer = mExchange.getIdAccountTransfer();
            if (!TextUtils.equals(idTransfer, AccountManager.ID_OUTSIDE)) {
                String idAccount = mExchange.getIdAccount();
                mExchange.setId(UUID.randomUUID().toString());
                mExchange.setAmount(tvAmount.getText().toString());
                mExchange.setIdAccount(idTransfer);
                mExchange.setIdAccountTransfer(idAccount);
                mExchange.setCodeTransfer(codeTransfer);
                ExchangeManger.getInstance().insertOrUpdate(mExchange);
            }
        } else {
            ExchangeManger.getInstance().insertOrUpdate(mExchange);
        }
        setResult(IntentCode.ADD_NEW_EXCHANGE);
        finish();
    }

    private void showDialogPickAccountReceive() {
        mDialogPickAccount.show(getFragmentManager(), null);
        mDialogPickAccount.registerPickAccount(new DialogPickAccount.AccountListener() {
            @Override
            public void onResultAccount(Account account) {
                mNameAccountTransfer = account.getName();
                tvCategoryName.setText(mNameAccountTransfer);
                mExchange.setIdAccountTransfer(account.getId());
            }
        }, true, mExchange.getIdAccount());
    }


    private void showActivityPickCategory() {
        ActivityPickCategory_.intent(this).mType(mExchange.getTypeExchange()).startForResult(IntentCode.PICK_CATEGORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case IntentCode.PICK_CATEGORY:
                Category category = data.getParcelableExtra(getString(R.string.extra_category));
                tvCategoryName.setText(category.getName());
                mExchange.setIdCategory(category.getId());
                break;
            case IntentCode.MORE_ADD:
                mExchange = data.getParcelableExtra(getString(R.string.extra_more_add));
                break;
        }
    }

    private void onSetValueAmount(String chart) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tvAmount.getText().toString());
        stringBuilder.append(chart);
        if (CurrencyExpression.getInstance().isValidateTypeMoney(stringBuilder.toString())) {
            if (stringBuilder.toString().length() > DialogCalculator.MAX) {
                return;
            }
            tvAmount.setText(stringBuilder.toString());
        }
    }

    private void onRequestExchangePlace() {
        if (mAccount.isSaveLocation()) {
            if ((mExchange.getLatitude() == 0 && mExchange.getLongitude() == 0) || TextUtils.isEmpty(mExchange.getAddress())) {
                onSetPlaceExchange();
            }
        }
        onSaveDataBase();
    }

    public boolean isAvailableTransfer() {
        if (TextUtils.isEmpty(mExchange.getIdAccountTransfer())) {
            Toast.makeText(this, "Chọn tài khoản nhận", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(tvAmount.getText().toString())) {
            Toast.makeText(this, "Nhập số tiền", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean isAvailableIncomeAndExpense() {
        String textAmount = tvAmount.getText().toString().trim();
        if (TextUtils.isEmpty(textAmount)) {
            Toast.makeText(this, "Fill the amount!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mExchange.getIdCategory())) {
            Toast.makeText(this, "Please pick category", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void onSetPlaceExchange() {
        Location location = mGoogleLocation.getCurrentLocation();
        mGoogleLocation.stopLocationUpDate();
        mGoogleLocation.disConnectApiGoogle();
        if (null == location) {
            return;
        }
        Geocoder mGeocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            StringBuilder builder = new StringBuilder();
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                int max = address.getMaxAddressLineIndex();
                for (int i = 0; i < max; i++) {
                    if (i == max - 1) {
                        builder.append(address.getAddressLine(i)).append("\n");
                    } else {
                        builder.append(address.getAddressLine(i)).append(", ");
                    }
                }
                builder.append(address.getCountryName());
            }
            mExchange.setAddress(builder.toString());
            mExchange.setLatitude(location.getLatitude());
            mExchange.setLongitude(location.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
