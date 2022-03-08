package org.devio.proj.nav_compiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;

import org.devio.proj.nav_annotation.Destination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * 自定义注解解释器
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"org.devio.proj.nav_annotation.Destination"})
public class NavProcessor extends AbstractProcessor {
    private static final String PAGE_TYPE_ACTIVITY = "Activity";
    private static final String PAGE_TYPE_FRAGMENT = "Fragment";
    private static final String PAGE_TYPE_DIALOG = "Dialog";
    private static final String OUTPUT_FILE_NAME = "destination.json";

    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        //日志打印工具类
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "enter init...");

        //创建打印文件
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //获取代码中所有使用@Destination 注解的类或字段
        //
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Destination.class);
        if (!elements.isEmpty()) {

            //key=pageUrl value 对应节点的配置 最后将map转成Json 写入Assets目录下
            HashMap<String, JSONObject> destMap = new HashMap<>();

            //处理解析 将mobile_navigation.xml 所有节点 Destination 通过代码 写入Json文件
            /**
             * e.g
             *    <fragment
             *         android:id="@+id/navigation_home"
             *         android:name="org.devio.proj.navigatorrouter.ui.home.HomeFragment"
             *         android:label="@string/title_home"
             *         tools:layout="@layout/fragment_home" />
             *
             */
            handleDestination(elements, destMap);

            //封装好节点信息 开始写入Json文件

            try {
                //创建资源文件
                FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
                // 获取创建资源文件默认路径： .../app/build/intermediates/javac/debug/classes/目录下
                // 希望存放的目录为： /app/main/assets/
                String resourcePath = resource.toUri().getPath();
                //  获取 .../app 之前的路径
                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);

                String assetsPath = appPath + "src/main/assets";

                File file = new File(assetsPath);

                if (!file.exists()) {
                    file.mkdirs();
                }

                String content = JSON.toJSONString(destMap);
                File outputFile = new File(assetsPath, OUTPUT_FILE_NAME);

                if (outputFile.exists()) {
                    outputFile.delete();
                }

                outputFile.createNewFile();


                FileOutputStream outputStream = new FileOutputStream(outputFile);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);

                writer.write(content);
                writer.flush();

                outputStream.close();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void handleDestination(Set<? extends Element> elements,
                                   HashMap<String, JSONObject> destMap) {

        for (Element element : elements) {
            /**
             * 在这里我们有必要认识一下什么是Element**。 在Java语言中，Element是一个接口，表示一个程序元素，
             * 它可以指代包、类、方法或者一个变量。Element已知的子接口有如下几种：
             *
             *     - PackageElement 表示一个包程序元素。提供对有关包及其成员的信息的访问。
             *     - ExecutableElement 表示某个类或接口的方法、构造方法或初始化程序（静态或实例），包括注释类型元素。
             *     - TypeElement 表示一个类或接口程序元素。提供对有关类型及其成员的信息的访问。注意，枚举类型是一种类，而注解类型是一种接口。
             *     - VariableElement 表示一个字段、enum 常量、方法或构造方法参数、局部变量或异常参数。
             */
            //由此可知 注解只能在类上，由此可知element为TypeElement
            TypeElement typeElement = (TypeElement) element;

            //完全限定全类名 e.g: org.devio.proj.navigatorrouter.ui.home.HomeFragment
            String clzName = typeElement.getQualifiedName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "clzName ...... " + clzName);
            //获取使用在类上的注解
            Destination annotation = typeElement.getAnnotation(Destination.class);
            //取出使用注解时传入的参数
            String pageUrl = annotation.pageUrl();
            boolean asStarter = annotation.asStarter();
            //自定义创建节点Id
            int id = Math.abs(clzName.hashCode());

            //获取注解标记的类型(fragment activity dialog) 对应之前Navigator.NAME 注解字段
            String destType = getDestinationType(typeElement);


            if (destMap.containsKey(pageUrl)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "different page must be different pageUrl:" + pageUrl);
            } else {
                //创建Destination 节点数据
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("clzName", clzName);
                jsonObject.put("asStarter", asStarter);
                jsonObject.put("destType", destType);
                jsonObject.put("id", id);
                jsonObject.put("pageUrl", pageUrl);

                destMap.put(pageUrl, jsonObject);
            }

        }
    }

    /**
     * 根据typeElement类 类型程序元素 获取注解标记的类型(Fragment Activity Dialog)
     */
    private String getDestinationType(TypeElement typeElement) {

        //TypeMirror 表示Java编程语言中的类型。 类型包括基本类型，声明的类型（类和接口类型），数组类型，类型变量和null类型。
        TypeMirror typeMirror = typeElement.getSuperclass();
        //e.g: androidx.fragment.app.Fragment
        String superClzName = typeMirror.toString();
        messager.printMessage(Diagnostic.Kind.NOTE, "superClzName ...... " + superClzName);

        if (superClzName.contains(PAGE_TYPE_ACTIVITY)) {
            return PAGE_TYPE_ACTIVITY.toLowerCase();
        } else if (superClzName.contains(PAGE_TYPE_FRAGMENT)) {
            return PAGE_TYPE_FRAGMENT.toLowerCase();
        } else if (superClzName.contains(PAGE_TYPE_DIALOG)) {
            return PAGE_TYPE_DIALOG.toLowerCase();
        }

        //以上都不匹配 如果父类型是类的类型，或是接口的类型
        if (typeMirror instanceof DeclaredType) {
            Element element = ((DeclaredType) typeMirror).asElement();
            //如果这个父类的类型 是类或接口的类型
            if (element instanceof TypeElement) {
                //递归调用自己
                return getDestinationType((TypeElement) element);
            }
        }

        return null;
    }

}
