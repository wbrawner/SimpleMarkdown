package com.samsung.android.iap;

import com.samsung.android.iap.IAPServiceCallback;

interface IAPConnector {

	boolean requestCmd(IAPServiceCallback callback, in Bundle bundle);

	boolean unregisterCallback(IAPServiceCallback callback);

	///////////////////////////// IAP 5.0
    Bundle getProductsDetails(String packageName, String itemIds, int pagingIndex, int mode);

    Bundle getOwnedList(String packageName, String itemType, int pagingIndex, int mode);

    Bundle consumePurchasedItems(String packageName, String purchaseIds, int mode);
}
