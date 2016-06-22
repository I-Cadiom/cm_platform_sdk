/**
 * Copyright (c) 2015, The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cyanogenmod.platform.internal;

import android.annotation.NonNull;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import android.util.Slog;
import cyanogenmod.app.CMContextConstants;
import cyanogenmod.app.CustomTile;
import cyanogenmod.app.ICustomTileListener;
import cyanogenmod.app.ICMStatusBarManager;

import org.cyanogenmod.internal.statusbar.ExternalQuickSettingsRecord;

/**
 * Internal service which manages interactions with system ui elements
 * @hide
 */
public class CMStatusBarManagerService extends BrokerableCMSystemService<ICMStatusBarManager> {
    private static final String TAG = "CMStatusBarManagerService";
    private static final ComponentName TARGET_IMPLEMENTATION_COMPONENT =
            new ComponentName("org.cyanogenmod.cmstatusbarservice",
                    "org.cyanogenmod.cmstatusbarservice.CMStatusBarManagerService");

    private Context mContext;

    public CMStatusBarManagerService(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected ICMStatusBarManager getIBinderAsIInterface(@NonNull IBinder service) {
        return ICMStatusBarManager.Stub.asInterface(service);
    }

    @Override
    protected ICMStatusBarManager getDefaultImplementation() {
        return mFailureServiceImpl;
    }

    @Override
    protected ComponentName getServiceComponent() {
        return TARGET_IMPLEMENTATION_COMPONENT;
    }

    @Override
    public String getFeatureDeclaration() {
        return CMContextConstants.Features.STATUSBAR;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "registerCMStatusBar cmstatusbar: " + this);
        publishBinderService(CMContextConstants.CM_STATUS_BAR_SERVICE, new BinderService());
    }

    @Override
    public void onBootPhase(int phase) {
        if (phase == PHASE_THIRD_PARTY_APPS_CAN_START) {
            tryConnecting();
        }
    }

    private final ICMStatusBarManager mFailureServiceImpl = new ICMStatusBarManager() {
        @Override
        public IBinder asBinder() {
            return null;
        }

        @Override
        public void createCustomTileWithTag(String pkg, String opPkg, String tag, int id,
                CustomTile tile, int[] idReceived, int userId) throws RemoteException {

        }

        @Override
        public void removeCustomTileWithTag(String pkg, String tag, int id, int userId)
                throws RemoteException {

        }

        @Override
        public void registerListener(ICustomTileListener listener, ComponentName component,
                int userid)
                throws RemoteException {

        }

        @Override
        public void unregisterListener(ICustomTileListener listener, int userid)
                throws RemoteException {

        }

        @Override
        public void removeCustomTileFromListener(ICustomTileListener listener, String pkg,
                String tag,
                int id) throws RemoteException {

        }
    };

    private final class BinderService extends ICMStatusBarManager.Stub {
        /**
         * @hide
         */
        @Override
        public void createCustomTileWithTag(String pkg, String opPkg, String tag, int id,
               CustomTile customTile, int[] idOut, int userId) throws RemoteException {
            enforceCustomTilePublish();
            ICMStatusBarManager icmStatusBarManager = getImplementingServiceGuarded();
            icmStatusBarManager.createCustomTileWithTag(pkg, opPkg,tag, id, customTile,
                    idOut, userId);
        }

        /**
         * @hide
         */
        @Override
        public void removeCustomTileWithTag(String pkg, String tag, int id, int userId) {
            checkCallerIsSystemOrSameApp(pkg);
            ICMStatusBarManager icmStatusBarManager = getImplementingServiceGuarded();
            try {
                icmStatusBarManager.removeCustomTileWithTag(pkg, tag, id, userId);
            } catch (RemoteException e) {
                Slog.e(TAG, e.toString());
            }
        }

        /**
         * Register a listener binder directly with the status bar manager.
         *
         * Only works with system callers. Apps should extend
         * {@link cyanogenmod.app.CustomTileListenerService}.
         * @hide
         */
        @Override
        public void registerListener(final ICustomTileListener listener,
                                     final ComponentName component, final int userid) {
            enforceBindCustomTileListener();
            ICMStatusBarManager icmStatusBarManager = getImplementingServiceGuarded();
            try {
                icmStatusBarManager.registerListener(listener, component, userid);
            } catch (RemoteException e) {
                Slog.e(TAG, e.toString());
            }
        }

        /**
         * Remove a listener binder directly
         * @hide
         */
        @Override
        public void unregisterListener(ICustomTileListener listener, int userid) {
            enforceBindCustomTileListener();
            ICMStatusBarManager icmStatusBarManager = getImplementingServiceGuarded();
            try {
                icmStatusBarManager.unregisterListener(listener, userid);
            } catch (RemoteException e) {
                Slog.e(TAG, e.toString());
            }
        }

        /**
         * Allow an ICustomTileListener to simulate clearing (dismissing) a single customTile.
         *
         * @param token The binder for the listener, to check that the caller is allowed
         */
        @Override
        public void removeCustomTileFromListener(ICustomTileListener token, String pkg,
               String tag, int id) {
            ICMStatusBarManager icmStatusBarManager = getImplementingServiceGuarded();
            try {
                icmStatusBarManager.removeCustomTileFromListener(token, pkg, tag, id);
            } catch (RemoteException e) {
                Slog.e(TAG, e.toString());
            }
        }
    };

    private static boolean isUidSystem(int uid) {
        final int appid = UserHandle.getAppId(uid);
        return (appid == android.os.Process.SYSTEM_UID
                || appid == android.os.Process.PHONE_UID || uid == 0);
    }

    private static boolean isCallerSystem() {
        return isUidSystem(Binder.getCallingUid());
    }

    private static void checkCallerIsSystemOrSameApp(String pkg) {
        if (isCallerSystem()) {
            return;
        }
        final int uid = Binder.getCallingUid();
        try {
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(
                    pkg, 0, UserHandle.getCallingUserId());
            if (ai == null) {
                throw new SecurityException("Unknown package " + pkg);
            }
            if (!UserHandle.isSameApp(ai.uid, uid)) {
                throw new SecurityException("Calling uid " + uid + " gave package"
                        + pkg + " which is owned by uid " + ai.uid);
            }
        } catch (RemoteException re) {
            throw new SecurityException("Unknown package " + pkg + "\n" + re);
        }
    }

    private void enforceCustomTilePublish() {
        mContext.enforceCallingOrSelfPermission(
                cyanogenmod.platform.Manifest.permission.PUBLISH_CUSTOM_TILE,
                "StatusBarManagerService");
    }

    private void enforceBindCustomTileListener() {
        mContext.enforceCallingOrSelfPermission(
                cyanogenmod.platform.Manifest.permission.BIND_CUSTOM_TILE_LISTENER_SERVICE,
                "StatusBarManagerService");
    }
}
