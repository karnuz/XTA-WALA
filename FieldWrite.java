import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.io.PrintWriter;
import java.util.Iterator;


import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.strings.StringStuff;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;








class FieldWrite {
/*	public static void pruneForAppLoader() {
    Predicate<IClass> f = new Predicate<IClass>() {
      @Override public boolean test(IClass c) {
        return (c.getClassLoader().getReference().equals(ClassLoaderReference.Application));
      }
    };

  }*/

  public static void main(String[] args) throws IOException {
    try{
      String classpath = args[1];
      AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

      // invoke WALA to build a class hierarchy
      ClassHierarchy cha = ClassHierarchyFactory.make(scope);

      /*MethodReference mr = StringStuff.makeMethodReference("hypo.hypot(II)I");

      // Resolve the method name into the IMethod, the canonical representation of the method information.
      IMethod m = cha.resolveMethod(mr);
      if (m == null) {
        Assertions.UNREACHABLE("could not resolve " + mr);
      }
      
      // Set up options which govern analysis choices.  In particular, we will use all Pi nodes when
      // building the IR.
      //AnalysisOptions options = new AnalysisOptions();
      //options.getSSAOptions().setPiNodePolicy(SSAOptions.getAllBuiltInPiNodes());
      
      // Create an object which caches IRs and related information, reconstructing them lazily on demand.
      //IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
      
      // Build the IR and cache it.
      //IR ir = cache.getIR(m, Everywhere.EVERYWHERE);

      if (ir == null) {
        Assertions.UNREACHABLE("Null IR for " + m);
      }

*/

      /*Iterator<SSAInstruction> iriterator = ir.iterateAllInstructions();
      
      PrintWriter writerfr = new PrintWriter("FieldRead", "UTF-8");
      while(iriterator.hasNext()){
        if(iriterator.next() instanceof SSAGetInstruction){
          writerfr.println(iriterator.next().toString());
        }
      }
      writerfr.close();
        */




      PrintWriter writerfr = new PrintWriter("FieldWrite", "UTF-8");
      AnalysisOptions options = new AnalysisOptions();
      IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
      
      for(IClass c : cha){
        Collection<IMethod> methods = c.getAllMethods();
        for(IMethod m : methods){
          System.err.println(m.getSignature());
          if(cache.getIR(m, Everywhere.EVERYWHERE) != null){
            IR ir = cache.getIR(m , Everywhere.EVERYWHERE);
            System.err.println("hello");
            Iterator<SSAInstruction> iriterator = ir.iterateAllInstructions();
            while(iriterator.hasNext()){
              SSAInstruction currentinstruction = iriterator.next();
              if(currentinstruction instanceof SSAPutInstruction){
                SSAPutInstruction currentinstructiondowncasted = (SSAPutInstruction) currentinstruction;
                //System.err.println(currentinstruction.toString());
                writerfr.println(m.getSignature() + " " + currentinstructiondowncasted.getDeclaredField().toString() + " " + currentinstructiondowncasted.getDeclaredField().getSignature());
              }
            }
          }
          
        }
      }
      writerfr.close();

      /*
      Iterator<SSAInstruction> iriterator1 = ir.iterateAllInstructions();
      PrintWriter writerall = new PrintWriter("allir", "UTF-8");
      while(iriterator1.hasNext()){
          writerall.println(iriterator1.next().toString());
      }
      writerall.close();
      */

/*
      System.err.println(ir.toString());

      PrintWriter writer = new PrintWriter("IR", "UTF-8");
      writer.println(ir.toString());
      writer.close();
  */    

/*
      PrintWriter writer = new PrintWriter("Methods.facts", "UTF-8");
      
      for(IClass c : cha){
        Collection<IMethod> methods = c.getAllMethods();
        for(IMethod m : methods){
          String w = m.getSignature().toString();
          writer.println(c.getName().toString()+"   "+w);
        }
      }
      
      writer.close();
    
  */    
      
    } catch (WalaException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}