package idv.tgp10110.tgp10110ryanchiang.fragment_second;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import idv.tgp10110.tgp10110ryanchiang.R;

public class MapFragment extends Fragment {
    private static final String TAG = "TAG_MapFragment";
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private MapView mapView;
    private GoogleMap googleMap;
    private Activity activity;

    // 初始化與畫面無直接關係之資料
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // 載入並建立Layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity(); // 取得Activity參考
        // 將指定的Layout充氣(Inflate the layout for this fragment)
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    // Layout已建立
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestPermissionLauncher = getRequestPermissionLauncher();

        findViews(view);
        handleMapView(savedInstanceState);
    }


    private void findViews(View view) {
        mapView = view.findViewById(R.id.mapView);
    }

    private ActivityResultLauncher<String> getRequestPermissionLauncher() {
        return registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        showMyLocation();
                    }
                });
    }

    private void handleMapView(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.onStart();
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
//            googleMap.setOnMapLongClickListener(this::addMarker);
            List<LatLng> castleLatLng = new ArrayList<>();
            LatLng castle_1 = new LatLng(43.3887273, 145.7824096);
            LatLng castle_2 = new LatLng(41.7971839, 140.7568306);
            LatLng castle_3 = new LatLng(41.4299007, 140.108412);
            LatLng castle_4 = new LatLng(40.6074516, 140.4641804);
            LatLng castle_5 = new LatLng(40.5058021, 141.4623075);
            LatLng castle_6 = new LatLng(39.6999544, 141.1479233);
            LatLng castle_7 = new LatLng(38.2952022, 140.9827552);
            LatLng castle_8 = new LatLng(38.2544625, 140.8502011);
            LatLng castle_9 = new LatLng(39.7224884, 140.1217534);
            LatLng castle_10 = new LatLng(38.1985928, 140.2246166);
            LatLng castle_11 = new LatLng(37.5986488, 140.4277994);
            LatLng castle_12 = new LatLng(37.487824, 139.927477);
            LatLng castle_13 = new LatLng(37.132411, 140.2115032);
            LatLng castle_14 = new LatLng(36.3747514, 140.4767657);
            LatLng castle_15 = new LatLng(36.3375072, 139.4500917);
            LatLng castle_16 = new LatLng(36.4049555, 138.9487678);
            LatLng castle_17 = new LatLng(36.316845, 139.3749143);
            LatLng castle_18 = new LatLng(36.1065743, 139.1940873);
            LatLng castle_19 = new LatLng(35.9244802, 139.4895383);
            LatLng castle_20 = new LatLng(35.722386, 140.2139217);
            LatLng castle_21 = new LatLng(35.6877513, 139.7525013);
            LatLng castle_22 = new LatLng(35.652765, 139.2541586);
            LatLng castle_23 = new LatLng(35.2509537, 139.1513249);
            LatLng castle_24 = new LatLng(35.6869648, 138.5747726);
            LatLng castle_25 = new LatLng(35.6652788, 138.5691437);
            LatLng castle_26 = new LatLng(36.5660163, 138.193845);
            LatLng castle_27 = new LatLng(36.4061596, 138.2308484);
            LatLng castle_28 = new LatLng(36.3320521, 138.3854852);
            LatLng castle_29 = new LatLng(36.2386563, 137.9667664);
            LatLng castle_30 = new LatLng(35.8334202, 138.0600325);
            LatLng castle_31 = new LatLng(37.9279367, 139.3012556);
            LatLng castle_32 = new LatLng(37.1491434, 138.211568);
            LatLng castle_33 = new LatLng(36.7479361, 137.0188998);
            LatLng castle_34 = new LatLng(37.0278498, 136.9574573);
            LatLng castle_35 = new LatLng(36.5655059, 136.6578294);
            LatLng castle_36 = new LatLng(36.1523615, 136.2699571);
            LatLng castle_37 = new LatLng(35.9994633, 136.2937435);
            LatLng castle_38 = new LatLng(35.3623027, 137.4450982);
            LatLng castle_39 = new LatLng(35.433918, 136.7798826);
            LatLng castle_40 = new LatLng(35.1558275, 138.9907462);
            LatLng castle_41 = new LatLng(34.9792945, 138.3817985);
            LatLng castle_42 = new LatLng(34.7752332, 138.011705);
            LatLng castle_43 = new LatLng(35.3883547, 136.9369888);
            LatLng castle_44 = new LatLng(35.1846657, 136.897493);
            LatLng castle_45 = new LatLng(34.9562994, 137.1566643);
            LatLng castle_46 = new LatLng(34.9227512, 137.5575079);
            LatLng castle_47 = new LatLng(34.770164, 136.1249213);
            LatLng castle_48 = new LatLng(34.5673108, 136.4909627);
            LatLng castle_49 = new LatLng(35.4740036, 136.2081794);
            LatLng castle_50 = new LatLng(35.276452, 136.2474686);
            LatLng castle_51 = new LatLng(35.1534372, 136.1408504);
            LatLng castle_52 = new LatLng(35.1458413, 136.1554885);
            LatLng castle_53 = new LatLng(35.0142299, 135.748218);
            LatLng castle_54 = new LatLng(34.6873153, 135.5262013); // 大阪城
            LatLng castle_55 = new LatLng(34.4169867, 135.6492481);
            LatLng castle_56 = new LatLng(35.3006044, 134.8291681);
            LatLng castle_57 = new LatLng(35.0730832, 135.2156322);
            LatLng castle_58 = new LatLng(34.6528009, 134.9917886);
            LatLng castle_59 = new LatLng(34.839449, 134.6939047);
            LatLng castle_60 = new LatLng(34.745674, 134.388985);
            LatLng castle_61 = new LatLng(34.4293872, 135.8246734);
            LatLng castle_62 = new LatLng(34.2294761, 135.1738548);
            LatLng castle_63 = new LatLng(35.507426, 134.239995);
            LatLng castle_64 = new LatLng(35.4751335, 133.0484896);
            LatLng castle_65 = new LatLng(35.3609317, 133.1830298);
            LatLng castle_66 = new LatLng(34.4582171, 131.7589231);
            LatLng castle_67 = new LatLng(35.0621398, 134.0030167);
            LatLng castle_68 = new LatLng(34.809081, 133.6201174);
            LatLng castle_69 = new LatLng(34.7254737, 133.7601847);
            LatLng castle_70 = new LatLng(34.66519, 133.9338693);
            LatLng castle_71 = new LatLng(34.491069, 133.3589302);
            LatLng castle_72 = new LatLng(34.6736223, 132.7073215);
            LatLng castle_73 = new LatLng(34.4014949, 132.4552292);
            LatLng castle_74 = new LatLng(34.1752398, 132.172017);
            LatLng castle_75 = new LatLng(34.4179549, 131.3820209);
            LatLng castle_76 = new LatLng(34.0660912, 134.5464894);
            LatLng castle_77 = new LatLng(34.3554467, 134.0474817);
            LatLng castle_78 = new LatLng(34.2859572, 133.7980924);
            LatLng castle_79 = new LatLng(34.064502, 133.0059763);
            LatLng castle_80 = new LatLng(33.8484885, 132.7839616);
            LatLng castle_81 = new LatLng(33.8455768, 132.7633459);
            LatLng castle_82 = new LatLng(33.509524, 132.5389233);
            LatLng castle_83 = new LatLng(33.219449, 132.5630103);
            LatLng castle_84 = new LatLng(33.5607925, 133.5293004);
            LatLng castle_85 = new LatLng(33.592764, 130.3806112);
            LatLng castle_86 = new LatLng(33.5398007, 130.5221919);
            LatLng castle_87 = new LatLng(33.529083, 129.8675613);
            LatLng castle_88 = new LatLng(33.3242445, 130.3844096);
            LatLng castle_89 = new LatLng(33.2451406, 130.3002167);
            LatLng castle_90 = new LatLng(33.3685375, 129.5569425);
            LatLng castle_91 = new LatLng(32.7892192, 130.3650807);
            LatLng castle_92 = new LatLng(32.8061859, 130.7036448);
            LatLng castle_93 = new LatLng(32.2108918, 130.7597978);
            LatLng castle_94 = new LatLng(33.2398952, 131.6097883);
            LatLng castle_95 = new LatLng(32.9691026, 131.405579);
            LatLng castle_96 = new LatLng(31.6291057, 131.3481154);
            LatLng castle_97 = new LatLng(31.5983168, 130.5528058);
            LatLng castle_98 = new LatLng(26.6912793, 127.9268339);
            LatLng castle_99 = new LatLng(26.2835007, 127.7974821);
            LatLng castle_100 = new LatLng(26.2170135, 127.7173321);

            castleLatLng.add(castle_1);
            castleLatLng.add(castle_2);
            castleLatLng.add(castle_3);
            castleLatLng.add(castle_4);
            castleLatLng.add(castle_5);
            castleLatLng.add(castle_6);
            castleLatLng.add(castle_7);
            castleLatLng.add(castle_8);
            castleLatLng.add(castle_9);
            castleLatLng.add(castle_10);
            castleLatLng.add(castle_11);
            castleLatLng.add(castle_12);
            castleLatLng.add(castle_13);
            castleLatLng.add(castle_14);
            castleLatLng.add(castle_15);
            castleLatLng.add(castle_16);
            castleLatLng.add(castle_17);
            castleLatLng.add(castle_18);
            castleLatLng.add(castle_19);
            castleLatLng.add(castle_20);
            castleLatLng.add(castle_21);
            castleLatLng.add(castle_22);
            castleLatLng.add(castle_23);
            castleLatLng.add(castle_24);
            castleLatLng.add(castle_25);
            castleLatLng.add(castle_26);
            castleLatLng.add(castle_27);
            castleLatLng.add(castle_28);
            castleLatLng.add(castle_29);
            castleLatLng.add(castle_30);
            castleLatLng.add(castle_31);
            castleLatLng.add(castle_32);
            castleLatLng.add(castle_33);
            castleLatLng.add(castle_34);
            castleLatLng.add(castle_35);
            castleLatLng.add(castle_36);
            castleLatLng.add(castle_37);
            castleLatLng.add(castle_38);
            castleLatLng.add(castle_39);
            castleLatLng.add(castle_40);
            castleLatLng.add(castle_41);
            castleLatLng.add(castle_42);
            castleLatLng.add(castle_43);
            castleLatLng.add(castle_44);
            castleLatLng.add(castle_45);
            castleLatLng.add(castle_46);
            castleLatLng.add(castle_47);
            castleLatLng.add(castle_48);
            castleLatLng.add(castle_49);
            castleLatLng.add(castle_50);
            castleLatLng.add(castle_51);
            castleLatLng.add(castle_52);
            castleLatLng.add(castle_53);
            castleLatLng.add(castle_54);
            castleLatLng.add(castle_55);
            castleLatLng.add(castle_56);
            castleLatLng.add(castle_57);
            castleLatLng.add(castle_58);
            castleLatLng.add(castle_59);
            castleLatLng.add(castle_60);
            castleLatLng.add(castle_61);
            castleLatLng.add(castle_62);
            castleLatLng.add(castle_63);
            castleLatLng.add(castle_64);
            castleLatLng.add(castle_65);
            castleLatLng.add(castle_66);
            castleLatLng.add(castle_67);
            castleLatLng.add(castle_68);
            castleLatLng.add(castle_69);
            castleLatLng.add(castle_70);
            castleLatLng.add(castle_71);
            castleLatLng.add(castle_72);
            castleLatLng.add(castle_73);
            castleLatLng.add(castle_74);
            castleLatLng.add(castle_75);
            castleLatLng.add(castle_76);
            castleLatLng.add(castle_77);
            castleLatLng.add(castle_78);
            castleLatLng.add(castle_79);
            castleLatLng.add(castle_80);
            castleLatLng.add(castle_81);
            castleLatLng.add(castle_82);
            castleLatLng.add(castle_83);
            castleLatLng.add(castle_84);
            castleLatLng.add(castle_85);
            castleLatLng.add(castle_86);
            castleLatLng.add(castle_87);
            castleLatLng.add(castle_88);
            castleLatLng.add(castle_89);
            castleLatLng.add(castle_90);
            castleLatLng.add(castle_91);
            castleLatLng.add(castle_92);
            castleLatLng.add(castle_93);
            castleLatLng.add(castle_94);
            castleLatLng.add(castle_95);
            castleLatLng.add(castle_96);
            castleLatLng.add(castle_97);
            castleLatLng.add(castle_98);
            castleLatLng.add(castle_99);
            castleLatLng.add(castle_100);


            castleLatLng.add(castle_1);
            String[] hundredCastles = {"根室半島砦跡群", "五稜郭", "松前城", "弘前城", "根城", "盛岡城", "多賀城", "仙台城", "久保田城", "山形城", "二本松城",
                    "會津若松城", "白河小峰城", "水戶城", "足利氏館", "箕輪城", "新田金山城", "鉢形城", "川越城", "佐倉城", "江戶城", "八王子城", "小田原城", "武田氏館",
                    "甲府城", "松代城", "上田城", "小諸城", "松本城", "高遠城", "新發田城", "春日山城", "高岡城", "七尾城", "金澤城", "丸岡城", "一乘谷城", "岩村城",
                    "岐阜城", "山中城", "駿府城", "掛川城", "犬山城", "名古屋城", "岡崎城", "長篠城", "伊賀上野城", "松坂城", "小谷城", "彥根城", "安土城", "觀音寺城",
                    "二条城", "大阪城", "千早城", "竹田城", "篠山城", "明石城", "姬路城", "赤穗城", "高取城", "和歌山城", "鳥取城", "松江城", "月山富田城", "津和野城",
                    "津山城", "備中松山城", "鬼之城", "岡山城", "福山城", "郡山城", "廣島城", "岩國城", "萩城", "德島城", "高松城", "丸龜城", "今治城", "湯築城",
                    "松山城", "大洲城", "宇和島城", "高知城", "福岡城", "大野城", "名護屋城", "吉野之里", "佐賀城", "平戶城", "島原城", "熊本城", "人吉城", "大分府內城",
                    "岡城", "飫肥城", "鹿兒島城", "今歸仁城", "中城城", "首里城"};


            for (int i = 0; i < 100; i++) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(castleLatLng.get(i))
                        .title(hundredCastles[i])
                        .snippet("No." + (i + 1))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_castle_map_red_48))
                        .draggable(false);
                googleMap.addMarker(markerOptions);
            }

