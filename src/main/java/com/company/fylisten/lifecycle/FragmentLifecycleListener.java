package com.company.fylisten.lifecycle;

public interface FragmentLifecycleListener extends LifecycleListener{

    default void onAttach(){}
    default void onCreate(){}
    default void onCreateView(){}
    default void onStart(){}
    default void onResume(){}
    default void onPause(){}
    default void onStop(){}
    default void onDestroy(){}
    default void onDestroyView(){}
    default void onDetach(){}

}
