package com.samsung.android.sdk.iap.lib.activity;

import java.util.ArrayList;
 
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.samsung.android.sdk.iap.lib.R;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.helper.HelperUtil;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnIapBindListener;
import com.samsung.android.sdk.iap.lib.vo.ConsumeVo;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.OwnedProductVo;
import com.samsung.android.sdk.iap.lib.vo.ProductVo;
import com.samsung.android.sdk.iap.lib.vo.PurchaseVo;


public abstract class BaseActivity extends Activity
{
    private static final String  TAG = BaseActivity.class.getSimpleName();

    protected ErrorVo                   mErrorVo            = new ErrorVo();
    protected PurchaseVo                mPurchaseVo         = null;
    protected ArrayList<ProductVo>      mProductsDetails    = null;
    protected ArrayList<OwnedProductVo> mOwnedList          = null;
    protected ArrayList<ConsumeVo>      mConsumeList        = null;
    private   Dialog                    mProgressDialog     = null;

    /**
     * Helper Class between IAPService and 3rd Party Application
     */
    IapHelper                mIapHelper   = null;
    
    /** Flag value to show successful pop-up. Error pop-up appears whenever it fails or not. */
    protected boolean mShowSuccessDialog  = true;
    protected boolean mShowErrorDialog    = true;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        // 1. Store IapMode passed by Intent
        // ====================================================================
        Intent intent = getIntent();
        
        // ====================================================================

        // 2. IapHelper Instance creation
        //    To test on development, set mode to test mode using
        //    use IapHelper.IAP_MODE_TEST_SUCCESS or
        //    IapHelper.IAP_MODE_TEST_FAIL constants.
        // ====================================================================
        mIapHelper = IapHelper.getInstance( this );
        // ====================================================================
       
