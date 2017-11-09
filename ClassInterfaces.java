import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;



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
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.classLoader.IClassLoader;



class ClassInterfaces {

	public static void main(String[] args) throws IOException {
    System.err.println("Extracting Class Interfaces...");
    	try{
    		String classpath = args[1];
      		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

      		// invoke WALA to build a class hierarchy
      		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
      		
      		PrintWriter writer = new PrintWriter("ClassInterfaces.facts", "UTF-8");
      			
      		for(IClass c : cha){
      			String classname = c.getName().toString();
      			Collection<IClass> interfacelist = c.getAllImplementedInterfaces();
      			for(IClass i : interfacelist){
      				String intfname = i.getName().toString();
      				//if(!intfname.substring(0,1).equals("L")){
      					writer.println(classname + "	" + intfname );	
      				//}
      				
      			}
      		}

      		writer.close();

      		

    	
    	} catch (WalaException e) {
      	// TODO Auto-generated catch block
      	e.printStackTrace();
    	}

	}
}
	