/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import org.apache.bcel.classfile.Method;

/**
 * 检查所有的异常是否都按规定抛出
 * @author MaYichao
 * @since 1.0.0
 */
public class FindThrowException extends AbstractFindbugsPlugin {

    /** ServiceException */
    private static final String EXCEPTION_NAME = "cn.howso.exception.BusinessException";
    /** 记录上一句带入的是否是正确的异常 */
    private boolean lastIsException = false;
    /** 记录上一行的内容 */
    private String lastLine = null;

    public FindThrowException(BugReporter br) {
        super(br);
    }

    //2009-9-2 马翼超 FIX:将正确的异常使用方法也判为错误的问题.
    //因为saw与visit方法的区别,所以将错误使用saw方法的地方改为visit.
    @Override
    public void visit(Method obj) {
        //System.out.println("[debug]:invoke sawMethod()" + getMethodName());
        //初始化参数
        lastIsException = false;
        lastLine = null;
        super.visit(obj);
    }

    

    @Override
    public void sawOpcode(int seen) {
        //super.sawOpcode(seen);
        //try {

        //TODO 现在只检查了一种唯一的调用方式.
        /* 以下这种异常检查时判断为错误的.为什么?
         * try {
        response.getWriter().print(res);
        } catch (IOException e) {
        throw new ServiceException(e, GlobalExceptions.RESPONSE_IOEXCEPTION);
        }
         */
        if (seen == INVOKESPECIAL) {
            //System.out.println("[debug1]:"+lastLine);
            lastLine = getDottedClassConstantOperand();
            //System.out.println("[debug2]:"+lastLine);
            if (EXCEPTION_NAME.equals(lastLine)) {
                //reportBug("HS_PRJACC_UNSUMMED", HIGH_PRIORITY);
                lastIsException = true;
            }
        } else {
            if (seen == ATHROW) {
                
                if (!lastIsException) {
                    //System.out.println("last line:");
                    //System.out.println(lastLine);
                    //System.out.println("=======================");
                    /*有一种特殊情况,当代码中调用到XXXX.class时,编译器会自动生成一个
                     * 隐含的class$(String) 方法,在这个方法中,会自动加入一个异常捕获.
                     * 这个异常是不需要进行代码检查的.因此,要想办法排除掉这类方法中的检查.
                    */
                    if (!isJVMAutoCreateMethod(getMethod())){
                        reportBug("HS_ERROR_EXCEPTION", NORMAL_PRIORITY);
                    }
                }
            }
            lastIsException = false;
        }


    }
}
