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
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;




class InstantiatedClasses {
    public static void main(String[] args) throws IOException {
        try{
            String classpath = args[1];
            AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

            // invoke WALA to build a class hierarchy
            ClassHierarchy cha = ClassHierarchyFactory.make(scope);

            PrintWriter writerfr = new PrintWriter("InstantiatedClasses.facts", "UTF-8");
      
            AnalysisOptions options = new AnalysisOptions();
            IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
      
            for(IClass c : cha){
                Collection<IMethod> methods = c.getAllMethods();
                for(IMethod m : methods){
                    String methodsig = m.getSignature();
                    String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
                    String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
                    //System.err.println(methodsig);
                    if(cache.getIR(m, Everywhere.EVERYWHERE) != null){
                        IR ir = cache.getIR(m , Everywhere.EVERYWHERE);
                        Iterator<SSAInstruction> iriterator = ir.iterateAllInstructions();
                        while(iriterator.hasNext()){
                            SSAInstruction currentinstruction = iriterator.next();
                            if(currentinstruction instanceof SSANewInstruction){
                                SSANewInstruction currentinstructiondowncasted = (SSANewInstruction) currentinstruction;
                                String instklass =  currentinstructiondowncasted.getConcreteType().getName().toString();
                                //System.err.println(currentinstruction.toString());
                                if(instklass.substring(0,1).equals("[") ){
                                    writerfr.println( methodklass + "	" + methodselector + "	" + "Ljava/util/Arrays");
                                    writerfr.println( methodklass + "	" + methodselector + "	" +  instklass.substring(instklass.lastIndexOf('[')+1));    
                                }
                                else{
                                    writerfr.println( methodklass + "	" + methodselector + "	" + instklass);
                                }
                                
                            }
                        }
                    }
                }
            }
            writerfr.close();

        } catch (WalaException e) {
        e.printStackTrace();
        }

    }
}


