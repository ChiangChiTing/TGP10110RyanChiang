package idv.tgp10110.tgp10110ryanchiang.fragment_second;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import idv.tgp10110.tgp10110ryanchiang.R;
import idv.tgp10110.tgp10110ryanchiang.bean.User;

public class ProfileUpdateFragment extends Fragment {
    private static final String TAG = "TAG_ProfileUpdateFragment";
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private Bundle bundle;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private EditText etNicknameUp, etPasswordUp, etPasswordConfirmUp;
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

        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("castleUser");
            if (user != null) {
                etNicknameUp.setText(user.getUserName());
                etPasswordUp.setText(user.getUserPassword());
                etPasswordConfirmUp.setText(user.getUserPassword());
            }
        }

        // 更新按鈕監聽器
        view.findViewById(R.id.btSignOut).setOnClickListener(v -> {
            // 取得使用者輸入之更新後暱稱/密碼/密碼確認(皆String)
            String name = etNicknameUp.getText().toString().trim();
            if (name.length() <= 0) {
                Toast.makeText(requireContext(), "攻城師大名為必填欄位",
                        Toast.LENGTH_LONG).show();
                return;
            } else {
                user.setUserName(name);
            }
            String password1 = etPasswordUp.getText().toString().trim();
            String password2 = etPasswordConfirmUp.getText().toString().trim();
            if (password1.equals(password2)) {
                user.setUserPassword(etPasswordUp.getText().toString());
            } else {
                Toast.makeText(requireContext(), "密碼不一致", Toast.LENGTH_LONG).show();
                return;
            }
                addOrReplace(user); // 沒有圖片的話也要

        });

        // 取消按鈕監聽器(返回上一頁)
        view.findViewById(R.id.btUpdateProfile).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
    }



    private void findViews(View view) {
        etNicknameUp = view.findViewById(R.id.etNicknameUp);
        etPasswordUp = view.findViewById(R.id.etPasswordUp);
        etPasswordConfirmUp = view.findViewById(R.id.etPasswordConfirmUp);

    }

    // 新增或修改Firestore上的日記
    private void addOrReplace(final User user) {
        // 如果Firestore沒有該ID的Document就建立新的，已經有就更新內容
        db.collection("CastleUsers").document(user.getUserId()).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "textInserted"
                                + " with ID: " + user.getUserId();
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        // 修改完畢回上頁
                        Navigation.findNavController(etNicknameUp).popBackStack();
                    } else {
                        String message = task.getException() == null ?
                                "textInsertFail" :
                                task.getException().getMessage();
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }


}