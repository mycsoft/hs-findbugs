/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import java.util.HashMap;
import java.util.Map;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;

/**
 * 检查所有的异常是否都按规定抛出.
 *
 * 重点检查try-catch中未将异常抛出的问题.
 *
 * @author MaYichao
 * @since 1.0.0
 */
public class FindThrowException extends AbstractFindbugsPlugin {

    /**
     * ServiceException
     */
    private static final String EXCEPTION_NAME = "cn.howso.exception.BusinessException";
    private static final String TAG = "HS_ERROR_EXCEPTION";
    private static final String DEBUG_CLASS = "cn/hongsong/co/facade/FacadeUtil";
    /**
     * 记录上一句带入的是否是正确的异常
     */
    private boolean lastIsException = false;
    /**
     * 记录上一行的内容
     */
    private String lastLine = null;
    private int lastSeen = 0;
    private int lastSeen2 = 0;

//    private TryCatchArea tca = null;
    private int lineNumber = 0;

    private Map<Integer, TryCatchArea> tryCatchAreaMap = new HashMap<>();
    /**
     * 当前Try-catch区域.
     */
    private TryCatchArea currentTCA = null;

    public FindThrowException(BugReporter br) {
        super(br);
    }

    //2009-9-2 马翼超 FIX:将正确的异常使用方法也判为错误的问题.
    //因为saw与visit方法的区别,所以将错误使用saw方法的地方改为visit.
//    @Override
//    public void visit(Method obj) {
//    }
    private void initForVisit(Code code) {
        //初始化参数
        lastIsException = false;
        lastLine = null;
        lastSeen = 0;
        lastSeen2 = 0;
//        tca = null;
        lineNumber = 0;
        tryCatchAreaMap.clear();
        currentTCA = null;

        if (getMethodName().startsWith("<")) {
            //跳过内置方法.
            return;
        }
//        System.out.println(String.format("Method is %s", getMethodName()));
        checkTryCatchArea(code);
    }

    private void checkAllUnprocessedTryCatchArea() {
        //如何保证此方法在每个Method检查结束时,可以正常的被调用到? 放在 visitCode方法后.
        //事后处理.检查有多少正常处理的catch块.
//        int i = 0;
        if (tryCatchAreaMap.isEmpty()) {
            return;
        }
        for (TryCatchArea tca : tryCatchAreaMap.values()) {
            if (!tca.processed) {
                //未进行处理,记录bug.

//                System.out.println(i ++ +"\t|"+getMethodName()
//                        + "|unprocessed:" + tca);
//                System.out.println("unprocessed:" + tca);
                reportBug(TAG, HIGH_PRIORITY, tca.catchStart);
            }
        }
    }

    /**
     * 解析代码.
     *
     * 一般这个内容会是一个方法或代码块.
     *
     * @param code 代码内容.
     */
    @Override
    public void visitCode(Code code) {
        if (isNotDebugClass()) {
            return;
        }
        initForVisit(code);
        super.visitCode(code); //To change body of generated methods, choose Tools | Templates.
        checkAllUnprocessedTryCatchArea();
//        for (TryCatchArea tca : tryCatchAreaMap.values()) {
//            if (!tca.processed) {
//                System.out.println("code:\n" + code);
//                break;
//            }
//        }
        tryCatchAreaMap.clear();
    }

    /**
     * Debug类过滤器.
     *
     * 在非debug模式下,都返回false.
     * 在debug模式下,如果当前类=={@link #DEBUG_CLASS},则返回true,否则返回false.
     *
     * @return
     */
    private boolean isNotDebugClass() {
//        return !DEBUG_CLASS.equals(getClassName());
        return false;
    }

    /**
     * 检查是否有try-catch块.
     *
     * 如果有就保存到{@link #tryCatchAreaMap}中.
     *
     * @param code
     */
    private void checkTryCatchArea(Code code) {
        if (code == null) {
            return;
        }
        CodeException[] ces = code.getExceptionTable();
        if (ces != null) {
            for (int i = 0; i < ces.length; i++) {
                CodeException ce = ces[i];
//                System.out.println(String.format("%d:{from:%d,to:%d,handle:%d,type:%s} @ %s class %s", i, ce.getStartPC(), ce.getEndPC(), ce.getHandlerPC(), ce.getCatchType(), getMethodName(), code.getClass()));
                TryCatchArea tca = createTCA(ce);
                tryCatchAreaMap.put(tca.tryEnd, tca);
            }

        } else {
//            System.out.println("no exception @ " + getMethodName());
        }
    }

