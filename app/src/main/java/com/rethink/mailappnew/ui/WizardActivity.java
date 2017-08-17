package com.rethink.mailappnew.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.github.fcannizzaro.materialstepper.style.DotStepper;
import com.rethink.mailappnew.ui.fragments.StepSample;
/**
 * Created by Shibin.co on 12/06/17.
 */


public class WizardActivity extends DotStepper {

    private static final String TAG = "DotSample";
    private int i = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setErrorTimeout(1500);
        setTitle("Set Your Account");
        addStep(createFragment(new StepSample()));
        addStep(createFragment(new StepSample()));
        addStep(createFragment(new StepSample()));
        addStep(createFragment(new StepSample()));
        addStep(createFragment(new StepSample()));
        super.onCreate(savedInstanceState);
    }

    private AbstractStep createFragment(AbstractStep fragment) {

        showLog("Create Fragment");

        Bundle b = new Bundle();
        b.putInt("position", i++);
        fragment.setArguments(b);
        return fragment;
    }
    private static void showLog(String message) {
        Log.d(TAG, message);
    }
}

