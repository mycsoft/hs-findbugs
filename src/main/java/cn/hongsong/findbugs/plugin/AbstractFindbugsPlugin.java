/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import cn.hongsong.findbugs.FindBugsPluginUtils;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.StatelessDetector;
import java.text.DateFormat;
import java.util.Date;
import org.apache.bcel.classfile.Method;

/**
 * Findbugs插件基类.
 * @author Ma Yichao
 * @since 1.0.0
 */
public abstract class AbstractFindbugsPlugin extends BytecodeScanningDetector implements StatelessDetector {
    /** 日志对象 */
    protected static final Logger log = Logger.getLogger(AbstractFindbugsPlugin.class);

    
    protected BugReporter bugReporter;
    public static final boolean DEBUG = false;

    public AbstractFindbugsPlugin(BugReporter br) {
        //super(br);
        super();
        bugReporter = br;
    }

    protected void reportBug(String tag, int priority) {
        if (log.isDebugEnabled()){
            log.debug("report bug:"+tag);
            log.debug("priority = " + priority);
        }
        bugReporter.reportBug(
                new BugInstance(tag, priority).addClassAndMethod(this).addSourceLine(this));
    }

    /**发布bug到指定的行上.
     */
    protected void reportBug(String tag, int priority,int pc) {
        if (log.isDebugEnabled()){
            log.debug("report bug:"+tag);
            log.debug("priority = " + priority);
        }
        bugReporter.reportBug(
                new BugInstance(tag, priority).addClassAndMethod(this).addSourceLine(this,pc));
    }

    /** 检查当前行是否确定了一个循环.<b>本方法只处理当前行.</b>
     * @param seen 当前行信息.
     * @return 如果是,则返回循环体对象;否则,返回null.
     */
    protected Loop isLoop(int seen){
        Loop loop = null;
        if (isBranch(seen)) {
            int current = getPC();
            int dest = getBranchTarget();
            if (dest < current) {
                loop = new Loop();
                loop.start = dest;
                loop.end = current;
            }
        }

        return loop;
    }

    protected static class Loop{
        public int start;
        public int end;

    }

    protected static class Logger {
        DateFormat format = DateFormat.getTimeInstance(DateFormat.FULL);
        public Logger() {
        }

        public static Logger getLogger(Class c){
            return new Logger();
        }

        public void debug(String string) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[debug](");
            buffer.append(format.format(
                    new Date(System.currentTimeMillis())));
            buffer.append("):");
            buffer.append(string);
            System.out.println(buffer.toString());
        }

        public boolean isDebugEnabled() {
            return DEBUG;
        }


    }

    /**
     * 是否编译器自动生成的方法
     */
    protected static boolean isJVMAutoCreateMethod(Method method) {
        //检查方法名中是否有$.
        //1.4中,JVM编译器会自动增加内部方法来处理某些运算.
        if (log.isDebugEnabled()) {
            log.debug("================================");
            log.debug(method.getName());
        }
        return FindBugsPluginUtils.isJVMAutoCreateMethod(method);
    }
}
