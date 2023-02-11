package com.company.fylisten.lifecycle;


import android.util.ArraySet;

import java.util.Set;

/**
 * ListenerFragment中用来回调的接口
 */
public class ActivityFragmentLifecycle implements Lifecycle {

    //一个ListenerFragment可能被多个用户监听，
    // 而一个ListenerFragment只持有一个ActivityFragmentLifecycle
    //所以维护一个Set来做生命周期回调分发，
    // ArraySet实现，数据量一般在千级以下
    private final Set<LifecycleListener> lifecycleListeners =
//            Collections.newSetFromMap(new HashMap<LifecycleListener,Boolean>());
            new ArraySet<>();

    /**
     * 当前所在的Fragment进行到了哪里
     * 让后续进来的监听者可以立刻获知
     */
    private int state = INITIAL;
    private final static int INITIAL = -1;
    private final static int isCreated = 0;
    private final static int isStarted = 1;
    private final static int isResumed = 2;
    private final static int isPaused = 3;
    private final static int isStopped = 4;
    private final static int isDestroyed = 5;


    @Override
    public void addListener(LifecycleListener listener) {
        lifecycleListeners.add(listener);
        switch (state){
            case isCreated:
                listener.onCreate();
                break;
            case isStarted:
                listener.onStart();
                break;
            case isResumed:
                listener.onResume();
                break;
            case isPaused:
                listener.onPause();
                break;
            case isStopped:
                listener.onStop();
                break;
            case isDestroyed:
                listener.onDestroy();
                break;
            default:
                break;
        }
    }

    //------------ 回调监听 --------------

    /**
     * 监听到 onAttach()
     */
    public void onAttach() {
        state = INITIAL;
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            if (lifecycleListener instanceof FragmentLifecycleListener){
                ((FragmentLifecycleListener) lifecycleListener).onAttach();
            }
        }
    }

    public void onCreate() {
        state= isCreated;
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onCreate();
        }
    }

    public void onCreateView() {
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            if (lifecycleListener instanceof FragmentLifecycleListener){
                ((FragmentLifecycleListener) lifecycleListener).onCreateView();
            }
        }
    }

    public void onStart() {
        state= isStarted;
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onStart();
        }
    }

    public void onResume() {
        state = isResumed;
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onResume();
        }
    }

    public void onPause() {
        state = isPaused;
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onPause();
        }
    }

    public void onStop() {
        state = isStopped;
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onStop();
        }
    }

    public void onDestroy() {
        state= isDestroyed;
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onDestroy();
        }
    }

    public void onDestroyView() {
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            if (lifecycleListener instanceof FragmentLifecycleListener){
                ((FragmentLifecycleListener) lifecycleListener).onDestroyView();
            }
        }
    }

    public void onDetach() {
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            if (lifecycleListener instanceof FragmentLifecycleListener){
                ((FragmentLifecycleListener) lifecycleListener).onDetach();
            }
        }
    }


}
