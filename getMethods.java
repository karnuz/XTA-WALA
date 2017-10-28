import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.io.PrintWriter;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.classLoader.IMethod;



class getMethods {
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
      
      PrintWriter writer = new PrintWriter("Methods.facts", "UTF-8");
      
      for(IClass c : cha){
        Collection<IMethod> methods = c.getAllMethods();
        for(IMethod m : methods){
          String w = m.getSignature().toString();
          writer.println(c.getName().toString()+"   "+w);
        }
      }
      
      writer.close();
    
      
      
    } catch (WalaException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}