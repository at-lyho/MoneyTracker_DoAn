package com.dut.moneytracker.ui.exchanges;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dut.moneytracker.R;
import com.dut.moneytracker.ui.category.ActivityPickCategory;
import com.dut.moneytracker.ui.interfaces.DetailExchangeListener;
import com.dut.moneytracker.constant.RequestCode;
import com.dut.moneytracker.constant.ResultCode;
import com.dut.moneytracker.currency.CurrencyUtils;
import com.dut.moneytracker.dialogs.DialogCalculator;
import com.dut.moneytracker.dialogs.DialogConfirm;
import com.dut.moneytracker.dialogs.DialogConfirm_;
import com.dut.moneytracker.dialogs.DialogInput;
import com.dut.moneytracker.dialogs.DialogInput_;
import com.dut.moneytracker.models.realms.AccountManager;
import com.dut.moneytracker.models.realms.CategoryManager;
import com.dut.moneytracker.models.realms.ExchangeManger;
import com.dut.moneytracker.constant.ExchangeType;
import com.dut.moneytracker.objects.Category;
import com.dut.moneytracker.objects.Exchange;
import com.dut.moneytracker.objects.Place;
import com.dut.moneytracker.utils.DateTimeUtils;
import com.dut.moneytracker.utils.DialogUtils;
import com.dut.moneytracker.view.DayPicker;
import com.dut.moneytracker.view.TimePicker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
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

/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 12/03/2017.
 */
