package idv.tgp10110.tgp10110ryanchiang.fragment_second;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import idv.tgp10110.tgp10110ryanchiang.R;
import idv.tgp10110.tgp10110ryanchiang.bean.Castle;


public class StampFragment extends Fragment {
    private static final String TAG = "TAG_StampFragment";
    private RecyclerView rvStamps;
    private SearchView searchView;
    private Activity activity;
    private File file;
    private Uri contentUri; // 拍照用Uri
    private Uri cropImageUri; // 裁切用Uri
    private FirebaseFirestore db; // 雲端資料庫(NoSQL)
    private FirebaseStorage storage; // 存圖檔
    private ListenerRegistration registration;
    private Bitmap bitmap;
    private ImageView curImageView;
    private Castle curCastle;

    // 拍照啟動器
    ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::takePictureResult);

    // 裁切啟動器
    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::cropPictureResult);

    // 初始化與畫面無直接關係之資料
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // 取得Firebase資料庫物件(存castle)
        storage = FirebaseStorage.getInstance(); // 取得FirebaseStorage物件(存圖片用)
    }

    // 載入並建立Layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity(); // 取得Activity參考
        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_stamp, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
    }

    // 畫面即將顯示前
    @Override
    public void onStart() {
        super.onStart();
//        handleSearchView();

        handleRecyclerView();
    }

    private void findViews(View view) {
        rvStamps = view.findViewById(R.id.rvStamps);
        searchView = view.findViewById(R.id.searchView);
    }

    private void handleSearchView() {
        // 註冊/實作 查詢文字監聽器
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 當點擊提交鍵(虛擬鍵盤)時，自動被呼叫
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 當查詢文字改變時，自動被呼叫
            @Override
            public boolean onQueryTextChange(String newText) {
                StampAdapter adapter = (StampAdapter) rvStamps.getAdapter();
                if (adapter == null) {
                    return false;
                }

                if (newText.isEmpty()) {
                    adapter.castles = getCastleList();
                } else {
                    List<Castle> resultList = new ArrayList<>();
                    for (Castle castle : adapter.castles) {
                        if (castle.getCastleName().toLowerCase().contains(newText.toLowerCase())) {
                            resultList.add(castle);
                        }
                    }
                    adapter.castles = resultList;
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void handleRecyclerView() {
        List<Castle> castleList = getCastleList();

        db.collection("castles").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            // 將儲存在Firestore內資料庫的各個Spot物件都加進List<Spot>裡
                            Castle recyclerCastle = document.toObject(Castle.class);
                            Log.d(TAG, "name" + recyclerCastle.getCastleName());
                            Log.d(TAG, "number" + recyclerCastle.getCastleNumber());
                            int castleIndex = recyclerCastle.getCastleNumber() - 1;

                            if (castleIndex >= 0) {
                                castleList.set(castleIndex, recyclerCastle);
                                showAllCastles();
                            }
                        }
                    } else {
                        String message = task.getException() == null ?
                                "No Message" :
                                task.getException().getMessage();
                        Log.e(TAG, "exception message: " + message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    StampAdapter curMyAdapter = (StampAdapter) rvStamps.getAdapter();

                    curMyAdapter.notifyDataSetChanged();

                });

        StampAdapter stampAdapter = (StampAdapter) rvStamps.getAdapter();
        if (stampAdapter == null) {
            stampAdapter = new StampAdapter(activity, castleList);
            rvStamps.setAdapter(stampAdapter);
        }

        // LayoutManager(設定外觀用)
        rvStamps.setLayoutManager(new LinearLayoutManager(activity));
    }

    private void showAllCastles() {
    }

    // 處理拍照回傳結果並呼叫裁切方法
    private void takePictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            crop(contentUri);
        }
    }

    // 設定裁切並開啟裁切發射器
    private void crop(Uri sourceImageUri) {
        File file = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        file = new File(file, "picture_cropped.jpg");
        Uri destinationUri = Uri.fromFile(file);
        Intent cropIntent = UCrop.of(sourceImageUri, destinationUri)
                .withAspectRatio(1, 1) // 設定裁減比例(限定固定為正方形)
//                .withMaxResultSize(500, 500) // 設定結果尺寸不可超過指定寬高
                .getIntent(requireContext());
        cropPictureLauncher.launch(cropIntent);
    }

    // 處理裁切回傳結果
    private void cropPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            cropImageUri = UCrop.getOutput(result.getData());
            if (cropImageUri != null) {
                bitmap = null;
                try {
                    // 取得Uri物件
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
                        // Android 9-
                        // 取得InputStream物件
                        InputStream is = activity.getContentResolver().openInputStream(cropImageUri);
                        // 取得Bitmap物件
                        bitmap = BitmapFactory.decodeStream(is);
                    } else {
                        // Android 9(+
                        // 從Uri物件建立ImageDecoder.Source物件
                        ImageDecoder.Source source = ImageDecoder.createSource(
                                activity.getContentResolver(),
                                cropImageUri);
                        // 取得Bitmap物件
                        bitmap = ImageDecoder.decodeBitmap(source);
//==================================================================================

                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                // 將照片顯示在ImageView元件上
                uploadImage(cropImageUri);

            }
        }
    }

    private void uploadImage(Uri imageUri) {
        // 取得storage根目錄位置
        StorageReference rootRef = storage.getReference();
        final String imagePath = getString(R.string.app_name) + "/castles/" +
                curCastle.getCastleNumber() + "_" + curCastle.getCastleId();
        Log.d(TAG, "imagePath : " + imagePath);
        // 建立當下目錄的子路徑
        final StorageReference imageRef = rootRef.child(imagePath);
        // 將儲存在uri的照片上傳
        imageRef.putFile(imageUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "上傳成功";
                        // 圖檔新增成功再將圖檔路徑存入castle物件所代表的document內
                        curCastle.setCastleImagePath(imagePath);
                        curCastle.setStamped(true);


                        Log.d(TAG, "curCastle" + curCastle.getCastleImagePath());

                        Toast.makeText(requireContext(), "恭喜攻略" + curCastle.getCastleName() + "成功", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException() == null ?
                                "上傳失敗" :
                                task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    curImageView.setImageBitmap(bitmap);

                    addOrReplace(curCastle);


                });
    }

    // 新增或修改Firestore上的景點
    private void addOrReplace(final Castle castle) {
        // 如果Firestore沒有該ID的Document就建立新的，已經存在就更新內容
        db.collection("castles").document(castle.getCastleId()).set(castle)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "已完成 : "
                                + " with ID: " + castle.getCastleId();
                        Log.d(TAG, message);
//                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                    } else {
                        String message = task.getException() == null ?
                                "沒有成功" :
                                task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    // ==========自定義Adapter內部類別==========<含有內部自定義類別MyAdapter.MyViewHolder>
    class StampAdapter extends RecyclerView.Adapter<StampAdapter.StampViewHolder> {
        Context context;
        List<Castle> castles;

        public StampAdapter(Context context, List<Castle> castles) {
            this.context = context;
            this.castles = castles;
        }

        public void setCastles(List<Castle> castles) {
            this.castles = castles;
        }

        // ====================自定義AdapterHolder內部類別====================
        class StampViewHolder extends RecyclerView.ViewHolder {
            ImageView ivStamp;
            TextView tvName;
            TextView tvDate;
            ImageView ivFavorite;

            // 建構子1個參數(View型態)，該參數就是選項容器元件，用來取得各容器元件的參考
            public StampViewHolder(@NonNull View itemView) {
                super(itemView);
                ivStamp = itemView.findViewById(R.id.ivStamp);
                tvName = itemView.findViewById(R.id.tvDiaryTitle);
                tvDate = itemView.findViewById(R.id.tvDiaryDate);
                ivFavorite = itemView.findViewById(R.id.ivFavorite);

                // ==========第二種監聽器放置位置==========
                // 此種方式MyAdapter及MyViewHolder兩個內部類別皆不得為static
                itemView.setOnClickListener(view ->
                        Toast.makeText(context, tvName.getText(), Toast.LENGTH_SHORT).show());
                ivStamp.setOnClickListener(view -> {
                });
            }
        }
        // ====================以上自定義AdapterHolder內部類別====================

        // 以下為父類別RecyclerView.Adapter三個須Override方法
        // 回傳選項數量(百名城)
        @Override
        public int getItemCount() {
            return castles == null ? 0 : castles.size();
        }

        // 宣告ItemView，並載入選項容器元件的外觀
        // 實例化自定義的viewHolder，並回傳
        @NonNull
        @Override
        public StampViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 從context取出打氣筒/載入器(LayoutInflater)將layout檔(cardview_item)充氣
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_view_stamp, parent, false); // 間接依存(RecyclerView適用)

            return new StampViewHolder(itemView);
        }

        // 透過自定義的viewHolder物件，將資料綁定至各元件上(並對各元件進行其他處理)
        @Override
        public void onBindViewHolder(@NonNull StampViewHolder viewHolder, int position) {
            // 取得目前ViewHolder的位置，才能精準得知要對哪個Castle物件進行操作
            final Castle castle = castles.get(position); // 取得當前位置(目前哪個城)

            // 若無法取得Castle物件的圖片路徑(物件該屬性為空值)，就設定為預設圖片
            if (castle.getCastleImagePath() == null) {
                viewHolder.ivStamp.setImageResource(R.drawable.default_stamp_pic);
            } else {
                downloadImage(viewHolder.ivStamp, castle); // 若有取得則呼叫方法顯示

            }

//            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
//            Date stampedDate2 = castle.getStampedDate();
//            Log.d(TAG, "name" + castle.getCastleName() + "----" + castle.getCastleNumber());
//            Log.d(TAG, "date" + castle.getStampedDate() == null ? "null" : "YA~~~");
//
//            String timeStamp2 = new SimpleDateFormat("yyyy/MM/dd").format(stampedDate2);
//            viewHolder.tvDate.setText(timeStamp2 + "攻略");
//
//
//            viewHolder.tvDate.setText("這裡要放拍照日期");

            String stamp = castle.getStrStampedDate();
            Log.d(TAG, "XXXXXXXXXIIIIIIIIIIBBBBBBBBBBBB : " + stamp);
            if (castle.isStamped()) {
                viewHolder.tvDate.setText(stamp + "攻略");
            } else {
                viewHolder.tvDate.setText("此城尚未攻略");
            }

            viewHolder.tvName.setText(castle.getCastleName());

            if (castle.isFavorite()) {
                viewHolder.ivFavorite.setImageResource(R.drawable.ic_heart_48);
            } else {
                viewHolder.ivFavorite.setImageResource(R.drawable.ic_heart_plus_48);
            }

            viewHolder.ivStamp.setTag(position);
            // ==========第一種監聽器放置位置==========

            // 愛心監聽器
            viewHolder.ivFavorite.setOnClickListener(view -> {
                if (castle.getCastleId() != null) {
                    if (castle.isFavorite()) {
                        viewHolder.ivFavorite.setImageResource(R.drawable.ic_heart_plus_48);
                        castle.setFavorite(false);
                        Toast.makeText(requireContext(), "已將此城移除我的最愛", Toast.LENGTH_SHORT).show();
                    } else {
                        viewHolder.ivFavorite.setImageResource(R.drawable.ic_heart_48);
                        castle.setFavorite(true);
                        Toast.makeText(requireContext(), "已將此城加入我的最愛", Toast.LENGTH_SHORT).show();

                    }

                    addOrReplace(castle);
                } else {
                    Toast.makeText(requireContext(), "必須攻略完成才能加入我的最愛", Toast.LENGTH_SHORT).show();
                }

            });

// =================================================================================================
// =================================================================================================
// =================================================================================================
            // 城章圖片監聽器
            viewHolder.ivStamp.setOnClickListener(view -> {
                // 將當前的Castle物件指派給外部類別(RecyclerFragment)使用
                StampFragment.this.curCastle = castle;
                // 先取得插入document的ID


                final String id = db.collection("castles").document().getId();
                castle.setCastleId(id);
                Log.d(TAG, "castle.setId(id) : " + id);

                curImageView = (ImageView) view;

                // 新建拍照意圖物件(意圖呼叫相機功能)
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 取得外部檔案目錄(資料夾)
                File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (dir != null && !dir.exists()) {
                    if (!dir.mkdirs()) {
                        Log.e(TAG, "textDirNotCreated");
                        return;
                    }
                }
                // 建立裁切前圖片之File物件
                file = new File(dir, "before_crop.jpg");
                contentUri = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".fileProvider",
                        file);
                // 將Uri放入Intent物件中傳遞
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                // 開啟拍照發射器
                try {
                    takePictureLauncher.launch(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(requireContext(), "textNoCameraAppFound",
                            Toast.LENGTH_SHORT).show();
                }


                TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
                Date stampedDate = new Date();
                String timeStamp = new SimpleDateFormat("yyyy/MM/dd").format(stampedDate);


//                viewHolder.tvDate.setText(timeStamp + "攻略");
                castle.setStrStampedDate(timeStamp);
                castle.setStamped(true);
                castle.setStampedDate(stampedDate);
//                castle.setImagePath(uploadImage(cropImageUri));
                viewHolder.tvDate.setText(castle.getStrStampedDate() + "攻略");
            });
        }
    }
    // ==========以上自定義Adapter內部類別==========

    // 下載Firebase storage的照片並顯示在ImageView上
    private void downloadImage(final ImageView imageView, final Castle castle) {
        final int ONE_MEGABYTE = 1024 * 1024;
        // 取得雲端Storage的StorageReference物件
        StorageReference imageRef = storage.getReference().child(castle.getCastleImagePath());
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        String message = task.getException() == null ?
                                "textImageDownloadFail" + ": " + castle.getCastleImagePath() :
                                task.getException().getMessage() + ": " + castle.getCastleImagePath();
                        Log.e(TAG, message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }





    private List<Castle> getCastleList() {
        List<Castle> castleList = new ArrayList<>();
        castleList.add(new Castle(1, "根室半島砦跡群"));
        castleList.add(new Castle(2, "五稜郭"));
        castleList.add(new Castle(3, "松前城"));
        castleList.add(new Castle(4, "弘前城"));
        castleList.add(new Castle(5, "根城"));
        castleList.add(new Castle(6, "盛岡城"));
        castleList.add(new Castle(7, "多賀城"));
        castleList.add(new Castle(8, "仙台城"));
        castleList.add(new Castle(9, "久保田城"));
        castleList.add(new Castle(10, "山形城"));
        castleList.add(new Castle(11, "二本松城"));
        castleList.add(new Castle(12, "會津若松城"));
        castleList.add(new Castle(13, "白河小峰城"));
        castleList.add(new Castle(14, "水戶城"));
        castleList.add(new Castle(15, "足利氏館"));
        castleList.add(new Castle(16, "箕輪城"));
        castleList.add(new Castle(17, "新田金山城"));
        castleList.add(new Castle(18, "鉢形城"));
        castleList.add(new Castle(19, "川越城"));
        castleList.add(new Castle(20, "佐倉城"));
        castleList.add(new Castle(21, "江戶城"));
        castleList.add(new Castle(22, "八王子城"));
        castleList.add(new Castle(23, "小田原城"));
        castleList.add(new Castle(24, "武田氏館"));
        castleList.add(new Castle(25, "甲府城"));
        castleList.add(new Castle(26, "松代城"));
        castleList.add(new Castle(27, "上田城"));
        castleList.add(new Castle(28, "小諸城"));
        castleList.add(new Castle(29, "松本城"));
        castleList.add(new Castle(30, "高遠城"));
        castleList.add(new Castle(31, "新發田城"));
        castleList.add(new Castle(32, "春日山城"));
        castleList.add(new Castle(33, "高岡城"));
        castleList.add(new Castle(34, "七尾城"));
        castleList.add(new Castle(35, "金澤城"));
        castleList.add(new Castle(36, "丸岡城"));
        castleList.add(new Castle(37, "一乘谷城"));
        castleList.add(new Castle(38, "岩村城"));
        castleList.add(new Castle(39, "岐阜城"));
        castleList.add(new Castle(40, "山中城"));
        castleList.add(new Castle(41, "駿府城"));
        castleList.add(new Castle(42, "掛川城"));
        castleList.add(new Castle(43, "犬山城"));
        castleList.add(new Castle(44, "名古屋城"));
        castleList.add(new Castle(45, "岡崎城"));
        castleList.add(new Castle(46, "長篠城"));
        castleList.add(new Castle(47, "伊賀上野城"));
        castleList.add(new Castle(48, "松坂城"));
        castleList.add(new Castle(49, "小谷城"));
        castleList.add(new Castle(50, "彥根城"));
        castleList.add(new Castle(51, "安土城"));
        castleList.add(new Castle(52, "觀音寺城"));
        castleList.add(new Castle(53, "二条城"));
        castleList.add(new Castle(54, "大阪城"));
        castleList.add(new Castle(55, "千早城"));
        castleList.add(new Castle(56, "竹田城"));
        castleList.add(new Castle(57, "篠山城"));
        castleList.add(new Castle(58, "明石城"));
        castleList.add(new Castle(59, "姬路城"));
        castleList.add(new Castle(60, "赤穗城"));
        castleList.add(new Castle(61, "高取城"));
        castleList.add(new Castle(62, "和歌山城"));
        castleList.add(new Castle(63, "鳥取城"));
        castleList.add(new Castle(64, "松江城"));
        castleList.add(new Castle(65, "月山富田城"));
        castleList.add(new Castle(66, "津和野城"));
        castleList.add(new Castle(67, "津山城"));
        castleList.add(new Castle(68, "備中松山城"));
        castleList.add(new Castle(69, "鬼之城"));
        castleList.add(new Castle(70, "岡山城"));
        castleList.add(new Castle(71, "福山城"));
        castleList.add(new Castle(72, "郡山城"));
        castleList.add(new Castle(73, "廣島城"));
        castleList.add(new Castle(74, "岩國城"));
        castleList.add(new Castle(75, "萩城"));
        castleList.add(new Castle(76, "德島城"));
        castleList.add(new Castle(77, "高松城"));
        castleList.add(new Castle(78, "丸龜城"));
        castleList.add(new Castle(79, "今治城"));
        castleList.add(new Castle(80, "湯築城"));
        castleList.add(new Castle(81, "松山城"));
        castleList.add(new Castle(82, "大洲城"));
        castleList.add(new Castle(83, "宇和島城"));
        castleList.add(new Castle(84, "高知城"));
        castleList.add(new Castle(85, "福岡城"));
        castleList.add(new Castle(86, "大野城"));
        castleList.add(new Castle(87, "名護屋城"));
        castleList.add(new Castle(88, "吉野之里"));
        castleList.add(new Castle(89, "佐賀城"));
        castleList.add(new Castle(90, "平戶城"));
        castleList.add(new Castle(91, "島原城"));
        castleList.add(new Castle(92, "熊本城"));
        castleList.add(new Castle(93, "人吉城"));
        castleList.add(new Castle(94, "大分府內城"));
        castleList.add(new Castle(95, "岡城"));
        castleList.add(new Castle(96, "飫肥城"));
        castleList.add(new Castle(97, "鹿兒島城"));
        castleList.add(new Castle(98, "今歸仁城"));
        castleList.add(new Castle(99, "中城城"));
        castleList.add(new Castle(100, "首里城"));


        return castleList;
    }


}