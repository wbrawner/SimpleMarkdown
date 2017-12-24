package com.samsung.android.sdk.iap.lib.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.iap.lib.R;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.helper.HelperListenerManager;
import com.samsung.android.sdk.iap.lib.helper.HelperUtil;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnPaymentListener;
import com.samsung.android.sdk.iap.lib.vo.PurchaseVo;

public class PaymentActivity extends BaseActivity
{
    private static final String  TAG = PaymentActivity.class.getSimpleName();

    /** Item ID */
    private String  mItemId               = null;
    private String  mPassThroughParam   = "";
    private int mMode = HelperDefine.IAP_MODE_PRODUCTION;
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // 1. Save ItemId, ShowSuccessDialog passed by Intent
        // ====================================================================
        Intent intent = getIntent();
        
        if( intent != null && intent.getExtras() != null 
                && intent.getExtras().containsKey( "ItemId" ) )
        {
            Bundle extras = intent.getExtras();

            mItemId            = extras.getString( "ItemId" );
            mPassThroughParam = extras.getString( "PassThroughParam" );
            mShowSuccessDialog = extras.getBoolean( "ShowSuccessDialog", true );
            mShowErrorDialog = extras.getBoolean( "ShowErrorDialog", true );
            mMode = extras.getInt("OperationMode", HelperDefine.IAP_MODE_PRODUCTION);
        }
        else
        {
            Toast.makeText( this, 
                    R.string.mids_sapps_pop_an_invalid_value_has_been_provided_for_samsung_in_app_purchase,
                            Toast.LENGTH_LONG ).show();
         
            // Set error to pass result to third-party application
            // ----------------------------------------------------------------
            mErrorVo.setError( HelperDefine.IAP_ERROR_COMMON,
                               getString(R.string.mids_sapps_pop_an_invalid_value_has_been_provided_for_samsung_in_app_purchase) );
            // ----------------------------------------------------------------
            
            finish();
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
        OnPaymentListener onPaymentListener =
                HelperListenerManager.getInstance().getOnPaymentListener();
        HelperListenerManager.getInstance().setOnPaymentListener(null);
        if( null != onPaymentListener )
        {
            onPaymentListener.onPayment(mErrorVo, mPurchaseVo);
        }
        super.onDestroy();
    }

    protected void succeedBind()
    {
        if ( mIapHelper != null )
        {
            startPaymentActivity(
                    PaymentActivity.this,
                    HelperDefine.REQUEST_CODE_IS_IAP_PAYMENT,
                    mItemId,
                    mPassThroughParam,
                    mMode);
        }
    }

