package com.example.androidprojecttemplate.views;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.androidprojecttemplate.R;

import com.example.androidprojecttemplate.models.IngredientData;
import com.example.androidprojecttemplate.models.MealData;
import com.example.androidprojecttemplate.models.Pair;
import com.example.androidprojecttemplate.models.RecipeData;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RecipePage extends AppCompatActivity {
    // ui
    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;

    private NavigationView navView;

    private LinearLayout ingredientContainer;
    private Button addIngredient;
    private Button submit;
    private final int MAX_INGREDIENTS = 5;
    private int ingredientCount;
    private ArrayList<EditText> ingredients;
    private ArrayList<EditText> quantities;

    public void createIngredientRow() {
        // create row
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);

        // row elements
        EditText input = new EditText(this);
        EditText quantityInput = new EditText(this);
        Button removeButton = new Button(this);

        row.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = row.getWidth();
                Log.d("test", String.valueOf(width));
            }
        });

        removeButton.setLayoutParams(new ViewGroup.LayoutParams(
                205,
                ViewGroup.LayoutParams.MATCH_PARENT));

        quantityInput.setLayoutParams(new ViewGroup.LayoutParams(
                205,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        input.setLayoutParams(new ViewGroup.LayoutParams(
                959,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        removeButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int cornerRadius = removeButton.getHeight() / 2;

                GradientDrawable drawable = new GradientDrawable();

                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(cornerRadius);

                drawable.setColor(Color.rgb(102, 80, 164)); // purple

                removeButton.setPadding(40, 4, 40, 4);
                removeButton.setBackground(drawable);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientContainer.removeView(row);
                ingredients.remove(input);
                quantities.remove(quantityInput);
                ingredientCount--;
            }
        });

        removeButton.setAllCaps(false);
        removeButton.setTextColor(Color.WHITE);
        removeButton.setText("-");
        removeButton.setTextSize(14);

        input.setHint("ingredient");

        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityInput.setHint("0");

        row.addView(input);
        row.addView(quantityInput);
        row.addView(removeButton);

        ingredientContainer.addView(row);

        ingredients.add(input);
        quantities.add(quantityInput);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page);

        // navbar
        Toolbar homeToolBar = (Toolbar) findViewById(R.id.nav_toolbar);
        setSupportActionBar(homeToolBar);

        // nav menu
        dl = (DrawerLayout) findViewById(R.id.dl);
        abdt = new ActionBarDrawerToggle(this, dl, R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);

        dl.addDrawerListener(abdt);
        abdt.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setVisibility(View.GONE);
        navView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                System.out.println(R.id.inputmeal);
                int id = item.getItemId();
                if (id == R.id.inputmeal) {
                    if (!(RecipePage.this instanceof RecipePage)) {
                        Intent intent = new Intent(RecipePage.this, InputMealPage.class);
                        startActivity(intent);
                    }
                    return true;
                } else if (id == R.id.recipe) {
                    Intent intent = new Intent(RecipePage.this, RecipePage.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.ingredient) {
                    Intent intent = new Intent(RecipePage.this, IngredientPage.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.list) {
                    Intent intent = new Intent(RecipePage.this, ListPage.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.personalinfo) {
                    Intent intent = new Intent(RecipePage.this, PersonalInfo.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        // page functionality
        ingredientContainer = findViewById(R.id.container);
        addIngredient = findViewById(R.id.addIngredientRow);
        submit = findViewById(R.id.submitRecipeData);

        ingredients = new ArrayList<>();
        quantities = new ArrayList<>();

        addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ingredientCount < MAX_INGREDIENTS) {
                    createIngredientRow();
                    ingredientCount++;
                } else {
                    Toast.makeText(RecipePage.this, "Max 10 ingredients", Toast.LENGTH_SHORT).show();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference cookbookReference = FirebaseDatabase.getInstance().getReference().child("Cookbook");
                DatabaseReference ingredientReference = FirebaseDatabase.getInstance().getReference().child("Ingredients");

                ArrayList<IngredientData> ingredientsDB = new ArrayList<>();
                // get ingredients from ingredients database
                ingredientReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ingredient : snapshot.getChildren()) {
                            int ingredientId = Integer.parseInt(ingredient.getKey());
                            String ingredientName = ingredient.getValue().toString();
                            ingredientsDB.add(new IngredientData(
                                    ingredientId,
                                    ingredientName,
                                    0,
                                    0
                            ));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RecipePage.this,
                                "Something went wrong in the outer portion",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                int time = 0;
                String description = "test";
                String name = "example";
                cookbookReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        RecipeData data = new RecipeData();

                        boolean allIngredientsFound = true;

                        // iterate through ingredients on this page
                        for (int i = 0; i < ingredients.size(); i++) {
                            String ingredientName = ingredients.get(i).getText().toString();
                            int ingredientQuantity = Integer.parseInt(quantities.get(i).getText().toString());
                            int ingredientId = 0;

                            // iterate through ingredients in database to check if ingredient is here
                            boolean ingredientFound = false;

                            for (IngredientData j : ingredientsDB) {
                                if (j.getName() == ingredientName) {
                                    ingredientFound = true;
                                    ingredientId = j.getId();
                                }
                            }

                            if (ingredientFound) {
                                data.add(ingredientId, ingredientQuantity);
                            } else {
                                allIngredientsFound = false;
                            }
                        }

                        if (allIngredientsFound) {
                            data.setDescription(description);
                            data.setName(name);
                            data.setTime(time);
                        }
                        // update the meal data
                        /*
                        MealData data = new MealData();
                        data.setCalories(Integer.parseInt(calories));
                        data.setUsername(email);
                        data.setDate(date);

                        mealReference.child(meal).setValue(data);

                        // reset input text
                        mealInput.setText("");
                        calorieInput.setText("");

                        // display toast
                        Toast.makeText(RecipePage.this,
                                "Meal submitted!",
                                Toast.LENGTH_SHORT).show();*/
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RecipePage.this,
                                "Something went wrong in the outer portion",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (navView.getVisibility() == View.VISIBLE) {
            navView.setVisibility(View.GONE);
        } else {
            navView.setVisibility(View.VISIBLE);
        }
        return true;
    }
}