/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs;

import java.util.ResourceBundle;
import org.apache.bcel.classfile.Method;

/**
 * 工具方法类.
 * @author MaYichao
 * @since 1.0.0
 */
public class FindBugsPluginUtils {

    //TODO 配置信息要改为配置文件,以便于扩展.
    /** 资源配置对象 */
    private static final ResourceBundle resourceBundle =
            ResourceBundle.getBundle(
            //FindBugsPluginUtils.class.getResource(
            //"com.myc.findbugs.plugin.config").getFile()
            "com_myc_findbugs_plugin_config"
            );
    /** 顶层包 */
    private static final String TOP_PACKAGE = resourceBundle.getString("util.package.top");
    /** 层类型 */
    public static final int FW_LAYER_UNKNOWN = 0;
    public static final int FW_LAYER_DAO_IMPL = 10;
    public static final int FW_LAYER_DAO = 20;
    public static final int FW_LAYER_SERVICE_IMPL = 25;
    public static final int FW_LAYER_SERVICE = 30;
    public static final int FW_LAYER_ACTION = 50;
    public static final int FW_LAYER_FORM = 60;
    public static final int FW_LAYER_UTIL = 100;
    
    /** 层包后缀常量 */
    private static final String PK_POST_ACTION = resourceBundle.getString("util.package.post.action");
    private static final String PK_POST_FORM = resourceBundle.getString("util.package.post.form");
    private static final String PK_POST_SERVICE = resourceBundle.getString("util.package.post.service");
    private static final String PK_POST_SERVICE_IMPL = resourceBundle.getString("util.package.post.service.impl");
    private static final String PK_POST_DAO = resourceBundle.getString("util.package.post.dao");
    private static final String PK_POST_DAO_IMPL = resourceBundle.getString("util.package.post.impl");
    private static final StringBuffer AUTOMETHOD_FLAG = new StringBuffer("$");

    /**
     * 顶层包
     * @return the TOP_PACKAGE
     */
    public static String getTOP_PACKAGE() {
        return TOP_PACKAGE;
    }

    private FindBugsPluginUtils() {
    }

    /** 根据包确定一个类在框架中的层次 */
    public static int getLayerByPackageName(String pkName) {
        int frameworkLayer = FW_LAYER_UNKNOWN;
        if (pkName != null && pkName.startsWith(getTOP_PACKAGE())) {
            if (pkName.endsWith(PK_POST_ACTION)) {
                frameworkLayer = FW_LAYER_ACTION;
            } else if (pkName.endsWith(PK_POST_FORM)) {
                frameworkLayer = FW_LAYER_FORM;
            } else if (pkName.endsWith(PK_POST_SERVICE)) {
                frameworkLayer = FW_LAYER_SERVICE;
            } else if (pkName.endsWith(PK_POST_SERVICE_IMPL)) {
                frameworkLayer = FW_LAYER_SERVICE_IMPL;
            } else if (pkName.endsWith(PK_POST_DAO)) {
                frameworkLayer = FW_LAYER_DAO;
            } else if (pkName.endsWith(PK_POST_DAO_IMPL)) {
                frameworkLayer = FW_LAYER_DAO_IMPL;
            } else {
                frameworkLayer = FW_LAYER_UNKNOWN;
            }
        } else {
            frameworkLayer = FW_LAYER_UNKNOWN;
        }
        return frameworkLayer;
    }

    /**
     * 是否编译器自动生成的方法
     */
    public static boolean isJVMAutoCreateMethod(Method method) {
        //检查方法名中是否有$.
        //1.4中,JVM编译器会自动增加内部方法来处理某些运算.这些方法名都会有'$'
        return method.getName().contains(AUTOMETHOD_FLAG);
    }
}
