package com.example.androidprojecttemplate.viewModels;


//import android.content.Intent;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;

//import com.example.androidprojecttemplate.models.DataForPantry;
//import com.example.androidprojecttemplate.models.IngredientData;
//import com.example.androidprojecttemplate.models.UserData;
import com.example.androidprojecttemplate.models.CookbookData;
import com.example.androidprojecttemplate.models.FirebaseDB;
import com.example.androidprojecttemplate.models.Pair;
import com.example.androidprojecttemplate.models.RecipeData;
import com.example.androidprojecttemplate.views.IngredientListPage;
import com.example.androidprojecttemplate.views.RecipeListPage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Array;
import java.util.*;
//import java.util.logging.Handler;

public class RecipeListViewModel {
    private static RecipeListViewModel instance;
    private final RecipeListPage theData;

    private FirebaseAuth theAuthenticationVariable;
    private FirebaseUser user;
    private DatabaseReference pantryRef;
    private DatabaseReference referenceForSpecificUser;
    private DatabaseReference referenceForRecipe;

    private String theUsersEmailFromAuthenticationDatabase;
    private String theReturnQuantity = null;

    private String temp = "hel";

    private int changer = 0;

    private Timer timer;

    public RecipeListViewModel() {
        theData = new RecipeListPage();
    }

    public static synchronized RecipeListViewModel getInstance() {
        if (instance == null) {
            instance = new RecipeListViewModel();
        }
        return instance;
    }

    public void getCurrentUser() {
        theAuthenticationVariable = FirebaseDB.getInstance().getFirebaseAuth();
        user = FirebaseDB.getInstance().getUser();
        theUsersEmailFromAuthenticationDatabase = FirebaseDB.getInstance().getEmail();
    }

    //
    public ArrayList<String[]> getRecipeIngredients(RecipeData recipe) {
        //Arraylist of String arrays to hold each ingredient and its quantities
        ArrayList<String[]> recipeQuantities = new ArrayList<>();

        //Should have a reference pointing directly at a recipe's ingredient list
        referenceForRecipe = FirebaseDatabase.getInstance().getReference()
                .child("Cookbook").child(recipe.getName()).child("ingredients");

        referenceForRecipe.addValueEventListener(new ValueEventListener() {
            //snapshot should be pointing the value inside of a recipe
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //This for each loop will iterate through each ingredient in the list
                for (DataSnapshot snapshots : snapshot.getChildren()) {
                    //Get the name and quantity
                    String name = snapshots.getKey();
                    String quantity = Integer.toString((int) snapshots.getValue());

                    //Store the name and quantity in the arraylist
                    recipeQuantities.add(new String[]{name, quantity});
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("RECIPE INGREDIENTS ERROR", error.toString());
            }
        });

        return recipeQuantities;
    }
  
  public ArrayList<String[]> getPantryIngredients() {
        //Arraylist of String arrays to hold each ingredient and its quantities
        ArrayList<String[]> pantryQuantities = new ArrayList<>();
    
    //Sets up the variables needed to authenticate user's data
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        String email = user.getEmail();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users");

        userRef.addValueEventListener(new ValueEventListener() {
            //The snapshot should be pointed to Users in the database
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Each child should be pointing to a specific user
                for (DataSnapshot snapshots : snapshot.getChildren()) {
                    //Grabs the email of the user in the snapshot
                    String theEmailFromFirebase = snapshots.child("username")
                            .getValue().toString();

                    //Checks if the email from the snapshot matches the email of the current user
                    if (theEmailFromFirebase.equals(email)) {
                        /*
                        Only runs the code the the emails matches, which means we are checking the
                        correct pantry
                        */

                        /*
                        Sets the reference to a user's Pantry Ingredients
                        Since the snapshot should be pointing at the correct user's pantry,
                        snapshots.child("Name").getValue().toString should just be the user's name
                        */
                        pantryRef = FirebaseDatabase.getInstance().getReference()
                                .child("Pantry").child(snapshots.child("name")
                                        .getValue().toString()).child("Ingredients");

                        //I don't like the idea of a nested event listener, but idk what else to do rn
                        pantryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //Each snapshot should be point at an individual ingredient in a
                                //user's pantry
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    //Get the pantry ingredient's name and quantity
                                    String name = snapshot1.getKey();
                                    String quantity = Integer.toString((int) snapshot1
                                            .child("quantity").getValue());

                                    //Add the name and quantity to the Arraylist of String[]
                                    pantryQuantities.add(new String[]{name, quantity});
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("PANTRY NESTED ERROR", error.toString());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("USER ERROR", error.toString());
            }
        });

        return pantryQuantities;
    }
}


// get hashmap of all ingredient:quantity from certain recipe
    /**
     * This method will return a hashmap of all the ingredients and their quantities
     * that are required for a certain recipe
     * @param recipeName
     * @return HashMap<String, String> ingredients
     */
    public HashMap<String, String> getRecipeIngredients(String recipeName) {
        referenceForRecipe = FirebaseDatabase.getInstance().getReference().child("Recipe");

        HashMap<String, String> ingredients = new HashMap<>();

        referenceForRecipe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot theSnapshot : snapshot.getChildren()) {
                    if (theSnapshot.child("name").getValue().toString().equals(recipeName)) {
                        for (DataSnapshot ingredient : theSnapshot.child("ingredients").getChildren()) {
                            ingredients.put(ingredient.getKey(), ingredient.getValue().toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", "Something went wrong");
            }
        });

        return ingredients;

    }


//get hashmap of all ingredient:quantity from ingredients list of current user
    /**
     * This method will return a hashmap of all the ingredients and their quantities
     * that the user has in their pantry
     * @return HashMap<String, String> ingredients
     */
    public HashMap<String, String> getIngredients() {
        referenceForPantry = FirebaseDatabase.getInstance().getReference().child("Pantry");

        HashMap<String, String> ingredients = new HashMap<>();

        referenceForPantry.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot theSnapshot : snapshot.getChildren()) {
                    String theEmailFromFirebase = theSnapshot.child("username").getValue().toString();
                    String theUsersName = theSnapshot.child("name").getValue().toString();

                    if (theEmailFromFirebase.equals(theUsersEmailFromAuthenticationDatabase)) {
                        referenceForSpecificUser = referenceForPantry.child(theUsersName).child("Ingredients");

                        referenceForSpecificUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ingredient : snapshot.getChildren()) {
                                    ingredients.put(ingredient.getKey(), ingredient.getValue().toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("Error", "Something went wrong");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", "Something went wrong");
            }
        });

        return ingredients;
    }


    /**
     * This method will return a hashmap of all the ingredients and their quantities
     * that are required for a certain recipe but are missing from the user's pantry
     * @param recipeName
     * @return HashMap<String, String> missingIngredients
     */
    public HashMap<String, String> getMissingIngredients(String recipeName) {
        HashMap<String, String> recipeIngredients = getRecipeIngredients(recipeName);
        HashMap<String, String> pantryIngredients = getIngredients();
        HashMap<String, String> missingIngredients = new HashMap<>();

        for (String ingredient : recipeIngredients.keySet()) {
            if (!pantryIngredients.containsKey(ingredient)) {
                missingIngredients.put(ingredient, recipeIngredients.get(ingredient));
            } else {
                int requiredQuantity = Integer.parseInt(recipeIngredients.get(ingredient));
                int pantryQuantity = Integer.parseInt(pantryIngredients.get(ingredient));

                if (pantryQuantity < requiredQuantity) {
                    missingIngredients.put(ingredient, Integer.toString(requiredQuantity - pantryQuantity));
                }
            }
        }

        return missingIngredients;
    }
        