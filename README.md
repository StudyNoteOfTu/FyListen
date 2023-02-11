## 快速使用：

```java
//一句话让你随心所欲监听生命周期
FyListen.listenTo(this,new LifecycleListener(){...} );
```

## 0. 依赖导入

step1: 在项目根 build.gradle 中导入

```css
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

step2: 添加依赖：

```css
dependencies {
    implementation 'com.github.StudyNoteOfTu:FyListen:Tag'
}
```

## 1. 为什么选择 FyListen？

在 Android 框架中定义的大多数应用组件都存在生命周期。生命周期由操作系统或进程中运行的框架代码管理。它们是 Android 工作原理的核心，应用必须遵循它们。如果不这样做，可能会引发内存泄漏甚至应用崩溃。

Android Jetpack 的 Lifecycle 只支持 AndroidX 项目，没有直接兼容低版本，而且还需要手动添加注解设置，也不够精简。当然，Jetpack比较完善，他甚至可以在View上进行生命周期绑定，FyListen 只是一个小工具，只能在有限的方面做出代码精简、优化。

如果手动进行生命周期的监听，需要重写 onCreate()、onDestroy()等方法，并在其中进行外部回调，可能你的代码是这样：

```java
class MyLocationListener {
    public MyLocationListener(Context context, Callback callback) {
        // ...
    }

    void start() {
        // connect to system location service
    }

    void stop() {
        // disconnect from system location service
    }
}

class MyActivity extends AppCompatActivity {
    private MyLocationListener myLocationListener;

    @Override
    public void onCreate(...) {
        myLocationListener = new MyLocationListener(this, (location) -> {
            // update UI
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        myLocationListener.start();
        //.....以及需要在onStart()中需要处理的其他业务逻辑
    }

    @Override
    public void onStop() {
        super.onStop();
        myLocationListener.stop();
        //.....以及需要在onStop()中需要处理的其他业务逻辑
    }
}
```

你会发现这样非常辛苦！你不仅需要重写 Activity 或者 Fragment 中的生命周期方法，还要把业务代码四分五裂的写在一个Activity中。看起来非常乱！而且难以维护。那么如何优雅地进行生命周期监听？

我们来看一下再加一个业务后，FyListen 如何让代码变得更加简洁、健壮：

**你可以为不同的业务专门写一个监听回调，将代码进行解耦、并处理避免内存泄漏**：

```java
//下载器,我希望在activity退出的时候，停止下载，释放对Activity的引用，解决内存泄漏
public class Downloader implements ActivityLifecycleListener{
    public void startDownload(Callback callback){
        //网络请求的耗时异步操作
        new Thread(()->{...}).start();
    }
    //Context退出，避免内存泄漏，需要终止下载
    public void cancelDownload(){}
    
    /**
     * 重写需要监听的生命周期
     */
    @Override
    public void onDestroy(){
        cancelDownload();
    }
}

//加载资源
public class ResourceLoader implements ActivityLifecycleListener{
    @Override
    public void onResume(){
        //reload something...
    }
}

public class MyActivity extends AppCompatActivity {
    Downloader downloader = new Downloader();
    ResourceLoader resourceLoader = new ResourceLoader();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //开启线程下载资源
        downloader.startDownload(new Callback(){
            @Override
            void onFinish();
            @Override
            void onError();
        });
        //为Downloader注册生命周期监听，在Activity的onDestroy时通知中断下载，释放引用
        FyListen.listenTo(this, downloader);//一旦Activity退出，downloader就被通知到，同时关闭资源下载，释放匿名内部类Callback对Activity的引用
        
        //为ResourceLoader注册生命周期监听，在Activity的onResume时通知进行资源加载
        FyListen.listenTo(this,resourceLoader);//当activity到了resume生命周期，resourceLoader就被通知，去做一些资源加载功能
    }
    
    @Override
    public void onStart() {
        super.onStart();
        //在Activity原本的生命周期回调中，你可以专注在非业务的处理！
        //,,,
    }
    
}
```

FyListen 不仅能优雅地监听 Activity 的生命周期，也能优雅地监听 Fragment 的生命周期！

**何谓优雅？一句话能解决的事就是优雅**

```java
public class MyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //一句话即可监听Activity的生命周期
        FyListen.listenTo(this, new ActivityLifecycleListener() {
            @Override
            public void onDestroy() {
                Log.e(TAG,"onDestroy");
            }
        });
    }
}
```

**一句话也能监听Fragment的生命周期：**

```java
public class MyFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //一句话即可监听Fragment的生命周期
        FyListen.listenTo(this, new FragmentLifecycleListener() {
            @Override
            public void onDetach() {
                Log.e(TAG,"onDetach");
            }
        });
    }
}
```

**你甚至可以选择监听哪些生命周期，而且代码非常简洁：**

```java
public class MyFragment extends Fragment {
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //一句话即可监听Fragment的生命周期
        FyListen.listenTo(this, new FragmentLifecycleListener() {
            @Override
            public void onPause() {
                Log.e(TAG,"onPause");
            }
            @Override
            public void onStop() {
                Log.e(TAG,"onStop");
            }
            @Override
            public void onDestroy() {
                Log.e(TAG,"onDestroy");
            }
            //...
        });
    }
}
```

## 2. 监听 Activity 的生命周期的详细使用说明：

你可以选择匿名类来回调生命周期的响应：

```java
public class MyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        FyListen.listenTo(this, new ActivityLifecycleListener() {
            @Override
            public void onDestroy() {
                Log.e(TAG,"onDestroy");

            }
        });

    }
}
```

你也可以选择使用实现了 ActivityLifecycleListener 接口的类来回调生命周期的响应：

```java
public class MyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //使用监听
        FyListen.listenTo(this,new MyListener());
    }
}