//            addMarker(latLng);
            googleMap.setOnInfoWindowLongClickListener(Marker::remove);

        });
    }

//    private void addMarker(LatLng latLng) {
//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(latLng)
//                .title("TEST")
//                .snippet("簡述文字")
//                .draggable(false);
//        googleMap.addMarker(markerOptions);
//    }

    private void showMyLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        googleMap.setMyLocationEnabled(true);
//        final Location location = googleMap.getMyLocation();
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


        LatLng latLngCenter = new LatLng(34.6873153, 135.5262013);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLngCenter)
                .zoom(5)
//                .tilt(45)
//                .bearing(90)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(latLngCenter)
//                .title("TEST")
//                .snippet("簡述文字")
//                .draggable(true);
//        googleMap.addMarker(markerOptions);
//        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // ⼀般圖
//        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN); // 地形圖
//        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // 衛星圖
//        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // ⼀般圖+衛星圖
//        googleMap.setTrafficEnabled(true); // 交通資訊圖層
//        googleMap.setBuildingsEnabled(true); // 建築物圖層

        // 按鈕相關設定
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false); // 是否顯示回到目前定位按鈕(右上角)
        uiSettings.setZoomControlsEnabled(true); // 是否顯示縮放按鈕
        uiSettings.setCompassEnabled(true); // 是否顯示指北針
        // 手勢相關設定
        uiSettings.setScrollGesturesEnabled(true); // 捲動手勢
        uiSettings.setZoomGesturesEnabled(true); // 縮放手勢
        uiSettings.setTiltGesturesEnabled(false); // 傾斜手勢
        uiSettings.setRotateGesturesEnabled(false); // 旋轉手勢


    }


    @Override
    public void onStart() {
        super.onStart();
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // 回傳訊息視窗元件
        @Nullable
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            final View view = View.inflate(activity, R.layout.infowindow_castle_map, null);
            final String title = marker.getTitle();
            final String snippet = marker.getSnippet();
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvSnippet = view.findViewById(R.id.tvSnippet);
            tvTitle.setText(title);
            tvSnippet.setText(snippet);
            return view;
        }

        // 回傳訊息視窗的內容元件
        @Nullable
        @Override
        public View getInfoContents(@NonNull Marker marker) {
            return null;
        }


    }


}





