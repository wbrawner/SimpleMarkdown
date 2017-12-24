package com.samsung.android.sdk.iap.lib.listener;

import com.samsung.android.sdk.iap.lib.helper.task.GetOwnedListTask;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.OwnedProductVo;

import java.util.ArrayList;

/**
 * Callback Interface used with
 * {@link GetOwnedListTask}
 */
public interface OnGetOwnedListListener
{
    /**
     * Callback method to be invoked 
     * when {@link GetOwnedListTask} has been finished.
     * @param _errorVO
     * @param _ownedList
     */
    void onGetOwnedProducts(ErrorVo _errorVO, ArrayList<OwnedProductVo> _ownedList);
}
