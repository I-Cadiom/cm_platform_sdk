/*
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
package cyanogenmod.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;

import cyanogenmod.platform.R;

public class SelfRemovingListPreference extends ListPreference {

    private boolean mAvailable = true;

    public SelfRemovingListPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public SelfRemovingListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SelfRemovingListPreference(Context context) {
        super(context);
    }

    private void init(Context context, AttributeSet attrs) {
        mAvailable = Constraints.checkConstraints(context, attrs);
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        if (!mAvailable) {
            preferenceManager.getPreferenceScreen().removePreference(this);
        }
    }
}
