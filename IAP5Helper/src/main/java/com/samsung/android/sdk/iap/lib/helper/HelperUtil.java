package com.samsung.android.sdk.iap.lib.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.samsung.android.sdk.iap.lib.R;
import com.samsung.android.sdk.iap.lib.activity.BaseActivity;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;

/**
 * Created by sangbum7.kim on 2017-08-17.
 */

public class HelperUtil {
    private static final String TAG  = HelperUtil.class.getSimpleName();
    /**
     * show dialog
     * @param _title
     * @param _message
     */
    public static void showIapDialogIfNeeded
    (
            final Activity _activity,
            String         _title,
            String         _message,
            final boolean  _finishActivity,
            final Runnable _onClickRunable,
            boolean        _showDialog
    )
    {
        if( _showDialog == false )
        {
            if( _finishActivity == true )
            {
                try{ _activity.finish(); }
                catch( Exception _e ){ _e.printStackTrace(); }
            }

            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder( _activity );

        alert.setTitle( _title );
        alert.setMessage( _message );

        alert.setPositiveButton( android.R.string.ok,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface _dialog, int _which )
                    {
                        if( null != _onClickRunable )
                        {
                            _onClickRunable.run();
                        }

                        _dialog.dismiss();

                        if( true == _finishActivity )
                        {
                            _activity.finish();
                        }
                    }
                } );

        if( true == _finishActivity )
        {
            alert.setOnCancelListener( new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel( DialogInterface dialog )
                {
                    _activity.finish();
                }
            });
        }

        try
        {
            alert.show();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Check that Apps package is installed
     * @param _context Context
     * @return If it is true Billing package is installed. otherwise, not installed.
     */
    static public boolean isInstalledAppsPackage( Context _context )
    {
        PackageManager pm = _context.getPackageManager();
        try
        {
            //// TODO: 2017-08-16 Make sure the packageInfo is normal and set the version code
            PackageInfo packageInfo = pm.getPackageInfo(HelperDefine.GALAXY_PACKAGE_NAME, PackageManager.GET_META_DATA);
            Log.d(TAG, "isInstalledAppsPackage: versionCode " + packageInfo.versionCode);
            return packageInfo.versionCode >= HelperDefine.APPS_PACKAGE_VERSION;

        }
        catch( PackageManager.NameNotFoundException e )
        {
            e.printStackTrace();
            return false;
        }
    }

    static public boolean isEnabledAppsPackage(Context context) {
        //// TODO: 2017-08-16 Make sure the status is normal
        int status = context.getPackageManager().getApplicationEnabledSetting(HelperDefine.GALAXY_PACKAGE_NAME);
        Log.d(TAG, "isEnabledAppsPackage: status " + status);
        return !((status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) || (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER));
    }


    /**
     * check validation of installed Billing package in your device
     * @param _context
     * @return If it is true Billing package is valid. otherwise, is not valid.
     */
    static public boolean isValidAppsPackage( Context _context )
    {
        boolean result = true;
        try
        {
            Signature[] sigs = _context.getPackageManager().getPackageInfo(
                    HelperDefine.GALAXY_PACKAGE_NAME,
                    PackageManager.GET_SIGNATURES ).signatures;
            Log.d(TAG, "isValidAppsPackage: HASHCODE : " + sigs[0].hashCode());
            if( sigs[0].hashCode() != HelperDefine.APPS_SIGNATURE_HASHCODE )
            {
                result = false;
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    /**
     * SamsungAccount authentication
     * @param _activity
     */
    static public void startAccountActivity( final Activity _activity )
    {
        ComponentName com = new ComponentName( HelperDefine.GALAXY_PACKAGE_NAME,
                HelperDefine.IAP_PACKAGE_NAME + ".activity.AccountActivity" );
        Context context = _activity.getApplicationContext();

        Intent intent = new Intent();
        intent.setComponent( com );

        if(intent.resolveActivity(context.getPackageManager()) != null)
        {
            _activity.startActivityForResult(intent,
                    HelperDefine.REQUEST_CODE_IS_ACCOUNT_CERTIFICATION);
        }
    }

    /**
     * go to about page of SamsungApps in order to install IAP package.
     */
    static public void installAppsPackage( final BaseActivity _activity )
    {
        // 1. When user click the OK button on the dialog,
        //    go to SamsungApps IAP Detail page
        // ====================================================================

        Runnable OkBtnRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                Context context = _activity.getApplicationContext();

                // Link of SamsungApps for IAP install
                // ------------------------------------------------------------
                Uri appsDeepLink = Uri.parse(
                        "samsungapps://StoreVersionInfo/");
                // ------------------------------------------------------------

                Intent intent = new Intent();
                intent.setData( appsDeepLink );

                if( Build.VERSION.SDK_INT >= HelperDefine.HONEYCOMB_MR1 )
                {
                    intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            HelperDefine.FLAG_INCLUDE_STOPPED_PACKAGES );
                }
                else
                {
                    intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP );
                }

                if(intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
            }
        };
        // ====================================================================

        // 2. Set error in order to notify result to third-party application.
        // ====================================================================
        ErrorVo errorVo = new ErrorVo();
        _activity.setErrorVo( errorVo );

        errorVo.setError( HelperDefine.IAP_PAYMENT_IS_CANCELED,
                _activity.getString(R.string.mids_sapps_pop_payment_canceled) );
        // ====================================================================

        // 3. Show information dialog
        // ====================================================================
        HelperUtil.showIapDialogIfNeeded( _activity,
                _activity.getString( R.string.mids_sapps_header_update_galaxy_apps ),
                _activity.getString( R.string.mids_sapps_pop_a_new_version_is_available_galaxy_apps_will_be_updated_to_the_latest_version_to_complete_this_purchase ),
                true,
                OkBtnRunnable,
                true );
        // ====================================================================
    }
}
