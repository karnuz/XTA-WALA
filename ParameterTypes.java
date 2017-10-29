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



class ParameterTypes {


    public static void main(String[] args) throws IOException {
    try{
      String classpath = args[1];
      AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));


      // invoke WALA to build a class hierarchy
      ClassHierarchy cha = ClassHierarchyFactory.make(scope);
      PrintWriter writer = new PrintWriter("ParameterTypes.facts", "UTF-8");
      
        for(IClass c : cha){
            Collection<IMethod> methods = c.getAllMethods();
            for(IMethod m : methods){
                int z = m.getNumberOfParameters();
                if(z!=1){
                    for(int i=1; i<=z-1; i++){
                        writer.println(m.getSignature().toString() + "    " + m.getParameterType(i).toString() );  
                    }

                }
                
            }
        }
        writer.close();    
      
      } catch (WalaException e) {
      
      e.printStackTrace();
    }

  }
}
