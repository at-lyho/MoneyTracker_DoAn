package com.dut.moneytracker.models;

import android.text.TextUtils;

import com.dut.moneytracker.constant.FilterType;
import com.dut.moneytracker.currency.CurrencyUtils;
import com.dut.moneytracker.models.realms.AccountManager;
import com.dut.moneytracker.objects.Exchange;
import com.dut.moneytracker.objects.Filter;
import com.dut.moneytracker.utils.DateTimeUtils;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 15/03/2017.
 */
public class FilterManager {
    private static final String TAG = FilterManager.class.getSimpleName();
    private static FilterManager ourInstance;

    public static FilterManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new FilterManager();
        }
        return ourInstance;
    }

    private FilterManager() {
    }

    public Filter getFilterDefault() {
        Filter filter = new Filter();
        filter.setRequestByAccount(false);
        filter.setDateFilter(new Date());
        filter.setViewType(FilterType.DAY);
        return filter;
    }

    public Filter getFilterDefaultAccount(String idAccount) {
        Filter filter = new Filter();
        filter.setRequestByAccount(true);
        filter.setAccountId(idAccount);
        filter.setDateFilter(new Date());
        filter.setViewType(FilterType.DAY);
        return filter;
    }

    /**
     * @param filter
     * @param exchanges
     * @return if exchanges == null not append amount value
     */
    public String getLabel(Filter filter, List<Exchange> exchanges) {
        String amountFormat = "";
        if (exchanges != null) {
            String amount = AccountManager.getInstance().getTotalAmountByListExchange(exchanges);
            amountFormat = CurrencyUtils.getInstance().getStringMoneyType(amount, "VND");
        }
        String dateFormat = "";
        switch (filter.getViewType()) {
            case FilterType.ALL:
                dateFormat = "Tất cả";
                break;
            case FilterType.DAY:
                dateFormat = DateTimeUtils.getInstance().getStringFullDate(filter.getDateFilter());
                break;
            case FilterType.MONTH:
                dateFormat = DateTimeUtils.getInstance().getStringMonthYear(filter.getDateFilter());
                break;
            case FilterType.YEAR:
                dateFormat = DateTimeUtils.getInstance().getStringYear(filter.getDateFilter());
                break;
            case FilterType.CUSTOM:
                String fromDate = DateTimeUtils.getInstance().getStringDateUs(filter.getFormDate());
                String toDate = DateTimeUtils.getInstance().getStringDateUs(filter.getToDate());
                dateFormat = String.format(Locale.US, "%s đến %s", fromDate, toDate);
                break;
        }
        if (TextUtils.isEmpty(amountFormat)) {
            return dateFormat;
        }
        return String.format(Locale.US, "%s\n%s", dateFormat, amountFormat);
    }


    public Filter changeFilter(final Filter currentFilter, int steps) {
        Filter filter = copyFilter(currentFilter);
        int type = filter.getViewType();
        if (type == FilterType.ALL || type == FilterType.CUSTOM) {
            return filter;
        }
        Date newDate = DateTimeUtils.getInstance().changeDateStep(filter.getDateFilter(), filter.getViewType(), steps);
        filter.setDateFilter(newDate);
        return filter;
    }

    private Filter copyFilter(Filter currentFilter) {
        Filter filter = new Filter();
        filter.setAccountId(currentFilter.getAccountId());
        filter.setRequestByAccount(currentFilter.isRequestByAccount());
        filter.setViewType(currentFilter.getViewType());
        filter.setDateFilter(currentFilter.getDateFilter());
        filter.setToDate(currentFilter.getToDate());
        filter.setFormDate(currentFilter.getFormDate());
        return filter;
    }
}
