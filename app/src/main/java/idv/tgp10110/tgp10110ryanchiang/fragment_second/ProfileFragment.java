package idv.tgp10110.tgp10110ryanchiang.fragment_second;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import idv.tgp10110.tgp10110ryanchiang.FirstActivity;
import idv.tgp10110.tgp10110ryanchiang.R;
import idv.tgp10110.tgp10110ryanchiang.bean.Diary;
import idv.tgp10110.tgp10110ryanchiang.bean.User;


public class ProfileFragment extends Fragment {
    private static final String TAG = "TAG_ProfileFragment";
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private Button btSignOut, btUpdateProfile;
    private TextView tvProfileID, tvProfileAccount, tvProfileName, tvProfileCount, tvProfileLevel, tvProfileLogInType;
    private Bundle bundle;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private User user;


    // 初始化與畫面無直接關係之資料
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // 取得Firebase資料庫物件
        storage = FirebaseStorage.getInstance(); // 取得FirebaseStorage物件(存圖片用)
        bundle = getArguments();
        user = new User();


    }

    // 載入並建立Layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        handleUpdate();
        handleSignOut();
        showProfile();
    }


    private void findViews(View view) {
        tvProfileID = view.findViewById(R.id.tvProfileID);
        tvProfileAccount = view.findViewById(R.id.tvProfileAccount);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileCount = view.findViewById(R.id.tvProfileCount);
        tvProfileLevel = view.findViewById(R.id.tvProfileLevel);
//        tvProfileLogInType = view.findViewById(R.id.tvProfileLogInType);
        btSignOut = view.findViewById(R.id.btSignOut);
        btUpdateProfile = view.findViewById(R.id.btUpdateProfile);

    }

    private void handleUpdate() {
//        btUpdateProfile.setOnClickListener(view -> {
//            // 新建Bundle物件(各Fragment之間傳遞資料用的背包)
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("user", user); // 將實作Serializable序列化之Diary物件放進背包
//            Navigation.findNavController(btUpdateProfile)
//                    .navigate(R.id.actionProfileToUpdate, bundle);
//        });
    }

    private void showProfile() {
        FirebaseUser user_1 = auth.getCurrentUser();
        String user_UID = user_1.getUid();
        db.collection("castleUsers").document(user_UID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        user = task.getResult().toObject(User.class);
                        if (user != null) {
                            tvProfileID.setText(user.getUserId());
                            tvProfileAccount.setText(user.getUserAccount());
                            tvProfileName.setText(user.getUserName());
//                            tvProfileCount.setText(user.getStampCount());
                            tvProfileLevel.setText(user.getUserRank());
//                            tvProfileLogInType.setText(user.getSignInType());
                        }
                    } else {
                        String message = task.getException() == null ?
                                "XXX" :
                                task.getException().getMessage();
                        Log.e(TAG, "exception message: " + message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });

//            if (user != null) {
//                tvProfileID.setText(user.getUserId());
//                tvProfileAccount.setText(user.getUserAccount());
//                tvProfileName.setText(user.getUserName());
//                tvProfileCount.setText(user.getStampCount());
//                tvProfileLevel.setText(user.getUserRank());
//                tvProfileLogInType.setText(user.getSignInType());
//            }
    }


    private void handleSignOut() {
        btSignOut.setOnClickListener(view ->
                new AlertDialog.Builder(activity)
                        // 設定標題
                        .setTitle("登出")
                        // 設定圖示
                        .setIcon(R.drawable.ic_waring)
                        // 設定訊息文字
                        .setMessage("您確定要登出嗎?")
                        // 設定positive與negative按鈕上面的文字與點擊事件監聽器
                        .setPositiveButton("登出", (dialog, which) -> profileSignOut()) // 登出
                        .setNegativeButton("取消", (dialog, which) -> dialog.cancel()) // 取消並關閉對話視窗
                        .setCancelable(false) // false代表要點擊按鈕方能關閉，預設為true
                        .show());
    }


    private void profileSignOut() {
        Intent intent = new Intent();
        auth.signOut();

        // 下列程式會登出Google帳號，user再次登入時會再次跳出Google登入畫面
        // 如果沒有登出，則不會再次跳出Google登入畫面
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // 由google-services.json轉出
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(requireActivity(), options);
        client.signOut().addOnCompleteListener(requireActivity(), task -> {
//            NavController navController = Navigation.findNavController(textView);
//            navController.navigate(R.id.action_memberList_to_login);
            intent.setClass(activity, FirstActivity.class);
            startActivity(intent);
            activity.finish();
            Log.d(TAG, "Signed out");
        });
        //登出FB帳號
//        LoginManager.getInstance().logOut();
////        NavController navController = Navigation.findNavController(textView);
////        navController.navigate(R.id.action_memberList_to_login);
//        intent.setClass(activity, MainActivity.class);
//        startActivity(intent);
//        activity.finish();
//        Log.d(TAG, "Signed out");
    }
}