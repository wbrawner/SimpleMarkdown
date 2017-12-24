package com.samsung.android.sdk.iap.lib.helper;

import android.content.Context;

import com.samsung.android.sdk.iap.lib.helper.task.ConsumePurchasedItemsTask;
import com.samsung.android.sdk.iap.lib.helper.task.GetOwnedListTask;
import com.samsung.android.sdk.iap.lib.helper.task.GetProductsDetailsTask;
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetOwnedListListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetProductsDetailsListener;
import com.samsung.android.sdk.iap.lib.listener.OnPaymentListener;

/**
 * Created by sangbum7.kim on 2017-08-29.
 */

public class HelperListenerManager {
    private static HelperListenerManager mInstance = null;

    private OnGetProductsDetailsListener mOnGetProductsDetailsListener      = null;
    private OnGetOwnedListListener     mOnGetOwnedListListener      = null;
    private OnConsumePurchasedItemsListener     mOnConsumePurchasedItemsListener      = null;
    private OnPaymentListener mOnPaymentListener     = null;

    /**
     * HelperListenerManager singleton reference method
     */
    public static HelperListenerManager getInstance( )
    {
        if( mInstance == null)
            mInstance =  new HelperListenerManager();
        return mInstance;
    }

    public static void destroy()
    {
        mInstance = null;
    }

    /**
     * HelperListenerManager constructor
     */
    private HelperListenerManager(  )
    {
        mOnGetProductsDetailsListener = null;
        mOnGetOwnedListListener = null;
        mOnConsumePurchasedItemsListener = null;
        mOnPaymentListener = null;
    }

    /**
     * Register {@link OnGetProductsDetailsListener} callback interface to be invoked
     * when {@link GetProductsDetailsTask} has been finished.
     * @param _onGetProductsDetailsListener
     */
    public void setOnGetProductsDetailsListener( OnGetProductsDetailsListener _onGetProductsDetailsListener )
    {
        mOnGetProductsDetailsListener = _onGetProductsDetailsListener;
    }

    public OnGetProductsDetailsListener getOnGetProductsDetailsListener( )
    {
        return mOnGetProductsDetailsListener;
    }


    /**
     * Register {@link OnGetOwnedListListener} callback interface to be invoked
     * when {@link GetOwnedListTask} has been finished.
     * @param _onGetOwnedListListener
     */
    public void setOnGetOwnedListListener( OnGetOwnedListListener _onGetOwnedListListener )
    {
        mOnGetOwnedListListener = _onGetOwnedListListener;
    }

    public OnGetOwnedListListener getOnGetOwnedListListener( )
    {
        return mOnGetOwnedListListener;
    }


    /**
     * Register {@link OnConsumePurchasedItemsListener} callback interface to be invoked
     * when {@link ConsumePurchasedItemsTask} has been finished.
     * @param _onConsumePurchasedItemsListener
     */
    public void setOnConsumePurchasedItemsListener( OnConsumePurchasedItemsListener _onConsumePurchasedItemsListener )
    {
        mOnConsumePurchasedItemsListener = _onConsumePurchasedItemsListener;
    }

    public OnConsumePurchasedItemsListener getOnConsumePurchasedItemsListener( )
    {
        return mOnConsumePurchasedItemsListener;
    }


    /**
     * Register a callback interface to be invoked
     * when Purchase Process has been finished.
     * @param _onPaymentListener
     */
    public void setOnPaymentListener( OnPaymentListener _onPaymentListener )
    {
        mOnPaymentListener = _onPaymentListener;
    }

    public OnPaymentListener getOnPaymentListener()
    {
        return mOnPaymentListener;
    }
}
