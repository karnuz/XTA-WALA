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
      PrintWriter writer = new PrintWriter("ParamTypes.facts", "UTF-8");
      
        for(IClass c : cha){
            Collection<IMethod> methods = c.getAllMethods();
            for(IMethod m : methods){
                String methodsig = m.getSignature();
                String methodklass = "L"+methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
                String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
                int z = m.getNumberOfParameters();
                if(z!=1){
                    for(int i=1; i<=z-1; i++){
                        //repetition of same type of parameters. can be removed by using set data structure.
                        String parameterType = m.getParameterType(i).getName().toString();
                        if(parameterType.substring(0,1).equals("[")){
                            writer.println(methodklass + "	" + methodselector + "	" + "Ljava/util/Arrays" );
                            writer.println(methodklass + "	" + methodselector + "	" + parameterType.substring(parameterType.lastIndexOf('[')+1) );  
                        }
                        else{
                            writer.println(methodklass + "	" + methodselector + "	" + parameterType);    
                        }
                        
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
