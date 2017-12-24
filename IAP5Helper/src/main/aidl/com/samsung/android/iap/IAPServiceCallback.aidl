package com.samsung.android.iap;

import android.os.Bundle;

interface IAPServiceCallback {
	oneway void responseCallback(in Bundle bundle);
}