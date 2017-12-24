package com.samsung.android.sdk.iap.lib.listener;

/**
  * Callback Interface to be invoked when bind to IAPService has been finished.
 */
public interface OnIapBindListener
{
    /**
     * Callback method to be invoked after binding to IAP service successfully.
     * @param result
     */
    public void onBindIapFinished( int result );
}