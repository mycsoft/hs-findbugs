/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import org.apache.bcel.classfile.Method;

/**
 * 检查项目台帐的引用是否都正常调用了汇总方法.
 * @author MaYichao
 * @since 1.0.0
 */
public class FindAccError extends AbstractFindbugsPlugin {

    /** 是否调用了项目台帐的方法 */
    private boolean useAccMethod = false;
    /** 是否调用了项目台帐汇总的方法 */
    private boolean hasSumAcc = false;
    /** 包名 */
    //private static final String ACC_SERVICE_PACKAGE = "cn/com/jsepc/epii/business/prj/service/user15/";
    /** 类名 */
    //private static final String ACC_SERVICE = ACC_SERVICE_PACKAGE + "PrjAccService";
    /** 汇总方法 */
    //private static final String ACC_SERVICE_SUM_METHOD = "sumPrjAcc";
    private static final String ACC_SERVICE_PACKAGE = "sample.";
    private static final String ACC_SERVICE = ACC_SERVICE_PACKAGE + "PrjAccService";
    private static final String ACC_SERVICE_SUM_METHOD = "sumPrjAcc";

    public FindAccError(BugReporter br) {
        super(br);
    }

    @Override
    public void visitMethod(Method obj) {
        //初始化参数
        useAccMethod = false;
        hasSumAcc = false;

        super.visitMethod(obj);
    }

    @Override
    public void sawOpcode(int seen) {
        //super.sawOpcode(seen);
        //try {
        if (seen == INVOKEVIRTUAL &&
                ACC_SERVICE.equals(getDottedClassConstantOperand())) {
            //reportBug("HS_PRJACC_UNSUMMED", HIGH_PRIORITY);
            useAccMethod = true;
            hasSumAcc = false;
            if (ACC_SERVICE_SUM_METHOD.equals(getNameConstantOperand())) {
                hasSumAcc = true;
            }
            return;
        }

        if (seen == RETURN) {
            if (useAccMethod == true && hasSumAcc == false) {
                reportBug("HS_PRJACC_UNSUMMED", HIGH_PRIORITY);
            }
        }
//        } catch (ClassNotFoundException ex) {
//            //通知用户,发现了没有载入的类.
//            bugReporter.reportMissingClass(ex);
//        }
    }
}
