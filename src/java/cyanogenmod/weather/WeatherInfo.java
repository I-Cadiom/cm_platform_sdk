/*
 * Copyright (C) 2016 The CyanongenMod Project
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

package cyanogenmod.weather;

import android.os.Parcel;
import android.os.Parcelable;
import cyanogenmod.os.Build;
import cyanogenmod.weatherservice.ServiceRequest;
import cyanogenmod.weatherservice.ServiceRequestResult;

import java.util.ArrayList;

/**
 * This class represents the weather information that a
 * {@link cyanogenmod.weatherservice.WeatherProviderService} will use to update the weather content
 * provider. A weather provider service will be called by the system to process an update
 * request at any time. If the service successfully processes the request, then the weather provider
 * service is responsible of calling
 * {@link ServiceRequest#complete(ServiceRequestResult)} to notify the
 * system that the request was completed and that the weather content provider should be updated
 * with the supplied weather information.
 */
public final class WeatherInfo implements Parcelable {

    private String mCityId;
    private String mCity;
    private String mCondition;
    private int mConditionCode;
    private float mTemperature;
    private int mTempUnit;
    private float mHumidity;
    private float mWindSpeed;
    private float mWindDirection;
    private int mWindSpeedUnit;
    private long mTimestamp;
    private ArrayList<DayForecast> mForecastList;

    public WeatherInfo(String cityId, String city, String condition, int conditionCode, float temp,
                       int tempUnit, float humidity, float windSpeed, float windDir,
                       int speedUnit, ArrayList<DayForecast> forecasts, long timestamp) {
        this.mCityId = cityId;
        this.mCity = city;
        this.mCondition = condition;
        this.mConditionCode = conditionCode;
        this.mHumidity = humidity;
        this.mWindSpeed = windSpeed;
        this.mWindDirection = windDir;
        this.mWindSpeedUnit = speedUnit;
        this.mTimestamp = timestamp;
        this.mTemperature = temp;
        this.mTempUnit = tempUnit;
        this.mForecastList = forecasts;
    }

    /**
     * @return city id
     */
    public String getCityId() {
        return mCityId;
    }

    /**
     * @return city name
     */
    public String getCity() {
        return mCity;
    }

    /**
     * @return An implementation specific weather condition code
     */
    public int getConditionCode() {
        return mConditionCode;
    }

    /**
     * @return weather condition
     */
    public String getCondition() {
        return mCondition;
    }

    /**
     * @return humidity
     */
    public float getHumidity() {
        return mHumidity;
    }

    /**
     * @return time stamp when the request was processed
     */
    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     * @return wind direction (degrees)
     */
    public float getWindDirection() {
        return mWindDirection;
    }

    /**
     * @return wind speed
     */
    public float getWindSpeed() {
        return mWindSpeed;
    }

    /**
     * @return wind speed unit
     */
    public int getWindSpeedUnit() {
        return mWindSpeedUnit;
    }

    /**
     * @return current temperature
     */
    public float getTemperature() {
        return mTemperature;
    }

    /**
     * @return temperature unit
     */
    public int getTemperatureUnit() {
        return mTempUnit;
    }

    /**
     * @return List of {@link cyanogenmod.weather.WeatherInfo.DayForecast}
     */
    public ArrayList<DayForecast> getForecasts() {
        return mForecastList;
    }

    private WeatherInfo(Parcel parcel) {
        int parcelableVersion = parcel.readInt();
        int parcelableSize = parcel.readInt();
        int startPosition = parcel.dataPosition();
        if (parcelableVersion >= Build.CM_VERSION_CODES.ELDERBERRY) {
            mCityId = parcel.readString();
            mCity = parcel.readString();
            mCondition = parcel.readString();
            mConditionCode = parcel.readInt();
            mTemperature = parcel.readFloat();
            mTempUnit = parcel.readInt();
            mHumidity = parcel.readFloat();
            mWindSpeed = parcel.readFloat();
            mWindDirection = parcel.readInt();
            mWindSpeedUnit = parcel.readInt();
            mTimestamp = parcel.readLong();
            int forecastListSize = parcel.readInt();
            mForecastList = new ArrayList<>();
            while (forecastListSize > 0) {
                mForecastList.add(DayForecast.CREATOR.createFromParcel(parcel));
                forecastListSize--;
            }
        }
        parcel.setDataPosition(startPosition + parcelableSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Build.PARCELABLE_VERSION);

        int sizePosition = dest.dataPosition();
        dest.writeInt(0);
        int startPosition = dest.dataPosition();

        // ==== ELDERBERRY =====
        dest.writeString(mCityId);
        dest.writeString(mCity);
        dest.writeString(mCondition);
        dest.writeInt(mConditionCode);
        dest.writeFloat(mTemperature);
        dest.writeInt(mTempUnit);
        dest.writeFloat(mHumidity);
        dest.writeFloat(mWindSpeed);
        dest.writeFloat(mWindDirection);
        dest.writeInt(mWindSpeedUnit);
        dest.writeLong(mTimestamp);
        dest.writeInt(mForecastList.size());
        for (DayForecast dayForecast : mForecastList) {
            dayForecast.writeToParcel(dest, 0);
        }

        int parcelableSize = dest.dataPosition() - startPosition;
        dest.setDataPosition(sizePosition);
        dest.writeInt(parcelableSize);
        dest.setDataPosition(startPosition + parcelableSize);
    }