private static class MyListener implements ActivityLifecycleListener{
    @Override
    public void onDestroy() {
        //ActivityLifecycleListener.super.onDestroy();
        Log.e(TAG,"onDestroy");
    }
}
```

你也可以选择想监听的生命周期回调，例如，我只想监听 Activity 的 ``onStart()`` ,``onResume()`` 和 ``onDestroy()``:

```java
public class MyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //使用监听
        FyListen.listenTo(this,new MyListener());
    }
}

private static class MyListener implements ActivityLifecycleListener{
    @Override
    public void onStart() {
        Log.e(TAG,"onStart");
    }

    @Override
    public void onResume() {
        Log.e(TAG,"onStart");
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"onDestroy");
    }
}
```

## 3. 监听 Fragment 的生命周期

FyListen 也支持 androidx 和 Android 的 Fragment 的生命周期监听，使用方法与Activity的几乎一样。

你可以使用匿名类来监听：

```java
public class MyFragment extends Fragment {
    View view;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) view = inflater.inflate(R.layout.fragment_support,container,false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FyListen.listenTo(this, new FragmentLifecycleListener() {
            @Override
            public void onDetach() {
                Log.e(TAG,"onDetach");
            }
        });
    }
}
```

你也可以选择你希望监听的生命周期：

```java
public class MyFragment extends Fragment {
    View view;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) view = inflater.inflate(R.layout.fragment_support,container,false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FyListen.listenTo(this, new FragmentLifecycleListener() {
            @Override
            public void onPause() {
                Log.e(TAG,"onPause");
            }

            @Override
            public void onStop() {
                Log.e(TAG,"onStop");
            }

            @Override
            public void onDestroy() {
                Log.e(TAG,"onDestroy");
            }
            //...
        });
    }
}
```

## 4. 结合主流框架，解决内存泄漏问题：

你不仅可以如本文最开始提供的例子一样，对自己的下载器进行生命周期回调，从而解决内存泄漏。凡是有内存泄漏问题的地方，你都有机会用简单的语句进行处理！

### 4.1 解决 RxJava 内存泄漏：

发起一个网络请求，加载网络数据，速度很慢，如果Activity退出了，还没请求出结果，Activity由于匿名内部类持有引用，无法回收！解决办法：使用CompositeDisposable与FyListen的封装+compose()操作符。

结合 RxJava 的compose 操作符，可以非常简洁地处理内存泄漏问题，示例代码如下：

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //发起一个网络请求，加载网络数据，速度很慢，如果Activity退出了，还没请求出结果，Activity由于匿名内部类持有引用，无法回收！解决办法：使用CompositeDisposable与FyListen的封装+compose()操作符，如下：
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                //模拟请求数据
                Thread.sleep(15000);//耗时15s
                //模拟数据请求结束
                //如果Activity在期间退出了，由于仍然被引用，无法释放：发生内存泄漏！！！
                emitter.onNext("AAA");
            }
        })
                .compose(CompositeDisposableTransformer.listenToActivity(this))//直接调用了传入参数的 apply 方法！可以用于过滤/代码复用等
                .compose(new SchedulerTransformer<>())
//                .subscribeOn(Schedulers.io())//可以放到compose的transformer中
//                .observeOn(AndroidSchedulers.mainThread())//可以放到compose的transformer中
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        //数据请求成功
                        Log.i("TAG",o.toString());
                    }
                });

    }
    
    private static class CompositeDisposableTransformer<T> implements ObservableTransformer<T,T>,ActivityLifecycleListener{

        final CompositeDisposable compositeDisposable = new CompositeDisposable();

        @Override
        public void onDestroy() {
            //在Activity退出的时候回调到这里，停止各种事件！
            if (!compositeDisposable.isDisposed()){
                compositeDisposable.dispose();
            }
        }

        @Override
        public ObservableSource<T> apply(Observable<T> upstream) {
            //通过compose操作符，将上游的disposal进行注册
            return upstream.doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    compositeDisposable.add(disposable);
                }
            });
        }
		//将FyListen的监听工具封装进来。
        public static <T> CommonTransformer<T> listenToActivity(Activity activity){
            CompositeDisposableTransformer<T> transformer = new CompositeDisposableTransformer<>();
            FyListen.listenTo(activity,transformer);
            return transformer;
        }
    }
    
    //线程切换的compose封装
    private static class SchedulerTransformer<T> implements ObservableTransformer<T,T>{
        @Override
        public ObservableSource<T> apply(Observable<T> upstream) {
            return upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
    }
    
}
```

你能用 FyListen 做的代码优化还有非常多，Java 的特性之一是单继承，但 FyListen 和 Lifecycle 一样，选择了使用接口进行实现，让代码的扩展性加强。简单的使用方法，可以让程序员更快上手，处理内存泄漏！
