package com.example.week;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by 현기 on 2017-07-19.
 */

public interface FragmentReplaceable extends Serializable {

    public void replaceFragment(int fragmentId, Bundle bundle);
}
