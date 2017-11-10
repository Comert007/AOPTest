# 在android中使用AOP
> 在上一篇 
  [使用自定义注解实现MVP中Model和View的注入](https://insight.io/github.com/sourfeng/MvpAnnotation/tree/master/)
  中，使用了自定义的方式进行依赖注入这一篇我们将继续对注解进行深入了解。在日常的开发过程中，我们经常会在同一个地方使用到相同的代码，以往我们的处理方式是可以将其进行一个封装，然后在
不同的地方进行调用这样确实也很方便，但是还有另外的方式，就是自定义注解实现AOP。

需求：在开发过程中有很多页面需要判断登录，实现这样一个功能，能够在不同需要实现的地方进行登录的校验！

## AOP

`AOP`是`Aspect Oriented Program`的首字母缩写AOP，其意是面向切面编程），其实很多前端的开发可能都没有听说过这个，但是对于
后端的小伙伴来说这个是在是太熟悉了，因为很多时候他们就靠这个来进行`Log`的打印。

那么`AOP`到底是什么呢?  
### AOP定义

**先看定义：运行时，动态地将代码切入到类的指定方法、指定位置上的编程思想**

在解释`AOP`之前，首先得说说和面向切面编程相对的另一个编程思想：面向对象编程（`OOP`。在面向对象的思想中，我们以“一切皆对象”为原则，为不同的对象赋予不同的
功能，在需要使用到的时候，我们就对实例化对象，然后调用其功能，这样降低了代码的复杂度，使类可重用。

但是在使用的过程中，会出现这么一种情况，类A和类B，都需要进行实现一个功能（比如：是否登录的判断），以往我们的做法很简单，
将这个登录判断的功能写在一个类中（这里命名为C），然后在各自的引用的地方调用这个类的方法，确实这样是解决了这个问题，但是
这样却使A,B 两个类与C类之间就会有耦合。有没有什么办法，能让我们在需要的时候，随意地加入代码呢？
为了解决这样的问题就出现了面向切面编程的思想，即是：这种在运行时，动态地将代码切入到类的指定方法、指定位置上的编程思想就是面向切面的编程

### AOP和OOP之间的关系

AOP的实际操作是将几个类之间共有的功能单独出来，然后在这几个需要的时候进行切入，改变其本来的运行方式。这样分析下来，我们可以
得出一个结论，即是：面向切面编程(`AOP`)其实是面向对象编程（`OOP`）的一个补充。



## 加入AspectJ

AspectJ AspectJ实际上是对AOP编程思想的一个实现。

* 在项目的gradle文件下加入：  
 
    ```    
     dependencies {
             classpath 'com.android.tools.build:gradle:3.0.0'
             classpath 'org.aspectj:aspectjtools:1.8.9'
             classpath 'org.aspectj:aspectjweaver:1.8.9'

             // NOTE: Do not place your application dependencies here; they belong
             // in the individual module build.gradle files
         }
    ```
 
 * 在app的gradle文件下加入：
 
    1. 引入aspectjtools
 
         ```java
         import org.aspectj.bridge.IMessage
         import org.aspectj.bridge.MessageHandler
         import org.aspectj.tools.ajc.Main
         ```
 
    2. 导入第三方包  

         ```java
         compile 'org.aspectj:aspectjrt:1.8.9'
         ```
 
 3. 使用AspectJ编译器ajc
 
 使用ajc会对所有受 aspect 影响的类进行织入，这样才能使我们的Aspect
 
 ```java   
 //获取 log实例
 final def log = project.logger
 //获取variants
 final def variants = project.android.applicationVariants
 variants.all { variant ->
     if (!variant.buildType.isDebuggable()) {
         log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
         return;
     }
 
     //编译时做如下处理
     JavaCompile javaCompile = variant.javaCompile
     javaCompile.doLast {
         String[] args = ["-showWeaveInfo",
                          "-1.8",
                          "-inpath", javaCompile.destinationDir.toString(),
                          "-aspectpath", javaCompile.classpath.asPath,
                          "-d", javaCompile.destinationDir.toString(),
                          "-classpath", javaCompile.classpath.asPath,
                          "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
         log.debug "ajc args: " + Arrays.toString(args)
 
         MessageHandler handler = new MessageHandler(true);
         new Main().run(args, handler);
         for (IMessage message : handler.getMessages(null, true)) {
             switch (message.getKind()) {
                 case IMessage.ABORT:
                 case IMessage.ERROR:
                 case IMessage.FAIL:
                     log.error message.message, message.thrown
                     break;
                 case IMessage.WARNING:
                     log.warn message.message, message.thrown
                     break;
                 case IMessage.INFO:
                     log.info message.message, message.thrown
                     break;
                 case IMessage.DEBUG:
                     log.debug message.message, message.thrown
                     break;
             }
         }
     }
 }      
 ```
 
 至此，我们就将AspectJ的准备工作做好了，那么接下来就是使用了

 ## 在Android中使用AOP

 先来介绍几个概念：

 * `Pointcut`:切入点，就是在程序运行过程中，在**何处**注入我们想运行的特定代码。
    注意：这里的**何处**，并不是真正意义上的具体位置，而是可切入的范围，比如整个包下面所有类及所有方法，或者某个类下面的所有方法。
 * `Joint point`:连接点，程序中可能作为代码注入目标的特定的点，所以此处才是执行注入的具体的位置。
 * `Advice`: 通知，即是在程序运行过程中，当执行到切点位置时，执行注入到class文件中**什么**样的代码，
 比较常用的类型是`before`，`around`，`after`。从字面上面我们就可以看出其意，
 就是在目标方法执行之前，执行之时替代目标方法，执行之后的代码。
 * `Aspect`: 切面，其实就是`Pointcut`和`Advice`的组合，所以如上可以总结为**在何处做什么**。

 ### 创建`CheckLogin`注解

 可能有人会问：为什么是创建注解呢？不能是其的什么类或者对象么？
 `AOP`本来就是为了解决耦合才进行使用的，如果使用其他的，或让AspectJ与其耦合，那我们使用`AOP`干什么呢？

```java

@Retention(RetentionPolicy.RUNTIME) //保留到源码中，同时也保留到class中，最后加载到虚拟机中
@Target({ElementType.METHOD,ElementType.CONSTRUCTOR}) //可以注解在方法或构造上
public @interface CheckLogin {
}

```

在上次的讲解中已经提到元注解`@Retention`,表示注解的表示方式，这里再回顾一下：

* SOURCE：只保留在源码中，不保留在class中，同时也不加载到虚拟机中
* CLASS：保留在源码中，同时也保留到class中，但是不加载到虚拟机中
* RUNTIME：保留到源码中，同时也保留到class中，最后加载到虚拟机中

`@Target` 这个注解表示注解的作用范围，主要有如下:

* ElementType.FIELD 注解作用于变量
* ElementType.METHOD 注解作用于方法
* ElementType.PARAMETER 注解作用于参数
* ElementType.CONSTRUCTOR 注解作用于构造方法
* ElementType.LOCAL_VARIABLE 注解作用于局部变量
* ElementType.PACKAGE 注解作用于包

所以如上的`CheckLogin`表示将注解可以注入到构造方法和其他方法上，并且保留到源码中，同时也保留到class中，最后加载到虚拟机中。

### 创建Aspect类

到此，才是我们这章的重点，就是怎么构建一个`Aspect`类,这里以`CheckLoginAspectJ`为例。

```java
@Aspect
public class CheckLoginAspectJ {
    private static final String TAG = "CheckLogin";

    /**
     * 找到处理的切点
     * * *(..)  可以处理CheckLogin这个类所有的方法
     */
    @Pointcut("execution(@com.yw.android.aoptest.aop.CheckLogin  * *(..))")
    public void executionCheckLogin() {

    }

    /**
     * 处理切面
     *
     * @param joinPoint
     * @return
     */
    @Around("executionCheckLogin()")
    public Object checkLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.i(TAG, "checkLogin: ");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CheckLogin checkLogin = signature.getMethod().getAnnotation(CheckLogin.class);
        if (checkLogin != null) {
            Context context = (Context) joinPoint.getThis();
            if (BaseApplication.isLogin) {
                Log.i(TAG, "checkLogin: 登录成功 ");
                return joinPoint.proceed();
            } else {
                Log.i(TAG, "checkLogin: 请登录");
                Toast.makeText(context, "请登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                return null;
            }
        }
        return joinPoint.proceed();
    }

```

### @Pointcut说明

在上方代码`Pointcut`之后紧跟了一个`execution`的表达式，这个就代表切入点的位置，也就是我们上述的**何处**

解释一下`execution`的用法：

`execution`仅仅是AOP中pointcut expression表达式中的一种。其他还有如下这几种：

* args()：用于匹配当前执行的方法传入的参数为指定类型的执行方法
* @args()：用于匹配当前执行的方法传入的参数持有指定注解的执行
* execution()：用于匹配方法执行的连接点
* this()：用于匹配当前AOP代理对象类型的执行方法；注意是AOP代理对象的类型匹配，这样就可能包括引入接口也类型匹配
* target()：用于匹配当前目标对象类型的执行方法；注意是目标对象的类型匹配，这样就不包括引入接口也类型匹配
* @target()：用于匹配当前目标对象类型的执行方法，其中目标对象持有指定的注解；
* within()：用于匹配指定类型内的方法执行
* @within()：用于匹配所有持有指定注解类型内的方法；
* @annotation：用于匹配当前执行方法持有指定注解的方法

这里重点解释一下`execution`,因为在我们的日常使用中，`execution`是最多的。

#### 类型匹配语法

* `*`：匹配任何数量字符,即是全部；
* `..`：匹配任何数量字符的重复，如在类型模式中匹配任何数量子包；而在方法参数模式中匹配任何数量参数。
* `+`：匹配指定类型的子类型；仅能作为后缀放在类型模式后边。
* `()`:表示方法没有任何参数
* `(..)`:表示匹配接受任意个参数的方法

```
//匹配String类型
java.lang.String
//匹配java包下任何子包的String类型
java.*.String
//匹配java包及任何子包下的任何类型
java..*

```


#### execution表达式

execution的表达式如下：
**execution(modifiers-pattern? ret-type-pattern declaring-type-pattern? name-pattern(param-pattern)throws-pattern?)**

* modifiers-pattern：修饰符匹配，如`public`、`private`、`protect`，可选。
* ret-type-pattern：返回类型匹配，必填。
* declaring-type-pattern：声明类型匹配，可选。
* name-pattern(param-pattern)：
    * name-pattern:方法名匹配，必填
    * param-pattern：方法参数匹配，必填
* throws-pattern：异常匹配，可选。

至此，我们可以知道，上述中代码代表的匹配意思了

```
"execution(@com.yw.android.aoptest.aop.CheckLogin  * *(..))"
```
返回类型:`com.yw.android.aoptest.aop.CheckLogin`;
声明类型: * ,表示任何
方法名： `*`,任何方法
参数：`(..)`,任意个参数

即是：匹配`com.yw.android.aoptest.aop.CheckLogin`类下的所有声明和所以任意参数方法。

### @Advice说明

```
@Around("executionCheckLogin()")
    public Object checkLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        ...
    }
```

在上述代码中我们使用的是`@Around`,这个也是很常用的。

`@Around("executionCheckLogin()")`将切面表达式与通知进行绑定，使用我们的代码注入在使用`@CheckLogin`的地方生效
,其中参数是上面切面的方法名。

而在方法中参数就是`JoinPoint`,常用的也就是这个`ProceedingJoinPoint`。

#### JoinPoint

```java
public interface JoinPoint {
    String toString();         //连接点所在位置的相关信息
    String toShortString();     //连接点所在位置的简短相关信息
    String toLongString();     //连接点所在位置的全部相关信息
    Object getThis();         //返回AOP代理对象
    Object getTarget();       //返回目标对象
    Object[] getArgs();       //返回被通知方法参数列表
    Signature getSignature();  //返回当前连接点签名
    SourceLocation getSourceLocation();//返回连接点方法所在类文件中的位置
    String getKind();        //连接点类型
    StaticPart getStaticPart(); //返回连接点静态部分
}

```

#### ProceedingJoinPoint

`ProceedingJoinPoint `继承了`JoinPoint`

```
public interface ProceedingJoinPoint extends JoinPoint {
    public Object proceed() throws Throwable;
    public Object proceed(Object[] args) throws Throwable;
}
```
使用proceed()方法来执行目标方法,即是被`@CheckLogin`注解的方法,我们再来看看我们的方法

```java
@Around("executionCheckLogin()")
    public Object checkLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.i(TAG, "checkLogin: ");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CheckLogin checkLogin = signature.getMethod().getAnnotation(CheckLogin.class);
        if (checkLogin != null) {
            Context context = (Context) joinPoint.getThis();
            if (BaseApplication.isLogin) {
                Log.i(TAG, "checkLogin: 登录成功 ");
                return joinPoint.proceed();
            } else {
                Log.i(TAG, "checkLogin: 请登录");
                Toast.makeText(context, "请登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                return null;
            }
        }
        return joinPoint.proceed();
    }

```

1. 先获取一个方法前面对象`MethodSignature`，这个对象有两个方法：

```java
public interface MethodSignature extends CodeSignature {
    Class getReturnType();      /* name is consistent with reflection API */
	Method getMethod();
}

```

一个是获取目标方法的返回类型，一个是目标方法的Methond对象。
然后通过：
```
signature.getMethod().getAnnotation(CheckLogin.class);
```
就可以获取目标方法的注解，如果注解实例不为空，说明加了`CheckLogin`注解。

```
Context context = (Context) joinPoint.getThis();
```
通过上述方法，可以获取目标方法所在类的对象，但是这里强转成了Context，也就是说，改注解只能在有上下文的类里使用。
然后通过登录的标志进行判断，是让目标方法继续执行，还是跳转至登录。


## 简单测试

```java
private Button btnAop;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    btnAop = (Button) findViewById(R.id.btn_aop);
    btnAop.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
             onAop();
        }
    });
}

@CheckLogin
public void onAop(){
    Log.d("tag","执行方法参数");
}
```

1. 设置登录标志为未登录：

```
I/CheckLogin: checkLogin:
I/CheckLogin: checkLogin: 请登录
```
检测出未登录，跳转到了登录界面

2. 设置登录标志为已登录：

```
I/CheckLogin: checkLogin:
I/CheckLogin: checkLogin: 登录成功
D/tag: 执行方法参数
```

检测出已登录，执行目标方法。

## 总结

`AOP`的使用不光在检测登录，还有其他的一些用处：

* 打印日志，在需要打印日志的地方加上这样的方式，就可以打印日志，是不是比写一个打印方法简单多了
* 缓存，假设目标方法是个数据请求，那么是不是可以在目标方法执行之后，进行缓存
* 数据校验，我们的代码中很多地方都会去校验数据，那么自定义一个AOP，然后传入你需要注解的对象进行校验。

这样的方式应该还有很多，只是现在还没有用到，希望大家可以多多提出自己的想法。
