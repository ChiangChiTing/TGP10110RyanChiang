package idv.tgp10110.tgp10110ryanchiang.fragment_second;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import idv.tgp10110.tgp10110ryanchiang.R;
import idv.tgp10110.tgp10110ryanchiang.bean.Diary;


public class DiaryFragment extends Fragment {
    private static final String TAG = "TAG_DiaryFragment";
    private RecyclerView rvDiaries;
    private SearchView searchView;
    private Activity activity;
    private FirebaseFirestore db;  // 雲端資料庫(NoSQL)
    private FirebaseStorage storage; // 存圖檔
    private ListenerRegistration registration;
    private List<Diary> diaries;
    private Bitmap bitmap;
    private Diary curDiary;

    // 初始化與畫面無直接關係之資料
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity(); // 取得Activity參考
        db = FirebaseFirestore.getInstance(); // 取得Firebase資料庫物件
        storage = FirebaseStorage.getInstance(); // 取得FirebaseStorage物件(存圖片用)
        diaries = new ArrayList<>();
        // 加上異動監聽器(監聽資料是否發生異動，有則同步更新)
        listenToDiaries();
    }

    // 載入並建立Layout
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_diary, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        // 設定RecyclerView整頁模式
        // 1. 設定LayoutManager為StaggeredGridLayoutManager
        rvDiaries.setLayoutManager(new StaggeredGridLayoutManager(1, RecyclerView.HORIZONTAL));
        // 2. 實例化PagerSnapHelper物件
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        // 3. 附加至RecyclerView
        pagerSnapHelper.attachToRecyclerView(rvDiaries);

        // 加號按鈕監聽器(點擊後將導向前往新增日記資料頁面)
        view.findViewById(R.id.btAdd).setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.actionDiaryToInsert));

        // 鉛筆按鈕監聽器(點擊後將導向前往編輯日記資料頁面)
        view.findViewById(R.id.btUpdate).setOnClickListener(v -> {
                    // 新建Bundle物件(各Fragment之間傳遞資料用的背包)
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("diary", curDiary); // 將實作Serializable序列化之Diary物件放進背包
                    // 攜帶背包將頁面導向到更新日記頁面
                    Navigation.findNavController(v)
                            .navigate(R.id.actionDiaryToUpdate, bundle);
                }
        );

        // 垃圾桶按鈕監聽器(點擊後將刪除該篇日記)
        view.findViewById(R.id.btDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    // 設定標題
                    .setTitle("刪除日記")
                    // 設定圖示
                    .setIcon(R.drawable.ic_waring)
                    // 設定訊息文字
                    .setMessage("您確定要刪除嗎?")
                    // 設定positive與negative按鈕上面的文字與點擊事件監聽器
                    .setPositiveButton("刪除", (dialog, which) -> deleteDiary()) // 登出
                    .setNegativeButton("取消", (dialog, which) -> dialog.cancel()) // 取消並關閉對話視窗
                    .setCancelable(false) // false代表要點擊按鈕方能關閉，預設為true
                    .show();


        });


        // SearchView監聽器(監聽查詢篩選狀況)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                showDiaries();
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }


    private void deleteDiary() {
        // 刪除Firestore內的日記資料
        db.collection("diaries").document(curDiary.getDiaryId()).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 刪除該日記在Firebase storage對應的圖檔
                        if (curDiary.getDiaryImagePath() != null) {
                            storage.getReference().child(curDiary.getDiaryImagePath()).delete()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d(TAG, "textImageDeleted");
                                        } else {
                                            String message = task1.getException() == null ?
                                                    "textImageDeleteFailed" + ": " + curDiary.getDiaryImagePath() :
                                                    task1.getException().getMessage() + ": " + curDiary.getDiaryImagePath();
                                            Log.e(TAG, message);
                                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        Toast.makeText(requireContext(), "該日記已刪除", Toast.LENGTH_SHORT).show();
                        // 有加上異動監聽器，會重新下載並指派給DiaryListFragment.diaries，而且會自動呼叫showDiaries()
                        // 所以diaries不需要移除被刪除的diary，也可以省略呼叫showDiaries()
                        // diaries.remove(diary);
                        // showDiaries();
                    } else {
                        Toast.makeText(requireContext(), "日記刪除失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void findViews(View view) {
        rvDiaries = view.findViewById(R.id.rvDiaries);
        searchView = view.findViewById(R.id.searchView);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 顯示所有建立的日記資訊
        showAllDiaries();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 解除異動監聽器
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    /**
     * 取得所有日記資訊後顯示
     */
    private void showAllDiaries() {
        db.collection("diaries").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // 先清除舊資料後再儲存新資料
                        if (!diaries.isEmpty()) {
                            diaries.clear();
                        }
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 將儲存在Firestore內資料庫的各個Diary物件都加進List<Diary>裡
                            diaries.add(document.toObject(Diary.class));
                        }
                        // 顯示日記
                        showDiaries();
                    } else {
                        String message = task.getException() == null ?
                                "無日記" :
                                task.getException().getMessage();
                        Log.e(TAG, "exception message: " + message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 此方法內建立內部類別DiaryAdapter及其內部類別DiaryViewHolder
    private void showDiaries() {
        DiaryAdapter diaryAdapter = (DiaryAdapter) rvDiaries.getAdapter();
        if (diaryAdapter == null) {
            diaryAdapter = new DiaryAdapter(requireContext());
            rvDiaries.setAdapter(diaryAdapter);
        }
        // SearchView相關
        // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
        String queryStr = searchView.getQuery().toString();
        if (queryStr.isEmpty()) {
            diaryAdapter.setDiaries(diaries); // 顯示所有資料
        } else {
            List<Diary> searchDiaries = new ArrayList<>();
            // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
            for (Diary diary : diaries) {
                if (diary.getDiaryName().toUpperCase().contains(queryStr.toUpperCase())) {
                    searchDiaries.add(diary);
                }
            }
            diaryAdapter.setDiaries(searchDiaries); // 顯示篩選後資料
        }
        diaryAdapter.notifyDataSetChanged(); // 提醒(通知)自定義Adapter要顯示的資料筆數已變動
    }

    // =========================自定義Adapter內部類別=========================
    private class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {
        List<Diary> diaries;
        private final Context context;

        DiaryAdapter(Context context) {
            this.context = context;
            this.diaries = new ArrayList<>();
        }

        // 顯示所有資料(傳入來自Firestore內資料庫的各Diary)
        public void setDiaries(List<Diary> diaries) {
            this.diaries = diaries;
        }

        // ====================自定義ViewHolder內部類別====================
        class DiaryViewHolder extends RecyclerView.ViewHolder {
            ImageView ivDiary;
            TextView tvName, tvDate, tvDetail, tvRemark;
            ScrollView scrollView;
            LinearLayout layoutTextViewList;

            DiaryViewHolder(View itemView) {
                super(itemView);
                ivDiary = itemView.findViewById(R.id.ivDiary);
                tvName = itemView.findViewById(R.id.tvDiaryTitle);
                tvDate = itemView.findViewById(R.id.tvDiaryDate);
                tvRemark = itemView.findViewById(R.id.tvRemark);
                scrollView = itemView.findViewById(R.id.scrollView);
                layoutTextViewList = itemView.findViewById(R.id.layoutTextViewList);


            }
        }
        // ====================以上自定義ViewHolder內部類別====================

        // 以下為父類別RecyclerView.Adapter三個須Override方法
        // 回傳選項數量(日記)
        @Override
        public int getItemCount() {
            return diaries.size();
        }

        // 宣告ItemView，並載入選項容器元件的外觀
        // 實例化自定義的viewHolder，並回傳
        @NonNull
        @Override
        public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
            // 從context取出打氣筒/載入器(LayoutInflater)將layout檔(item_view_diary)充氣
            View itemView = layoutInflater.inflate(R.layout.item_view_diary, parent, false);
            return new DiaryViewHolder(itemView);
        }

        // 透過自定義的viewHolder物件，將資料綁定至各元件上(並對各元件進行其他處理)
        @Override
        public void onBindViewHolder(@NonNull DiaryViewHolder viewHolder, int position) {
            // 取得目前ViewHolder的位置，才能精準得知要對哪個Diary物件進行操作
            final Diary diary = diaries.get(position);
            curDiary = diary;
            // 若無法取得Diary物件的圖片路徑(物件該屬性為空值)，就設定為預設圖片(no_image)
            if (diary.getDiaryImagePath() == null) {
                viewHolder.ivDiary.setImageResource(R.drawable.diary_no_image);
            } else {
                showImage(viewHolder.ivDiary, diary.getDiaryImagePath()); // 若有取得則呼叫方法顯示
            }
            viewHolder.scrollView.post(() -> viewHolder.scrollView.fullScroll(View.FOCUS_DOWN));
            viewHolder.layoutTextViewList.addView(getTextView(diary.getDiaryDetail()));

            // 取得Diary物件的屬性
            viewHolder.tvName.setText(diary.getDiaryName());
            viewHolder.tvDate.setText(diary.getDiaryDate());
            viewHolder.tvRemark.setText(diary.getDiaryRemark());

// ==============================================================================================================
// ==============================================================================================================
// ==============================================================================================================
            viewHolder.ivDiary.setOnClickListener(view -> {
                ImageView iv = new ImageView(context);
//                iv.setImageBitmap(bitmap);
                iv.setImageResource(R.mipmap.ic_launcher);
                Log.d(TAG, "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZzzz");
                Toast toast = new Toast(context);
                toast.setView(iv);
                Log.d(TAG, "toast:" + toast);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            });

            // 整個itemView範圍的監聽器(點擊會開啟修改頁面)
            viewHolder.itemView.setOnClickListener(v -> {
                // 新建Bundle物件(各Fragment之間傳遞資料用的背包)
                Bundle bundle = new Bundle();
                bundle.putSerializable("diary", diary); // 將實作Serializable序列化之Diary物件放進背包
                // 攜帶背包將頁面導向到更新日記頁面
                Navigation.findNavController(v)
                        .navigate(R.id.actionDiaryToUpdate, bundle);
            });

            // 整個itemView範圍的監聽器(長按將直接刪除資料)
            viewHolder.itemView.setOnLongClickListener(v -> {
                // 刪除Firestore內的日記資料
                db.collection("diaries").document(diary.getDiaryId()).delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // 刪除該日記在Firebase storage對應的圖檔
                                if (diary.getDiaryImagePath() != null) {
                                    storage.getReference().child(diary.getDiaryImagePath()).delete()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Log.d(TAG, "textImageDeleted");
                                                } else {
                                                    String message = task1.getException() == null ?
                                                            "textImageDeleteFailed" + ": " + diary.getDiaryImagePath() :
                                                            task1.getException().getMessage() + ": " + diary.getDiaryImagePath();
                                                    Log.e(TAG, message);
                                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                                Toast.makeText(requireContext(), "該日記已刪除", Toast.LENGTH_SHORT).show();
                                // 有加上異動監聽器，會重新下載並指派給DiaryListFragment.diaries，而且會自動呼叫showDiaries()
                                // 所以diaries不需要移除被刪除的diary，也可以省略呼叫showDiaries()
                                // diaries.remove(diary);
                                // showDiaries();
                            } else {
                                Toast.makeText(requireContext(), "日記刪除失敗", Toast.LENGTH_SHORT).show();
                            }
                        });
                return true;
            });
        }
    }
    // =========================以上自定義Adapter內部類別=========================


    private TextView getTextView(final String text) {
        TextView textView = new TextView(activity);
        Log.d(TAG, "getTextView()");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.TOP;
//        layoutParams.setMarginEnd(10);
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(24);
        textView.setText(text);
        textView.setPadding(3, 3, 3, 3);
        return textView;
    }

    // 下載Firebase storage的照片並顯示在ImageView上
    private void showImage(final ImageView imageView, final String path) {
        final int ONE_MEGABYTE = 1024 * 1024;
        // 取得雲端Storage的StorageReference物件
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap); // 將圖片顯示在imageView原件(ivDiary)上
                    } else {
                        String message = task.getException() == null ?
                                "textImageDownloadFail" + ": " + path :
                                task.getException().getMessage() + ": " + path;
                        Log.e(TAG, message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 監聽資料是否發生異動，有則同步更新。
     * 開啟2台模擬器，一台新增/修改/刪除；另一台畫面會同步更新
     * 但自己做資料異動也會觸發監聽器
     */
    private void listenToDiaries() {
        if (registration == null) {
            registration = db.collection("diaries").addSnapshotListener((snapshots, e) -> {
                Log.d(TAG, "event happened");
                if (e == null) {
                    List<Diary> diaries = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Diary diary = dc.getDocument().toObject(Diary.class);
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(TAG, "Added diary: " + diary.getDiaryName());
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified diary: " + diary.getDiaryName());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed diary: " + diary.getDiaryName());
                                    break;
                                default:
                                    break;
                            }
                        }

                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            diaries.add(document.toObject(Diary.class));
                        }
                        this.diaries = diaries;
                        showDiaries();
                    }
                } else {
                    Log.e(TAG, e.getMessage(), e);
                }
            });
        }
    }
}
