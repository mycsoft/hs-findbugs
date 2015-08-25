/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.findbugs.plugin;

import edu.umd.cs.findbugs.BugReporter;
import org.apache.bcel.classfile.Method;

/**
 * Sample
 * @author MaYichao
 */
public class HelloFindRunInvocations extends AbstractFindbugsPlugin {

//    BugReporter bugReporter;
//    private final BugAccumulator bugAccumulator;
//    private static final List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
//    private static final Map<String, MethodInfo> methodInfoList = new HashMap<String, MethodInfo>();
    public HelloFindRunInvocations(BugReporter bugReporter) {
        super(bugReporter);
//        this.bugReporter = bugReporter;
//        bugAccumulator = new BugAccumulator(bugReporter);
    }

    @Override
    public void visit(Method obj) {
        //标记方法表待检查清单中.
        MethodInfo mi = new MethodInfo();
        mi.method = obj;

        mi.className = getClassContext().getJavaClass().getClassName();
//        methodInfoList.put(mi.getMethodFullName(), mi);
        reportBug("HS_UNUSED_METHOD", HIGH_PRIORITY);
        super.visit(obj);

    }
//    @Override
//    public void visitClassContext(ClassContext classContext) {
//        classContext.getJavaClass().accept(this);
//    }
    
    @Override
    public void report() {
        //        super.report(); //To change body of generated methods, choose Tools | Templates.
//        bugReporter.reportBug(new BugInstance("HS_UNUSED_METHOD", HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this));
//        bugAccumulator.accumulateBug(new BugInstance("HS_UNUSED_METHOD", HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this), this);
//        reportBug("HS_UNUSED_METHOD", HIGH_PRIORITY);
    }

//    @Override
//    public void sawOpcode(int seen) {
////        super.sawOpcode(seen); 
////        StringBuilder methodKey = new StringBuilder();
//        System.out.println("HS_UNUSED_METHOD:" + getClassName());
////        System.exit(0);
//
//    }

    class MethodInfo {

        Method method;
        String className;
        boolean hasUsed = false;

        /**
         * 取得方法的全名.
         *
         * @return 格式:package.Class#method(parames)
         */
        String getMethodFullName() {
            StringBuilder sb = new StringBuilder();
            sb.append(className).append("#(");

            //TODO 加入参数.
            sb.append(")");
            return sb.toString();
        }
    }

}
