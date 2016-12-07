/**
 * Copyright (C) 2016 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cyanogenmod.internal.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public final class PackageUtils {
    private static final int FLAG_SUSPENDED = 1 << 30;

    private PackageUtils() {
        // This class is not supposed to be instantiated
    }

    /**
     * Checks whether a given package exists
     *
     * @return true if package exists
     */
    public static boolean isAppPresent(final Context context, final String packageName) {
        return getApplicationInfo(context, packageName, 0) != null;
    }

    /**
     * Checks whether a given package exists and is enabled
     *
     * @return true if package is enabled
     */
    public static boolean isAppEnabled(final Context context, final String packageName) {
        return isAppEnabled(context, packageName, 0);
    }

    /**
     * Checks whether a given package with the given flags exists and is enabled
     *
     * @return true if package is enabled
     */
    public static boolean isAppEnabled(final Context context,
                                       final String packageName, final int flags) {
        final ApplicationInfo info = getApplicationInfo(context, packageName, flags);
        return info != null && info.enabled;
    }

    /**
     * Checks if the app can possibly be on the SDCard.
     * This is just a workaround and doesn't guarantee that the app is on SD card.
     *
     * @return true if app is on SDCard
     */
    public static boolean isAppOnSdcard(final Context context, final String packageName) {
        return isAppEnabled(context, packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
    }

    /**
     * Checks if the app is suspended
     *
     * @return true if the app is suspended
     */
    public static boolean isAppSuspended(final Context context, final String packageName) {
        final ApplicationInfo info = getApplicationInfo(context, packageName, 0);
        return info != null && isAppSuspended(info);
    }

    /**
     * Checks if the app is suspended
     *
     * @return true if the app is suspended
     */
    public static boolean isAppSuspended(final ApplicationInfo info) {
        return info != null && (info.flags & FLAG_SUSPENDED) != 0;
    }

    /**
     * Gets the ApplicationInfo of the package
     *
     * @return null if package cannot be found or the ApplicationInfo is null
     */
    public static ApplicationInfo getApplicationInfo(final Context context,
                                              final String packageName, final int flags) {
        final PackageManager packageManager = context.getPackageManager();
        try {
            final ApplicationInfo info = packageManager.getApplicationInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return info;
    }
}
