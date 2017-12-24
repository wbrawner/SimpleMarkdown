package com.samsung.android.sdk.iap.lib.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.iap.lib.R;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.helper.HelperListenerManager;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnGetProductsDetailsListener;

public class ProductActivity extends BaseActivity
{
    @SuppressWarnings("unused")
    private static final String  TAG = ProductActivity.class.getSimpleName();

    /**
     *  Product Type
     *  Item            : 00 {@link HelperDefine#ITEM_TYPE_CONSUMABLE}
     *  Subscription    : 01 {@link HelperDefine#ITEM_TYPE_SUBSCRIPTION}
     *  All             : 10 {@link HelperDefine#ITEM_TYPE_ALL}
     */
    private String  mProductIds    = null;
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // 1. save StartNum, EndNum and ItemType passed by Intent
        // ====================================================================
        Intent intent = getIntent();
        
        if( intent != null && intent.getExtras() != null
                && intent.getExtras().containsKey("ProductIds")  )
        {
            Bundle extras = intent.getExtras();
            mProductIds    = extras.getString("ProductIds");
            mShowErrorDialog = extras.getBoolean( "ShowErrorDialog", true );
            Log.d(TAG, "onCreate: mProductIds [" + mProductIds + "]");
        }
        else
        {
            Toast.makeText( this, 
                            R.string.mids_sapps_pop_an_invalid_value_has_been_provided_for_samsung_in_app_purchase,
                            Toast.LENGTH_LONG ).show();
            
            // Set error to notify result to third-party application
            // ----------------------------------------------------------------
            mErrorVo.setError( HelperDefine.IAP_ERROR_COMMON,
                               getString(R.string.mids_sapps_pop_an_invalid_value_has_been_provided_for_samsung_in_app_purchase) );
            // ----------------------------------------------------------------
            
            finish();
            return;
        }
        // ====================================================================

        // 2. If IAP package is installed and valid,
        // bind to IAPService to load item list.
        // ====================================================================
        if( checkAppsPackage() == true )
        {
            bindIapService();
        }
        // ====================================================================
    }

    @Override
    protected void onDestroy()
    {
        super.preDestory();
        Log.d(TAG, "onDestroy: ");
        OnGetProductsDetailsListener onProductsDetailsListener
                = HelperListenerManager.getInstance().getOnGetProductsDetailsListener();
        HelperListenerManager.getInstance().setOnGetProductsDetailsListener(null);
        if( null != onProductsDetailsListener )
        {
            onProductsDetailsListener.onGetProducts(mErrorVo, mProductsDetails);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode) {
            case HelperDefine.REQUEST_CODE_IS_ENABLE_BILLING:
                if(checkAppsPackage()) {
                    bindIapService();
                }
                break;
        }
    }
    
    /**
     * If binding to IAPService is successful, this method is invoked.
     * This method loads the item list through IAPService.
     */
    protected void succeedBind()
    {
        mIapHelper.safeGetProductsDetails( ProductActivity.this,
                mProductIds,
                mShowErrorDialog );
    }
}