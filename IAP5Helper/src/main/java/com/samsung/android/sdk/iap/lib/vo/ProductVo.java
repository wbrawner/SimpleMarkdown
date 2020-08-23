package com.samsung.android.sdk.iap.lib.vo;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductVo extends BaseVo
{
    private static final String TAG = ProductVo.class.getSimpleName();

    //Subscription data
    private String mSubscriptionDurationUnit;
    private String mSubscriptionDurationMultiplier;

    private String mItemImageUrl;
    private String mItemDownloadUrl;
    private String mReserved1;
    private String mReserved2;
    private String mFreeTrialPeriod;

// 미사용
//    private String mItemPricePSMS;

    private String mJsonString;

    public ProductVo(){}

    public ProductVo(String _jsonString )
    {
        super( _jsonString );
        
        setJsonString( _jsonString );
        Log.i( TAG, mJsonString );
        
        try
        {
            JSONObject jObject = new JSONObject( _jsonString );

            setSubscriptionDurationUnit( jObject.optString( "mSubscriptionDurationUnit" ) );

            setSubscriptionDurationMultiplier( jObject.optString( "mSubscriptionDurationMultiplier" ) );

            setItemImageUrl(jObject.optString( "mItemImageUrl" ));
            setItemDownloadUrl(jObject.optString( "mItemDownloadUrl" ));
            setReserved1(jObject.optString( "mReserved1" ));
            setReserved2(jObject.optString( "mReserved2" ));
            setFreeTrialPeriod(jObject.optString( "mFreeTrialPeriod" ));
        }
        catch( JSONException e )
        {
            e.printStackTrace();
        }
    }

    public String getSubscriptionDurationUnit()
    {
        return mSubscriptionDurationUnit;
    }

    public void setSubscriptionDurationUnit( String _subscriptionDurationUnit )
    {
        mSubscriptionDurationUnit = _subscriptionDurationUnit;
    }

    public String getSubscriptionDurationMultiplier()
    {
        return mSubscriptionDurationMultiplier;
    }

    public void setSubscriptionDurationMultiplier(
                                       String _subscriptionDurationMultiplier )
    {
        mSubscriptionDurationMultiplier = _subscriptionDurationMultiplier;
    }

    public String getItemImageUrl() { return mItemImageUrl; }
    public void setItemImageUrl( String _itemImageUrl )
    {
        mItemImageUrl = _itemImageUrl;
    }

    public String getItemDownloadUrl() { return mItemDownloadUrl; }
    public void setItemDownloadUrl( String _itemDownloadUrl )
    {
        mItemDownloadUrl = _itemDownloadUrl;
    }

    public String getReserved1() { return mReserved1; }
    public void setReserved1( String _reserved1 )
    {
        mReserved1 = _reserved1;
    }

    public String getReserved2() { return mReserved2; }
    public void setReserved2( String _reserved2 )
    {
        mReserved2 = _reserved2;
    }

    public String getFreeTrialPeriod() { return mFreeTrialPeriod; }
    public void setFreeTrialPeriod( String _freeTrialPeriod ){ mFreeTrialPeriod = _freeTrialPeriod; }

    public String getJsonString()
    {
        return mJsonString;
    }
    public void setJsonString( String _jsonString )
    {
        mJsonString = _jsonString;
    }
    
    public String dump()
    {
        String dump = super.dump() + "\n";
        
        dump += "SubscriptionDurationUnit       : "
                                       + getSubscriptionDurationUnit() + "\n" +
                "SubscriptionDurationMultiplier : " +
                                           getSubscriptionDurationMultiplier() + "\n" +
                "ItemImageUrl    : " + getItemImageUrl() + "\n" +
                "ItemDownloadUrl : " + getItemDownloadUrl() + "\n" +
                "Reserved1       : " + getReserved1() + "\n" +
                "Reserved2       : " + getReserved2() + "\n" +
                "FreeTrialPeriod : " + getFreeTrialPeriod();
        return dump;
    }
}