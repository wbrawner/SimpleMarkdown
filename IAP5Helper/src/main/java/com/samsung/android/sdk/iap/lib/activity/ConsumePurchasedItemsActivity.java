package com.samsung.android.sdk.iap.lib.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.iap.lib.R;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.helper.HelperListenerManager;
import com.samsung.android.sdk.iap.lib.helper.HelperUtil;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener;

public class ConsumePurchasedItemsActivity extends BaseActivity
{
    @SuppressWarnings("unused")
    private static final String  TAG = ConsumePurchasedItemsActivity.class.getSimpleName();

    /**
     *  Product Type
     *  Item            : 00 {@link HelperDefine#ITEM_TYPE_CONSUMABLE}
     *  Subscription    : 01 {@link HelperDefine#ITEM_TYPE_SUBSCRIPTION}
     *  All             : 10 {@link HelperDefine#ITEM_TYPE_ALL}
     */
    private String     mPurchaseIds = "";
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // 1. save StartNum, EndNum and ItemType passed by Intent
        // ====================================================================
        Intent intent = getIntent();
        
        if( intent != null && intent.getExtras() != null
                && intent.getExtras().containsKey("PurchaseIds"))
        {
            Bundle extras = intent.getExtras();
            mPurchaseIds    = extras.getString("PurchaseIds");
            mShowErrorDialog = extras.getBoolean( "ShowErrorDialog", true );
            Log.d(TAG, "onCreate: PurchaseIds [" + mPurchaseIds + "]");
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
        // 2. If IAP package is installed and valid, start SamsungAccount
        //    authentication activity to start purchase.
        // ====================================================================
        if( checkAppsPackage() == true )
        {
            Log.i( TAG, "Samsung Account Login..." );

            HelperUtil.startAccountActivity( this );
        }
        // ====================================================================
    }

    @Override
    protected void onDestroy()
    {
        super.preDestory();
        Log.d(TAG, "onDestroy: ");
        OnConsumePurchasedItemsListener onConsumePurchasedItemsListener
                = HelperListenerManager.getInstance().getOnConsumePurchasedItemsListener();
        HelperListenerManager.getInstance().setOnConsumePurchasedItemsListener(null);
        if( null != onConsumePurchasedItemsListener )
        {
            onConsumePurchasedItemsListener.onConsumePurchasedItems(mErrorVo, mConsumeList);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult>> requestCode : " + _requestCode + ", resultCode : " + _resultCode);
        switch(_requestCode) {
            case HelperDefine.REQUEST_CODE_IS_ACCOUNT_CERTIFICATION :
                Log.i(TAG, "REQUEST_CODE_IS_ACCOUNT_CERTIFICATION Result : " + _resultCode);
                // 1) If SamsungAccount authentication is succeed
                // ------------------------------------------------------------
                if( RESULT_OK == _resultCode )
                {
                    // bind to IAPService
                    // --------------------------------------------------------
                    bindIapService();
                    // --------------------------------------------------------
                }
                // ------------------------------------------------------------
                // 2) If SamsungAccount authentication is cancelled
                // ------------------------------------------------------------
                else
                {
                    mErrorVo.setError( HelperDefine.IAP_ERROR_COMMON,
                            getString( R.string.mids_sapps_pop_unknown_error_occurred ) );
                }
            break;
            case HelperDefine.REQUEST_CODE_IS_ENABLE_BILLING:
                Log.i(TAG, "REQUEST_CODE_IS_ENABLE_BILLING Result : " + _resultCode);
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
        Log.d(TAG, "succeedBind: ");
        if ( mIapHelper != null )
        {
            mIapHelper.safeConsumePurchasedItems( ConsumePurchasedItemsActivity.this,
                    mPurchaseIds,
                    mShowErrorDialog );
        }
    }
}