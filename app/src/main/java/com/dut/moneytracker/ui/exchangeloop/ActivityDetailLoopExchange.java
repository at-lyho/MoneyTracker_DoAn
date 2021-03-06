package com.dut.moneytracker.ui.exchangeloop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.moneytracker.R;
import com.dut.moneytracker.constant.ExchangeType;
import com.dut.moneytracker.constant.IntentCode;
import com.dut.moneytracker.currency.CurrencyUtils;
import com.dut.moneytracker.dialogs.DialogCalculator;
import com.dut.moneytracker.dialogs.DialogConfirm;
import com.dut.moneytracker.dialogs.DialogInput;
import com.dut.moneytracker.dialogs.DialogPickAccount;
import com.dut.moneytracker.models.realms.AccountManager;
import com.dut.moneytracker.models.realms.CategoryManager;
import com.dut.moneytracker.models.realms.ExchangeLoopManager;
import com.dut.moneytracker.objects.Account;
import com.dut.moneytracker.objects.Category;
import com.dut.moneytracker.objects.ExchangeLooper;
import com.dut.moneytracker.ui.base.SpinnerTypeLoopManger;
import com.dut.moneytracker.ui.category.ActivityPickCategory_;
import com.dut.moneytracker.utils.DateTimeUtils;
import com.dut.moneytracker.utils.DialogUtils;
import com.dut.moneytracker.view.DayPicker;
import com.dut.moneytracker.view.DayPicker_;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
 * Created by ly.ho on 19/03/2017.
 */
@EActivity(R.layout.activity_add_loop_exchange)
@OptionsMenu(R.menu.menu_detail_exchange)
public class ActivityDetailLoopExchange extends AppCompatActivity implements OnMapReadyCallback {
    @ViewById(R.id.toolbar)
    Toolbar mToolbar;
    @ViewById(R.id.tvTabIncome)
    TextView tvTabIncome;
    @ViewById(R.id.tvTabExpense)
    TextView tvTabExpense;
    @ViewById(R.id.tvCategoryName)
    TextView tvCategoryName;
    @ViewById(R.id.tvAmount)
    TextView mTvAmount;
    @ViewById(R.id.tvDescription)
    TextView tvDescription;
    @ViewById(R.id.tvDate)
    TextView tvDate;
    @ViewById(R.id.tvAccount)
    TextView tvAccount;
    @ViewById(R.id.spinnerTypeLoop)
    AppCompatSpinner mAppCompatSpinner;
    @ViewById(R.id.switchLoop)
    SwitchCompat switchCompat;
    @ViewById(R.id.tvAddress)
    TextView tvAddress;
    @Extra
    ExchangeLooper mExchangeLoop;
    private GoogleMap mGoogleMap;
    private SpinnerTypeLoopManger mSpinnerTypeLoopManger;
    private DialogCalculator mDialogCalculator;
    private DialogPickAccount mDialogPickAccount;
    private DialogInput mDialogInput;
    private boolean isClickTabIncome;
    private boolean isClickTabExpense;

    @AfterViews
    void init() {
        initToolbar();
        initDialog();
        initSpinner();
        onShowData();
        initMap();
    }

