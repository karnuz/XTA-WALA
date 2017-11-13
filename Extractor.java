import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.io.PrintWriter;
import java.util.Iterator;
import java.io.FileWriter;  


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
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;



class Extractor{

	public static void main(String[] args) throws IOException {
		try{	
			String classpath = args[1];
			AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
			ClassHierarchy cha = ClassHierarchyFactory.make(scope);

		

			
			gatherClasses(cha);
			gatherFields(cha);
			gatherSubKlasses(cha);
			gatherInstantiatedClasses(cha);
			gatherMethods(cha);
			gatherMethodCalls(cha);
			getParameterTypes(cha);
			getReturnTypes(cha);
			gatherReadFieldInstances(cha);
			gatherWriteFieldInstances(cha);
		
		}catch (WalaException e) {
		e.printStackTrace();
		}
	}



	public static void gatherClasses(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting Class Names...");
		File file = new File("Classes.facts");	
		FileWriter writer = new FileWriter(file, true);
		for(IClass c:cha){
			String classname = c.getName().toString();
			writer.write(classname+"\n");	
		}
		writer.flush();
		writer.close();
	}

	public static void gatherFields(ClassHierarchy cha) throws IOException {
    	System.err.println("Extracting Fields...");
        File file = new File("Fields.facts");
        	
        	for(IClass c: cha){
        		Collection<IField> fields = c.getAllFields();
	            for(IField f : fields){
	                    printFields(f,c);
	            }	
        	}
    }

