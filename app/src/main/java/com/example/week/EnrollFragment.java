package com.example.week;


import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

/**
 * Created by 현기 on 2017-07-19.
 */

public class EnrollFragment extends Fragment {

    TextView textViewContentsNum;
    EditText editTextComments;
    CharSequence maxCharSequence;
    Button nextButton;
    EditText editTextRestorantAddress;
    EditText editTextRestorantName;
    EditText editTextRestorantTel;

    //지오코더
    Geocoder geocoder;

    //파이어베이스
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("RestorantINFO");

    //넘길 위도, 경도
    double latitude;
    double longitude;

    Bundle bundle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.enroll_fragment, container, false);

        textViewContentsNum = (TextView) view.findViewById(R.id.textView_contents_num);
        editTextComments = (EditText)view.findViewById(R.id.editText_restorant_comments);
        nextButton = (Button)view.findViewById(R.id.nextButton);
        editTextRestorantAddress = (EditText)view.findViewById(R.id.editText_restorant_address);
        editTextRestorantName = (EditText)view.findViewById(R.id.editText_restorant_name);
        editTextRestorantTel = (EditText)view.findViewById(R.id.editText_restorant_tel);

        geocoder = new Geocoder(getContext());

        getRestorantAddress();   // 주소창 완료 입력시 전화번호 및 위도 경도가져오기, 지오코딩
        commentListener(view);  // 맛집 정보 내용 입력 리스너
        moveButton();  //다음 버튼 이동


        return view;
    }

    // 주소창 완료 입력시 전화번호 및 위도 경도가져오기, 지오코딩
    public void getRestorantAddress(){
        editTextRestorantAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    Log.i("주소입력","주소");
                    getAddress();   // 주소를 통해 전화번호, 위도, 경도 가져오기
                }

                return false;
            }
        });
    }
    public void getAddress(){
        List<Address> list = null;
        String addressStr = editTextRestorantAddress.getText().toString();
        try{
            list = geocoder.getFromLocationName(addressStr,10);
        }catch (IOException e){
            e.printStackTrace();
            Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
        }

        if(list != null){
            if(list.size() == 0){
                Toast.makeText(getActivity(),"해당주소는 없습니다.", Toast.LENGTH_LONG).show();
            }else{
                editTextRestorantTel.setText(list.get(0).getPhone());       //전화번호 가져오기
                latitude = list.get(0).getLatitude();        //위도
                longitude = list.get(0).getLongitude();     //경도 가져오기
                Toast.makeText(getActivity(),"주소등록", Toast.LENGTH_LONG).show();
            }
        }

    }


    // 다음으로 이동
    public void moveButton(){
        //다음 버튼 이동
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(bundle == null){
                   bundle = new Bundle();
                }
                bundle.putDouble("latitude",latitude);
                bundle.putDouble("longitude", longitude);

                RestorantVO vo = dataSetRestorantVO();
                myRef.child(vo.getName()).setValue(vo);

                ((FragmentReplaceable)getActivity()).replaceFragment(2, bundle);
            }
        });
    }

    public RestorantVO dataSetRestorantVO(){
        RestorantVO vo = new RestorantVO();
        vo.setAddress(editTextRestorantAddress.getText().toString());
        vo.setComment(editTextComments.getText().toString());
        vo.setLatitude(latitude);
        vo.setLongitude(longitude);
        vo.setTel(editTextRestorantTel.getText().toString());
        vo.setName(editTextRestorantName.getText().toString());

        return vo;
    }



    //맛집정보 글자 수 제한
    public void commentListener(View view){
        // 맛집 정보 내용 리스너
        editTextComments.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //글자수 제한
                if(s.length()<=500) {
                    textViewContentsNum.setText(s.length()+"/500");
                    if(s.length()==500){
                        maxCharSequence = s;
                    }
                }else{
                    textViewContentsNum.setText("500/500");
                    editTextComments.setText(maxCharSequence);
                }

            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }


}
