package com.dut.moneytracker.models.realms;

import com.dut.moneytracker.objects.ExchangeLooper;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 03/04/2017.
 */

public class ExchangeLoopManager extends RealmHelper {
    private static ExchangeLoopManager exchangeLoopManager;

    public static ExchangeLoopManager getInstance() {
        if (exchangeLoopManager == null) {
            exchangeLoopManager = new ExchangeLoopManager();
        }
        return exchangeLoopManager;
    }

    public List<ExchangeLooper> getListLoopExchange() {
        realm.beginTransaction();
        RealmResults<ExchangeLooper> realmResults = realm.where(ExchangeLooper.class).findAll();
        realmResults.sort("created", Sort.ASCENDING);
        List<ExchangeLooper> exchangeLoopers = realmResults.subList(0, realmResults.size());
        realm.commitTransaction();
        return exchangeLoopers;
    }

    public void deleteExchangeLoopById(String id) {
        realm.beginTransaction();
        ExchangeLooper exchangeLooper = realm.where(ExchangeLooper.class).like("id", id).findFirst();
        exchangeLooper.deleteFromRealm();
        realm.commitTransaction();
    }
}
