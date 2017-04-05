package com.dut.moneytracker.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dut.moneytracker.R;
import com.dut.moneytracker.constant.FilterType;
import com.dut.moneytracker.constant.RequestCode;
import com.dut.moneytracker.constant.ResultCode;
import com.dut.moneytracker.dialogs.DialogCustomFilter;
import com.dut.moneytracker.dialogs.DialogCustomFilter_;
import com.dut.moneytracker.dialogs.DialogPickFilter;
import com.dut.moneytracker.models.FilterManager;
import com.dut.moneytracker.models.realms.AccountManager;
import com.dut.moneytracker.objects.Account;
import com.dut.moneytracker.objects.Filter;
import com.dut.moneytracker.ui.account.ActivityEditAccount;
import com.dut.moneytracker.ui.base.SpinnerAccountManger;
import com.dut.moneytracker.ui.charts.income.FragmentIncomeChartPager;
import com.dut.moneytracker.ui.charts.income.FragmentIncomeChartPager_;
import com.dut.moneytracker.ui.dashboard.FragmentDashboard;
import com.dut.moneytracker.ui.dashboard.FragmentDashboard_;
import com.dut.moneytracker.ui.exchangeloop.FragmentLoopExchange;
import com.dut.moneytracker.ui.exchangeloop.FragmentLoopExchange_;
import com.dut.moneytracker.ui.exchanges.FragmentExchangesPager;
import com.dut.moneytracker.ui.exchanges.FragmentExchangesPager_;
import com.dut.moneytracker.ui.interfaces.MainListener;
import com.dut.moneytracker.ui.user.LoginActivity;
import com.dut.moneytracker.ui.user.UserInformationActivity;
import com.dut.moneytracker.view.CircleImageView;
import com.google.firebase.auth.FirebaseAuth;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

