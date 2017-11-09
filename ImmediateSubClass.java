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



class ImmediateSubClass {


  public static void main(String[] args) throws IOException {
    System.err.println("Extracting SubClasses...");
    try{
      String classpath = args[1];
      AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));


      // invoke WALA to build a class hierarchy
      ClassHierarchy cha = ClassHierarchyFactory.make(scope);

      PrintWriter writer = new PrintWriter("ImmediateSubclass.facts", "UTF-8");

      //obtaining immediate subclasses
      for(IClass c : cha){
        Collection<IClass> subclass = cha.getImmediateSubclasses(c);
        for(IClass s : subclass){
          String w = s.getName().toString();
          //if(!w.substring(0,1).equals("L")){
            writer.println(c.getName().toString()+ "	" + w );
          //}
        }
      }
      writer.close();    
      
      
    } catch (WalaException e) {
      
      e.printStackTrace();
    }

  }
}
