package com.example.firebaseauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button signOutBtn;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        tvStatus = findViewById(R.id.tvStatus);
        signOutBtn = findViewById(R.id.signOut_btn);

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        if (currentUser == null) {
            // If not signed in, show sign-in options
            promptSignInOptions();
        }
    }

    private void promptSignInOptions() {
        // Simple choice dialog: Email or Anonymous
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign in");
        builder.setMessage("Choose sign-in method:");
        builder.setPositiveButton("Email", (dialog, which) -> showEmailSignInDialog());
        builder.setNeutralButton("Anonymous", (dialog, which) -> signInAnonymously());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(false);
        builder.show();
    }

    private void showEmailSignInDialog() {
        // Build a simple form for email + password
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        final EditText inputEmail = new EditText(this);
        inputEmail.setHint("Email");
        inputEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(inputEmail);

        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("Password (min 6 chars)");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email sign-in");
        builder.setView(layout);
        builder.setPositiveButton("Sign in", (dialog, which) -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString();
            if (email.isEmpty() || password.length() < 6) {
                Toast.makeText(MainActivity.this, "Enter valid email/password (min 6 chars)", Toast.LENGTH_SHORT).show();
                // reopen the dialog for retry
                showEmailSignInDialog();
            } else {
                signInWithEmail(email, password);
            }
        });
        builder.setNeutralButton("Create account", (dialog, which) -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString();
            if (email.isEmpty() || password.length() < 6) {
                Toast.makeText(MainActivity.this, "Enter valid email/password (min 6 chars)", Toast.LENGTH_SHORT).show();
                showEmailSignInDialog();
            } else {
                createAccountWithEmail(email, password);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            promptSignInOptions(); // go back to main choice
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Signed in: "
                                    + (user != null ? user.getEmail() : "unknown"), Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, inform the user
                            Toast.makeText(MainActivity.this, "Authentication failed: "
                                    + (task.getException() != null ? task.getException().getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
                            // Let user try again
                            promptSignInOptions();
                        }
                    }
                });
    }

    private void createAccountWithEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Account created: " +
                                    (user != null ? user.getEmail() : "unknown"), Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Account creation failed: " +
                                    (task.getException() != null ? task.getException().getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
                            promptSignInOptions();
                        }
                    }
                });
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Signed in anonymously", Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Anonymous sign-in failed: " +
                                    (task.getException() != null ? task.getException().getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
                            promptSignInOptions();
                        }
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
        // Re-prompt sign-in options after signing out
        promptSignInOptions();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String nameOrEmail = (user.getEmail() != null) ? user.getEmail() : ("User id: " + user.getUid());
            tvStatus.setText("Signed in: " + nameOrEmail);
            signOutBtn.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("Not signed in");
            signOutBtn.setVisibility(View.GONE);
        }
    }
}
