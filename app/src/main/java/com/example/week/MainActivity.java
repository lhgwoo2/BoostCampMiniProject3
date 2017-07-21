package com.example.week;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements FragmentReplaceable {

    EnrollFragment firstFragment;
    MapFragment mapFragment;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstFragment = new EnrollFragment();
        mapFragment = new MapFragment();
        setDefaultFragment();

    }

    // 프래그먼트 전환
    @Override
    public void replaceFragment(int fragmentId, Bundle bundle) {
        /**
         *  R.id.main_container(activity_main.xml)에 띄우겠다.
         * 파라미터로 오는 fragmentId에 따라 다음에 보여질 Fragment를 설정한다.
         **/
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (fragmentId == 1) {
            fragmentTransaction.replace(R.id.main_container, firstFragment);
        } else if (fragmentId == 2) {

            if(mapFragment.getArguments() == null){
                mapFragment.setArguments(bundle);
            }else{
                mapFragment.getArguments().putAll(bundle);
            }
            fragmentTransaction.replace(R.id.main_container, mapFragment);
        }

        // 모바일에서 백 버튼 클릭 시 이전화면으로 이동
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    //프래그먼트 처음 설정
    public void setDefaultFragment() {

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.main_container, firstFragment);

        fragmentTransaction.commit();
    }
}
