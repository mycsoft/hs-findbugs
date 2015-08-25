/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import org.apache.bcel.classfile.Method;

/**
 *检查一个方法中,不可以有过多的return.
 * @author MaYichao
 */
public class FindMoreReturn extends AbstractFindbugsPlugin {

    /** 最多可以有的return数量 */
    public static final int MAX = 2;
    /** 当前方法一共有多少个return */
    private int count = 0;

    public FindMoreReturn(BugReporter br) {
        super(br);
    }

    @Override
    public void visitMethod(Method obj) {
        //初始化参数

        //if (count > MAX) {
        //reportBug("HS_MORE_RETURN", NORMAL_PRIORITY);
        //}

        count = 0;

        super.visitMethod(obj);


    }



    @Override
    public void sawOpcode(int seen) {

//        switch (seen){
//            case RETURN:
//            case IRETURN:
//            case FRETURN:
//            case DRETURN:
//            case LRETURN:
//            case ARETURN:
//                count++;
//            if (count > MAX) {
//                reportBug("HS_MORE_RETURN", NORMAL_PRIORITY);
//            }
//        }
        if (isReturn(seen)) {
            count++;
            if (count > MAX) {
                reportBug("HS_MORE_RETURN", LOW_PRIORITY);
            }
        }

    }
}
