package idv.tgp10110.tgp10110ryanchiang.fragment_second;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import idv.tgp10110.tgp10110ryanchiang.R;
import idv.tgp10110.tgp10110ryanchiang.bean.Diary;


public class DiaryUpdateFragment extends Fragment implements DialogInterface.OnClickListener,
        DatePickerDialog.OnDateSetListener {
    private static final String TAG = "TAG_DiaryUpdateFragment";
    private FirebaseFirestore db; // 雲端資料庫(NoSQL)
    private FirebaseStorage storage; // 存圖檔
    private ImageView ivDiary; // 日記圖片
    private EditText etName, etDate, etDetail, etRemark;
    private File file;
    private Uri contentUri; // 拍照需要的Uri
    private Uri cropImageUri; // 截圖的Uri
    private Diary diary; // Java Bean物件
    private Activity activity;
    private boolean pictureTaken; // 是否有拍照

    // 拍照啟動器
    ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::takePictureResult);
    // 選圖啟動器
    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::pickPictureResult);
    // 裁切啟動器
    ActivityResultLauncher<Intent> cropPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::cropPictureResult);

    // 取得拍照結果
    private void takePictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            crop(contentUri); // 拍照成功就進行裁切(傳入Uri當參數)
        }
    }

    // 取得選圖結果
    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                // 第一個getData()回傳Intent物件，第二個getData()回傳Uri物件
                crop(result.getData().getData()); // 拍照成功就進行裁切(傳入Uri當參數)
            }
        }
    }

    // 裁切照片
    private void crop(Uri sourceImageUri) {
        File file = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg"); // 裁切後的照片
        Uri destinationUri = Uri.fromFile(file); // 裁切後之照片的Uri
        // 設定裁切選項(來源Uri,輸出Uri)
        Intent cropIntent = UCrop.of(sourceImageUri, destinationUri)
//                .withAspectRatio(16, 9) // 設定裁減比例
//                .withMaxResultSize(500, 500) // 設定結果尺寸不可超過指定寬高
                .getIntent(requireContext());
        cropPictureLauncher.launch(cropIntent); // 開啟裁切啟動器
    }

    // 拍照或挑選照片完畢後都會裁切，裁切完畢會呼叫此方法
    private void cropPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            cropImageUri = UCrop.getOutput(result.getData()); // 裁切完之Uri
            if (cropImageUri != null) {
                Bitmap bitmap = null;
                try {
                    // 藉由裁切完之Uri取得點陣圖Bitmap物件
                    bitmap = BitmapFactory.decodeStream(
                            requireContext().getContentResolver().openInputStream(cropImageUri));
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                if (bitmap != null) {
                    ivDiary.setImageBitmap(bitmap); // 將圖片顯示在ivDiary上
                    pictureTaken = true; // 有拍照 or 選圖
                } else {
                    ivDiary.setImageResource(R.drawable.diary_no_image); // 若沒有拍照或選圖則顯示預設圖
                    pictureTaken = false;
                }
            }
        }
    }

    // 初始化與畫面無直接關係之資料
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity(); // 取得Activity參考
        db = FirebaseFirestore.getInstance(); // 取得Firebase資料庫物件
        storage = FirebaseStorage.getInstance(); // 取得FirebaseStorage物件(存圖片用)
        diary = new Diary(); // 建立Java Bean物件
    }

    // 載入並建立Layout
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_diary_update, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivDiary = view.findViewById(R.id.ivDiary);
        etName = view.findViewById(R.id.etName);
        etDate = view.findViewById(R.id.etDate);
        etDetail = view.findViewById(R.id.etDetail);
        etRemark = view.findViewById(R.id.etRemark);

        if (getArguments() != null) {
            diary = (Diary) getArguments().getSerializable("diary");
            if (diary != null) {
                etName.setText(diary.getDiaryName());
                etDate.setText(diary.getDiaryDate());
                etDetail.setText(diary.getDiaryDetail());
                etRemark.setText(diary.getDiaryRemark());

                // 如果存有圖片路徑，取得圖片後顯示
                if (diary.getDiaryImagePath() != null) {
                    showImage(ivDiary, diary.getDiaryImagePath());
                }
            }
        }

        // 拍照按鈕監聽器
        view.findViewById(R.id.btTakePicture).setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 意圖呼叫相機功能
            // 取得外部檔案目錄(資料夾)
            File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir != null && !dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "textDirNotCreated");
                    return;
                }
            }
            file = new File(dir, "picture_before_cropped.jpg");
            // 取得File物件的Uri
            contentUri = FileProvider.getUriForFile(
                    requireContext(), requireContext().getPackageName() + ".provider", file);
            // 將Uri放入Intent中傳遞
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            try {
                takePictureLauncher.launch(intent); // 開啟拍照啟動器
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), "textNoCameraAppFound",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 選圖按鈕監聽器
        view.findViewById(R.id.btPickPicture).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureLauncher.launch(intent); // 開啟裁切啟動器
        });

        etDate.setOnClickListener(View -> {
            // 1. 取得Calendar物件
            Calendar calendar = Calendar.getInstance();

            // 2. 實例化DatePickerDialog物件
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    activity,
                    (DatePickerDialog.OnDateSetListener) this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // 3. 設定可選取日期區間
            // 3.1 取得DatePicker物件
            DatePicker datePicker = datePickerDialog.getDatePicker();
            // 3.2 設定可選取的最小日期
            calendar.add(Calendar.YEAR, -16);
            datePicker.setMinDate(calendar.getTimeInMillis());
            // 3.3 設定可選取的最大日期
//            calendar.add(Calendar.MONTH, 2);
//            datePicker.setMaxDate(calendar.getTimeInMillis());

            // 4. 顯示對話框
            datePickerDialog.show();
        });

        // 提交按鈕監聽器
        view.findViewById(R.id.btFinishUpdate).setOnClickListener(v -> {
            // 取得使用者輸入之日記名稱/日期/內容/附註(皆String)
            String name = etName.getText().toString().trim();
            if (name.length() <= 0) {
                Toast.makeText(requireContext(), "日記名稱為必填欄位",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String date = etDate.getText().toString().trim();
            String detail = etDetail.getText().toString().trim();
            String remark = etRemark.getText().toString().trim();
            // 設定屬性
            diary.setDiaryName(name);
            diary.setDiaryDate(date);
            diary.setDiaryDetail(detail);
            diary.setDiaryRemark(remark);

            // 如果有拍照，上傳至Firebase storage
            if (pictureTaken) {
                // document ID成為image path一部分，避免與其他圖檔名稱重複
                final String imagePath = getString(R.string.app_name) + "/diaries/" + diary.getDiaryId();
                storage.getReference().child(imagePath).putFile(cropImageUri)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "textImageUploadSuccess");
                                // 圖檔新增成功再將圖檔路徑存入spot物件所代表的document內
                                diary.setDiaryImagePath(imagePath);
                            } else {
                                String message = task.getException() == null ?
                                        "textImageUploadFail" :
                                        task.getException().getMessage();
                                Log.e(TAG, "message: " + message);
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            }
                            // 無論圖檔上傳成功或失敗都要將文字資料新增至DB
                            addOrReplace(diary);
                        });
            } else {
                addOrReplace(diary); // 沒有圖片的話也要
            }
        });

        // 取消按鈕監聽器(返回上一頁)
        view.findViewById(R.id.btCancel).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
    }

    // 新增或修改Firestore上的日記
    private void addOrReplace(final Diary diary) {
        // 如果Firestore沒有該ID的Document就建立新的，已經有就更新內容
        db.collection("diaries").document(diary.getDiaryId()).set(diary)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "日記修改成功";
                        Log.d(TAG, message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        // 修改完畢回上頁
                        Navigation.findNavController(ivDiary).popBackStack();
                    } else {
                        String message = task.getException() == null ?
                                "日記修改失敗" :
                                task.getException().getMessage();
                        Log.e(TAG, "message: " + message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 下載Firebase storage的照片並顯示在ImageView上
    private void showImage(final ImageView imageView, final String path) {
        final int ONE_MEGABYTE = 1024 * 1024;
        StorageReference imageRef = storage.getReference().child(path);
        imageRef.getBytes(ONE_MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        byte[] bytes = task.getResult();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
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
     * DatePickerDialog的監聽器
     * 當日期被選取時，自動被呼叫
     *
     * @param view       DatePicker物件
     * @param year       選取的年
     * @param month      選取的月
     * @param dayOfMonth 選取的日
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        final String text = "" + year + "/" + (month + 1) + "/" + dayOfMonth;
        etDate.setText(text);
    }

    /**
     * AlertDialog的Button點擊監聽器
     * 當按鈕(確定、否定、不決定)被點擊時，自動被呼叫
     *
     * @param dialog AlertDialog物件
     * @param which  對話框按鈕編號
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            // 確定按鈕
            case DialogInterface.BUTTON_POSITIVE:
                Toast.makeText(requireContext(), "Submit successfully！", Toast.LENGTH_SHORT).show();
                break;
            // 否定/不決定 按鈕
            case DialogInterface.BUTTON_NEGATIVE:
            case DialogInterface.BUTTON_NEUTRAL:
                dialog.cancel();
                break;
        }
    }
}
