package com.samsung.android.sdk.iap.lib.listener;

import com.samsung.android.sdk.iap.lib.helper.task.GetOwnedListTask;
import com.samsung.android.sdk.iap.lib.vo.ConsumeVo;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;

import java.util.ArrayList;

/**
 * Callback Interface used with
 * {@link GetOwnedListTask}
 */
public interface OnConsumePurchasedItemsListener
{
    /**
     * Callback method to be invoked 
     * when {@link GetOwnedListTask} has been finished.
     * @param _errorVO
     * @param _consumeList
     */
    void onConsumePurchasedItems(ErrorVo _errorVO, ArrayList<ConsumeVo> _consumeList);
}
