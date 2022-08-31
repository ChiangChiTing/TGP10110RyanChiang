package idv.tgp10110.tgp10110ryanchiang.fragment_first;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import idv.tgp10110.tgp10110ryanchiang.R;
import idv.tgp10110.tgp10110ryanchiang.bean.User;


public class SignupFragment extends Fragment {
    private final static String TAG = "TAG_SignupFragment";
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Bundle bundle;
    private EditText etEmail, etPassword, etPasswordConfirm, etNickname;
    private Button btSignUpCommit;


    // 初始化與畫面無直接關係之資料
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity(); // 取得Activity參考
        auth = FirebaseAuth.getInstance();

    }

    // 載入並建立Layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        btSignUpCommit.setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    // 設定標題
                    .setTitle("註冊確認")
                    // 設定圖示
                    .setIcon(R.drawable.ic_waring)
                    // 設定訊息文字
                    .setMessage("您確定所輸入資料皆正確嗎?")
                    // 設定positive與negative按鈕上面的文字與點擊事件監聽器
                    .setPositiveButton("確認送出", (dialog, which) -> preSignUp()) // 登出
                    .setNegativeButton("重新確認", (dialog, which) -> dialog.cancel()) // 取消並關閉對話視窗
                    .setCancelable(false) // false代表要點擊按鈕方能關閉，預設為true
                    .show();
        });
    }

    private void preSignUp() {
        bundle = new Bundle();
        User user = new User();
        // 使用者UID = Uid ( Firebase自動抓取 )

        // 登入方式 1.E-mail 2.Google 3.FB
        user.setSignInType("1");

        // 使用者名稱(暱稱)
        String name = etNickname.getText().toString();
        if (name.length() <= 0) {
            Toast.makeText(requireContext(), "攻城師大名為必填欄位",
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            user.setUserName(name);
        }

        // 使用者帳號(E-mail)
        user.setUserAccount(etEmail.getText().toString());

        // 使用者密碼
        String password1 = etPassword.getText().toString();

        // 使用者密碼確認
        String password2 = etPasswordConfirm.getText().toString();
        if (password1.equals(password2)) {
            user.setUserPassword(etPassword.getText().toString());
        } else {
            Toast.makeText(requireContext(), "密碼不一致", Toast.LENGTH_LONG).show();
            return;
        }

        // 等級(依stampCount變動)
        user.setUserRank("新手攻城師");

        // 目前集章數量
        user.setStampCount(0);

        signUp(user);
    }

    private void findViews(View view) {
        etEmail = view.findViewById(R.id.etSignUpEmail);
        etPassword = view.findViewById(R.id.etSignUpPassword);
        etPasswordConfirm = view.findViewById(R.id.etPasswordConfirm);
        etNickname = view.findViewById(R.id.etSignUpNickname);
        btSignUpCommit = view.findViewById(R.id.btSignUp);

    }

    // 一般註冊
    private void signUp(User user) {
        if (isEmailOrPasswordEmpty(user.getUserAccount(), user.getUserPassword())) {
            return;
        }

        /* 利用user輸入的email與password建立新的帳號 */
        // ===================以下是新執行緒==================
        auth.createUserWithEmailAndPassword(user.getUserAccount(), user.getUserPassword())
                .addOnCompleteListener(taskCreateUser -> { // 此方法可開啟新執行緒(連接Firebase)
                    // 建立成功(連接Firebase)則轉至下頁；失敗則顯示錯誤訊息
                    if (taskCreateUser.isSuccessful()) {
                        FirebaseUser firebaseUser = taskCreateUser.getResult().getUser();
                        if (firebaseUser != null) {
                            String uid = taskCreateUser.getResult().getUser().getUid();
                            user.setUserId(uid);
                            FirebaseFirestore.getInstance()
                                    .collection("castleUsers").document(user.getUserId())
                                    .set(user).addOnCompleteListener(taskInsertDB -> {
                                        if (taskInsertDB.isSuccessful()) {
                                            Toast.makeText(requireContext(), "註冊成功，歡迎加入攻城師", Toast.LENGTH_LONG).show();

                                            Navigation.findNavController(etEmail)
                                                    .navigate(R.id.actionSignupToLogin);
                                        }
                                    });
                        }

                    } else {
                        String message;
                        Exception exception = taskCreateUser.getException();
                        if (exception == null) {
//                            message = "Register fail."; // 待使用I18N
                            message = "註冊失敗";
                        } else {
                            String exceptionType;
                            // FirebaseAuthInvalidCredentialsException 代表帳號驗證不成功，例如email格式不正確
                            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                exceptionType = "帳號或密碼格式不正確，請確認是否為正確的E-mail格式或密碼格式(至少6個字元)"; // 待使用I18N
                            }
                            // FirebaseAuthInvalidUserException 代表無此user，例如帳密錯誤
                            else if (exception instanceof FirebaseAuthInvalidUserException) {
                                exceptionType = "帳號或密碼不正確"; // 待使用I18N
                            }
                            // FirebaseAuthUserCollisionException 代表此帳號已被使用
                            else if (exception instanceof FirebaseAuthUserCollisionException) {
                                exceptionType = "此帳號已被註冊"; // 待使用I18N
                            } else {
                                exceptionType = exception.getClass().toString();
                            }
//                            message = exceptionType + ": " + exception.getLocalizedMessage();
                            message = exceptionType;
                        }
                        Log.e(TAG, message);
//                        tvIncorrectMessage.setText(message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                    }
                });
        // ===================以上是新執行緒==================
    }

    private boolean isEmailOrPasswordEmpty(String email, String password) {
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
//            tvIncorrectMessage.setText("帳號或密碼未輸入");
            Toast.makeText(requireContext(), "帳號或密碼未輸入", Toast.LENGTH_LONG).show();

            return true;
        } else {
            return false;
        }
    }
}