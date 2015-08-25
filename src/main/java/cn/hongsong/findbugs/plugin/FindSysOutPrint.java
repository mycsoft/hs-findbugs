/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;

/**
 * 检查代码中使用System.out.print的地方.
 *
 * @author Ma Yichao
 * @since 1.0.0
 */
public class FindSysOutPrint extends AbstractFindbugsPlugin {

    public FindSysOutPrint(BugReporter br) {
        super(br);
    }

    @Override
    public void sawOpcode(int seen) {
        //检查代码中使用System.out或err的地方.
        if (seen == GETSTATIC
                && getDottedClassConstantOperand().equals("java.lang.System")
                && ("out".equals(getNameConstantOperand()) || "err".equals(getNameConstantOperand())) //&& getDottedMethodSig().equals("out")
                ) {
            reportBug("HS_SYSTEM_OUT_PRINT", NORMAL_PRIORITY);
        }
    }
}
