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



class Methods {
	public static void main(String[] args) throws IOException {
		System.err.println("Extracting Methods...");
		try{
			String classpath = args[1];
			AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

			// invoke WALA to build a class hierarchy
			ClassHierarchy cha = ClassHierarchyFactory.make(scope);
	  
			PrintWriter writer = new PrintWriter("Methods.facts", "UTF-8");
	  
			for(IClass c : cha){
				String classname = c.getName().toString();
				Collection<IMethod> methods = c.getAllMethods();
				for(IMethod m : methods){
					String methodsig = m.getSignature().toString();
					String methodklass = "L"+ methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
					String methodselector = methodsig.substring(methodsig.lastIndexOf('.') + 1 );
					//writer.println(c.getName().toString()+"   "+ w );
					//if(!classname.substring(0,5).equals("Ljava") ){
						writer.println(classname + "	"  + methodklass + "	" + methodselector );
					//}
					
				}
			}
			writer.close();
	
	  
	  
		} catch (WalaException e) {
		e.printStackTrace();
		}

	}
}