package com.samsung.android.sdk.iap.lib.helper.task;

import android.os.Bundle;
import android.util.Log;

import com.samsung.android.iap.IAPConnector;
import com.samsung.android.sdk.iap.lib.R;
import com.samsung.android.sdk.iap.lib.activity.BaseActivity;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.vo.OwnedProductVo;

import java.util.ArrayList;

/**
 * Asynchronized Task to load a list of items
 */
public class GetOwnedListTask extends BaseTask
{
    private static final String TAG  = GetOwnedListTask.class.getSimpleName();
    private String          mProductType      = "";

    ArrayList<OwnedProductVo> mOwnedList  = new ArrayList<OwnedProductVo>();

    public GetOwnedListTask
            (
                    BaseActivity    _activity,
                    IAPConnector    _iapConnector,
                    String          _productType,
                    boolean         _showErrorDialog,
                    int             _mode
            )
    {
        super(_activity,_iapConnector,_showErrorDialog,_mode);
        mProductType         = _productType;

        mActivity.setOwnedList( mOwnedList );
    }

    @Override
    protected Boolean doInBackground( String... params )
    {
        Log.d(TAG, "doInBackground: start");
        try
        {
            int pagingIndex = 1;
            do {
                Log.d(TAG, "doInBackground: pagingIndex = " + pagingIndex);
                // 1) call getItemList() method of IAPService
                // ============================================================
                Bundle bundle = mIapConnector.getOwnedList(
                        mActivity.getPackageName(),
                        mProductType,
                        pagingIndex,
                        mMode);
                // ============================================================

                // 2) save status code, error string and extra String.
                // ============================================================
                if (bundle != null) {
                    mErrorVo.setError(bundle.getInt(HelperDefine.KEY_NAME_STATUS_CODE),
                            bundle.getString(HelperDefine.KEY_NAME_ERROR_STRING));

                    mErrorVo.setExtraString(bundle.getString(
                            HelperDefine.KEY_NAME_IAP_UPGRADE_URL));
                } else {
                    mErrorVo.setError(
                            HelperDefine.IAP_ERROR_COMMON,
                            mActivity.getString(
                                    R.string.mids_sapps_pop_unknown_error_occurred));
                }
                // ============================================================

                // 3) If item list is loaded successfully,
                //    make item list by Bundle data
                // ============================================================
                if (mErrorVo.getErrorCode() == HelperDefine.IAP_ERROR_NONE) {
                    if (bundle != null) {
                        String nextPagingIndex = bundle.getString(HelperDefine.NEXT_PAGING_INDEX);
                        if (nextPagingIndex != null && nextPagingIndex.length() > 0)
                            pagingIndex = Integer.parseInt(nextPagingIndex);
                        else
                            pagingIndex = -1;

                        ArrayList<String> ownedProductStringList =
                                bundle.getStringArrayList(HelperDefine.KEY_NAME_RESULT_LIST);

                        if (ownedProductStringList != null) {
                            for (String ownedProductString : ownedProductStringList) {
                                OwnedProductVo ownedPrroductVo = new OwnedProductVo(ownedProductString);
                                mOwnedList.add(ownedPrroductVo);
                            }
                        } else {
                            Log.d(TAG, "Bundle Value 'RESULT_LIST' is null.");
                        }
                    }
                }
                // ============================================================
                // 4) If failed, print log.
                // ============================================================
                else {
                    Log.d(TAG, mErrorVo.getErrorString());
                    return true;
                }
                // ============================================================
            } while (pagingIndex > 0);
        }
        catch( Exception e )
        {
            mErrorVo.setError(
                    HelperDefine.IAP_ERROR_COMMON,
                    mActivity.getString(
                            R.string.mids_sapps_pop_unknown_error_occurred ) );

            e.printStackTrace();
            return false;
        }

        return true;
    }
}
