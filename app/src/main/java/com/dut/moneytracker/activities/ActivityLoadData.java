package com.dut.moneytracker.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;

import com.dut.moneytracker.R;
import com.dut.moneytracker.constant.GroupTag;
import com.dut.moneytracker.models.realms.AccountManager;
import com.dut.moneytracker.models.AppPreferences;
import com.dut.moneytracker.models.realms.CategoryManager;
import com.dut.moneytracker.models.realms.CurrencyManager;
import com.dut.moneytracker.models.realms.PaymentManager;
import com.dut.moneytracker.objects.Category;
import com.dut.moneytracker.objects.GroupCategory;
import com.dut.moneytracker.utils.ResourceUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

import io.realm.RealmList;

/**
 * Copyright@ AsianTech.Inc
 * Created by ly.ho on 04/03/2017.
 */

public class ActivityLoadData extends AppCompatActivity {
    private static final String TAG = ActivityLoadData.class.getSimpleName();
    private int idCategory = -1;
    private ProgressBar mProgressBar;
    private AccountManager mAccountManager = AccountManager.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_splash);
        initView();
        // The first load data from server
        onLoadDataServer();
        // After then
        onLoadCategory();
        onCreatePaymentType();
        onCreateDefaultAccount();
        onCreateCurrency();
        // The end start main
        AppPreferences.getInstance().setCurrentUserId(this, FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void initView() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setProgress(50);
    }

    private void onCreatePaymentType() {
        String[] payMenType = getResources().getStringArray(R.array.payment_type);
        PaymentManager.getInstance().createPaymentType(payMenType);
    }

    private void onLoadDataServer() {
        //TODO
    }

    private void onCreateCurrency() {
        CurrencyManager.getInstance().createDefaultCurrency(this);
    }

    private void onCreateDefaultAccount() {
        if (mAccountManager.getListAccount().isEmpty()) {
            mAccountManager.createDefaultAccount(this);
        }
        Log.d(TAG, "onCreateDefaultAccount: " + mAccountManager.getListAccount().size());
    }

    private void onLoadCategory() {
        if (!AppPreferences.getInstance().isInitCategory(this)) {
            createGroupCategory();
            AppPreferences.getInstance().setInitCategory(this, true);
        }
    }

    private void createGroupCategory() {
        CategoryManager mCategoryManager = CategoryManager.getInstance();
        String[] nameGroups = getResources().getStringArray(R.array.group_name);
        String[] pathGroups = getResources().getStringArray(R.array.group_path);
        int size = nameGroups.length;
        for (int i = 0; i < size; i++) {
            GroupCategory groupCategory = new GroupCategory();
            groupCategory.setId(String.valueOf(i));
            groupCategory.setName(nameGroups[i]);
            groupCategory.setByteImage(loadByteBitmap(pathGroups[i]));
            setListCategory(groupCategory, i);
            mCategoryManager.insertOrUpdate(groupCategory);
        }
    }

    private void setListCategory(GroupCategory groupCategory, int index) {
        switch (index) {
            case GroupTag.INCOME:
                setListChildCategory(groupCategory, R.array.income_name, R.array.income_path);
                break;
            case GroupTag.FOOD:
                setListChildCategory(groupCategory, R.array.food_drink_name, R.array.food_drink_path);
                break;
            case GroupTag.TRANSPORTATION:
                setListChildCategory(groupCategory, R.array.transportation_name, R.array.transportation_path);
                break;
            case GroupTag.ENTERTAINMENT:
                setListChildCategory(groupCategory, R.array.entertainment_name, R.array.entertainment_path);
                break;
            case GroupTag.HEALTH:
                setListChildCategory(groupCategory, R.array.health_name, R.array.health_path);
                break;
            case GroupTag.FAMILY:
                setListChildCategory(groupCategory, R.array.family_name, R.array.family_path);
                break;
            case GroupTag.SHOPPING:
                setListChildCategory(groupCategory, R.array.shopping_name, R.array.shopping_path);
                break;
            case GroupTag.EDUCATION:
                setListChildCategory(groupCategory, R.array.education_name, R.array.education_path);
                break;
            case GroupTag.LOVE:
                setListChildCategory(groupCategory, R.array.love_name, R.array.love_path);
                break;
            case GroupTag.OTHER:
                setListChildCategory(groupCategory, R.array.other_name, R.array.other_path);
                break;
        }
    }

    private void setListChildCategory(GroupCategory groupCategory, int idListName, int idListPath) {
        String[] name = getResources().getStringArray(idListName);
        String[] path = getResources().getStringArray(idListPath);
        Log.d(TAG, "setListChildCategory: \n" + Arrays.toString(name) + "\n" + Arrays.toString(path));
        RealmList<Category> realmList = new RealmList<>();
        int size = name.length;
        for (int i = 0; i < size; i++) {
            idCategory += 1;
            Category category = new Category();
            category.setId(String.valueOf(idCategory));
            category.setName(name[i]);
            category.setByteImage(loadByteBitmap(path[i]));
            realmList.add(category);
        }
        groupCategory.setCategories(realmList);
    }

    private byte[] loadByteBitmap(String path) {
        return ResourceUtils.getInstance().convertBitmap(getResources(), this, path);
    }

    class AsyncTaskLoadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            onLoadCategory();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}