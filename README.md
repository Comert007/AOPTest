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

## 在Android中使用AOP

### 加入AspectJ

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
 
 ```
 import org.aspectj.bridge.IMessage
 import org.aspectj.bridge.MessageHandler
 import org.aspectj.tools.ajc.Main
 ```
 
 2. 导入第三方包
 ```
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
 
 