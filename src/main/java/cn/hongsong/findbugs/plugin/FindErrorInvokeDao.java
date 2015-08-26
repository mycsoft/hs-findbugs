/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ba.ClassContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import static cn.hongsong.findbugs.FindBugsPluginUtils.*;

/**
 * 检查Dao的错误调用. Dao应该只能在Service层之后被调用.
 *
 * @author MaYichao
 * @since 1.0.1
 */
public class FindErrorInvokeDao extends AbstractFindbugsPlugin {

    private final static java.util.logging.Logger log2 = java.util.logging.Logger.getLogger(FindErrorInvokeDao.class.getName());

    /**
     * 需要忽略的基类. 暂时只排除了实现IService接口的类.
     */
    private static final String[] IGNORE_CLASS_NAME_LIST = new String[]{
        //        "/cn/howso/service/IService",
        "cn.howso.service.IService"
//        "cn.howso.service.impl.ServiceImpl"
    };
    private static List<JavaClass> ignoreClassList;
    private static JavaClass daoClass;

    public FindErrorInvokeDao(BugReporter br) {
        super(br);
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        //排除可以调用DAO的类.
        if (initDaoClass() == null) {
            //找不到对应的dao基类.
//            System.out.println(String.format("Can't find daoClass %s", "cn.howso.dao.SpringMybatisDao"));
            return;
        }
        //Service
        if (isIgnoreClass(classContext)) {
//            System.out.println("ignore " + classContext.getJavaClass().getClassName());
            return;
        }

//        System.out.println("visit class " + getClassName());
        //ServiceBean
        super.visitClassContext(classContext);
//        classContext.getJavaClass().accept(this);
//        String name = classContext.getJavaClass().getClassName();
//        if (name.startsWith("cn.howso.service.")) {
//            System.out.println("======" + name);
//        }
    }

    @Override
    public void sawClass() {
//        if (isIgnoreClass(getClassContext())) {
//            return;
//        }
//        System.out.println("sawClass " + getClassName());
        super.sawClass(); //To change body of generated methods, choose Tools | Templates.
    }

    private JavaClass initDaoClass() {
        if (daoClass == null) {
            try {
                daoClass = Repository.lookupClass("cn.howso.dao.SpringMybatisDao");
            } catch (ClassNotFoundException ex) {
                log2.log(Level.SEVERE, null, ex);
            }
        }
        return daoClass;
    }

    /**
     * 检查当前是否是要忽略的类.按父类过滤.
     *
     * @param classContext
     * @return
     */
    private boolean isIgnoreClass(ClassContext classContext) {
        boolean ignore = false;
        JavaClass thisJC = classContext.getJavaClass();
        if (!thisJC.getPackageName().startsWith(getTOP_PACKAGE())) {
            //忽略不在根包内的代码.
//            System.out.println(String.format("class %s not in %s", thisJC.getPackageName(), getTOP_PACKAGE()));
            ignore = true;
        } else {
            if (ignoreClassList == null) {
                ignoreClassList = new ArrayList<>();
                for (String name : IGNORE_CLASS_NAME_LIST) {
                    try {
//                    JavaClass c = AnalysisContext.lookupSystemClass(name);
                        JavaClass c = Repository.lookupClass(name);
                        ignoreClassList.add(c);
                    } catch (ClassNotFoundException ex) {
                        log2.log(Level.SEVERE, null, ex);
                    }
                }
            }
            for (JavaClass jc : ignoreClassList) {
                try {
                    ignore = thisJC.implementationOf(jc);
                    if (ignore) {
                        break;
                    }
                } catch (ClassNotFoundException ex) {
                    log2.log(Level.SEVERE, String.format("%s.implementationOf(%s) failed!", thisJC.getClassName(), jc.getClassName()), ex);
                }
            }
            if (!ignore) {
                try {
                    ignore = thisJC.implementationOf(initDaoClass());
                } catch (ClassNotFoundException ex) {
                    log2.log(Level.SEVERE, String.format("%s.implementationOf(%s) failed!", thisJC.getClassName(), initDaoClass().getClassName()), ex);

                }
            }
        }
//        ignore = !thisJC.getClassName().equalsIgnoreCase("cn.howso.controller.CoLoginController");
        return ignore;
    }

    @Override
    public void sawOpcode(int seen) {
//        System.out.println(String.format("|%d|%s", seen, getDottedClassName()));
        switch (seen) {
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC:
            case INVOKEVIRTUAL_QUICK:
            case INVOKENONVIRTUAL_QUICK:
            case INVOKESUPER_QUICK:
            case INVOKESTATIC_QUICK:
            case INVOKEINTERFACE_QUICK:
            case INVOKEVIRTUALOBJECT_QUICK:
//            if (getDottedClassConstantOperand().equals(className)) {

                //记录方法为已调用.
                String className = "none";
                try {
                    className = getDottedClassConstantOperand();
//                    System.out.println(String.format("|%d|%s call %s", seen, getDottedClassName(), className));
//                    System.out.println("==2" + className);
                    //test
//                    className = "cn.howso.dao.mapper.CompanyUserinfoMapper";
                    if (className.startsWith(getTOP_PACKAGE())) {
                        //只处理包内的类.
                        JavaClass calledClass = Repository.lookupClass(className);
                        if (calledClass.implementationOf(daoClass)) {
//                            System.out.println(String.format("|%d|%s call %s", seen, getDottedClassName(), className));
                            reportBug("HS_ILLEGAL_CALL_DAO", HIGH_PRIORITY);
                        }
                    }
                } catch (Exception ex) {
//                    java.util.logging.Logger.getLogger(FindErrorInvokeDao.class.getName()).log(Level.SEVERE, null, ex);
//                    ex.printStackTrace();
                    log2.log(Level.WARNING, String.format("check|%d|%s", seen, getDottedClassName(), className), ex);
                }

                break;
            default:
                break;
        }
    }

}
