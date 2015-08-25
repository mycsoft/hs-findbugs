/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import org.apache.bcel.classfile.Method;

/**
 *
 * @author MaYichao
 */
public class FindMoreLoop extends AbstractFindbugsPlugin {

    /** 最多可以有的return数量 */
    private static final int MAX = 3;
    /** 当前方法一共有多少个return */
    private int count = 0;
    /** 循环的开始与结束 */
    private int loopstart = -1;
    private int loopend = -1;

    public FindMoreLoop(BugReporter br) {
        super(br);
    }

    @Override
    public void visitMethod(Method obj) {
        //初始化参数

        //if (count > MAX) {
        //reportBug("HS_MORE_RETURN", NORMAL_PRIORITY);
        //}

        count = 0;
        loopstart = -1;
        loopend = -1;
        super.visitMethod(obj);
    }

    @Override
    public void sawOpcode(int seen) {
        //switch (seen) {
        //case GOTO:
        if (isBranch(seen)) {
            int current = getPC();
            int dest = getBranchTarget();
            if (dest < current) {
                if (loopstart < 0 || dest > loopend) {
                    loopstart = dest;
                    loopend = current;
                    count = 1;
                } else {

                    loopstart = Math.min(loopstart, dest);
                    loopend = current;
                    count++;
                    if (count > MAX) {
                        reportBug("HS_MORE_LOOP", HIGH_PRIORITY);
                    }
                }
            }


        }

    }
}