        // 3. This activity is invisible excepting progress bar as default.
        // ====================================================================
        try
        {
            if( mProgressDialog != null )
            {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mProgressDialog = new Dialog(this, R.style.Theme_Empty);
            mProgressDialog.setContentView(R.layout.progress_dialog);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        // ====================================================================
        
        super.onCreate( savedInstanceState );
    }

    public void setProductsDetails(ArrayList<ProductVo> _productsDetails )
    {
        mProductsDetails = _productsDetails;
    }

    public void setOwnedList(ArrayList<OwnedProductVo> _ownedList )
    {
        mOwnedList = _ownedList;
    }

    public void setPurchaseVo( PurchaseVo  _purchaseVo )
    {
        mPurchaseVo = _purchaseVo;
    }

    public void setConsumeList( ArrayList<ConsumeVo> _consumeList )
    {
        mConsumeList = _consumeList;
    }

    public void setErrorVo( ErrorVo _errorVo )
    {
        mErrorVo = _errorVo;
    }

    public boolean checkAppsPackage()
    {
        Log.d(TAG, "checkAppsPackage: pos0");
        // 1. If Billing Package is installed in your device
        // ====================================================================
        if(HelperUtil.isInstalledAppsPackage(this)) {
            Log.d(TAG, "checkAppsPackage: pos1");
            // 1) If Billing package installed in your device is valid
            // ================================================================
            if (!HelperUtil.isEnabledAppsPackage(this)) {
                Log.d(TAG, "checkAppsPackage: pos2");

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + HelperDefine.GALAXY_PACKAGE_NAME));
                        startActivityForResult(intent, HelperDefine.REQUEST_CODE_IS_ENABLE_BILLING);
                    }
                };
                Log.d(TAG, "checkAppsPackage: pos3");
                //// TODO: 2017-08-16 need to set the error string
                HelperUtil.showIapDialogIfNeeded(this,
                        getString(R.string.mids_sapps_header_samsung_in_app_purchase_abb),
                        getString(R.string.mids_sapps_pop_unable_to_open_samsung_in_app_purchase_msg),
                        false,
                        runnable,
                        true);

            } else if (HelperUtil.isValidAppsPackage(this)) {
                Log.d(TAG, "checkAppsPackage: pos4");
                return true;

                // ================================================================
                // 2) If IAP package installed in your device is not valid
                // ================================================================
            } else {
                Log.d(TAG, "checkAppsPackage: pos5");
                // Set error to notify result to third-party application
                // ------------------------------------------------------------
                //// TODO: 2017-08-16 need to set the error string
                mErrorVo.setError(HelperDefine.IAP_ERROR_COMMON,
                        getString(R.string.mids_sapps_pop_an_invalid_installation_of_in_app_purchase_has_been_detected_check_and_try_again));
                // ------------------------------------------------------------
                // show alert dialog if IAP Package is invalid
                // ------------------------------------------------------------
                HelperUtil.showIapDialogIfNeeded(
                        this,
                        getString(R.string.mids_sapps_header_samsung_in_app_purchase_abb),
                        getString(R.string.mids_sapps_pop_an_invalid_installation_of_in_app_purchase_has_been_detected_check_and_try_again),
                        true,
                        null,
                        mShowErrorDialog);
                // ------------------------------------------------------------
            }
            // ================================================================

            // ====================================================================
            // 2. If IAP Package is not installed in your device
            // ====================================================================
        } else {
            Log.d(TAG, "checkAppsPackage: pos6");
            HelperUtil.installAppsPackage( this );
        }
        // ====================================================================
        Log.d(TAG, "checkAppsPackage: pos7");
        return false;
    }

    /**
     * Binding to IAPService
     * Once IAPService bound successfully, invoke succeedBind() method.
     */
    public void bindIapService()
    {
        Log.i( TAG, "start Bind... ");
        
        // 1. Bind to IAPService
        // ====================================================================
        mIapHelper.bindIapService( new OnIapBindListener()
        {
            @Override
            public void onBindIapFinished( int _result )
            {
                Log.i( TAG, "Binding OK... ");
                
                // 1) If IAPService is bound successfully.
                // ============================================================
                if( _result == HelperDefine.IAP_RESPONSE_RESULT_OK )
                {
                    succeedBind();
                }
                // ============================================================
                // 2) If IAPService is not bound.
                // ============================================================
                else
                {
                    // a) Set error for notifying result to third-party
                    //    application
                    // --------------------------------------------------------
                    mErrorVo.setError( HelperDefine.IAP_ERROR_COMMON,
                                       getString(R.string.mids_sapps_pop_unknown_error_occurred) );
                    // --------------------------------------------------------
                    // b) show alert dialog when bind is failed
                    // --------------------------------------------------------
                    HelperUtil.showIapDialogIfNeeded( BaseActivity.this,
                                                 getString(R.string.mids_sapps_header_samsung_in_app_purchase_abb),
                                                 getString(R.string.mids_sapps_pop_unknown_error_occurred)
                                                     + "[Lib_Bind]",
                                                 true,
                                                 null,
                                                 mShowErrorDialog);
                    // --------------------------------------------------------
                }
                // ============================================================
            }
        });
        // ====================================================================
    }

    /**
     * dispose IapHelper {@link ConsumePurchasedItemsActivity}, {@link OwnedProductActivity},{@link PaymentActivity}
     * and {@link ProductActivity}
     * To do that, preDestory must be invoked at first in onDestory of each child activity
     */
    protected void preDestory()
    {
        // 1. Invoke dispose Method to unbind service and release inprogress flag
        // ====================================================================
        if( null != mIapHelper )
        {
            mIapHelper.dispose();
            mIapHelper = null;
        }
    }

    @Override
    protected void onDestroy() 
    {
        // 1. dismiss ProgressDialog
        // ====================================================================
        try
        {
            if( mProgressDialog != null )
            {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        // ====================================================================

        super.onDestroy();
    }

    abstract void succeedBind();
}