package com.example.week;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by 현기 on 2017-07-19.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    Button preButton;
    GoogleMap mMap;

    double latitude;
    double longitude;
    String titleRestorant;
    TextView addressView;
    Geocoder geocoder;
    String nowAddress;

    Bundle bundle;

    View view;//메인뷰

    //Map
    GoogleMap googleMap;

    //파이어베이스
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("RestorantINFO");

    //데이터 리스트
    ArrayList<RestorantVO> voList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // 서포트프래그먼트 중복 호출시 오류 방지
        try {
            view = inflater.inflate(R.layout.map_fragment, container, false);
        } catch (InflateException e) {
        }


        preButton = (Button) view.findViewById(R.id.map_preButton);
        addressView = (TextView) view.findViewById(R.id.address_view);

        voList = new ArrayList<>();

        getDataBundle();    // 데이터 가져오기, 없으면 기본데이터 셋팅
        preButtonAction();  // 버튼 클릭시 화면전환
        initializeMap(); //googleMap init
        addressViewSetAddress();    //주소 출력 창

        return view;
    }

    // 컨테이너에서 프래그먼트 뷰 제거
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
    }

    public void addressViewSetAddress() {
        geocoder = new Geocoder(getContext());
        List<Address> list = null;
        try {
            if(geocoder !=null){
                list = geocoder.getFromLocation(latitude, longitude, 1);

                if(list != null && list.size() > 0){
                    nowAddress = list.get(0).getAddressLine(0).toString();
                    addressView.setText(nowAddress);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("GeoAddress", "주소 실패");
        }

    }

    //데이터 가져오기
    public void getDataBundle() {
        bundle = this.getArguments();
        if (bundle != null) {
            latitude = bundle.getDouble("latitude", 35.141233);
            longitude = bundle.getDouble("longitude", 126.925594);
            titleRestorant = bundle.getString("titleRestorant", "맛집");
            Log.i("위도경도", latitude + "," + longitude);
        }

    }

    //이전 버튼 동작
    public void preButtonAction() {
        preButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 좋은 방법 - 프래그먼트를 유연하게 사용하자.
                 * 인터페이스를 통한 방법
                 * 굳이 MainActivity가 아니더라도
                 * FragmentReplaceable을 상속받으면
                 * 이 프래그먼트를 사용할 수 있다.
                 * */

                if (bundle == null) {
                    bundle = new Bundle();
                }
                ((FragmentReplaceable) getActivity()).replaceFragment(1, bundle);
            }
        });
    }

    //call this method in your onCreateMethod
    private void initializeMap() {
        if (mMap == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
            mapFrag.getMapAsync(this);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(this.getActivity());

        this.googleMap = googleMap;

        initMarker(googleMap);
        markerListener(googleMap);  // 마커 리스너

    }

    public void initMarker(GoogleMap googleMap){

        LatLng latLng = new LatLng(latitude,longitude);
        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        googleMap.animateCamera(cameraUpdate);
        getFirebaseDataMarkerInit();
    }

    //파이어베이스를 활용한 데이터가져오기
    public void getFirebaseDataMarkerInit(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.e("data", dataSnapshot.toString());
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                if(!map.isEmpty()){
                    Set keys = map.keySet();
                    Iterator<String> it = keys.iterator();
                    while(it.hasNext()){
                        String key = it.next();
                        Log.i("키값",map.get(key).toString());
                        HashMap keyMap = (HashMap) map.get(key);


                        LatLng latLng = new LatLng((double)keyMap.get("latitude"), (double)keyMap.get("longitude"));
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .snippet((String)keyMap.get("tel")+"\n"+(String)keyMap.get("comment"))
                                .title((String)keyMap.get("name"))).setDraggable(true);
                    }
                }
                /*for(Map.Entry<String, Object> entry : map.entrySet()){

                    Map data = (Map)entry.getValue();
                    LatLng latLng = new LatLng(Double.parseDouble((String) data.get("latitude")),Double.parseDouble((String) data.get("longitude")));
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .snippet((String)data.get("tel")+"\n"+(String)data.get("comment"))
                            .title(titleRestorant)).setDraggable(true);

                }*/
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("파이어실패", "Failed to read value.", error.toException());
            }
        });
    }


    public void markerListener(final GoogleMap googleMap){
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                List<Address> addList=null;
                try {
                    addList = geocoder.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                addressView.setText(addList.get(0).getAddressLine(0).toString());
            }
        });
    }
}
