package com.pentaware.foodie.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.pentaware.foodie.R;
import com.pentaware.foodie.models.maps.LocationCoordinates;
import com.pentaware.foodie.models.maps.distance.Distance;
import com.pentaware.foodie.models.maps.distance.DistanceResponse;
import com.pentaware.foodie.models.maps.distance.Element;
import com.pentaware.foodie.utils.enums.DeliveryPartnerStatus;
import com.pentaware.foodie.utils.enums.LocationType;
import com.pentaware.foodie.utils.enums.OrderStatus;
import com.pentaware.foodie.utils.enums.PaymentMethod;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommonMethods {

    public static String getOTP() {
        StringBuilder otp = new StringBuilder();

        for(int i=0 ; i<6; i++) {
            double randomNum = Math.random();
            int num = (int) (randomNum * 10);

            otp.append(num);
        }

        return otp.toString();
    }

    public static String getCoordinateHash(double latitude, double longitude) {
        return GeoFireUtils.getGeoHashForLocation(
                new GeoLocation(latitude, longitude)
        );
    }

    public static String getDecimalValueUpToOnePlace(double value) {
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(value);
    }

    public static String getCoordinateStringFromLocation(LocationCoordinates location) {
        return location.getLat() + "," + location.getLng();
    }

    public static String getCoordinateStringFromLocation(double latitude, double longitude) {
        return latitude + "," + longitude;
    }

    public static String getOrderStatusString(OrderStatus status) {
        switch(status) {
            case WAITING_FOR_CONFIRMATION:
                return "Waiting for Restaurant Confirmation";

            case BEING_PREPARED:
                return "Order is being prepared";

            case PREPARED:
                return "Order prepared, waiting for pickup";

            case WAY_TO_DELIVERY:
                return "Order on way for delivery";

            case DELIVERED:
                return "Order Delivered";

            default:
                return "Order Cancelled";
        }
    }

    public static String getPaymentMethodString(PaymentMethod method) {
        switch(method) {
            case CASH_ON_DELIVERY:
                return "Cash on Delivery";

            default:
                return "Prepaid";
        }
    }

    public static String getFormattedDateTime(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.UK);
        return sdf.format(date);
    }

    public static boolean appInBackground() {
        ActivityManager.RunningAppProcessInfo myProcess = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(myProcess);
        return myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }

    public static float getDeliveryCharges(DistanceResponse distanceResponse) {
        float deliveryCharges = 0;

        Element element = distanceResponse.rows.get(0).elements.get(0);
        Distance distance = element.distance;

        long distanceInMetre = distance.value;
        if(distanceInMetre > 5000) {
            deliveryCharges = 30;
        }

        return deliveryCharges;
    }

    public static String getDeliveryTimeString(DistanceResponse distanceResponse) {
        Element element = distanceResponse.rows.get(0).elements.get(0);

        long deliveryDuration = element.duration.value;

        if(deliveryDuration > 3600) {
            return TimeUnit.SECONDS.toHours(element.duration.value) + " hrs " +
                    TimeUnit.SECONDS.toMinutes(element.duration.value) + " mins ";
        }

        return TimeUnit.SECONDS.toMinutes((long) (element.duration.value * 1.5)) + " mins";
    }

    public static String getOrderStatusString(OrderStatus orderStatus,
                                              DeliveryPartnerStatus deliveryPartnerStatus) {
        if(orderStatus == OrderStatus.WAITING_FOR_CONFIRMATION) {
            return "Waiting for Restaurant Confirmation";
        }

        if(orderStatus == OrderStatus.BEING_PREPARED) {
            return "Food is being prepared";
        }

        if(orderStatus == OrderStatus.PREPARED) {
            if(deliveryPartnerStatus == DeliveryPartnerStatus.WAITING_FOR_CONFIRMATION
                    || deliveryPartnerStatus == DeliveryPartnerStatus.WAY_TO_RESTAURANT) {
                return "Food Prepared, Waiting for Pickup";
            }
        }

        if(orderStatus == OrderStatus.WAY_TO_DELIVERY) {
            if(deliveryPartnerStatus == DeliveryPartnerStatus.WAY_TO_DELIVERY_LOCATION) {
                return "Food is on way for delivery";
            }

            if(deliveryPartnerStatus == DeliveryPartnerStatus.REACHED_DELIVERY_LOCATION) {
                return "Your order has reached delivery location, please meet out delivery" +
                        " partner to get your order";
            }
        }

        if(orderStatus == OrderStatus.DELIVERED) {
            return "Order Delivered";
        }

        return "Unknown Status";
    }

    public static LocationType getLocationType(OrderStatus orderStatus,
                                               DeliveryPartnerStatus deliveryPartnerStatus) {
        switch(orderStatus) {
            case WAITING_FOR_CONFIRMATION:
            case BEING_PREPARED:
                return LocationType.RESTAURANT;

            default:
                if(deliveryPartnerStatus == DeliveryPartnerStatus.WAITING_FOR_CONFIRMATION) {
                    return LocationType.RESTAURANT;
                } else {
                    return LocationType.DELIVERY_PARTNER;
                }
        }
    }

    public static Bitmap getResizedBitmap(Context context, int bitmapResourceId,
                                          int height, int width) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) AppCompatResources.getDrawable(
                context, bitmapResourceId);

        Bitmap bmp = bitmapDrawable.getBitmap();
        return Bitmap.createScaledBitmap(bmp, width, height, false);
    }
}
