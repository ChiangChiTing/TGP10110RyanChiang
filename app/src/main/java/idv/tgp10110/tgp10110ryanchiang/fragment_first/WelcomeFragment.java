package idv.tgp10110.tgp10110ryanchiang.fragment_first;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import idv.tgp10110.tgp10110ryanchiang.R;


public class WelcomeFragment extends Fragment {
    private static final String TAG = "TAG_WelcomeFragment";
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private Bundle bundle;

    // 初始化與畫面無直接關係之資料
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity(); // 取得Activity參考
        bundle = getArguments();
    }

    // 載入並建立Layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bundle = new Bundle();
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.actionWelcomeToLogin,bundle);
            }
        }, 2000); // 2秒後跳轉到Login頁面
    }

    private void findViews(View view) {
    }
}