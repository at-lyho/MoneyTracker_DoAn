package com.dut.moneytracker.ui.dashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.dut.moneytracker.R;
import com.dut.moneytracker.adapter.ClickItemListener;
import com.dut.moneytracker.adapter.ClickItemRecyclerView;
import com.dut.moneytracker.adapter.ExchangeTabAdapter;
import com.dut.moneytracker.constant.ExchangeType;
import com.dut.moneytracker.constant.IntentCode;
import com.dut.moneytracker.currency.CurrencyUtils;
import com.dut.moneytracker.models.realms.AccountManager;
import com.dut.moneytracker.models.realms.ExchangeManger;
import com.dut.moneytracker.objects.Account;
import com.dut.moneytracker.objects.Exchange;
import com.dut.moneytracker.ui.MainActivity;
import com.dut.moneytracker.ui.base.BaseFragment;
import com.dut.moneytracker.ui.charts.objects.LineChartMoney;
import com.dut.moneytracker.ui.charts.objects.ValueLineChart;
import com.dut.moneytracker.ui.exchanges.ActivityDetailExchange_;
import com.github.mikephil.charting.charts.LineChart;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;


/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 06/03/2017.
 */
@EFragment(R.layout.fragment_tab_account)
public class FragmentChildTab extends BaseFragment implements TabAccountListener, RealmChangeListener<RealmResults<Exchange>> {
    //View
    @ViewById(R.id.recyclerExchange)
    RecyclerView mRecyclerExchange;
    @ViewById(R.id.tvAmount)
    TextView mTvAmount;
    @ViewById(R.id.cardViewCardAccount)
    CardView mCardView;
    @ViewById(R.id.lineChart)
    LineChart mLineChart;
    @FragmentArg
    Account mAccount;
    private Handler mHandler = new Handler();
    private RealmResults<Exchange> mExchanges;
    private ExchangeTabAdapter mExchangeAdapter;
    private LineChartMoney mLineChartMoney;
    private int positionItem;
    private BroadcastReceiver mReceiverAddNewExchange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isAdded()) {
                onLoadChart();
                onShowAmount();
            }
        }
    };

    @AfterViews
    void init() {
        mLineChartMoney = new LineChartMoney(getContext(), mLineChart);
        mCardView.setVisibility(View.GONE);
        onShowAmount();
        onLoadChart();
        onLoadExchanges();
    }

    @Override
    public void onResume() {
        getContext().registerReceiver(mReceiverAddNewExchange, new IntentFilter(getString(R.string.receiver_add_new_exchange)));
        super.onResume();
    }

    @Override
    public void onLoadChart() {
        mLineChartMoney.setColorChart(mAccount.getColorHex());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                List<ValueLineChart> mValueLineCharts = ExchangeManger.getInstance().getValueChartByDailyDay(mAccount.getId(), FragmentDashboard.LIMIT_DAY_CHART);
                mLineChartMoney.updateNewValueLineChart(mValueLineCharts);
                mLineChartMoney.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onShowAmount() {
        mTvAmount.setTextColor(Color.parseColor(mAccount.getColorHex()));
        String money = CurrencyUtils.getInstance().getStringMoneyFormat(AccountManager.getInstance().getAmountAvailableByAccount(mAccount.getId()), CurrencyUtils.DEFAULT_CURRENCY_CODE);
        mTvAmount.setText(money);
    }

    @Override
    public void onLoadExchanges() {
        mExchanges = ExchangeManger.getInstance().onLoadExchangeAsyncByAccount(mAccount.getId(), FragmentDashboard.LIMIT_DAY_EXCHANGE);
        mExchangeAdapter = new ExchangeTabAdapter(getContext(), mExchanges);
        mRecyclerExchange.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerExchange.setNestedScrollingEnabled(false);
        mRecyclerExchange.setAdapter(mExchangeAdapter);
        mRecyclerExchange.addOnItemTouchListener(new ClickItemRecyclerView(getContext(), new ClickItemListener() {
            @Override
            public void onClick(View view, int position) {
                positionItem = position;
                onShowDetailExchange(mExchanges.get(position));
            }
        }));
        mExchanges.addChangeListener(this);
    }

    @Override
    public void onChange(RealmResults<Exchange> element) {
        if (mExchangeAdapter != null) {
            mExchangeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onShowDetailExchange(Exchange exchange) {
        ActivityDetailExchange_.intent(FragmentChildTab.this).mExchange(exchange).startForResult(IntentCode.DETAIL_EXCHANGE);
    }

    @OnActivityResult(IntentCode.DETAIL_EXCHANGE)
    void onResult(int resultCode, Intent data) {
        switch (resultCode) {
            case IntentCode.EDIT_EXCHANGE:
                Exchange exchangeEdit = data.getParcelableExtra(getString(R.string.extra_detail_exchange));
                if (exchangeEdit.getTypeExchange() == ExchangeType.TRANSFER && !TextUtils.equals(exchangeEdit.getIdAccountTransfer(), AccountManager.ID_OUTSIDE)) {
                    ExchangeManger.getInstance().updateExchangeTransfer(exchangeEdit);
                } else {
                    ExchangeManger.getInstance().insertOrUpdate(exchangeEdit);
                }
                break;
            case IntentCode.DELETE_EXCHANGE:
                Exchange exchangeDelete = (Exchange) mExchangeAdapter.getItem(positionItem);
                if (exchangeDelete.getTypeExchange() == ExchangeType.TRANSFER && !TextUtils.equals(exchangeDelete.getIdAccountTransfer(), AccountManager.ID_OUTSIDE)) {
                    ExchangeManger.getInstance().deleteExchangeTransfer(exchangeDelete.getCodeTransfer());
                } else {
                    ExchangeManger.getInstance().deleteExchangeById(exchangeDelete.getId());
                }
        }

        //Reload tab account
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onLoadChart();
                onShowAmount();
            }
        });
    }

    @Click(R.id.tvMoreExchange)
    void onClickMoreExchange() {
        ((MainActivity) getActivity()).onLoadFragmentExchangesByAccount(mAccount.getId());
    }
}
