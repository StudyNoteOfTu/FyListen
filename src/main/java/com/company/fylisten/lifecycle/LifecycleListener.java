package com.company.fylisten.lifecycle;

/**
 * 用户回调，将被监听对象的生命周期回调给用户
 */
public interface LifecycleListener {
    //最基础的监听回调
    default void onCreate(){}
    default void onStart(){}
    default void onResume(){}
    default void onPause(){}
    default void onStop(){}
    default void onDestroy(){}


}
