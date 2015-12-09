/*
 * Copyright (C) 2015 The Cyanogen, Inc
 */
package cyanogenmod.externalviews;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.AttributeSet;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public final class LiveLockExternalView extends ExternalView {

    public static final String EXTRA_GRANT_PERMISSION_RECEIVER = "grant_permission_result_receiver";
    public static final String EXTRA_PERMISSION_LIST = "permissions_name_list";
    public static final String EXTRA_GRANT_PERMISSION_RESULT_LIST = "permissions_grant_result_list";

    private static final String TAG = LiveLockExternalView.class.getSimpleName();
    private static final String LIVE_LOCK_FILTER_CATEGORY = "org.cyanogenmod.intent.category.LIVE_LOCK";
    private boolean DEBUG = true;

    public LiveLockExternalView(Context context, AttributeSet attrs) {
        this(context,attrs,getComponentFromAttribute(attrs));
    }

    public LiveLockExternalView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context,attrs);
    }

    public LiveLockExternalView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context,attrs);
    }

    public LiveLockExternalView(Context context, AttributeSet attributeSet, ComponentName componentName) {
        super(context,attributeSet,componentName,false);
        checkPermissions();
    }

    private void checkPermissions() {
        LinkedList<String> permissionsNotGranted = new LinkedList<String>();
        try {
            if (DEBUG) Log.d(TAG,"Checking permissions for package " + mExtensionComponent.getPackageName());
            PackageInfo pkgInfo = mContext.getPackageManager().
                getPackageInfo(mExtensionComponent.getPackageName(),
                               PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions  = pkgInfo.requestedPermissions;
            int[] requestedPermissionsFlags = pkgInfo.requestedPermissionsFlags;

            for (int indx = 0 ; indx < requestedPermissions.length ; indx++) {
                if ((requestedPermissionsFlags[indx] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                    try {
                        PermissionInfo pi = mContext.getPackageManager().getPermissionInfo(requestedPermissions[indx],0);
                        if (DEBUG) Log.d(TAG,"Permission " + pi.name + " Protection Level " + pi.protectionLevel);

                        if (pi.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                            permissionsNotGranted.add(requestedPermissions[indx]);
                            if (DEBUG) Log.d(TAG,"Permission " + requestedPermissions[indx] + " has not been granted");
                        }
                    } catch(NameNotFoundException e) {
                        //TODO: What to do if PM can't find the permission??
                        if (DEBUG) {
                            Log.w(TAG,"Permission " + requestedPermissions[indx] + " not found. "
                              + "Unable to confirm if has been granted");
                        }
                    }
                } else {
                    if (DEBUG) Log.d(TAG,"Permission " + requestedPermissions[indx] + " has been already granted");
                }
            }

            if (permissionsNotGranted.size() == 0) {
                bindToService();
            } else {
                Intent rIntent = new Intent()
                        .setPackage(mExtensionComponent.getPackageName())
                        .addCategory(LIVE_LOCK_FILTER_CATEGORY);

                List<ResolveInfo> resolveInfo = mContext.getPackageManager().
                        queryIntentActivities(rIntent,PackageManager.GET_RESOLVED_FILTER);

                if (resolveInfo.size() >= 1) {
                    if (DEBUG) {
                        //TODO: Move this code out of the DEBUG block if
                        //we really must act if the condition is met
                        if (resolveInfo.size() >= 2) {
                            Log.w(TAG,"Got " + resolveInfo.size() + " resolvers! Defaulting to "
                                  + resolveInfo.get(0).activityInfo.name);
                        }
                    }

                    Intent mIntent = new Intent()
                            .setComponent(new ComponentName(mExtensionComponent.getPackageName(),resolveInfo.get(0).activityInfo.name))
                            .addCategory(LIVE_LOCK_FILTER_CATEGORY)
                            .setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            .putExtra(EXTRA_GRANT_PERMISSION_RECEIVER, new GrantPermissionResultReceiver())
                            .putExtra(EXTRA_PERMISSION_LIST,
                                    permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]));
                    mContext.startActivity(mIntent);
                } else {
                    //TODO: No resolvers == no way to let the user know permissions have not been granted.
                    //Should we bind the service anyway?
                }
            }
        } catch (NameNotFoundException e) {
            //TODO: The component name was not found. Should we throw an exception here?
            Log.e(TAG,"Package not found! " + e);
        }
    }

    private class GrantPermissionResultReceiver extends ResultReceiver {
        public GrantPermissionResultReceiver() {
            super(null);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            int[] permissionsGranted = resultData.getIntArray(EXTRA_GRANT_PERMISSION_RESULT_LIST);
            String[] permissions = resultData.getStringArray(EXTRA_PERMISSION_LIST);
            for (int indx = 0 ; indx < permissionsGranted.length; indx++) {
                if (permissionsGranted[indx] != PackageManager.PERMISSION_GRANTED) {
                    //TODO: Do we want(need) to let the user know that some functionality
                    //might not work properly (or at all!) because the permission was revoked?
                    if (DEBUG) Log.w(TAG,"Permission " + permissions[indx] + " was not granted");
                }
            }
            bindToService();
        }
    }
}
