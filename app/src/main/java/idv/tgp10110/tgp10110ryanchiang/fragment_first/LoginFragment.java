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
    private static final String FILENAME = "account"; // 內部儲存使用者帳號用


    // 初始化與畫面無直接關係之資料
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity(); // 取得Activity參考
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        /* Google */
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // 由google-services.json轉出，有時會編譯失敗，但不影響執行
                .requestIdToken(getString(R.string.default_web_client_id))
                // 要求輸入email
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(requireActivity(), options);
        /* Facebook */
        // 呼叫 CallbackManager.Factory.create() 建立 callbackManager 來處理FB登入回應
//        callbackManager = CallbackManager.Factory.create();

    }

    // 載入並建立Layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        // 若之前有登入過，顯示之前登入的帳號(E-mail)
        handleEditText();

        // 登入按鈕監聽器
        btSignIn.setOnClickListener(v -> {
            final String email = etEmail.getText().toString();
            final String password = etPassword.getText().toString();
            saveEmail(new User(email)); // 儲存使用者輸入的帳號(E-mail)
            signIn(email, password);

        });

        // 註冊按鈕監聽器
        btSignUp.setOnClickListener(v -> {
            bundle = new Bundle();
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.actionLoginToSignup, bundle);
        });

        // 第三方Google登入按鈕監聽器
        btSignInGoogle.setOnClickListener(v -> signInGoogle());

        // 第三方Facebook登入按鈕監聽器
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
    // 跳出Google登入畫面
    private void signInGoogle() {
        Intent signInIntent = client.getSignInIntent();
        // 跳出Google登入畫面
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
    // 使用Google帳號完成Firebase驗證
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // get the unique ID for the Google account
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    // 登入成功轉至下頁；失敗則顯示錯誤訊息
                    if (task.isSuccessful()) {
                        Navigation.findNavController(btSignInGoogle)
                                .navigate(R.id.actionLoginToResult);
                    } else {
                        Exception exception = task.getException();
                        String message = exception == null ? "Google登入失敗" : exception.getMessage();
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* Facebook */
    // 跳出FB登入畫面
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
    // 使用FB token完成Firebase驗證
//    private void signInFirebase(AccessToken token) {
//        Log.d(TAG, "signInFirebase: " + token);
//
//        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    // 登入成功轉至下頁；失敗則顯示錯誤訊息
//                    if (task.isSuccessful()) {
//                        Navigation.findNavController(btSignInGoogle)
//                                .navigate(R.id.actionLoginToResult);
//                    } else {
//                        Exception exception = task.getException();
////                        String message = "Sign in fail.";  // 待使用I18N
//                        String message = getString(R.string.textSignInFail);
//                        if (exception != null && exception.getMessage() != null) {
//                            message = exception.getMessage();
//                        }
//                        Log.e(TAG, message);
//                        tvMessage.setText(message);
//                    }
//                });
//    }

    // 一般登入
    private void signIn(String email, String password) {
        if (isEmailOrPasswordEmpty(email, password)) {
            return;
        }
        // 利用user輸入的email與password登入
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // 登入成功轉至下頁；失敗則顯示錯誤訊息
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
//                            message = "Sign in fail.";  // 待使用I18N
                            message = "登入失敗";
                        } else {
                            String exceptionType;
                            // FirebaseAuthInvalidCredentialsException代表帳號驗證不成功，例如E-mail格式不正確
                            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                exceptionType = "帳號或密碼格式不正確，請確認是否為正確的E-mail格式或密碼格式(至少6個字元)";
                            }
                            // FirebaseAuthInvalidUserException代表無此user，例如帳密錯誤
                            else if (exception instanceof FirebaseAuthInvalidUserException) {
                                exceptionType = "帳號或密碼不正確";
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
            Toast.makeText(requireContext(), "帳號或密碼未輸入", Toast.LENGTH_LONG).show();
            return true;
        } else {
            return false;
        }
    }

    // 畫面即將顯示前
    @Override
    public void onStart() {
        super.onStart();
        // 檢查user是否已經登入，是則FirebaseUser物件不為null
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            bundle = new Bundle();
            bundle.putString("nickname", "攻城師");
            NavController navController = Navigation.findNavController(btSignInGoogle);
            navController.navigate(R.id.actionLoginToResult, bundle);
        }
    }

    // Internal Storage(內部儲存體，儲存裝置上硬碟的私有檔案(Private))
    private void saveEmail(final User user) {
        try (
                // 取得FileOutputStream物件
                FileOutputStream fos = activity.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                // Java I/O相關程式
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
                // 取得FileInputStream物件
                FileInputStream fis = activity.openFileInput(FILENAME);
                // Java I/O相關程式
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
