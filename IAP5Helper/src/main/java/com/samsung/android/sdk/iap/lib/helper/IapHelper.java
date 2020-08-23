package com.samsung.android.sdk.iap.lib.helper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.samsung.android.iap.IAPConnector;
import com.samsung.android.sdk.iap.lib.R;
import com.samsung.android.sdk.iap.lib.activity.BaseActivity;
import com.samsung.android.sdk.iap.lib.activity.ConsumePurchasedItemsActivity;
import com.samsung.android.sdk.iap.lib.activity.OwnedProductActivity;
import com.samsung.android.sdk.iap.lib.activity.PaymentActivity;
import com.samsung.android.sdk.iap.lib.activity.ProductActivity;
import com.samsung.android.sdk.iap.lib.helper.task.ConsumePurchasedItemsTask;
import com.samsung.android.sdk.iap.lib.helper.task.GetOwnedListTask;
import com.samsung.android.sdk.iap.lib.helper.task.GetProductsDetailsTask;
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetOwnedListListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetProductsDetailsListener;
import com.samsung.android.sdk.iap.lib.listener.OnIapBindListener;
import com.samsung.android.sdk.iap.lib.listener.OnPaymentListener;

public class IapHelper extends HelperDefine
{
    private static final String TAG  = IapHelper.class.getSimpleName();

    /**
     * When you release a application,
     * this Mode must be set to {@link HelperDefine#IAP_MODE_PRODUCTION}
     * Please double-check this mode before release.
     */
    private int                   mMode = HelperDefine.IAP_MODE_PRODUCTION;
    // ========================================================================

    private Context mContext         = null;

    private IAPConnector mIapConnector    = null;
    private ServiceConnection mServiceConn     = null;

    // AsyncTask for API
    // ========================================================================
    private GetProductsDetailsTask mGetProductsDetailsTask        = null;
    private GetOwnedListTask mGetOwnedListTask        = null;
    private ConsumePurchasedItemsTask mConsumePurchasedItemsTask       = null;
    // ========================================================================

    // API listener
    private HelperListenerManager mListenerInstance = null;

    private static IapHelper mInstance = null;

    // State of IAP Service
    // ========================================================================
    private int mState = HelperDefine.STATE_TERM;
    private final static Object mOperationLock = new Object();
    static boolean mOperationRunningFlag = false;


    // ########################################################################
    // ########################################################################
    // 1. SamsungIAPHeler object create and reference
    // ########################################################################
    // ########################################################################

    /**
     * IapHelper constructor
     * @param _context
     */
    private IapHelper(Context _context  )
    {
        _setContextAndMode( _context );
        _setListenerInstance();
    }

    /**
     * IapHelper singleton reference method
     * @param _context Context
     */
    public static IapHelper getInstance(Context _context )
    {
        if( null == mInstance )
        {
            Log.d(TAG, "getInstance new: mContext " + _context );
            mInstance = new IapHelper( _context );
        }
        else
        {
            Log.d(TAG, "getInstance old: mContext " + _context );
            mInstance._setContextAndMode( _context );
        }

        return mInstance;
    }

    public void setOperationMode(OperationMode _mode)
    {
        if(_mode == OperationMode.OPERATION_MODE_TEST)
            mMode = HelperDefine.IAP_MODE_TEST;
        else if(_mode == OperationMode.OPERATION_MODE_TEST_FAILURE)
            mMode = HelperDefine.IAP_MODE_TEST_FAILURE;
        else
            mMode = HelperDefine.IAP_MODE_PRODUCTION;
    }

    private void _setContextAndMode( Context _context )
    {
        mContext = _context.getApplicationContext();
    }

    private void _setListenerInstance()
    {
        if(mListenerInstance != null) {
            mListenerInstance.destroy();
            mListenerInstance = null;
        }
        mListenerInstance = HelperListenerManager.getInstance();
    }


    // ########################################################################
    // ########################################################################
    // 2. Binding for IAPService
    // ########################################################################
    // ########################################################################

