/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ba.Hierarchy;

/**
 * 检查代码中将Exception未经日志系统输出的地方.
 * @author Ma Yichao
 */
public class FindExceptionPrint extends AbstractFindbugsPlugin {

    public FindExceptionPrint(BugReporter br) {
        super(br);
    }

    @Override
    public void sawOpcode(int seen) {
        try {
            //检查是否有 Exception.printStackTrace()的方法.
            if (seen == INVOKEVIRTUAL && Hierarchy.isSubtype(getDottedClassConstantOperand(), "java.lang.Exception")
                    && "printStackTrace".equals(getNameConstantOperand())) {
                reportBug("HS_EXCEPTION_PRINT", NORMAL_PRIORITY);
            }
        } catch (ClassNotFoundException ex) {
            //Logger.getLogger(FindExceptionPrint.class.getName()).log(Level.SEVERE, null, ex);
            //通知用户,发现了没有载入的类.
            bugReporter.reportMissingClass(ex);

        }
    }
}