    /**
     * 检查更新try-catch区的结束点.
     *
     * 如果pc是某一try-catch的try-End点,则当前行应该为goto行,根据对应的目标点更新对应try-catch区的结束点.
     *
     * @param pc 行号
     */
    private void checkAndUpdateTcaEnd(int pc) {
        //逐行检查,补偿计算try-catch End.
        TryCatchArea tca = findTryCathAreaByTryEnd(pc);
        if (tca == null) {
            return;
        }

        if (tca.processed) {
            return;
        }
//        try {
        tca.end = getBranchTarget();
//        } catch (IllegalStateException ex) {
//            System.out.println(String.format("get branch target failed! pc is %d", pc));
//            ex.printStackTrace();
//        }

    }

    @Override
    public void sawOpcode(int seen) {
//        if (!"cn/howso/util/ReadExcelUtils".equals(getClassName())) {
////            System.out.println("| " + seen);
//            return;
//        }

        int pc = getPC();
        if (seen == GOTO) {
            //只处理跳转点
            //更新结束点.
            checkAndUpdateTcaEnd(pc);
        } else {
            //TODO 非跳转点可能是finally块.如何可以正常识别?现在当有finally时,对应的catch块会被忽略.
        }
        //确认当前是否在try-catch块中.
        TryCatchArea tca = findTryCathAreaByCatchLine(pc);

        sawCatch(seen, tca);

    }

    private void sawCatch(int seen, TryCatchArea tca) {
        currentTCA = tca;
        int pc = getPC();
        /*
        检查每一行,是否在catch中,
        如果是,需要检查当前行是否是合理的处理语句.
         */

//        System.out.println(String.format("| %d | opcode:%d | code:%s", lineNumber++ , seen ,getCode()));
        if (tca == null || tca.processed) {
            //跳过已经处理过的块.
//            scanTryStart(seen);
        } else {
//            System.out.println("pc = " + pc + " tca:" + tca);
            if (tca.inCatch(pc)) {
                switch (seen) {
                    case ATHROW:
                        //有抛出,视为已处理.
                        tca.processed = true;
//                        System.out.println("已处理:" + tca);
                        break;
                    default:
//                        throw new AssertionError();
                }
            }
        }

    }

    private TryCatchArea createTCA(CodeException ce) {
        TryCatchArea tca = new TryCatchArea();
        tca.start = ce.getStartPC();
        tca.tryEnd = ce.getEndPC();
        tca.catchStart = ce.getHandlerPC();
        tca.status = TryStatus.noTry;
        return tca;
    }

    /**
     *
     * 检查当前行是否在try-catch块中.
     *
     * @param lineNumber
     * @return 如果是,则返回对应的try-catch块.
     */
    private TryCatchArea findTryCathArea(LineNumber lineNumber) {
        int ln = lineNumber.getLineNumber();
        return findTryCathAreaByCatchLine(ln);
    }

    /**
     * 根据catch行号查询try-catch块.
     *
     *
     * @param ln 行号.
     * @return 当ln在某一个try-catch块的catch区域中时,返回该try-catch块.否则返回null.
     * @see TryCatchArea#inCatch(int)
     */
    private TryCatchArea findTryCathAreaByCatchLine(int ln) {
        //检查是否在当前的tca中.
        if (currentTCA != null) {

            if (currentTCA.inCatch(ln)) {
                return currentTCA;
            }
        }

        if (tryCatchAreaMap.isEmpty()) {
            return null;
        }

        for (TryCatchArea tca : tryCatchAreaMap.values()) {
            if (tca.inCatch(ln)) {
                return tca;
            }
        }
        return null;

    }

    private TryCatchArea findTryCathAreaByTryEnd(int lineNumber) {
        return tryCatchAreaMap.get(lineNumber);
    }

    class TryCatchArea {

        int start;
        int end;
        int tryEnd;
        int catchStart;
        TryStatus status = TryStatus.noTry;
        /**
         * 是否已经处理掉.
         */
        boolean processed = false;

        @Override
        public String toString() {
            return String.format("{start:%d,tryEnd:%d,catchStart:%d,end:%d,status:%s}", start, tryEnd, catchStart, end, status);
        }

        /**
         * 检查行号是否在当前区块的Catch区中.
         *
         * @param ln 行号.
         * @return
         */
        private boolean inCatch(int ln) {
            return ln >= catchStart && (ln <= end || end == 0);
        }

    }

    enum TryStatus {
        noTry, inTry, inCatch, endCatch;
    }
}
