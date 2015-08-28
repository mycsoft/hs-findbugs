/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import java.util.Stack;
import org.apache.bcel.classfile.Method;

/**
 * 检查循环过多的错误.
 *
 * @author MaYichao
 * @since 1.0.0
 */
public class FindMoreLoop extends AbstractFindbugsPlugin {

    Stack<Loop> loopStack = new Stack<>();
    /**
     * 最多可以有的loop数量
     */
    private static final int MAX = 3;

    /**
     * 循环的开始与结束
     *
     * @param br
     */
    public FindMoreLoop(BugReporter br) {
        super(br);
    }

    @Override
    public void visitMethod(Method obj) {
        //初始化参数
        loopStack.clear();
        super.visitMethod(obj);
    }

    @Override
    public void sawBranchTo(int targetPC) {
        int current = getPC();
        int dest = targetPC;
        if (dest < current) {
            //循环的desc< current 否则就是分支.
            Loop loop = new Loop(dest, current, dest);

            int c = storeLoop(loop);
            if (c > MAX) {
                reportBug("HS_MORE_LOOP", HIGH_PRIORITY, loopStack.lastElement().pc);
            }
        }
    }

    /**
     * 统计并保存循环层次.
     *
     * @param loop
     * @return
     */
    private int storeLoop(Loop loop) {
        storeLoop(loop, loopStack);
        return loopStack.size();
    }

    private int storeLoop(Loop loop, Stack<Loop> stack) {
        if (stack.isEmpty()) {
            stack.push(loop);
            return 1;
        }

        //loop可能是最小的循环体,也可能是最大的那个.
        Loop lastLoop = stack.peek();
        Loop topLoop = stack.lastElement();

        //检查当前循环是否在上一个循环之中.
        if (lastLoop.containe(loop)) {
            stack.push(loop);
            return stack.size();
        } else if (loop.containe(topLoop)) {
            //最大的一个.
            stack.insertElementAt(loop, 0);
            return stack.size();
        } else if (topLoop.containe(loop) && loop.containe(lastLoop)) {
            //loop是当前大循环中的一个环.
            for (int i = 0; i < stack.size(); i++) {
                Loop loop2 = stack.get(i);
                if (loop.containe(loop2)) {
                    //不比loop大.
                    //插入loop.
                    stack.add(i, loop);
                    break;
                }
            }
            return stack.size();
        } else {
            stack.pop();
            return storeLoop(loop);
        }
    }

    class Loop {

        int start;
        int end;
        int pc;

        public Loop(int start, int end, int pc) {
            this.start = start;
            this.end = end;
            this.pc = pc;
        }

        /**
         * 是否包含循环.
         *
         * @param loop
         * @return
         */
        private boolean containe(Loop loop) {
            return loop.start > start && loop.end < end;
        }

    }

}
