package com.company.fylisten.listener;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.company.fylisten.lifecycle.ActivityFragmentLifecycle;


public class SupportListenerFragment extends Fragment {

    /**
     * 生命周期回调
     */
    private final ActivityFragmentLifecycle lifecycle;

    private ListenerFragment rootListenerFragment;//监听Activity的ListenerFragment
    private Fragment parentFragmentHint;//如果有父亲Fragment的话

    public SupportListenerFragment(ActivityFragmentLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public SupportListenerFragment() {
        this(new ActivityFragmentLifecycle());
    }

    /**
     * 交给上层，进行 listener 注入
     */
    public ActivityFragmentLifecycle getActivityFragmentLifecycle() {
        return lifecycle;
    }

    public void setParentFragmentHint(Fragment parentFragmentHint) {
        this.parentFragmentHint = parentFragmentHint;
    }

    //---------- 生命周期监听 --------------
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        lifecycle.onAttach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycle.onCreate();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        lifecycle.onCreateView();
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        lifecycle.onStart();
        Log.e("TAG","listenerFragment: onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        lifecycle.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        lifecycle.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        lifecycle.onStop();
        Log.e("TAG","listenerFragment: onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycle.onDestroy();
        Log.e("TAG","listenerFragment: onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lifecycle.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentFragmentHint = null;
        lifecycle.onDetach();
        Log.e("TAG","listenerFragment: onDetach");
    }
}
