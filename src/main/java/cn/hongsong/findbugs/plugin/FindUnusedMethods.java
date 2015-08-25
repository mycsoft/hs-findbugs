/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.MethodAnnotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.bcel.classfile.Method;

/**
 * 检查所有未使用到的方法. 包含公共方法.
 *
 * @author MaYichao
 */
public class FindUnusedMethods extends AbstractFindbugsPlugin {

//    private static final List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
//    private static final Map<String, MethodInfo> methodInfoList = new HashMap<String, MethodInfo>();
    /**
     * 以类名为关键字,按类进行信息存储.
     */
    private static final Map<String, Map<String, MethodInfo>> classInfoList = new HashMap<String, Map<String, MethodInfo>>();

    public FindUnusedMethods(BugReporter br) {
        super(br);
    }

    @Override
    public void sawOpcode(int seen) {
        switch (seen) {
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
//            if (getDottedClassConstantOperand().equals(className)) {

                //记录方法为已调用.
                String className = getDottedClassConstantOperand();
                MethodAnnotation called = new MethodAnnotation(className, getNameConstantOperand(), getSigConstantOperand(),
                        seen == INVOKESTATIC);
                MethodInfo mi = new MethodInfo();
//                mi.method = getMethod();
                mi.method = called;
                mi.className = className;
                mi.methodName = getNameConstantOperand() + getSigConstantOperand();
                mi.hasUsed = true;
                String key = mi.getMethodFullName();
                Map<String, MethodInfo> methodInfoList = getClassInfo(className);
                if (!methodInfoList.containsKey(key)) {
                    methodInfoList.put(key, mi);
                    System.out.println("add UsedMethods" + key);
                }
//                calledMethods.add(called);
//                calledMethodNames.add(getNameConstantOperand().toLowerCase());

                // System.out.println("Saw call to " + called);
//            }
                break;
            default:
                break;
        }
    }

    private Map<String, MethodInfo> getClassInfo(String className) {
        if (!classInfoList.containsKey(className)) {
            classInfoList.put(className, new HashMap<String, MethodInfo>());
        }
        Map<String, MethodInfo> info = classInfoList.get(className);
        return info;
    }

    @Override
    public void visitMethod(Method obj) {
//        if (!obj.isPrivate() || obj.isSynthetic()) {
//            return;
//        }
//        String methodName = getMethodName();
//        if (!methodName.equals("writeReplace") && !methodName.equals("readResolve")
//                && !methodName.equals("readObject") && !methodName.equals("readObjectNoData")
//                && !methodName.equals("writeObject")
//                && methodName.indexOf("debug") == -1 && methodName.indexOf("Debug") == -1
//                && methodName.indexOf("trace") == -1 && methodName.indexOf("Trace") == -1
//                && !methodName.equals("<init>") && !methodName.equals("<clinit>")) {
//            for(AnnotationEntry a : obj.getAnnotationEntries()) {
//                String typeName =  a.getAnnotationType();
//                if (typeName.equals("Ljavax/annotation/PostConstruct;")
//                        || typeName.equals("Ljavax/annotation/PreDestroy;")) {
//                    return;
//                }
//            }
//            definedPrivateMethods.add(MethodAnnotation.fromVisitedMethod(this));
//        }
        //记录方法为未调用.
        MethodInfo mi = new MethodInfo();
        mi.method = MethodAnnotation.fromVisitedMethod(this);
        mi.className = getDottedClassName();
        mi.methodName = getMethodName() + getMethodSig();
        mi.hasUsed = false;
        String key = mi.getMethodFullName();
        Map<String, MethodInfo> methodInfoList = getClassInfo(mi.className);
        if (!methodInfoList.containsKey(key)) {
            methodInfoList.put(key, mi);
            System.out.println("add UnusedMethods" + key);
        }
        super.visitMethod(obj);
    }
//    @Override
//    public void sawMethod() {
//        super.sawMethod();
//
////        System.out.println("check UnusedMethods" + key);
//    }

    @Override
    public void report() {
        //扫描出未调用的方法.
        List<MethodInfo> unusedList = new ArrayList<>();
        Map<String, MethodInfo> methodInfoList = getClassInfo(getDottedClassName());
        System.out.println("report " + methodInfoList.size());
        for (MethodInfo mi : methodInfoList.values()) {
            if (!mi.hasUsed) {
                unusedList.add(mi);
            }
        }
        //清空记录,防止重复发送.
        methodInfoList.clear();
        for (MethodInfo mi : unusedList) {
            System.out.println("report:" + mi.getMethodFullName());
            bugReporter.reportBug(new BugInstance(this, "HS_UNUSED_METHOD", NORMAL_PRIORITY).addClassAndMethod(mi.method));
        }
    }

    /**
     * 方法信息.
     */
    class MethodInfo {

        MethodAnnotation method;
        String className;
        String methodName;
        boolean hasUsed = false;

        /**
         * 取得方法的全名.
         *
         * @return 格式:package.Class#method(parames)
         */
        String getMethodFullName() {
//            StringBuilder sb = new StringBuilder();
//            sb.append(className).append("#(");
//
//            //TODO 加入参数.
//            sb.append(")");
//            return sb.toString();
            return className + "#" + methodName;
        }
    }

}
