/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import cn.hongsong.findbugs.FindBugsPluginUtils;
import edu.umd.cs.findbugs.BugReporter;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.Method;

/**
 * 检查是否有从循环中调用数据库操作.
 *
 * @author MaYichao
 */
public class FindInvokeSqlInLoop extends AbstractFindbugsPlugin {

    /**
     * 当前类的层
     */
    private int frameworkLayer = FindBugsPluginUtils.FW_LAYER_UNKNOWN;
    //private ArrayList<Integer> invoikList = null;
    private int lastInvoikPC = -1;

    public FindInvokeSqlInLoop(BugReporter br) {
        super(br);
    }

    @Override
    public void visitConstantClass(ConstantClass obj) {
        super.visitConstantClass(obj);
        //todo 确定当前类的层
        String pkName = getPackageName();
        frameworkLayer = FindBugsPluginUtils.getLayerByPackageName(pkName);
    }

    @Override
    public void visitMethod(Method obj) {
        //初始化参数
        lastInvoikPC = -1;
        super.visitMethod(obj);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sawOpcode(int seen) {
        /*如果当前行调用了下一层代码,则记录下来.
         * 如果当前行确定了一个循环,则检查记录的下层调用中有没有在这段循环中的.
         * 如果有,则报错.
         */

        //todo 如果当前行调用了下一层代码,则记录下来.
        if (seen == INVOKESPECIAL || seen == INVOKEVIRTUAL) {
            String cName = getDottedClassConstantOperand();
            if (cName != null) {
                int length = cName.lastIndexOf(".");
                if (length > 0) {
                    String pkName = cName.substring(0, length);
                    int layer = FindBugsPluginUtils.getLayerByPackageName(pkName);
                    switch (layer) {
                        case FindBugsPluginUtils.FW_LAYER_UNKNOWN:
                        case FindBugsPluginUtils.FW_LAYER_UTIL:

                            break;

                        default:
                            if (layer < frameworkLayer) {
                                //invoikList.add(new Integer(getPC()));
                                lastInvoikPC = getPC();
                            }
                    }
                }
            }
        //reportBug("HS_PRJACC_UNSUMMED", HIGH_PRIORITY);
            //lastIsException = true;

        }

        //如果当前行确定了一个循环,则检查记录的下层调用中有没有在这段循环中的.
        Loop loop = isLoop(seen);
        //if (loop != null && !invoikList.isEmpty()) {
        if (loop != null && lastInvoikPC >= 0) {
            if (loop.start < lastInvoikPC) {
                /*FIXME 现在这种方式可能会出现的错误,是当循环嵌套中有多层的SQL调用时,这样可能会只报出一次错误.
                 * 最好的方法是使用队列来记录所有可能的调用,不过,这样的内存消耗要大很多.
                 */

                reportBug("HS_LOOP_SQL", HIGH_PRIORITY, lastInvoikPC);
                lastInvoikPC = -1;
            }
        }

        //TODO 如何检查间接调用? 即循环调用了非下层的方法,但是该方法会调用到SQL.
        super.sawOpcode(seen);
    }
}