@EActivity(R.layout.activity_detail_exchange)
@OptionsMenu(R.menu.menu_detail_exchange)
public class ActivityDetailExchange extends AppCompatActivity implements DetailExchangeListener, OnMapReadyCallback {
    static final String TAG = ActivityDetailExchange.class.getSimpleName();
    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.tvExchangeName)
    TextView tvExchangeName;
    @ViewById(R.id.tvCategoryName)
    TextView tvCategoryName;
    @ViewById(R.id.tvAmount)
    TextView tvAmount;
    @ViewById(R.id.tvAccount)
    TextView tvAccount;
    @ViewById(R.id.tvCurrency)
    TextView tvCurrency;
    @ViewById(R.id.tvDescription)
    TextView mTvDescription;
    @ViewById(R.id.tvDate)
    TextView tvDate;
    @ViewById(R.id.tvTime)
    TextView tvTime;
    @ViewById(R.id.mapView)
    MapView mapView;
    @ViewById(R.id.tvTitleCategory)
    TextView mTvTitleCategory;
    @ViewById(R.id.rlCategory)
    RelativeLayout rlCategory;
    @ViewById(R.id.rlAccount)
    RelativeLayout rlAccount;
    @ViewById(R.id.tvTitleAccount)
    TextView tvTitleAccount;
    @ViewById(R.id.imgEditCategory)
    ImageView imgEditCategory;
    @ViewById(R.id.imgEditAccount)
    ImageView imgEditAccount;
    @Extra
    Exchange mExchange;
    //GoogleMap
    Place mPlace;
    GoogleMap mGoogleMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        setTitle(R.string.toolbar_detail_exchange);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        mPlace = mExchange.getPlace() != null ? mExchange.getPlace() : new Place();
        onShowDetailExchange();
        mapView.onCreate(new Bundle());
        mapView.getMapAsync(this);
    }

    @OptionsItem(android.R.id.home)
    void onClickHomeBack() {
        finish();
    }

    @OptionsItem(R.id.actionSave)
    void onClickSave() {
        onChangeExchange();
        finish();
    }

    @OptionsItem(R.id.actionDelete)
    void onClickDelete() {
        DialogConfirm dialogConfirm = DialogConfirm_.builder().build();
        dialogConfirm.setMessage(getString(R.string.dialog_confirm_delete_title));
        dialogConfirm.show(getSupportFragmentManager(), TAG);
        dialogConfirm.registerClickListener(new DialogConfirm.ClickListener() {
            @Override
            public void onClickResult(boolean value) {
                if (value) {
                    ExchangeManger.getInstance().deleteExchangeById(mExchange.getId());
                    setResult(ResultCode.DELETE_EXCHANGE);
                    finish();
                }
            }
        });
    }

    @Click(R.id.rlLocation)
    void onClickLocation() {
        onRequestPermissionMap();
    }

    @Click(R.id.rlCategory)
    void onClickPickCategory() {
        if (mExchange.getTypeExchange() == ExchangeType.TRANSFER) {
            return;
        }
        startActivityForResult(new Intent(this, ActivityPickCategory.class), RequestCode.PICK_CATEGORY);
    }

    @Click(R.id.rlAmount)
    void onClickAmount() {
        String amount = mExchange.getAmount();
        if (amount.startsWith("-")) {
            amount = amount.substring(1);
        }
        DialogCalculator dialogCalculator = new DialogCalculator();
        dialogCalculator.show(getFragmentManager(), null);
        dialogCalculator.setAmount(amount);
        dialogCalculator.registerResultListener(new DialogCalculator.ResultListener() {
            @Override
            public void onResult(String amount) {
                if (mExchange.getAmount().startsWith("-")) {
                    mExchange.setAmount("-" + amount);
                } else {
                    mExchange.setAmount(amount);
                }
                tvAmount.setText(CurrencyUtils.getInstance().getStringMoneyType(mExchange.getAmount(), mExchange.getCurrencyCode()));
            }
        });
    }

    @Click(R.id.rlDescription)
    void onClickDescription() {
        DialogInput dialogInput = DialogInput_.builder().build();
        dialogInput.register(new DialogInput.DescriptionListener() {
            @Override
            public void onResult(String content) {
                mTvDescription.setText(content);
                mExchange.setDescription(content);
            }
        });
        dialogInput.show(getSupportFragmentManager(), null);
    }

    @Click(R.id.rlDate)
    void onClickDate() {
        DayPicker dayPicker = new DayPicker();
        dayPicker.registerPicker(new DayPicker.DatePickerListener() {
            @Override
            public void onResultDate(Date date) {
                tvDate.setText(DateTimeUtils.getInstance().getStringFullDate(mExchange.getCreated()));
                mExchange.setCreated(date);
            }
        });
        dayPicker.show(getSupportFragmentManager(), null);
    }

    @Click(R.id.rlTime)
    void onClickTime() {
        TimePicker timePicker = new TimePicker();
        timePicker.registerPicker(new TimePicker.TimePickerListener() {
            @Override
            public void onResultHour(int hour) {
                Date date = mExchange.getCreated();
                Date newDate = DateTimeUtils.getInstance().setHours(date, hour);
                mExchange.setCreated(newDate);
            }

            @Override
            public void onResultMinute(int minute) {
                Date date = mExchange.getCreated();
                Date newDate = DateTimeUtils.getInstance().setMinute(date, minute);
                mExchange.setCreated(newDate);
            }

            @Override
            public void onResultStringTime(String time) {
                tvTime.setText(time);
            }
        });
        timePicker.show(getSupportFragmentManager(), null);
    }

    @Click(R.id.rlAccount)
    void onCLickAccount() {
        if (mExchange.getTypeExchange() == ExchangeType.TRANSFER) {
            return;
        }
        //TODO
    }

    void showDialogPickPlace() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), RequestCode.PICK_PLACE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        DialogUtils.getInstance().showProgressDialog(this, "Loading");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.PICK_PLACE) {
            DialogUtils.getInstance().dismissProgressDialog();
            if (resultCode == RESULT_OK) {
                com.google.android.gms.location.places.Place place = PlacePicker.getPlace(data, this);
                onSetExchangePlace(place);
                updateMap();
            }
        }

        if (requestCode == RequestCode.PICK_CATEGORY) {
            if (resultCode == ResultCode.PICK_CATEGORY) {
                Category category = data.getParcelableExtra(getString(R.string.extra_category));
                String idCategory = category.getId();
                String nameCategory = category.getName();
                tvCategoryName.setText(nameCategory);
                mExchange.setIdCategory(idCategory);
            }
        }
    }

    void onSetExchangePlace(com.google.android.gms.location.places.Place place) {
        if (place == null) {
            return;
        }
        if (place.getName() != null) {
            mPlace.setName(place.getName().toString());
        }
        if (place.getAddress() != null) {
            mPlace.setAddress(place.getAddress().toString());
        }
        mPlace.setLatitude(place.getLatLng().latitude);
        mPlace.setLongitude(place.getLatLng().longitude);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestCode.PERMISSION_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showDialogPickPlace();
                }
            }

        }
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
                                ActivityCompat.requestPermissions(ActivityDetailExchange.this,
                                        new String[]{Manifest.permission
                                                .ACCESS_FINE_LOCATION},
                                        RequestCode.PERMISSION_LOCATION);
                            }
                        }).show();

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        RequestCode.PERMISSION_LOCATION);
            }
        } else {
            showDialogPickPlace();
        }
    }

    @Override
    public void onShowDetailExchange() {
        switch (mExchange.getTypeExchange()) {
            case ExchangeType.INCOME:
            case ExchangeType.EXPENSES:
                showDetailTypeIncomeAndExpenses();
                break;
            case ExchangeType.TRANSFER:
                showDetailTypeTransfer();
                break;
        }
    }

    @Override
    public void onChangeExchange() {
        mExchange.setPlace(mPlace);
        ExchangeManger.getInstance().insertOrUpdate(mExchange);
        setResult(ResultCode.DETAIL_EXCHANGE);
    }

    private void showDetailTypeIncomeAndExpenses() {
        Category category = CategoryManager.getInstance().getCategoryById(mExchange.getIdCategory());
        tvCategoryName.setText(category.getName());
        tvAmount.setText(CurrencyUtils.getInstance().getStringMoneyType(mExchange.getAmount(), mExchange.getCurrencyCode()));
        mTvDescription.setText(mExchange.getDescription());
        tvCurrency.setText(String.valueOf(mExchange.getCurrencyCode()));
        String nameAccount = AccountManager.getInstance().getAccountNameById(mExchange.getIdAccount());
        tvAccount.setText(String.valueOf(nameAccount));
        tvDate.setText(DateTimeUtils.getInstance().getStringFullDate(mExchange.getCreated()));
        tvTime.setText(DateTimeUtils.getInstance().getStringTime(mExchange.getCreated()));
        if (!mExchange.getAmount().startsWith("-")) {
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        if (mExchange.getTypeExchange() == ExchangeType.INCOME) {
            tvExchangeName.setText(R.string.exchange_name_income);
        } else {
            tvExchangeName.setText(R.string.exchange_name_expense);
        }
    }

    private void showDetailTypeTransfer() {
        imgEditAccount.setVisibility(View.GONE);
        imgEditCategory.setVisibility(View.GONE);
        tvExchangeName.setText(R.string.exchange_name_transfer);
        mTvTitleCategory.setText(R.string.account_send);
        tvTitleAccount.setText(R.string.account_recever);
        mTvDescription.setText(mExchange.getDescription());
        tvCurrency.setText(String.valueOf(mExchange.getCurrencyCode()));
        String amount = mExchange.getAmount();
        if (amount.startsWith("-")) {
            String accountSend = AccountManager.getInstance().getAccountNameById(mExchange.getIdAccount());
            tvCategoryName.setText(accountSend);
            String accountReceiver = AccountManager.getInstance().getAccountNameById(mExchange.getIdAccountTransfer());
            tvAccount.setText(accountReceiver);
        } else {
            String accountSend = AccountManager.getInstance().getAccountNameById(mExchange.getIdAccountTransfer());
            tvCategoryName.setText(accountSend);
            String accountReceiver = AccountManager.getInstance().getAccountNameById(mExchange.getIdAccount());
            tvAccount.setText(accountReceiver);
        }
        tvAmount.setText(CurrencyUtils.getInstance().getStringMoneyType(mExchange.getAmount(), mExchange.getCurrencyCode()));
        tvDate.setText(DateTimeUtils.getInstance().getStringFullDate(mExchange.getCreated()));
        tvTime.setText(DateTimeUtils.getInstance().getStringTime(mExchange.getCreated()));
        if (!mExchange.getAmount().startsWith("-")) {
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }


    void onTargetLocationExchange() {
        LatLng sydney = new LatLng(mPlace.getLatitude(), mPlace.getLongitude());
        mGoogleMap.addMarker(new MarkerOptions().position(sydney).title(mPlace.getAddress()));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f));
    }

    void updateMap() {
        mGoogleMap.clear();
        onTargetLocationExchange();
        mapView.refreshDrawableState();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        onTargetLocationExchange();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}