    /**
     * Handle SamsungAccount authentication result and purchase result.
     */
    @Override
    protected void onActivityResult
    (   
        int     _requestCode,
        int     _resultCode,
        Intent  _intent
    )
    {
        switch( _requestCode )
        {
            // 1. Handle result of purchase
            // ================================================================
            case HelperDefine.REQUEST_CODE_IS_IAP_PAYMENT:
            {
                // 1) If payment is finished
                // ------------------------------------------------------------
                if( RESULT_OK == _resultCode )
                {
                    finishPurchase( _intent );
                }
                // ------------------------------------------------------------
                // 2) If payment is cancelled
                // ------------------------------------------------------------
                else if( RESULT_CANCELED == _resultCode )
                {
                    mErrorVo.setError( HelperDefine.IAP_PAYMENT_IS_CANCELED,
                                       getString(R.string.mids_sapps_pop_payment_canceled));
                    
                    HelperUtil.showIapDialogIfNeeded( this,
                                                 getString( R.string.mids_sapps_header_samsung_in_app_purchase_abb ),
                                                 mErrorVo.getErrorString(),
                                                 true,
                                                 null,
                                                 mShowErrorDialog );
                    
                    break;
                }
                // ------------------------------------------------------------
                
                break;
            }
            // ================================================================
            
            // 2. Handle result of SamsungAccount authentication
            // ================================================================
            case HelperDefine.REQUEST_CODE_IS_ACCOUNT_CERTIFICATION :
            {
                Log.i( TAG, "Samsung Account Result : " +  _resultCode );
                
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
                    mErrorVo.setError( HelperDefine.IAP_PAYMENT_IS_CANCELED,
                                       getString( R.string.mids_sapps_pop_payment_canceled ) );

                    HelperUtil.showIapDialogIfNeeded( this,
                                                 getString( R.string.mids_sapps_header_samsung_in_app_purchase_abb ),
                                                 getString( R.string.mids_sapps_pop_payment_canceled ),
                                                 true,
                                                 null,
                                                 mShowErrorDialog );
                }
                // ------------------------------------------------------------
                
                break;
            }
            // ================================================================

            case HelperDefine.REQUEST_CODE_IS_ENABLE_BILLING:
                if(checkAppsPackage()) {
                    Log.i( TAG, "Samsung Account Login..." );
                    HelperUtil.startAccountActivity(this);
                }
                break;
        }
    }

    /**
     * Invoked when payment has been finished.
     * @param _intent
     */
    private void finishPurchase( Intent  _intent )
    {
        // 1. If there is bundle passed from IAP
        // ====================================================================
        if(  null != _intent && null != _intent.getExtras() )
        {
            Bundle extras = _intent.getExtras();
            
            mErrorVo.setError( extras.getInt( HelperDefine.KEY_NAME_STATUS_CODE ),
                               extras.getString( HelperDefine.KEY_NAME_ERROR_STRING ) );
            
            // 1) If the purchase is successful,
            // ----------------------------------------------------------------
            if( mErrorVo.getErrorCode() == HelperDefine.IAP_ERROR_NONE )
            {
                //verification Checking is deleted
                // a) Create PurcahseVo with data in Intent
                // ------------------------------------------------------------
                mPurchaseVo = new PurchaseVo( extras.getString(
                        HelperDefine.KEY_NAME_RESULT_OBJECT ) );
                Log.d(TAG, "finishPurchase: " + mPurchaseVo.dump());
                // ------------------------------------------------------------

                /*
                // b) Validate the purchase
                // ------------------------------------------------------------
                mIapHelper.verifyPurchaseResult( PaymentActivity.this,
                                                        mPurchaseVo,
                                                        mShowSuccessDialog,
                                                        mShowErrorDialog );
                // ------------------------------------------------------------
                */

                mErrorVo.setError(
                        HelperDefine.IAP_ERROR_NONE ,
                        getString( R.string.dlg_msg_payment_success ) );

                HelperUtil.showIapDialogIfNeeded( this,
                        getString( R.string.mids_sapps_header_samsung_in_app_purchase_abb ),
                        mErrorVo.getErrorString(),
                        true,
                        null,
                        mShowSuccessDialog );

            }
            // ----------------------------------------------------------------
            // 2) If the purchase is failed
            // ----------------------------------------------------------------
            else
            {
                HelperUtil.showIapDialogIfNeeded( this,
                                                 getString( R.string.mids_sapps_header_samsung_in_app_purchase_abb ),
                                                 mErrorVo.getErrorString(),
                                                 true,
                                                 null,
                                                 mShowErrorDialog);
            }
            // ----------------------------------------------------------------
        }
        // ====================================================================
        // 2. If there is no bundle passed from IAP
        // ====================================================================
        else
        {
            mErrorVo.setError( HelperDefine.IAP_ERROR_COMMON,
                  getString( R.string.mids_sapps_pop_unknown_error_occurred ) );

            HelperUtil.showIapDialogIfNeeded( this,
                                         getString( R.string.mids_sapps_header_samsung_in_app_purchase_abb ),
                                         getString( R.string.mids_sapps_pop_unknown_error_occurred )
                                             + "[Lib_Payment]",
                                         true,
                                         null,
                                         mShowErrorDialog );

            return;
        }
        // ====================================================================
    }

    /**
     * Start payment.
     * @param _activity
     * @param _requestCode
     * @param _itemId
     * @param _passThroughParam
     */
    static private void startPaymentActivity
    (
            Activity _activity,
            int      _requestCode,
            String    _itemId,
            String    _passThroughParam,
            int      _mode
    )
    {
        try
        {
            Context context = _activity.getApplicationContext();
            Bundle bundle = new Bundle();
            bundle.putString( HelperDefine.KEY_NAME_THIRD_PARTY_NAME,
                    context.getPackageName() );

            bundle.putString( HelperDefine.KEY_NAME_ITEM_ID, _itemId );
            if(_passThroughParam != null)
                bundle.putString( HelperDefine.KEY_NAME_PASSTHROUGH_ID, _passThroughParam);
            bundle.putInt(HelperDefine.KEY_NAME_OPERATION_MODE, _mode);

            ComponentName com = new ComponentName( HelperDefine.GALAXY_PACKAGE_NAME,
                    HelperDefine.IAP_PACKAGE_NAME + ".activity.PaymentMethodListActivity" );

            Intent intent = new Intent( Intent.ACTION_MAIN );
            intent.addCategory( Intent.CATEGORY_LAUNCHER );
            intent.setComponent( com );

            intent.putExtras( bundle );

            if(intent.resolveActivity(context.getPackageManager()) != null) {
                _activity.startActivityForResult(intent, _requestCode);
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}