    public static void printFields(IField f, IClass c) throws IOException{
    	FileWriter writer = new FileWriter("Fields.facts", true);
    	String classname = c.getName().toString();
    	String fieldsig = f.getReference().getSignature().toString();
        String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
        String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
        String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
        if(fieldtype.substring(0,1).equals("[")){
            writer.write(classname + "	" + fieldklass + "	" + fieldname + "	" + "Ljava/util/Arrays\n");
            writer.write(classname + "	" + fieldklass + "	" + fieldname + "	" +  fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
        }
        else {
    	    writer.write(classname + "	" + fieldklass + "	" + fieldname + "	" + fieldtype + "\n" );
        }
        writer.flush();
        writer.close();
    }

    public static void gatherSubKlasses(ClassHierarchy cha) throws IOException {
	    System.err.println("Extracting SubClasses...");
	    	File file = new File("ImmediateSubclass.facts");
	      	FileWriter writer = new FileWriter("ImmediateSubclass.facts", true);
  			for(IClass c:cha){
  				String classname = c.getName().toString();
  				Collection<IClass> subklass = cha.getImmediateSubclasses(c);
	       		for(IClass s : subklass){
	      	  		writer.write(classname + "	" + s.getName().toString()+"\n" );
  				}
	       	}
	       	writer.flush();
	    	writer.close();      
  	}

  	public static void gatherInstantiatedClasses(ClassHierarchy cha) throws IOException {
        System.err.println("Extracting InstantiatedClasses...");
        File file = new File("InstantiatedClasses.facts");
            AnalysisOptions options = new AnalysisOptions();
            IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
             
            for(IClass c:cha){
            	Collection<IMethod> methods = c.getAllMethods();
	            for(IMethod m : methods){
	                if(cache.getIR(m, Everywhere.EVERYWHERE) != null){
	                    IR ir = cache.getIR(m , Everywhere.EVERYWHERE);
	                    Iterator<SSAInstruction> iriterator = ir.iterateAllInstructions();
	                    while(iriterator.hasNext()){
	                    	SSAInstruction currentinstruction = iriterator.next();
	                        if(currentinstruction instanceof SSANewInstruction){
	                            printInstantiatedClasses(currentinstruction, m);   
	                        }
	                    }
	                }
	            }
            }
                   
    	//} catch (WalaException e) {
        //e.printStackTrace();
        //}
    }

    public static void printInstantiatedClasses(SSAInstruction instruction, IMethod m) throws IOException{
    	FileWriter writer = new FileWriter("InstantiatedClasses.facts", true); 
    	String methodsig = m.getSignature();
        String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
        String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
    	String instklass =  ((SSANewInstruction) instruction).getConcreteType().getName().toString();  	
        if(instklass.substring(0,1).equals("[") ){
            writer.write( methodklass + "	" + methodselector + "	" + "Ljava/util/Arrays"+"\n");
            writer.write( methodklass + "	" + methodselector + "	" +  instklass.substring(instklass.lastIndexOf('[')+1)+"\n");    
        }
        else{
        writer.write( methodklass + "	" + methodselector + "	" + instklass+"\n");
        }
        writer.flush();
        writer.close();
    }

    public static void gatherMethods(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting Methods...");
			File file = new File("Methods.facts");
			for(IClass c:cha){
				Collection<IMethod> methods = c.getAllMethods();
				for(IMethod m : methods){
					printMethod(m,c);					
				}
			}
	}

	public static void printMethod(IMethod m, IClass c) throws IOException {
		FileWriter writer = new FileWriter("Methods.facts", true);
		String classname = c.getName().toString();
		String methodsig = m.getSignature().toString();
		String methodklass = "L"+ methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
		String methodselector = methodsig.substring(methodsig.lastIndexOf('.') + 1 );
		writer.write(classname + "	"  + methodklass + "	" + methodselector +"\n");
		writer.flush();
		writer.close();
	}

    public static void gatherMethodCalls(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting MethodCalls...");
		File file = new File("MethodCalls.facts");	
			AnalysisOptions options = new AnalysisOptions();
			IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
			for(IClass c:cha){
				Collection<IMethod> methods = c.getAllMethods();
				for(IMethod m : methods){
					if(cache.getIR(m, Everywhere.EVERYWHERE) != null){
						IR ir = cache.getIR(m , Everywhere.EVERYWHERE);
						Iterator<SSAInstruction> iriterator = ir.iterateAllInstructions();
						while(iriterator.hasNext()){
							SSAInstruction currentinstruction = iriterator.next();
							if(currentinstruction instanceof SSAAbstractInvokeInstruction){
								printMethodCalls(currentinstruction, m);
							}
						}
					}
				}
			}
	}

	public static void printMethodCalls(SSAInstruction instruction, IMethod m) throws IOException {
		FileWriter writer = new FileWriter("MethodCalls.facts", true);		
		String callermethod = m.getSignature();
		String callerklass = "L"+ callermethod.substring(0,callermethod.lastIndexOf('.')).replaceAll("\\.","/");
		String callerselector = callermethod.substring(callermethod.lastIndexOf('.')+1);
		String calledmethod = ((SSAAbstractInvokeInstruction) instruction).getDeclaredTarget().getSignature();
		String calleeklass = "L"+calledmethod.substring(0,calledmethod.lastIndexOf('.')).replaceAll("\\.","/");
		String calleeselector = calledmethod.substring(calledmethod.lastIndexOf('.')+1);
		writer.write(callerklass + "	" + callerselector + "	" + calleeklass + "	" + calleeselector+"\n");
		writer.flush();	
		writer.close();
	}
   
    public static void getParameterTypes(ClassHierarchy cha) throws IOException {
      	System.err.println("Extracting Parameter Types...");
    	File file = new File("ParamTypes.facts");
    		for(IClass c:cha){
    			String classname = c.getName().toString();
    			Collection<IMethod> methods = c.getAllMethods();
            	for(IMethod m : methods){
                	int z = m.getNumberOfParameters();
                	if(z!=1){
                   		for(int i=1; i<=z-1; i++){
                       		//repetition of same type of parameters. can be removed by using set data structure.
                       		String p = m.getParameterType(i).getName().toString();
                       		printParameterTypes(p, m);
                    	}
		            }
            	}
    		}
       
    }

    public static void printParameterTypes(String parameterType, IMethod m) throws IOException {
    	FileWriter writer = new FileWriter("ParamTypes.facts", true);
        String methodsig = m.getSignature();
        String methodklass = "L"+methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
        String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
        if(parameterType.substring(0,1).equals("[")){
            writer.write(methodklass + "	" + methodselector + "	" + "Ljava/util/Arrays"+"\n" );
            writer.write(methodklass + "	" + methodselector + "	" + parameterType.substring(parameterType.lastIndexOf('[')+1) +"\n");  
        }
        else{
            writer.write(methodklass + "	" + methodselector + "	" + parameterType+"\n");    
        }
        writer.flush();
      	writer.close();
    }

    public static void gatherReadFieldInstances(ClassHierarchy cha) throws IOException {
	  	System.err.println("Extracting ReadField Instances...");
	    File file = new File("ReadField.facts");
	        AnalysisOptions options = new AnalysisOptions();
	        IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
	        for(IClass c:cha){
	        	Collection<IMethod> methods = c.getAllMethods();
	            for(IMethod m : methods){
	                
	                if(cache.getIR(m, Everywhere.EVERYWHERE) != null){
	                    IR ir = cache.getIR(m , Everywhere.EVERYWHERE);
	                    Iterator<SSAInstruction> iriterator = ir.iterateAllInstructions();
	                    while(iriterator.hasNext()){
	                    SSAInstruction currentinstruction = iriterator.next();
	                        if(currentinstruction instanceof SSAGetInstruction){
	                            printReadFieldInstances(m, currentinstruction);
	                        }
	                    }
	                }
	          
	            }
	        }
  
	}

	public static void printReadFieldInstances(IMethod m, SSAInstruction instruction) throws IOException {
		FileWriter writer = new FileWriter("ReadField.facts", true);
		String methodsig = m.getSignature();
	    String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
	    String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
	    String fieldsig = ((SSAGetInstruction) instruction).getDeclaredField().getSignature().toString();
	    String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
	    String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
	    String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
	    if(fieldtype.substring(0,1).equals("[")){
	        writer.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + "Ljava/util/Arrays"+"\n");
	        writer.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
	    }
	    else {
	        writer.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + fieldtype+"\n");
	    }
	    writer.flush();
        writer.close();
	}

  	public static void getReturnTypes(ClassHierarchy cha) throws IOException {
    	System.err.println("Extracting ReturnTypes...");
        File file = new File("ReturnType.facts");
            for(IClass c : cha){
            	Collection<IMethod> methods = c.getAllMethods();
	            for(IMethod m : methods){
	                String returnType = m.getReturnType().getName().toString();
	                printReturnType(returnType,m);    
	            }
            }    
    }

    public static void printReturnType(String returnType, IMethod m) throws IOException {
    	FileWriter writer = new FileWriter("ReturnType.facts", true);
		String methodsig = m.getSignature();
        String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
        String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
        if(returnType.substring(0,1).equals("[")){
            writer.write( methodklass + "	" + methodselector + "	" + "Ljava/util/Arrays" +"\n");
            writer.write( methodklass + "	" + methodselector + "	" + returnType.substring(returnType.lastIndexOf('[')+1)+"\n" );
        }
        else {
            writer.write( methodklass + "	" + methodselector + "	" + returnType +"\n");
        }
        writer.flush();
        writer.close();
    }

    public static void gatherWriteFieldInstances(ClassHierarchy cha) throws IOException {
    	System.err.println("Extracting WriteField Instances...");
        File file = new File("WriteField.facts");
            AnalysisOptions options = new AnalysisOptions();
            IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
            for(IClass c : cha){
            	Collection<IMethod> methods = c.getAllMethods();
	            for(IMethod m : methods){
	                if(cache.getIR(m, Everywhere.EVERYWHERE) != null){
	                    IR ir = cache.getIR(m , Everywhere.EVERYWHERE);
	                    Iterator<SSAInstruction> iriterator = ir.iterateAllInstructions();
	                    while(iriterator.hasNext()){
	                        SSAInstruction currentinstruction = iriterator.next();
	                        if(currentinstruction instanceof SSAPutInstruction){
	                            printWriteFieldInstances(currentinstruction,m,c);
	                        }
	                    }
	                }
	            }
            }
    }

    public static void printWriteFieldInstances(SSAInstruction instruction, IMethod m, IClass c) throws IOException {
    	FileWriter writer = new FileWriter("WriteField.facts", true);
    	String classname = c.getName().toString();
		String methodsig = m.getSignature();
        String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
        String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
        String fieldsig = ((SSAPutInstruction) instruction).getDeclaredField().getSignature().toString();
        String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
        String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
        String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
        if(fieldtype.substring(0,1).equals("[")){
            writer.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + "Ljava/util/Arrays"+"\n");
            writer.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
        }
        else {
        writer.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + fieldtype+"\n");
        }
        writer.flush();
        writer.close();    
    }
}

