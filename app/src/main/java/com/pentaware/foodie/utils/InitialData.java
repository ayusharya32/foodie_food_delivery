package com.pentaware.foodie.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.FoodCategory;
import com.pentaware.foodie.models.FoodItem;
import com.pentaware.foodie.models.FoodSubCategory;
import com.pentaware.foodie.models.Restaurant;
import com.pentaware.foodie.models.RestaurantCustomFoodCategory;
import com.pentaware.foodie.models.RestaurantOwner;
import com.pentaware.foodie.utils.enums.AddressType;
import com.pentaware.foodie.utils.enums.MealOfDay;
import com.pentaware.foodie.utils.enums.RestaurantType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class InitialData {
    private static final String TAG = "IntialDatayy";

    private static final String COLLECTION_RESTAURANTS = "restaurants";
    private static final String COLLECTION_RESTAURANT_OWNERS = "restaurant_owners";
    private static final String COLLECTION_ADDRESSES = "addresses";
    private static final String COLLECTION_SUB_CATEGORIES = "sub_categories";
    private static final String COLLECTION_CATEGORIES = "categories";
    private static final String COLLECTION_RESTAURANT_CUSTOM_FOOD_CATEGORIES
            = "restaurant_custom_food_categories";
    private static final String COLLECTION_FOOD_ITEMS = "food_items";

    private static final String OWNER_ID = "ZVZUXxQVHaYwsovQKZs2Hl5waA73";
    private static final String OWNER_NAME = "Manoj Singh";
    private static final String RESTAURANT_ID = "aa9118db-72c5-454f-8418-817e3517c22a";

    private static InitialData INSTANCE;

    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private InitialData() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static InitialData getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new InitialData();
        }

        return INSTANCE;
    }

    public void setupInitialData() {
//        uploadSubCategories();
//        uploadCategories();
//        uploadRestaurantCustomFoodCategories();

//        uploadRestaurant();
//        uploadRestaurantOwner();

//        uploadRestaurantFoodItems();
    }

    public void generateCoordinatedHashesForRestaurantsAndAddresses() {
        db.collection(COLLECTION_ADDRESSES)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            List<Address> addresses = task.getResult().toObjects(Address.class);
                            uploadAddressesWithHashes(addresses);
                        }
                    }
                });

        db.collection(COLLECTION_RESTAURANTS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            List<Restaurant> restaurants = task.getResult().toObjects(Restaurant.class);
                            uploadRestaurantsWithHashes(restaurants);
                        }
                    }
                });
    }

    private void uploadAddressesWithHashes(List<Address> addresses) {
        for(Address address: addresses) {
            address.locationCoordinateHash = CommonMethods.getCoordinateHash(
                    address.latitude, address.longitude
            );

            db.collection(COLLECTION_ADDRESSES)
                    .document(address.id)
                    .set(address)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Address Hashed");
                            }
                        }
                    });
        }
    }

    private void uploadRestaurantsWithHashes(List<Restaurant> restaurants) {
        for(Restaurant restaurant: restaurants) {
            restaurant.locationCoordinateHash = CommonMethods.getCoordinateHash(
                    restaurant.latitude, restaurant.longitude
            );

            db.collection(COLLECTION_RESTAURANTS)
                    .document(restaurant.id)
                    .set(restaurant)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Restaurant Hashed");
                            }
                        }
                    });
        }
    }

    private void uploadRestaurantFoodItems() {
        FoodItem handiPaneer = new FoodItem();
        handiPaneer.id = UUID.randomUUID().toString();
        handiPaneer.restaurantId = RESTAURANT_ID;
        handiPaneer.subCategoryId = "7fdcd0e1-1e69-45da-a529-e8699b17b988"; // Paneer
        handiPaneer.categoryId = "125ab535-c104-4105-bab7-a82730816085"; // North Indian
        handiPaneer.restaurantCustomCategoryId = "bed849eb-51c0-4230-9d04-c208ca01c71b"; // Main Course

        handiPaneer.name = "Handi Paneer";
        handiPaneer.description = "";
        handiPaneer.imageUrl = "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2Fpaneer-handi.webp?alt=media&token=b3a65112-ec70-4fb1-a4f7-ae3d8110d880";
        handiPaneer.price = 199;
        handiPaneer.discountPercentage = 0;
        handiPaneer.isNonVeg = false;
        handiPaneer.customizationOptions = null;
        handiPaneer.mealOfDay = Arrays.asList(MealOfDay.LUNCH, MealOfDay.DINNER);

        Log.d(TAG, "uploadRestaurantFoodItems: Handi Paneer " + handiPaneer);

        FoodItem kadhaiPaneer = new FoodItem();
        kadhaiPaneer.id = UUID.randomUUID().toString();
        kadhaiPaneer.restaurantId = RESTAURANT_ID;
        kadhaiPaneer.subCategoryId = "7fdcd0e1-1e69-45da-a529-e8699b17b988"; // Paneer
        kadhaiPaneer.categoryId = "125ab535-c104-4105-bab7-a82730816085"; // North Indian
        kadhaiPaneer.restaurantCustomCategoryId = "bed849eb-51c0-4230-9d04-c208ca01c71b"; // Main Course

        kadhaiPaneer.name = "Kadhai Paneer";
        kadhaiPaneer.description = "";
        kadhaiPaneer.imageUrl = "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2Fkadhai_paneer.jpg?alt=media&token=cf98d990-d21d-4e48-b80b-ec28f9d3dc1a";
        kadhaiPaneer.price = 179;
        kadhaiPaneer.discountPercentage = 0;
        kadhaiPaneer.isNonVeg = false;
        kadhaiPaneer.customizationOptions = null;
        kadhaiPaneer.mealOfDay = Arrays.asList(MealOfDay.LUNCH, MealOfDay.DINNER);

        FoodItem vegManchurian = new FoodItem();
        vegManchurian.id = UUID.randomUUID().toString();
        vegManchurian.restaurantId = RESTAURANT_ID;
        vegManchurian.subCategoryId = "4ba831e1-4c9e-4ac3-b6e2-1db6d451aaa3"; // Manchurian
        vegManchurian.categoryId = "dcd17780-cd47-463d-a033-fa11a8cb2d08"; // Chinese
        vegManchurian.restaurantCustomCategoryId = "1c39a4d2-0a40-4c1c-8543-b99e7bb09cd9"; // Starters

        vegManchurian.name = "Veg Manchurian";
        vegManchurian.description = "";
        vegManchurian.imageUrl = "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2FVeg-Manchurian-FI-1.jpg?alt=media&token=f2a7311b-d0c3-4d35-8690-3a58ce6be0f3";
        vegManchurian.price = 149;
        vegManchurian.discountPercentage = 0;
        vegManchurian.isNonVeg = false;
        vegManchurian.customizationOptions = null;
        vegManchurian.mealOfDay = Arrays.asList(MealOfDay.LUNCH, MealOfDay.DINNER, MealOfDay.SNACKS);

        FoodItem vegSpringRoll = new FoodItem();
        vegSpringRoll.id = UUID.randomUUID().toString();
        vegSpringRoll.restaurantId = RESTAURANT_ID;
        vegSpringRoll.subCategoryId = "21c05f61-e6bc-4898-bbaa-cbcfcde682f3"; // Spring Roll
        vegSpringRoll.categoryId = "dcd17780-cd47-463d-a033-fa11a8cb2d08"; // Chinese
        vegSpringRoll.restaurantCustomCategoryId = "1c39a4d2-0a40-4c1c-8543-b99e7bb09cd9"; // Starters

        vegSpringRoll.name = "Veg Spring Roll";
        vegSpringRoll.description = "";
        vegSpringRoll.imageUrl = "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2FSpring-Roll-5.jpg?alt=media&token=b67a62f4-66b1-45e0-9d1b-60b9ca206b61";
        vegSpringRoll.price = 119;
        vegSpringRoll.discountPercentage = 0;
        vegSpringRoll.isNonVeg = false;
        vegSpringRoll.customizationOptions = null;
        vegSpringRoll.mealOfDay = Arrays.asList(MealOfDay.LUNCH, MealOfDay.DINNER, MealOfDay.SNACKS);

        List<FoodItem> foodItems = Arrays.asList(handiPaneer, kadhaiPaneer, vegManchurian, vegSpringRoll);

        Log.d(TAG, "uploadRestaurantFoodItems: " + foodItems);
        for(FoodItem item: foodItems) {
            db.collection(COLLECTION_FOOD_ITEMS)
                    .document(item.id)
                    .set(item)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Food Item added");
                            }
                        }
                    });
        }

    }

    private void uploadRestaurant() {
        Restaurant restaurant = getRestaurant();

        db.collection(COLLECTION_RESTAURANTS)
                .document(restaurant.id)
                .set(restaurant)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Upload Restaurant Successful");
                        }
                    }
                });
    }

    private void uploadRestaurantOwner() {
        Address ownerAddress = getRestaurantOwnerAddress();
        RestaurantOwner restaurantOwner = getRestaurantOwner(ownerAddress);

        db.collection(COLLECTION_RESTAURANT_OWNERS)
                .document(restaurantOwner.id)
                .set(restaurantOwner)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Upload Restaurant Owner Successful");
                        }
                    }
                });

        uploadOwnerAddress(ownerAddress);
    }

    private void uploadOwnerAddress(Address address) {
        db.collection(COLLECTION_ADDRESSES)
                .document(address.id)
                .set(address)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Upload Restaurant Owner Address Successful");
                        }
                    }
                });
    }

    private Restaurant getRestaurant() {
        Restaurant restaurant = new Restaurant();

        restaurant.id = UUID.randomUUID().toString();

        restaurant.ownerId = OWNER_ID;
        restaurant.name = "Firefly";

        restaurant.phone = "5555555555";
        restaurant.addressLine1 = "Kapoorthala Chauraha";
        restaurant.addressLine2 = "Kapoorthala";
        restaurant.city = "Lucknow";
        restaurant.state = "Uttar Pradesh";
        restaurant.pinCode = "226024";
        restaurant.latitude = 26.881643;
        restaurant.longitude = 80.948071;

        restaurant.mainCategories = Arrays.asList("Italian", "Coffee", "Chinese");
        restaurant.rating = 0.0f;
        restaurant.totalReviews = 0;

        restaurant.dineInAvailable = true;
        restaurant.type = RestaurantType.CAFE;

        restaurant.restaurantImages = Arrays.asList(
            "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2Frest_1.webp?alt=media&token=e0d1deb0-5759-42c1-826a-a1151c4e5c50",
            "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2Frest_3.jpg?alt=media&token=64e2f35d-5e0e-44e7-833d-07b61e5e4c5c"
        );

        restaurant.menuImages = Arrays.asList(
            "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2Fmenu_1.webp?alt=media&token=5af7f8f0-3616-4b27-981b-fe24bf4cd423",
            "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2Fmenu_2.webp?alt=media&token=e511ce52-489c-43b3-8231-4a26f08d01eb"
        );

         return restaurant;
    }

    private RestaurantOwner getRestaurantOwner(Address address) {
        RestaurantOwner owner = new RestaurantOwner();

        owner.id = OWNER_ID;
        owner.name = OWNER_NAME;
        owner.email = "manoj@gmail.com";
        owner.phone = "8484848484";
        owner.gender = "Male";
        owner.dob = "17/04/1987";

        owner.primaryAddressId = address.id;

        return owner;
    }

    private Address getRestaurantOwnerAddress() {
        Address restaurantOwnerAddress = new Address();

        restaurantOwnerAddress.id = UUID.randomUUID().toString();
        restaurantOwnerAddress.userId = OWNER_ID;
        restaurantOwnerAddress.name = OWNER_NAME;
        restaurantOwnerAddress.phone = "8484848484";
        restaurantOwnerAddress.addressLine1 = "Chandralok";
        restaurantOwnerAddress.addressLine2 = "Kapoorthala";
        restaurantOwnerAddress.city = "Lucknow";
        restaurantOwnerAddress.state = "Uttar Pradesh";
        restaurantOwnerAddress.pinCode = "226024";
        restaurantOwnerAddress.latitude = 26.881404;
        restaurantOwnerAddress.longitude = 80.942739;
        restaurantOwnerAddress.addressType = AddressType.HOME;

        return restaurantOwnerAddress;
    }

    private void uploadSubCategories() {
        List<FoodSubCategory> subCategoryList = Arrays.asList(
//                new FoodSubCategory(UUID.randomUUID().toString(), "Coffee", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2FCoffee.jpg?alt=media&token=8702ebdf-e344-4c10-9bc1-67da70cad990"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Burger", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Fburger.jpg?alt=media&token=ef17a320-adcd-42fe-ac28-9a6c9ee4a719"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Dosa", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Fdosa.jpg?alt=media&token=eed27022-117b-4658-a891-556b26a087a0"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Non Veg Biryani", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Fnon%20veg%20biryani.jpg?alt=media&token=f32eef7c-111f-478f-b1bd-827a653ec962"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Paneer", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Fpaneer.jpg?alt=media&token=1a9ead1d-a89a-495e-8488-4d1994e291e7"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Parathas", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Fparathas.jpg?alt=media&token=16b8e26d-ca89-44ee-955a-efbf22277aac"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Pizza", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Fpizza.jpg?alt=media&token=642000e6-6cf0-4ccb-b546-1d206c183930"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Rolls", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Frolls.jpg?alt=media&token=cc076cc7-7926-4d9a-b17a-078f6bd59916"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Shakes", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Fshakes.webp?alt=media&token=98095cee-7414-4c61-ab07-22cad15c0d15"),
//                new FoodSubCategory(UUID.randomUUID().toString(), "Veg Biryani", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/subcategories%2Fveg-biryani.jpg?alt=media&token=5d0eaafc-4b1a-4f41-9c73-7e26d0e1f62a")
                new FoodSubCategory(UUID.randomUUID().toString(), "Manchurian", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2FVeg-Manchurian-FI-1.jpg?alt=media&token=f2a7311b-d0c3-4d35-8690-3a58ce6be0f3"),
                new FoodSubCategory(UUID.randomUUID().toString(), "Spring Roll", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/restaurant%2Fmoti_mahal%2FSpring-Roll-5.jpg?alt=media&token=b67a62f4-66b1-45e0-9d1b-60b9ca206b61")
        );

        for(FoodSubCategory subCategory: subCategoryList) {
            db.collection(COLLECTION_SUB_CATEGORIES)
                    .document(subCategory.id)
                    .set(subCategory)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Food Sub Category added successfully");
                            }
                        }
                    });
        }
    }

    private void uploadCategories() {
        List<FoodCategory> categoryList = Arrays.asList(
                new FoodCategory(UUID.randomUUID().toString(), "North Indian", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/categories%2FNorth-Indian-Food.webp?alt=media&token=3c363bdb-3da9-4f52-97bd-0ef45131f39e"),
                new FoodCategory(UUID.randomUUID().toString(), "Chinese", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/categories%2Fchinese.webp?alt=media&token=3fc530bb-c8e0-40bf-b4d1-c35e51c0fc21"),
                new FoodCategory(UUID.randomUUID().toString(), "Italian", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/categories%2Fitalian.jpg?alt=media&token=db30d78f-7ef2-4862-94fd-57b52fed337c"),
                new FoodCategory(UUID.randomUUID().toString(), "Mexican", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/categories%2Fmexican.jpg?alt=media&token=2310f98b-231e-4f9a-a072-c03d2cd7fa09"),
                new FoodCategory(UUID.randomUUID().toString(), "South Indian", "https://firebasestorage.googleapis.com/v0/b/foodie-49af9.appspot.com/o/categories%2Fsouth%20indian.jpg?alt=media&token=933b2676-4453-4e4d-8b1b-059d8cbdd071")
        );

        for(FoodCategory category: categoryList) {
            db.collection(COLLECTION_CATEGORIES)
                    .document(category.id)
                    .set(category)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Food Category added successfully");
                            }
                        }
                    });
        }
    }

    private void uploadRestaurantCustomFoodCategories() {
        List<RestaurantCustomFoodCategory> foodCategories = Arrays.asList(
                new RestaurantCustomFoodCategory(UUID.randomUUID().toString(), "Starters", RESTAURANT_ID),
                new RestaurantCustomFoodCategory(UUID.randomUUID().toString(), "Main Course", RESTAURANT_ID),
                new RestaurantCustomFoodCategory(UUID.randomUUID().toString(), "Drinks and Beverages", RESTAURANT_ID)
        );

        for(RestaurantCustomFoodCategory category: foodCategories) {
            db.collection(COLLECTION_RESTAURANT_CUSTOM_FOOD_CATEGORIES)
                    .document(category.id)
                    .set(category)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: Custom Food Category added Successfully");
                        }
                    });
        }

    }
}
