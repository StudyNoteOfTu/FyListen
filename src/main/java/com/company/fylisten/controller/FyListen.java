package com.company.fylisten.controller;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;


import com.company.fylisten.lifecycle.ActivityLifecycleListener;
import com.company.fylisten.lifecycle.FragmentLifecycleListener;
import com.company.fylisten.lifecycle.LifecycleListener;
import com.company.fylisten.listener.ListenerFragment;
import com.company.fylisten.listener.SupportListenerFragment;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FyListen implements Handler.Callback{

    private static final String FRAGMENT_TAG = "person.tufengyi.manager";
    private static final int ID_REMOVE_FRAGMENT_MANAGER = 1;
    private static final int ID_REMOVE_SUPPORT_FRAGMENT_MANAGER = 2;
    /**
     * 对fm进行预处理的临时map(由于删除临时fm的工作并不着急，所以交给了handler，让出工作时间)
     */
    private final Map<FragmentManager, SupportListenerFragment> pendingSupportListenerFragment = new HashMap<>();

    private final Map<android.app.FragmentManager, ListenerFragment> pendingListenerFragment = new HashMap<>();


    private final Handler handler;

    private FyListen() {
        //自己处理主线程发来的事件
        // （因为这件事不着急，所以提交给messagequeue，别人的事做完在做我的）
        handler = new Handler(Looper.getMainLooper(),this);
    }
    //全局单例
    private static volatile FyListen instance;
    private static FyListen getInstance(){
        if (instance==null){
            synchronized (FyListen.class){
                if (instance==null){
                    instance = new FyListen();
                }
            }
        }
        return instance;
    }
    public static void listenTo(@NonNull Context context, ActivityLifecycleListener listener){
        getInstance().listenTo1(context,listener);
    }
    public static void listenTo(@NonNull Fragment fragment, FragmentLifecycleListener listener){
        getInstance().listenTo1(fragment,listener);
    }
    public static void listenTo(@NonNull android.app.Fragment fragment,FragmentLifecycleListener listener){
        getInstance().listenTo1(fragment,listener);
    }

    /**
     * 由于版本要求，Androidx会用到FragmentActivity来获取SupportFragmentManager
     */
    private void listenTo1(@NonNull Context context, LifecycleListener listener){
        if (context instanceof FragmentActivity){
            doWithFragmentActivity((FragmentActivity)context,listener);
        }else if (context instanceof Activity){
            doWithActivity((Activity)context,listener);
        }
    }

    /**
     * 监听androidx的fragment
     */
    private void listenTo1(@NonNull Fragment fragment, LifecycleListener listener){
        doWithSupportFragment(fragment,listener);
    }

    /**
     * 监听android的fragment
     */
    private void listenTo1(@NonNull android.app.Fragment fragment,LifecycleListener listener){
        doWithFragment(fragment,listener);
    }


    private void doWithFragment(android.app.Fragment fragment, LifecycleListener listener){
//        if (!checkFragmentAttached(fragment))
//            throw new RuntimeException("不允许在Fragment还没attach的时候开始监听，建议在onAttach()之后添加监听");
        android.app.FragmentManager fm = fragment.getChildFragmentManager();
        delNormal(fm,listener,fragment);
    }

    private void doWithActivity(Activity activity, LifecycleListener listener){
        android.app.FragmentManager fm = activity.getFragmentManager();
        delNormal(fm,listener,null);
    }

    private void doWithSupportFragment(Fragment fragment,LifecycleListener listener){
        //如果fragment还没attach，是没被赋值的
        //反射获取其state，至少应该是CREATED才可以
//        if (!checkFragmentAttached(fragment))
//            throw new RuntimeException("不允许在Fragment还没attach的时候开始监听，建议在onAttach()之后添加监听");
        FragmentManager fm = fragment.getChildFragmentManager();
        delSupport(fm,listener,fragment);
    }

    private boolean checkFragmentAttached(Object f) {
//        Class<?> fragmentClass = f.getClass();
//        Log.e("TAG",fragmentClass.getName());
        try {
//            if (!Class.forName("android.app.Fragment").isAssignableFrom(fragmentClass)
//                    &&!Class.forName("androidx.fragment.app.Fragment").isAssignableFrom(fragmentClass)){
//                //如果不是fragment，就报错
//                throw new IllegalArgumentException("checkFragmentAttached() 只能传入android下和androidx下的Fragment类");
//            }
            if (!(f instanceof Fragment) && !(f instanceof android.app.Fragment)){
                //如果不是fragment，就报错
                throw new IllegalArgumentException("checkFragmentAttached() 只能传入android下和androidx下的Fragment类");
            }
            //如果attach了，那么mHost就不为空
            Class<?> c = f.getClass();
            Field mState = c.getDeclaredField("mHost");
            mState.setAccessible(true);
            //只有attached之后才可以
            return null!=(mState.get(f));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void doWithFragmentActivity(FragmentActivity fragmentActivity,LifecycleListener listener){
        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
        delSupport(fm,listener,null);
    }

    private void delNormal(android.app.FragmentManager fm, LifecycleListener listener, android.app.Fragment parent){
        ListenerFragment current = (ListenerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current==null){
            current = pendingListenerFragment.get(fm);
            if (current==null){
                current= new ListenerFragment();
                current.setParentFragmentHint(parent);
                pendingListenerFragment.put(fm,current);
                fm.beginTransaction().add(current,FRAGMENT_TAG).commitAllowingStateLoss();
                handler.obtainMessage(ID_REMOVE_FRAGMENT_MANAGER,fm).sendToTarget();
            }
        }
        if (current!=null){
            current.getActivityFragmentLifecycle().addListener(listener);
        }
    }

    private void delSupport(FragmentManager fm,LifecycleListener listener,Fragment parent){
        SupportListenerFragment current = (SupportListenerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current==null){
            //如果之前没有添加过：
            //如果同时有人也尝试注册生命周期，后者将会拿到前者的ListenerFragment
            // 避免注册的ListenerFragment被覆盖而失败
            current = pendingSupportListenerFragment.get(fm);
            if (current==null){
                current = new SupportListenerFragment();
                current.setParentFragmentHint(parent);
                //记录在临时map
                pendingSupportListenerFragment.put(fm,current);
                //加到fm中
                fm.beginTransaction().add(current,FRAGMENT_TAG).commitAllowingStateLoss();
                //不着急清除，交给handler延时处理
                handler.obtainMessage(ID_REMOVE_SUPPORT_FRAGMENT_MANAGER,fm).sendToTarget();
            }
        }
        //注册监听器
        if (current!=null){
            current.getActivityFragmentLifecycle().addListener(listener);
        }
    }



    @Override
    public boolean handleMessage(@NonNull Message msg) {
        boolean handled = true;
        Object removed = null;
        switch (msg.what){
            case ID_REMOVE_FRAGMENT_MANAGER:

                break;
            case ID_REMOVE_SUPPORT_FRAGMENT_MANAGER:
                FragmentManager supportFm = (FragmentManager) msg.obj;
                removed = pendingSupportListenerFragment.remove(supportFm);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }
}