    /**
     * bind to IAPService
     *
     * @param _listener The listener that receives notifications
     * when bindIapService method is finished.
     */
    public void bindIapService( final OnIapBindListener _listener )
    {
        // exit If already bound
        // ====================================================================
        if( mState >= HelperDefine.STATE_BINDING )
        {
            if( _listener != null )
            {
                _listener.onBindIapFinished( HelperDefine.IAP_RESPONSE_RESULT_OK );
            }

            return;
        }
        // ====================================================================

        // Connection to IAP service
        // ====================================================================
        mServiceConn = new ServiceConnection()
        {
            @Override
            public void onServiceDisconnected( ComponentName _name )
            {
                Log.d( TAG, "IAP Service Disconnected..." );

                mState        = HelperDefine.STATE_TERM;
                mIapConnector = null;
                mServiceConn  = null;
            }

            @Override
            public void onServiceConnected
            (
                ComponentName _name,
                IBinder       _service
            )
            {
                mIapConnector = IAPConnector.Stub.asInterface( _service );

                if( _listener != null ) {
                    if (mIapConnector != null) {
                        mState = HelperDefine.STATE_BINDING;

                        _listener.onBindIapFinished(HelperDefine.IAP_RESPONSE_RESULT_OK);
                    } else {
                        mState = HelperDefine.STATE_TERM;

                        _listener.onBindIapFinished(
                                HelperDefine.IAP_RESPONSE_RESULT_UNAVAILABLE);
                    }
                }
            }
        };
        // ====================================================================
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(HelperDefine.GALAXY_PACKAGE_NAME, HelperDefine.IAP_SERVICE_NAME));
//                IAP_PACKAGE_NAME + ".service.IAPService"));
        // bind to IAPService
        // ====================================================================
        mContext.bindService( serviceIntent,
                              mServiceConn,
                              Context.BIND_AUTO_CREATE );
        // ====================================================================
    }


    /* ########################################################################
     * ########################################################################
     * 3. Method using IAP APIs.
     *    ( GetProductsDetailsTask, GetProductsDetailsTask, getInbox )
     * ########################################################################
     * ##################################################################### */
    ///////////////////////////////////////////////////////////////////////////
    // 3.1) getProductsDetails ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * <PRE>
     * This load item list by starting productActivity in this library,
     * and the result will be sent to {@link OnGetProductsDetailsListener} Callback interface.
     * To do that, {@link ProductActivity} must be described in AndroidManifest.xml of third-party application
     * as below.
     *
     * &lt;activity android:name="com.sec.android.iap.lib.activity.productActivity"
     *      android:theme="@style/Theme.Empty"
     *      android:configChanges="orientation|screenSize"/&gt;
     * </PRE>
     *
     * @param _productIds
     * @param _onGetProductsDetailsListener
     */
    public void getProductsDetails
    (
            String            _productIds,
            OnGetProductsDetailsListener _onGetProductsDetailsListener
    )
    {
        try
        {
            IapStartInProgressFlag();
            if( null == _onGetProductsDetailsListener )
            {
                throw new Exception( "_onGetProductsDetailsListener is null" );
            }

            mListenerInstance.setOnGetProductsDetailsListener( _onGetProductsDetailsListener );

            Intent intent = new Intent( mContext, ProductActivity.class );
            intent.putExtra( "ProductIds", _productIds );
            intent.putExtra( "ShowErrorDialog", true );
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

            mContext.startActivity( intent );
        }
        catch (IapInProgressException e) {
            e.printStackTrace();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * execute GetProductsDetailsTask
     */
    public void safeGetProductsDetails
    (
            BaseActivity _activity,
            String          _productIDs,
            boolean         _showErrorDialog
    )
    {
        try
        {
            if( mGetProductsDetailsTask != null &&
                    mGetProductsDetailsTask.getStatus() != Status.FINISHED )
            {
                mGetProductsDetailsTask.cancel( true );
            }

            mGetProductsDetailsTask = new GetProductsDetailsTask( _activity,
                    mIapConnector,
                    _productIDs,
                    _showErrorDialog,
                    mMode);
            mGetProductsDetailsTask.execute();
        }
        catch( Exception e )
        {
            if( null != _activity )
            {
                _activity.finish();
            }

            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // 3.2) getOwnedList ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * <PRE>
     * This load owned product list by starting OwnedListActivity in this library,
     * and the result will be sent to {@link OnGetOwnedListListener} Callback interface.
     * To do that, {@link OwnedProductActivity} must be described in AndroidManifest.xml of third-party application
     * as below.
     *
     * &lt;activity android:name="com.sec.android.iap.lib.activity.OwnedProductActivity"
     *      android:theme="@style/Theme.Empty"
     *      android:configChanges="orientation|screenSize"/&gt;
     * </PRE>
     *
     * @param _productType
     * @param _onGetOwnedListListener
     */
    public void getOwnedList
    (
            String            _productType,
            OnGetOwnedListListener _onGetOwnedListListener
    )
    {
        try
        {
            IapStartInProgressFlag();
            if( null == _onGetOwnedListListener )
            {
                throw new Exception( "_onGetOwnedListListener is null" );
            }

            mListenerInstance.setOnGetOwnedListListener( _onGetOwnedListListener );

            Log.d(TAG, "getOwnedList: " + mContext);
            Intent intent = new Intent( mContext, OwnedProductActivity.class );

            if(_productType==null)
                intent.putExtra( "ProductType",  "");
            else
                intent.putExtra( "ProductType", _productType );
            intent.putExtra( "ShowErrorDialog", true );
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

            mContext.startActivity( intent );
        }
        catch (IapInProgressException e)
        {
            e.printStackTrace();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * execute GetOwnedListTask
     */
    public void safeGetOwnedList
    (
            BaseActivity _activity,
            String          _productType,
            boolean         _showErrorDialog
    )
    {
        try
        {
            if( mGetOwnedListTask != null &&
                    mGetOwnedListTask.getStatus() != Status.FINISHED )
            {
                mGetOwnedListTask.cancel( true );
            }

            mGetOwnedListTask = new GetOwnedListTask( _activity,
                    mIapConnector,
                    _productType,
                    _showErrorDialog,
                    mMode);
            mGetOwnedListTask.execute();
        }
        catch( Exception e )
        {
            if( null != _activity )
            {
                _activity.finish();
            }

            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 3.3) consumePurchasedItems ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * <PRE>
     * This load item list by starting OwnedListActivity in this library,
     * and the result will be sent to {@link OnConsumePurchasedItemsListener} Callback interface.
     * To do that, {@link OwnedProductActivity} must be described in AndroidManifest.xml of third-party application
     * as below.
     *
     * &lt;activity android:name="com.sec.android.iap.lib.activity.OwnedListActivity"
     *      android:theme="@style/Theme.Empty"
     *      android:configChanges="orientation|screenSize"/&gt;
     * </PRE>
     *
     * @param _purchaseIds
     * @param _onConsumePurchasedItemsListener
     */
    public void consumePurchasedItems
    (
            String            _purchaseIds,
            OnConsumePurchasedItemsListener _onConsumePurchasedItemsListener
    )
    {
        try
        {
            IapStartInProgressFlag();
            if( null == _onConsumePurchasedItemsListener )
            {
                throw new Exception( "_onConsumePurchasedItemsListener is null" );
            }
            if( null == _purchaseIds ) throw new Exception( "_purchaseIds is null" );
            if( _purchaseIds.length() == 0 ) throw new Exception( "_purchaseIds is empty" );


            mListenerInstance.setOnConsumePurchasedItemsListener( _onConsumePurchasedItemsListener );

            Intent intent = new Intent( mContext, ConsumePurchasedItemsActivity.class );

            intent.putExtra( "PurchaseIds", _purchaseIds );
            intent.putExtra( "ShowErrorDialog", true );

            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

            mContext.startActivity( intent );
        }
        catch (IapInProgressException e)
        {
            e.printStackTrace();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * execute ConsumePurchasedItemsTask
     */
    public void safeConsumePurchasedItems
    (
            BaseActivity _activity,
            String          _purchaseIds,
            boolean         _showErrorDialog
    )
    {
        try
        {
            if( mConsumePurchasedItemsTask != null &&
                    mConsumePurchasedItemsTask.getStatus() != Status.FINISHED )
            {
                mConsumePurchasedItemsTask.cancel( true );
            }

            mConsumePurchasedItemsTask = new ConsumePurchasedItemsTask( _activity,
                    mIapConnector,
                    _purchaseIds,
                    _showErrorDialog,
                    mMode);
            mConsumePurchasedItemsTask.execute();
        }
        catch( Exception e )
        {
            if( null != _activity )
            {
                _activity.finish();
            }

            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // 3.2) startPurchase / ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * <PRE>
     * Start payment process by starting {@link PaymentActivity} in this library,
     * and result will be sent to {@link OnPaymentListener} interface.
     * To do that, PaymentActivity must be described in AndroidManifest.xml of third-party application
     * as below.
     *
     * &lt;activity android:name="com.sec.android.iap.lib.activity.PaymentActivity"
     *      android:theme="@style/Theme.Empty"
     *      android:configChanges="orientation|screenSize"/&gt;
     * </PRE>
     *
     * @param _itemId
     * @param _passThroughParam
     * @param _showSuccessDialog  If it is true, dialog of payment success is
     *                            shown. otherwise it will not be shown.
     * @param _onPaymentListener
     */
    public void startPayment
    (
        String              _itemId,
        String              _passThroughParam,
        boolean             _showSuccessDialog,
        OnPaymentListener   _onPaymentListener
    )
    {
        try
        {
            IapStartInProgressFlag();
            if( null == _onPaymentListener )
            {
                throw new Exception( "OnPaymentListener is null" );
            }
            if( _passThroughParam != null && _passThroughParam.getBytes().length > HelperDefine.PASSTHROGUH_MAX_LENGTH )
                throw new Exception( "PassThroughParam length exceeded (MAX " + HelperDefine.PASSTHROGUH_MAX_LENGTH +")" );
            mListenerInstance.setOnPaymentListener( _onPaymentListener );

            Intent intent = new Intent( mContext, PaymentActivity.class );
            intent.putExtra( "ItemId", _itemId );
            String encodedPassThroughParam = "";
            if(_passThroughParam!=null)
                encodedPassThroughParam = Base64.encodeToString(_passThroughParam.getBytes(),0);
            intent.putExtra( "PassThroughParam", encodedPassThroughParam);
            intent.putExtra( "ShowSuccessDialog", _showSuccessDialog );
            intent.putExtra( "ShowErrorDialog", true );
            intent.putExtra( "OperationMode", mMode );
            Log.d(TAG, "startPayment: " + mMode);

            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

            mContext.startActivity( intent );
        }
        catch (IapInProgressException e)
        {
            e.printStackTrace();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }


    // ########################################################################
    // ########################################################################
    // 4. etc
    // ########################################################################
    // ########################################################################
    
    /**
     * Stop running task, {@link GetProductsDetailsTask}, {@link ConsumePurchasedItemsTask}
     * or {@link GetOwnedListTask} } before dispose().
     */
    private void stopTasksIfNotFinished()
    {
        if( mGetProductsDetailsTask != null )
        {
            if ( mGetProductsDetailsTask.getStatus() != Status.FINISHED )
            {
                Log.e(TAG, "stopTasksIfNotFinished: mGetProductsDetailsTask Status > " + mGetProductsDetailsTask.getStatus());
                mGetProductsDetailsTask.cancel( true );
            }
        }

        if( mGetOwnedListTask != null )
        {
            if ( mGetOwnedListTask.getStatus() != Status.FINISHED )
            {
                Log.e(TAG, "stopTasksIfNotFinished: mGetOwnedListTask Status > "+ mGetOwnedListTask.getStatus());
                mGetOwnedListTask.cancel( true );
            }
        }

        if( mConsumePurchasedItemsTask != null )
        {
            if ( mConsumePurchasedItemsTask.getStatus() != Status.FINISHED )
            {
                Log.e(TAG, "stopTasksIfNotFinished: mConsumePurchasedItemsTask Status > " + mConsumePurchasedItemsTask.getStatus());
                mConsumePurchasedItemsTask.cancel( true );
            }
        }
    }
    
    /**
     * Unbind from IAPService and release used resources.
     */
    public void dispose()
    {
        stopTasksIfNotFinished();
        
        if( mContext != null && mServiceConn != null )
        {
            mContext.unbindService( mServiceConn );
        }
        
        mState         = HelperDefine.STATE_TERM;
        mServiceConn   = null;
        mIapConnector  = null;
        IapEndInProgressFlag();
    }

    void IapStartInProgressFlag() throws IapInProgressException {
        Log.d(TAG, "IapStartInProgressFlag: ");
        synchronized (mOperationLock)
        {
            if(mOperationRunningFlag)
            {
                throw new IapInProgressException("another operation is running");
            }
            mOperationRunningFlag = true;

        }
    }
    void IapEndInProgressFlag() {
        Log.d(TAG, "IapEndInProgressFlag: ");
        synchronized (mOperationLock)
        {
            mOperationRunningFlag = false;
        }
    }

    public static class IapInProgressException extends Exception {
        public IapInProgressException( String message ) {
            super(message);
        }
    }
}