package com.pentaware.foodie.utils;

import com.bumptech.glide.request.RequestOptions;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.AppInfo;
import com.pentaware.foodie.models.CartItem;
import com.pentaware.foodie.models.User;

import java.util.ArrayList;
import java.util.List;

public class CommonVariables {
    public static AppInfo appInfo;

    public static User enteredUserDetails = new User();

    public static User loggedInUserDetails;
    public static Address loggedInUserAddress;

    public static String razorPayKeyId = "rzp_live_dANcm3iqiuijPN";

    public static String razorPayKeySecret = "jGi7L1YUE5VRBFtlfTIF6Ahp";

    public static String YOUTUBE_API_KEY = "AIzaSyBZG2asnd6X4rSweZMzs-D9x3eCA3XQI0Q";

    public static String MAIL_SENDER_FROM = "texpediscia@gmail.com";

    public static String MAIL_PWD = "shivyan@123";

    public static boolean buyNowOption = false;

    public static String tagToSearch = "";

    public static int PAGE_SIZE = 20;


    public static String m_sFirebaseUserId;

    public static double wageHours  = 8;

    public static String userName = "";

    public static String phone = "";

    public static String email = "";

//    public static Cart selectedCart = null;


//    public static List<Cart> cartlist = new ArrayList<>();
//    public static Map<String, String> mapEarnings = new HashMap<>();
//
//    public static Map<String, String> mapAdvance = new HashMap<>();


    public static String rupeeSymbol = "₹ ";

    public static Address deliveryAddress = new Address();

    //  public static boolean showProductsFromShop = false;

    public static String selectedProductCategory = "";

    public static List<String> tagList = new ArrayList<>();

    public static List<String> activeTagList = new ArrayList<>();

    public static String activeTagDocId;

    public static List<String> brandList = new ArrayList<>();

    public static int NumberOfPointsInOneRupee = 8;

    public static double percentOfAmountCreditedIntoPoints = 1.25;

    public static int deliveryCharges = 39;

    public static RequestOptions glideRequestOptions = new RequestOptions();

    public static List<CartItem> userCartItems;
}

