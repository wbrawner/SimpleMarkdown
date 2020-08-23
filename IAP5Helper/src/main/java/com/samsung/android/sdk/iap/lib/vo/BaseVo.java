package com.samsung.android.sdk.iap.lib.vo;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.DateFormat;
import android.util.Base64;

public class BaseVo 
{
    private String mItemId;
    private String mItemName;
    private Double mItemPrice;
    private String mItemPriceString;
    private String mCurrencyUnit;
    private String mCurrencyCode;
    private String mItemDesc;
    private String mType;
    private Boolean mIsConsumable;
    
    public BaseVo(){}

    public BaseVo( String _jsonString )
    {
        try
        {
            JSONObject jObject = new JSONObject( _jsonString );
            
            setItemId( jObject.optString( "mItemId" ) );
            setItemName( jObject.optString( "mItemName" ) );
            setItemPrice( jObject.optDouble("mItemPrice" ) );
            setItemPriceString( jObject.optString( "mItemPriceString" ) );
            setCurrencyUnit( jObject.optString( "mCurrencyUnit" ) );
            setCurrencyCode(jObject.optString( "mCurrencyCode" ));
            setItemDesc( jObject.optString( "mItemDesc" ) );
            setType( jObject.optString( "mType" ) );
            Boolean isConsumable = false;
            if(jObject.optString( "mConsumableYN" )!=null && jObject.optString( "mConsumableYN" ).equals("Y"))
                isConsumable = true;
            setIsConsumable(isConsumable);
        }
        catch( JSONException e )
        {
            e.printStackTrace();
        }
    }

    public String getItemId()
    {
        return mItemId;
    }

    public void setItemId( String _itemId )
    {
        mItemId = _itemId;
    }

    public String getItemName()
    {
        return mItemName;
    }

    public void setItemName( String _itemName )
    {
        mItemName = _itemName;
    }

    public Double getItemPrice()
    {
        return mItemPrice;
    }

    public void setItemPrice( Double _itemPrice )
    {
        mItemPrice = _itemPrice;
    }
    
    public String getItemPriceString()
    {
        return mItemPriceString;
    }

    public void setItemPriceString( String _itemPriceString )
    {
        mItemPriceString = _itemPriceString;
    }

    public String getCurrencyUnit()
    {
        return mCurrencyUnit;
    }

    public void setCurrencyUnit( String _currencyUnit )
    {
        mCurrencyUnit = _currencyUnit;
    }

    public String getCurrencyCode() { return mCurrencyCode; }

    public void setCurrencyCode( String _currencyCode )
    {
        mCurrencyCode = _currencyCode;
    }

    public String getItemDesc()
    {
        return mItemDesc;
    }

    public void setItemDesc( String _itemDesc )
    {
        mItemDesc = _itemDesc;
    }

    public String getType()
    {
        return mType;
    }

    public void setType( String _itemDesc )
    {
        mType = _itemDesc;
    }

    public Boolean getIsConsumable()
    {
        return mIsConsumable;
    }

    public void setIsConsumable( Boolean _consumableYN ) { mIsConsumable = _consumableYN; }


    public String dump()
    {
        String dump = null;
        
        dump = "ItemId           : " + getItemId()           + "\n" +
               "ItemName         : " + getItemName()         + "\n" +
               "ItemPrice        : " + getItemPrice()        + "\n" +
               "ItemPriceString  : " + getItemPriceString()  + "\n" +
               "ItemDesc         : " + getItemDesc()         + "\n" +
               "CurrencyUnit     : " + getCurrencyUnit()     + "\n" +
               "CurrencyCode     : " + getCurrencyCode()     + "\n" +
               "IsConsumable     : " + getIsConsumable()     + "\n" +
               "Type             : " + getType();
        
        return dump;
    }
    
    protected String getDateString( long _timeMills )
    {
        String result     = "";
        String dateFormat = "yyyy.MM.dd HH:mm:ss";
        
        try
        {
            result = DateFormat.format( dateFormat, _timeMills ).toString();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            result = "";
        }
        
        return result;
    }
}