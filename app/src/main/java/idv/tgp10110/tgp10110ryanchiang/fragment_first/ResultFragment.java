package idv.tgp10110.tgp10110ryanchiang.fragment_first;

import static idv.tgp10110.tgp10110ryanchiang.util.Constants.PREFERENCES_FILE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import idv.tgp10110.tgp10110ryanchiang.R;
import idv.tgp10110.tgp10110ryanchiang.SecondActivity;
import idv.tgp10110.tgp10110ryanchiang.bean.User;


public class ResultFragment extends Fragment {
    private static final String TAG = "TAG_ResultFragment";
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private Bundle bundle;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ImageView ivToSecond, ivSignOutIconLeft, ivSignOutIconRight;

    // 初始化與畫面無直接關係之資料
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity(); // 取得Activity參考
        bundle = getArguments();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);

    }

    // 載入並建立Layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        handleIvButton();
//        handleSavePreferences();
    }

    private void findViews(View view) {
        ivToSecond = view.findViewById(R.id.ivToSecond);
        ivSignOutIconLeft = view.findViewById(R.id.ivSignOutIconLeft);
        ivSignOutIconRight = view.findViewById(R.id.ivSignOutIconRight);

    }

    private void handleIvButton() {
//        tv_Result.setText(" Welcome!! \n 攻城師" + sharedPreferences.getString("使用者名稱", null) + "");
        ivToSecond.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(activity, SecondActivity.class);
            startActivity(intent);
            activity.finish();
//            NavController navController = Navigation.findNavController(view);
//            navController.navigate(R.id.action_result_to_takePicture,bundle);
        });
        ivSignOutIconLeft.setScaleX(-1);
        ivSignOutIconLeft.setScaleY(1);
        ivSignOutIconLeft.setOnClickListener(view ->
                new AlertDialog.Builder(activity)
                        // 設定標題
                        .setTitle("登出")
                        // 設定圖示
                        .setIcon(R.drawable.ic_waring)
                        // 設定訊息文字
                        .setMessage("您確定要登出嗎?")
                        // 設定positive與negative按鈕上面的文字與點擊事件監聽器
                        .setPositiveButton("登出", (dialog, which) -> signOut()) // 登出
                        .setNegativeButton("取消", (dialog, which) -> dialog.cancel()) // 取消並關閉對話視窗
                        .setCancelable(false) // false代表要點擊按鈕方能關閉，預設為true
                        .show());

        ivSignOutIconRight.setOnClickListener(view ->
                new AlertDialog.Builder(activity)
                        // 設定標題
                        .setTitle("登出")
                        // 設定圖示
                        .setIcon(R.drawable.ic_waring)
                        // 設定訊息文字
                        .setMessage("您確定要登出嗎?")
                        // 設定positive與negative按鈕上面的文字與點擊事件監聽器
                        .setPositiveButton("登出", (dialog, which) -> signOut()) // 登出
                        .setNegativeButton("取消", (dialog, which) -> dialog.cancel()) // 取消並關閉對話視窗
                        .setCancelable(false) // false代表要點擊按鈕方能關閉，預設為true
                        .show());
    }

    private void signOut() {
        // 登出Firebase帳號
        auth.signOut();

        // 下列程式會登出Google帳號，user再次登入時會再次跳出Google登入畫面
        // 如果沒有登出，則不會再次跳出Google登入畫面
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // 由google-services.json轉出
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(requireActivity(), options);
        client.signOut().addOnCompleteListener(requireActivity(), task -> {
            Navigation.findNavController(ivToSecond).popBackStack();
            Log.d(TAG, "Signed out");
        });

        /* Google登出與Facebook登出會互相衝突 */
        /* Google登出與Facebook登出會互相衝突 */
        // 登出FB帳號
//        LoginManager.getInstance().logOut();
//        Navigation.findNavController(tvId).popBackStack();
//        Log.d(TAG, "Signed out");
    }


//    private void signOut_Facebook() {
////         登出Firebase帳號
//        auth.signOut();
////         登出FB帳號
//        LoginManager.getInstance().logOut();
//        Navigation.findNavController(textView).popBackStack();
//        Log.d(TAG, "Signed out");
//    }

    private void handleSavePreferences() {
        FirebaseUser user_1 = auth.getCurrentUser();
        String user_UID = user_1.getUid();
        db.collection("castleUsers").document(user_UID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User loginUser = task.getResult().toObject(User.class);
                        savePreferences("使用者名稱", loginUser.getUserName());
                        savePreferences("使用者UID", loginUser.getUserId());
                        savePreferences("使用者登入方式", loginUser.getSignInType());
                        savePreferences("使用者帳號", loginUser.getUserAccount());
                        savePreferences("使用者密碼", loginUser.getUserPassword());
                        savePreferences("使用者等級", loginUser.getUserRank());
                        savePreferences("使用者集章數量", String.valueOf(loginUser.getStampCount()));
                    } else {
                        String message = task.getException() == null ?
                                "textInsertFail" :
                                task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void savePreferences(String key, String value) {
        sharedPreferences
                // 開始編輯
                .edit()
                // 寫出資料
                .putString(key, value)
                // 存檔
                .apply();

    }
}