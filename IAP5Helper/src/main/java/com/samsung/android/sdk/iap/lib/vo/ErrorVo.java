package com.samsung.android.sdk.iap.lib.vo;

public class ErrorVo
{
    private int      mErrorCode    = 0;
    private String   mErrorString  = "";
    private String   mExtraString  = "";             
    
    public int getErrorCode()
    {
        return mErrorCode;
    }
    
    public void setError( int _errorCode, String _errorString )
    {
        mErrorCode = _errorCode;
        mErrorString = _errorString;
    }
    
    public String getErrorString()
    {
        return mErrorString;
    }
    
    public String getExtraString()
    {
        return mExtraString;
    }
    
    public void setExtraString( String _extraString )
    {
        mExtraString = _extraString;
    }
    
    public String dump()
    {
        String dump = "";
        
        dump = "ErrorCode    : " + getErrorCode()    +  "\n" +
               "ErrorString  : " + getErrorString()  +  "\n" +
               "ExtraString  : " + getExtraString();
                 
        return dump;
    }
}
