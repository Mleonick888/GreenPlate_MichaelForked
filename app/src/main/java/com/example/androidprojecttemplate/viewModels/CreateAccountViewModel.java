package com.example.androidprojecttemplate.viewModels;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.androidprojecttemplate.models.UserLoginData;
import com.example.androidprojecttemplate.views.CreateAccountActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountViewModel {
    private static CreateAccountViewModel instance;
    final private CreateAccountActivity theData;

    FirebaseAuth firebaseAuth;

    DatabaseReference reference;

    private static int temp = 0;

    public CreateAccountViewModel() {
        theData = new CreateAccountActivity();
        firebaseAuth = FirebaseAuth.getInstance();

    }

    public static synchronized CreateAccountViewModel getInstance() {
        if (instance == null) {
            instance = new CreateAccountViewModel();
        }

        return instance;
    }

    public int toLoginScreenFromCreate(String username, String password, String confirmPassword, String name) {
        // check validity of username and password

        // Username is empty
        if (TextUtils.isEmpty(username)) {
            temp = 1;
            // Password is empty
        } else if (TextUtils.isEmpty(password)) {
            temp = 2;
            // password and confirmPassword don't equal
        } else if (!TextUtils.equals(password, confirmPassword)) {
            temp = 3;
            // username is not a valid email
        } else if (!username.contains("@") || !username.contains(".com")) {
            temp = 4;
            // password is less than length 6
        } else if (password.length() < 6) {
            temp = 5;
            // Inputs have spaces
        } else if (username.contains(" ") || password.contains(" ") || confirmPassword.contains(" ")) {
            temp = 6;
            // confirmPassword is empty
        } else if (TextUtils.isEmpty(confirmPassword)) {
            temp = 7;
        } else if (TextUtils.isEmpty(name)) {
            temp = 10;
        }

        // Checks are done, can now create the username and password

        // create user with firebase
        firebaseAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // Add entires to the real-time database
                            reference = FirebaseDatabase.getInstance().getReference().child("Users");

                            UserLoginData theUser = new UserLoginData(username, password, name);

                            // Will track different usernames in the database by taking the first letter of their username (since you can't use the full username since it has special characters)
                            reference.child(name).setValue(theUser);

                            temp = 8;
                        } else {
                            temp = 9;
                        }
                    }
                });

        return temp;

    }

}
