package com.company.fylisten.lifecycle;

public interface ActivityLifecycleListener extends LifecycleListener{

    default void onCreate(){}
    default void onStart(){}
    default void onResume(){}
    default void onPause(){}
    default void onStop(){}
    default void onDestroy(){}
}