import static com.dut.moneytracker.ui.MainActivity.FragmentTag.DEFAULT;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MainListener {
    public enum FragmentTag {
        DEFAULT, DASHBOARD, EXCHANGES, EXCHANGE_LOOPS, PROFILE, CHART_INCOME
    }

    FragmentTag mFragmentTag = DEFAULT;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DASHBOARD = "DASHBOARD";
    private static final String EXCHANGES = "EXCHANGES";
    private static final String LOOP = "EXCHANGE_LOOPS";
    private static final String CHART_INCOME = "CHART_INCOME";
    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @ViewById(R.id.imgUserLogo)
    CircleImageView imgUserLogo;
    @ViewById(R.id.tvUserName)
    TextView tvUserName;
    @ViewById(R.id.tvEmail)
    TextView tvEmail;
    @ViewById(R.id.toolbar)
    Toolbar mToolbar;
    @ViewById(R.id.spinner)
    AppCompatSpinner spinner;
    @ViewById(R.id.imgDateFilter)
    ImageView imgDateFilter;
    // Navigation
    @ViewById(R.id.llDashBoard)
    LinearLayout llDashboard;
    @ViewById(R.id.rlProfile)
    RelativeLayout mRlProfile;
    @ViewById(R.id.llRecode)
    LinearLayout llRecode;
    @ViewById(R.id.llDefaultExchange)
    LinearLayout llDefaultExchange;
    @ViewById(R.id.imgSettingAccount)
    ImageView imgSettingAccount;
    private DialogPickFilter mDialogPickFilter;
    private DialogCustomFilter mDialogCustomFilter;
    //model
    FragmentManager mFragmentManager = getSupportFragmentManager();
    FragmentDashboard mFragmentDashboard;
    FragmentLoopExchange mFragmentLoopExchange;
    FragmentExchangesPager mFragmentExchangesPager;
    FragmentIncomeChartPager mFragmentIncomeChartPager;
    SpinnerAccountManger mSpinnerAccount;
    private Account mAccount;

    @AfterViews
    void init() {
        mFilter = FilterManager.getInstance().getFilterDefault();
        mDialogPickFilter = new DialogPickFilter();
        mDialogCustomFilter = DialogCustomFilter_.builder().build();
        initView();
        onLoadProfile();
        onLoadFragmentDashboard();
        mToolbar.setTitle(getString(R.string.main_account));
    }

    private Filter mFilter;


    void initView() {
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                switch (mFragmentTag) {
                    case DASHBOARD:
                        onLoadFragmentDashboard();
                        break;
                    case EXCHANGES:
                        onLoadFragmentAllExchanges();
                        break;
                    case EXCHANGE_LOOPS:
                        onLoadFragmentLoopExchange();
                        break;
                    case CHART_INCOME:
                        onLoadFragmentChartIncome();
                        break;
                    case PROFILE:
                        startActivityForResult(new Intent(MainActivity.this, UserInformationActivity.class), RequestCode.PROFILE);
                        break;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mFragmentTag = DEFAULT;
            }
        };
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        initSpinnerFilter();
    }

    // Change filer
    private void initSpinnerFilter() {
        mSpinnerAccount = new SpinnerAccountManger(this, spinner);
        mSpinnerAccount.registerSelectedItem(new SpinnerAccountManger.ItemSelectedListener() {
            @Override
            public void onItemSelected(String accountId) {
                if (TextUtils.isEmpty(accountId)) {
                    mFilter.setRequestByAccount(false);
                } else {
                    mFilter.setRequestByAccount(true);
                    mFilter.setAccountId(accountId);
                }
                onChangeFilter();
            }
        });
    }

    private void onShowDialogPickFilterDate() {
        mDialogPickFilter.show(getFragmentManager(), TAG);
        mDialogPickFilter.registerFilter(mFilter.getViewType(), new DialogPickFilter.FilterListener() {
            @Override
            public void onResult(int idFilter) {
                mFilter.setViewType(idFilter);
                mFilter.setDateFilter(new Date());
                onChangeFilter();
            }
        });
    }

    public void registerAccount(Account account) {
        mAccount = account;
    }

    void onLoadProfile() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        Glide.with(this).load(firebaseAuth.getCurrentUser().getPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .skipMemoryCache(true)
                .into(imgUserLogo);
        tvUserName.setText(String.valueOf(firebaseAuth.getCurrentUser().getDisplayName()));
        tvEmail.setText(String.valueOf(firebaseAuth.getCurrentUser().getEmail()));
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Click(R.id.imgDateFilter)
    void onClickFilter() {
        onShowDialogPickFilterDate();
    }

    @Click(R.id.rlProfile)
    void onCLickProfile() {
        mFragmentTag = FragmentTag.PROFILE;
        onCloseNavigation();
    }

    @Click(R.id.llDashBoard)
    void onClickDashBoard() {
        mFragmentTag = FragmentTag.DASHBOARD;
        onCloseNavigation();
    }

    @Click(R.id.llRecode)
    void onClickRecodes() {
        mFragmentTag = FragmentTag.EXCHANGES;
        onCloseNavigation();
    }

    @Click(R.id.llDefaultExchange)
    void onClickDefault() {
        mFragmentTag = FragmentTag.EXCHANGE_LOOPS;
        onCloseNavigation();
    }

    @Click(R.id.imgSettingAccount)
    void onSettingAccount() {
        startActivityEditAccount();
    }

    @Click(R.id.llChartIncome)
    void onClickChartIncome() {
        mFragmentTag = FragmentTag.CHART_INCOME;
        onCloseNavigation();
    }

    private void onCloseNavigation() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case ResultCode.PROFILE:
                onResultLogout();
                break;
            case ResultCode.EDIT_ACCOUNT:
                onResultEditAccount(data);
                break;
        }
    }

    void startActivityEditAccount() {
        Intent intent = new Intent(this, ActivityEditAccount.class);
        intent.putExtra(getString(R.string.extra_account), mAccount);
        startActivityForResult(intent, RequestCode.EDIT_ACCOUNT);
    }

    void onResultEditAccount(Intent data) {
        Account account = data.getParcelableExtra(getString(R.string.extra_account));
        AccountManager.getInstance().insertOrUpdate(account);
        mFragmentDashboard.notifyDataSetChanged();
    }

    void onResultLogout() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    public void onLoadFragmentChartIncome() {
        mFilter = FilterManager.getInstance().getFilterDefault();
        mSpinnerAccount.setSelectItem(null);
        mFragmentIncomeChartPager = FragmentIncomeChartPager_.builder().mFilter(mFilter).build();
        requestReplaceFragment(mFragmentIncomeChartPager, CHART_INCOME, true);
    }

    public void onLoadFragmentAllExchanges() {
        mFilter = FilterManager.getInstance().getFilterDefault();
        mSpinnerAccount.setSelectItem(null);
        onLoadFragmentExchange(mFilter);
    }

    public void onLoadFragmentAllExchangesByAccount(String idAccount) {
        mFilter = FilterManager.getInstance().getFilterDefaultAccount(idAccount);
        mSpinnerAccount.setSelectItem(idAccount);
        onLoadFragmentExchange(mFilter);
    }

    private void onLoadFragmentExchange(Filter filter) {
        mFragmentExchangesPager = FragmentExchangesPager_.builder().mFilter(filter).build();
        requestReplaceFragment(mFragmentExchangesPager, EXCHANGES, true);
    }

    public void onLoadFragmentDashboard() {
        mFragmentDashboard = FragmentDashboard_.builder().build();
        requestReplaceFragment(mFragmentDashboard, DASHBOARD, false);
    }

    public void onLoadFragmentLoopExchange() {
        mFragmentLoopExchange = FragmentLoopExchange_.builder().build();
        requestReplaceFragment(mFragmentLoopExchange, LOOP, true);
    }

    private void requestReplaceFragment(Fragment fragment, String TAG, boolean isStack) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameContent, fragment, TAG);
        if (isStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    // Change View
    @Override
    public boolean checkFragmentDashboard() {
        Fragment fragment = mFragmentManager.findFragmentByTag(DASHBOARD);
        if (fragment instanceof FragmentDashboard) {
            spinner.setVisibility(View.GONE);
            imgDateFilter.setVisibility(View.GONE);
            imgSettingAccount.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkFragmentExchanges() {
        Fragment fragment = mFragmentManager.findFragmentByTag(EXCHANGES);
        if (fragment instanceof FragmentExchangesPager) {
            spinner.setVisibility(View.VISIBLE);
            imgDateFilter.setVisibility(View.VISIBLE);
            imgSettingAccount.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkFragmentLoop() {
        Fragment fragment = mFragmentManager.findFragmentByTag(LOOP);
        if (fragment instanceof FragmentLoopExchange) {
            spinner.setVisibility(View.GONE);
            imgDateFilter.setVisibility(View.GONE);
            imgSettingAccount.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @Override
    public void onChangeFilter() {
        if (mFragmentExchangesPager == null) {
            return;
        }
        if (mFilter.getViewType() == FilterType.CUSTOM) {
            mDialogCustomFilter.show(mFragmentManager, TAG);
            mDialogCustomFilter.registerFilterListener(new DialogCustomFilter.FilterListener() {
                @Override
                public void onResultDate(Date fromDate, Date toDate) {
                    mFilter.setFormDate(fromDate);
                    mFilter.setToDate(toDate);
                    mFragmentExchangesPager.onReloadFragmentPager();
                }
            });
        } else {
            mFragmentExchangesPager.onReloadFragmentPager();
        }
    }
}
