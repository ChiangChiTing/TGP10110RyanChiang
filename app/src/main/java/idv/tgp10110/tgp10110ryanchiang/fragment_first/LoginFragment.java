package idv.tgp10110.tgp10110ryanchiang.fragment_first;

import static idv.tgp10110.tgp10110ryanchiang.util.Constants.PREFERENCES_FILE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import idv.tgp10110.tgp10110ryanchiang.R;
import idv.tgp10110.tgp10110ryanchiang.bean.User;


public class LoginFragment extends Fragment {
    private final static String TAG = "TAG_LoginFragment";
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Bundle bundle;
    private EditText etEmail, etPassword;
    private TextView tvSignUpHint, tvHintSSO;
    private ImageView btSignInGoogle, btSignInFB;
    private Button btSignIn, btSignUp;
    private GoogleSignInClient client;
    //    private CallbackManager callbackManager;
    private static final String FILENAME = "account"; // ??????????????????????????????


    // ??????????????????????????????????????????
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity(); // ??????Activity??????
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        /* Google */
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // ???google-services.json???????????????????????????????????????????????????
                .requestIdToken(getString(R.string.default_web_client_id))
                // ????????????email
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(requireActivity(), options);
        /* Facebook */
        // ?????? CallbackManager.Factory.create() ?????? callbackManager ?????????FB????????????
//        callbackManager = CallbackManager.Factory.create();

    }

    // ???????????????Layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ????????????Layout??????(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    // Layout?????????
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        // ???????????????????????????????????????????????????(E-mail)
        handleEditText();

        // ?????????????????????
        btSignIn.setOnClickListener(v -> {
            final String email = etEmail.getText().toString();
            final String password = etPassword.getText().toString();
            saveEmail(new User(email)); // ??????????????????????????????(E-mail)
            signIn(email, password);

        });

        // ?????????????????????
        btSignUp.setOnClickListener(v -> {
            bundle = new Bundle();
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.actionLoginToSignup, bundle);
        });

        // ?????????Google?????????????????????
        btSignInGoogle.setOnClickListener(v -> signInGoogle());

        // ?????????Facebook?????????????????????
//        btSignInFB.setOnClickListener(v -> signInFB());
    }


    private void findViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        tvSignUpHint = view.findViewById(R.id.tvDiaryTitle);
        btSignInGoogle = view.findViewById(R.id.btSignInGoogle);
        btSignInFB = view.findViewById(R.id.btSignInFB);
        btSignIn = view.findViewById(R.id.btSignIn);
        btSignUp = view.findViewById(R.id.btSignUp);


    }

    /* Google */
    // ??????Google????????????
    private void signInGoogle() {
        Intent signInIntent = client.getSignInIntent();
        // ??????Google????????????
        signInGoogleLauncher.launch(signInIntent);

    }


    /* Google */
    ActivityResultLauncher<Intent> signInGoogleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        firebaseAuthWithGoogle(account);
                    } else {
                        Log.e(TAG, "GoogleSignInAccount is null");
                    }
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.e(TAG, e.toString());
                }
            }
    );

    /* Google */
    // ??????Google????????????Firebase??????
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // get the unique ID for the Google account
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    // ??????????????????????????????????????????????????????
                    if (task.isSuccessful()) {
                        Navigation.findNavController(btSignInGoogle)
                                .navigate(R.id.actionLoginToResult);
                    } else {
                        Exception exception = task.getException();
                        String message = exception == null ? "Google????????????" : exception.getMessage();
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* Facebook */
    // ??????FB????????????
//    private void signInFB() {
//        LoginManager.getInstance().logInWithReadPermissions(this, callbackManager, Arrays.asList("email", "public_profile"));
//        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Log.d(TAG, "onSuccess(): " + loginResult);
//                signInFirebase(loginResult.getAccessToken());
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d(TAG, "onCancel()");
//            }
//
//            @Override
//            public void onError(@NonNull FacebookException exception) {
//                Log.e(TAG, "onError(): " + exception.getMessage());
//            }
//        });
//    }

    /* Facebook */
    // ??????FB token??????Firebase??????
//    private void signInFirebase(AccessToken token) {
//        Log.d(TAG, "signInFirebase: " + token);
//
//        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    // ??????????????????????????????????????????????????????
//                    if (task.isSuccessful()) {
//                        Navigation.findNavController(btSignInGoogle)
//                                .navigate(R.id.actionLoginToResult);
//                    } else {
//                        Exception exception = task.getException();
////                        String message = "Sign in fail.";  // ?????????I18N
//                        String message = getString(R.string.textSignInFail);
//                        if (exception != null && exception.getMessage() != null) {
//                            message = exception.getMessage();
//                        }
//                        Log.e(TAG, message);
//                        tvMessage.setText(message);
//                    }
//                });
//    }

    // ????????????
    private void signIn(String email, String password) {
        if (isEmailOrPasswordEmpty(email, password)) {
            return;
        }
        // ??????user?????????email???password??????
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // ??????????????????????????????????????????????????????
                    if (task.isSuccessful()) {
                        bundle = new Bundle();

//                        Navigation.findNavController(btSignInGoogle)
//                                .navigate(R.id.actionLoginToResult);
                        NavController navController = Navigation.findNavController(btSignInGoogle);
                        navController.navigate(R.id.actionLoginToResult, bundle);
                    } else {
                        String message;
                        Exception exception = task.getException();
                        if (exception == null) {
//                            message = "Sign in fail.";  // ?????????I18N
                            message = "????????????";
                        } else {
                            String exceptionType;
                            // FirebaseAuthInvalidCredentialsException????????????????????????????????????E-mail???????????????
                            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                exceptionType = "????????????????????????????????????????????????????????????E-mail?????????????????????(??????6?????????)";
                            }
                            // FirebaseAuthInvalidUserException????????????user?????????????????????
                            else if (exception instanceof FirebaseAuthInvalidUserException) {
                                exceptionType = "????????????????????????";
                            } else {
                                exceptionType = exception.getClass().toString();
                            }
                            message = exceptionType + ": " + exception.getLocalizedMessage();
                        }
                        Log.e(TAG, message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isEmailOrPasswordEmpty(String email, String password) {
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            Toast.makeText(requireContext(), "????????????????????????", Toast.LENGTH_LONG).show();
            return true;
        } else {
            return false;
        }
    }

    // ?????????????????????
    @Override
    public void onStart() {
        super.onStart();
        // ??????user???????????????????????????FirebaseUser????????????null
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            bundle = new Bundle();
            bundle.putString("nickname", "?????????");
            NavController navController = Navigation.findNavController(btSignInGoogle);
            navController.navigate(R.id.actionLoginToResult, bundle);
        }
    }

    // Internal Storage(??????????????????????????????????????????????????????(Private))
    private void saveEmail(final User user) {
        try (
                // ??????FileOutputStream??????
                FileOutputStream fos = activity.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                // Java I/O????????????
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(user);
            oos.flush();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private User loadEmail() {
        try (
                // ??????FileInputStream??????
                FileInputStream fis = activity.openFileInput(FILENAME);
                // Java I/O????????????
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (User) ois.readObject();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    private void handleEditText() {
        final User user = loadEmail();
        if (user != null) {
            etEmail.setText(user.getUserAccount());
        }
    }
}