    public static final Parcelable.Creator<WeatherInfo> CREATOR =
            new Parcelable.Creator<WeatherInfo>() {

                @Override
                public WeatherInfo createFromParcel(Parcel source) {
                    return new WeatherInfo(source);
                }

                @Override
                public WeatherInfo[] newArray(int size) {
                    return new WeatherInfo[size];
                }
            };

    /**
     * This class represents the weather forecast for a given day
     */
    public static class DayForecast implements Parcelable{
        float mLow, mHigh;
        int mConditionCode;
        String mCondition;

        public DayForecast(float low, float high, String condition, int conditionCode) {
            this.mLow = low;
            this.mHigh = high;
            this.mCondition = condition;
            this.mConditionCode = conditionCode;
        }

        /**
         * @return forecasted low temperature
         */
        public float getLow() {
            return mLow;
        }

        /**
         * @return not what you think. Returns the forecasted high temperature
         */
        public float getHigh() {
            return mHigh;
        }

        /**
         * @return forecasted weather condition
         */
        public String getCondition() {
            return mCondition;
        }

        /**
         * @return forecasted weather condition code. Implementation specific
         */
        public int getConditionCode() {
            return mConditionCode;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(Build.PARCELABLE_VERSION);

            int sizePosition = dest.dataPosition();
            dest.writeInt(0);
            int startPosition = dest.dataPosition();

            // ==== ELDERBERRY =====
            dest.writeFloat(mLow);
            dest.writeFloat(mHigh);
            dest.writeInt(mConditionCode);
            dest.writeString(mCondition);

            int parcelableSize = dest.dataPosition() - startPosition;
            dest.setDataPosition(sizePosition);
            dest.writeInt(parcelableSize);
            dest.setDataPosition(startPosition + parcelableSize);
        }

        public static final Parcelable.Creator<DayForecast> CREATOR =
                new Parcelable.Creator<DayForecast>() {
                    @Override
                    public DayForecast createFromParcel(Parcel source) {
                        return new DayForecast(source);
                    }

                    @Override
                    public DayForecast[] newArray(int size) {
                        return new DayForecast[size];
                    }
                };

        private DayForecast(Parcel parcel) {
            int parcelableVersion = parcel.readInt();
            int parcelableSize = parcel.readInt();
            int startPosition = parcel.dataPosition();
            if (parcelableVersion >= Build.CM_VERSION_CODES.ELDERBERRY) {
                mLow = parcel.readFloat();
                mHigh = parcel.readFloat();
                mConditionCode = parcel.readInt();
                mCondition = parcel.readString();
            }
            parcel.setDataPosition(startPosition + parcelableSize);
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("{Low temp: ").append(mLow)
                    .append(" High temp: ").append(mHigh)
                    .append(" Condition code: ").append(mConditionCode)
                    .append(" Condition: ").append(mCondition)
                    .append("}").toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
            .append("{CityId: ").append(mCityId)
            .append(" City Name: ").append(mCity)
            .append(" Condition: ").append(mCondition)
            .append(" Condition Code: ").append(mConditionCode)
            .append(" Temperature: ").append(mTemperature)
            .append(" Temperature Unit: ").append(mTempUnit)
            .append(" Humidity: ").append(mHumidity)
            .append(" Wind speed: ").append(mWindSpeed)
            .append(" Wind direction: ").append(mWindDirection)
            .append(" Wind Speed Unit: ").append(mWindSpeedUnit)
            .append(" Timestamp: ").append(mTimestamp).append(" Forecasts: [");
        for (DayForecast dayForecast : mForecastList) {
            builder.append(dayForecast.toString());
        }
        return builder.append("]}").toString();
    }
}