    private void initToolbar() {
        setTitle(getString(R.string.detail_exchange_loop));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_close_white);
    }

    private void initDialog() {
        mDialogPickAccount = DialogPickAccount.getInstance();
        mDialogCalculator = DialogCalculator.getInstance();
        mDialogInput = DialogInput.getInstance();
    }

    private void initSpinner() {
        mSpinnerTypeLoopManger = new SpinnerTypeLoopManger(this, mAppCompatSpinner);
        mSpinnerTypeLoopManger.registerSelectedItem(new SpinnerTypeLoopManger.ItemSelectedListener() {
            @Override
            public void onResultTypeLoop(int type) {
                mExchangeLoop.setTypeLoop(type);
            }
        });
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mExchangeLoop.setLoop(isChecked);
            }
        });
    }

    private void onShowData() {
        switchCompat.setChecked(mExchangeLoop.isLoop());
        mSpinnerTypeLoopManger.setSelectItem(mExchangeLoop.getTypeLoop());
        Category category = CategoryManager.getInstance().getCategoryById(mExchangeLoop.getIdCategory());
        tvCategoryName.setText(category.getName());
        mTvAmount.setText(CurrencyUtils.getInstance().getStringMoneyFormat(mExchangeLoop.getAmount(), CurrencyUtils.DEFAULT_CURRENCY_CODE));
        switch (mExchangeLoop.getTypeExchange()) {
            case ExchangeType.INCOME:
                loadViewTabIncome();
                isClickTabIncome = true;
                break;
            case ExchangeType.EXPENSES:
                loadViewTabExpense();
                isClickTabExpense = true;
                break;
        }
        tvDescription.setText(mExchangeLoop.getDescription());
        tvAccount.setText(AccountManager.getInstance().getAccountNameById(mExchangeLoop.getIdAccount()));
        tvDate.setText(DateTimeUtils.getInstance().getStringDateUs(mExchangeLoop.getCreated()));
    }

    private void initMap() {
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
    }

    @OptionsItem(android.R.id.home)
    void onClickHomeBack() {
        finish();
    }

    @OptionsItem(R.id.actionSave)
    void onClickSave() {
        if (TextUtils.isEmpty(mExchangeLoop.getAmount())) {
            Toast.makeText(this, R.string.input_money, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mExchangeLoop.getIdAccount())) {
            Toast.makeText(this, R.string.input_account, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mExchangeLoop.getIdCategory())) {
            Toast.makeText(this, getString(R.string.input_category), Toast.LENGTH_SHORT).show();
            return;
        }
        ExchangeLoopManager.getInstance().insertOrUpdate(mExchangeLoop);
        finish();
    }

    @OptionsItem(R.id.actionDelete)
    void onClickDelete() {
        DialogConfirm.getInstance().setMessage(getString(R.string.delete_exchange));
        DialogConfirm.getInstance().registerClickListener(new DialogConfirm.ClickListener() {
            @Override
            public void onClickResult(boolean value) {
                if (value) {
                    ExchangeLoopManager.getInstance().deleteExchangeLoopById(mExchangeLoop.getId());
                    finish();
                }
            }
        });
        DialogConfirm.getInstance().show(getSupportFragmentManager(), null);
    }

    @Click(R.id.tvTabIncome)
    void onClickTabIncome() {
        if (isClickTabIncome) {
            return;
        }
        isClickTabIncome = true;
        isClickTabExpense = false;
        mExchangeLoop.setIdCategory(null);
        tvCategoryName.setText("");
        mExchangeLoop.setTypeExchange(ExchangeType.INCOME);
        loadViewTabIncome();
    }

    private void loadViewTabIncome() {
        mTvAmount.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        tvTabIncome.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        tvTabIncome.setBackgroundResource(R.color.colorPrimary);
        tvTabExpense.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        tvTabExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.color_background_tab_unselect));
        if (mExchangeLoop.getAmount().startsWith("-")) {
            mExchangeLoop.setAmount(mExchangeLoop.getAmount().substring(1));
        }
        mTvAmount.setText(CurrencyUtils.getInstance().getStringMoneyFormat(mExchangeLoop.getAmount(), CurrencyUtils.DEFAULT_CURRENCY_CODE));
    }

    private void loadViewTabExpense() {
        mTvAmount.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        tvTabExpense.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        tvTabExpense.setBackgroundResource(R.color.colorPrimary);
        tvTabIncome.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        tvTabIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.color_background_tab_unselect));
        if (!mExchangeLoop.getAmount().startsWith("-")) {
            mExchangeLoop.setAmount(String.format(Locale.US, "-%s", mExchangeLoop.getAmount()));
        }
        mTvAmount.setText(CurrencyUtils.getInstance().getStringMoneyFormat(mExchangeLoop.getAmount(), CurrencyUtils.DEFAULT_CURRENCY_CODE));
    }

    @Click(R.id.tvTabExpense)
    void onClickTabExpense() {
        if (isClickTabExpense) {
            return;
        }
        isClickTabIncome = false;
        isClickTabExpense = true;
        mExchangeLoop.setIdCategory(null);
        tvCategoryName.setText("");
        mExchangeLoop.setTypeExchange(ExchangeType.EXPENSES);
        loadViewTabExpense();
    }

    @Click(R.id.rlCategory)
    void onClickCategory() {
        ActivityPickCategory_.intent(this).mType(mExchangeLoop.getTypeExchange()).startForResult(IntentCode.PICK_CATEGORY);
    }

    @Click(R.id.rlAmount)
    void onCLickAmount() {
        String amount = mExchangeLoop.getAmount() == null ? "" : mExchangeLoop.getAmount();
        if (amount.startsWith("-")) {
            amount = amount.substring(1);
        }
        mDialogCalculator.show(getFragmentManager(), null);
        mDialogCalculator.setAmount(amount);
        mDialogCalculator.registerResultListener(new DialogCalculator.ResultListener() {
            @Override
            public void onResult(String amount) {
                if (mExchangeLoop.getTypeExchange() == ExchangeType.INCOME) {
                    mExchangeLoop.setAmount(amount);
                } else {
                    mExchangeLoop.setAmount(String.format(Locale.US, "-%s", amount));
                }
                mTvAmount.setText(CurrencyUtils.getInstance().getStringMoneyFormat(mExchangeLoop.getAmount(), "VND"));
            }
        });
    }

    @Click(R.id.rlDescription)
    void onCLickDescription() {
        mDialogInput.register(new DialogInput.DescriptionListener() {
            @Override
            public void onResult(String content) {
                mExchangeLoop.setDescription(content);
                tvDescription.setText(content);
            }
        });
        mDialogInput.initValue(mExchangeLoop.getDescription());
        mDialogInput.show(getSupportFragmentManager(), null);
    }

    @Click(R.id.rlAccount)
    void onCLickAccount() {
        mDialogPickAccount.registerPickAccount(new DialogPickAccount.AccountListener() {
            @Override
            public void onResultAccount(Account account) {
                mExchangeLoop.setIdAccount(account.getId());
                tvAccount.setText(account.getName());
            }
        }, false, mExchangeLoop.getIdAccount());
        mDialogPickAccount.show(getFragmentManager(), null);
    }

    @Click(R.id.rlDate)
    void onClickDate() {
        DayPicker dayPicker = DayPicker_.builder().build();
        dayPicker.registerPicker(new DayPicker.DatePickerListener() {
            @Override
            public void onResultDate(Date date) {
                mExchangeLoop.setCreated(date);
                tvDate.setText(DateTimeUtils.getInstance().getStringDateUs(date));
            }
        });
        dayPicker.show(getSupportFragmentManager(), null);
    }

    @Click(R.id.rlLocation)
    void onClickLocation() {
        onRequestPermissionMap();
    }

    public void onRequestPermissionMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Please Grant Permissions",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(ActivityDetailLoopExchange.this,
                                        new String[]{Manifest.permission
                                                .ACCESS_FINE_LOCATION},
                                        IntentCode.PERMISSION_LOCATION);
                            }
                        }).show();

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        IntentCode.PERMISSION_LOCATION);
            }
        } else {
            showDialogPickPlace();
        }
    }

    private void showDialogPickPlace() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), IntentCode.PICK_PLACE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        DialogUtils.getInstance().showProgressDialog(this, "Loading");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentCode.PICK_CATEGORY) {
            if (resultCode == IntentCode.PICK_CATEGORY) {
                Category category = data.getParcelableExtra(getString(R.string.extra_category));
                String idCategory = category.getId();
                String nameCategory = category.getName();
                tvCategoryName.setText(nameCategory);
                mExchangeLoop.setIdCategory(idCategory);
            }
        }
        if (requestCode == IntentCode.PICK_PLACE) {
            DialogUtils.getInstance().dismissProgressDialog();
            if (resultCode == RESULT_OK) {
                com.google.android.gms.location.places.Place place = PlacePicker.getPlace(data, this);
                onSetExchangePlace(place);
                updateMap();
            }
        }
    }

    private void updateMap() {
        mGoogleMap.clear();
        onTargetLocationExchange();
    }

    private void onTargetLocationExchange() {
        LatLng sydney = new LatLng(mExchangeLoop.getLatitude(), mExchangeLoop.getLongitude());
        mGoogleMap.addMarker(new MarkerOptions().position(sydney));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, getResources().getInteger(R.integer.zoom_map)));
        tvAddress.setText(mExchangeLoop.getAddress());
    }

    private void onSetExchangePlace(com.google.android.gms.location.places.Place place) {
        if (place == null) {
            return;
        }
        String address = String.format(Locale.US, "%s\n%s", place.getName() != null ? place.getName() : "", place.getAddress() != null ? place.getAddress() : "");
        mExchangeLoop.setAddress(address);
        mExchangeLoop.setLatitude(place.getLatLng().latitude);
        mExchangeLoop.setLongitude(place.getLatLng().longitude);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        onTargetLocationExchange();
    }
}
