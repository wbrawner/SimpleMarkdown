package com.samsung.android.sdk.iap.lib.helper.task;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.android.iap.IAPConnector;
import com.samsung.android.sdk.iap.lib.R;
import com.samsung.android.sdk.iap.lib.activity.BaseActivity;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.helper.HelperUtil;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;

/**
 * Created by sangbum7.kim on 2017-09-01.
 */

public class BaseTask extends AsyncTask<String, Object, Boolean>
{
    private static final String TAG  = BaseTask.class.getSimpleName();

    protected BaseActivity    mActivity         = null;
    protected IAPConnector    mIapConnector    = null;
    protected int             mMode = HelperDefine.IAP_MODE_PRODUCTION;
    protected boolean        mShowErrorDialog  = true;

    protected ErrorVo mErrorVo   = new ErrorVo();

    public BaseTask(BaseActivity    _activity,
                    IAPConnector     _iapConnector,
                    boolean         _showErrorDialog,
                    int              _mode)
    {

        mActivity       = _activity;
        mIapConnector  = _iapConnector;
        mShowErrorDialog = _showErrorDialog;
        mMode = _mode;

        mActivity.setErrorVo( mErrorVo );
    }

    @Override
    protected Boolean doInBackground( String... params ) {
        return true;
    }

    @Override
    protected void onPostExecute( Boolean _result )
    {
        // 1. If result is true
        // ================================================================
        if( true == _result )
        {
            // 1) If list of product is successfully loaded
            // ============================================================
            if( mErrorVo.getErrorCode() == HelperDefine.IAP_ERROR_NONE )
            {
                // finish Activity in order to notify the result to
                // third-party application immediately.
                // --------------------------------------------------------
                if( mActivity != null )
                {
                    mActivity.finish();
                }
                // --------------------------------------------------------
            }
            // ============================================================
            // 2) If the IAP package needs to be upgraded
            // ============================================================
            else if( mErrorVo.getErrorCode() == HelperDefine.IAP_ERROR_NEED_APP_UPGRADE )
            {
                // a) When user click the OK button on the dialog,
                //    go to SamsungApps IAP Detail page.
                // --------------------------------------------------------
                Runnable OkBtnRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if( true == TextUtils.isEmpty(
                                mErrorVo.getExtraString() ) )
                        {
                            return;
                        }

                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        intent.setData(
                                Uri.parse( mErrorVo.getExtraString() ) );

                        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

                        try
                        {
                            mActivity.startActivity( intent );
                        }
                        catch( ActivityNotFoundException e )
                        {
                            e.printStackTrace();
                        }
                    }
                };
                // --------------------------------------------------------

                // b) Pop-up shows that the IAP package needs to be updated.
                // --------------------------------------------------------
                HelperUtil.showIapDialogIfNeeded( mActivity,
                        mActivity.getString( R.string.mids_sapps_header_samsung_in_app_purchase_abb ),
                        mErrorVo.getErrorString(),
                        true,
                        OkBtnRunnable,
                        true );
                // --------------------------------------------------------

                Log.e( TAG, mErrorVo.getErrorString() );
            }
            // ============================================================
            // 3) If error is occurred during loading list of product
            // ============================================================
            else
            {
                HelperUtil.showIapDialogIfNeeded( mActivity,
                        mActivity.getString( R.string.mids_sapps_header_samsung_in_app_purchase_abb ),
                        mErrorVo.getErrorString(),
                        true,
                        null,
                        mShowErrorDialog );

                Log.e( TAG, mErrorVo.getErrorString() );
            }
            // ============================================================
        }
        // ================================================================
        // 2. If result is false
        // ================================================================
        else
        {
            HelperUtil.showIapDialogIfNeeded( mActivity,
                    mActivity.getString( R.string.mids_sapps_header_samsung_in_app_purchase_abb ),
                    mActivity.getString( R.string.mids_sapps_pop_unknown_error_occurred )
                            + "[Lib_ProductsDetails]",
                    true,
                    null,
                    mShowErrorDialog );
        }
        // ================================================================
    }

    @Override
    protected void onCancelled()
    {
        Log.e(TAG, "onCancelled: task cancelled" );
        mActivity.finish();
    }
}
