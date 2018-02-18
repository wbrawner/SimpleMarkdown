package com.samsung.android.sdk.iap.lib.listener;

import com.samsung.android.sdk.iap.lib.helper.task.GetProductsDetailsTask;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.ProductVo;

import java.util.ArrayList;

/**
 * Callback Interface used with
 * {@link GetProductsDetailsTask}
 */
public interface OnGetProductsDetailsListener
{
    /**
     * Callback method to be invoked 
     * when {@link GetProductsDetailsTask} has been finished.
     * @param _errorVO
     * @param _productList
     */
    void onGetProducts(ErrorVo _errorVO, ArrayList<ProductVo> _productList);
}
