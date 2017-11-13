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

	static FileWriter ClassesFile;
	static FileWriter FieldsFile;
	static FileWriter SubclassesFile;
	static FileWriter InstantiatedFile;
	static FileWriter MethodsFile;
	static FileWriter MethodCallsFile;
	static FileWriter ParamTypesFile;
	static FileWriter ReturnTypesFile;
	static FileWriter ReadFieldFile;
	static FileWriter WriteFieldFile;

	public static void main(String[] args) throws IOException {
		try{	
			String classpath = args[1];
			AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
			ClassHierarchy cha = ClassHierarchyFactory.make(scope);
			
			openFileHandles();
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
			closeFieldHandles();
		
		}catch (WalaException e) {
		e.printStackTrace();
		}
	}

	public static void openFileHandles() throws IOException{
		if(ClassesFile == null){
			ClassesFile = new FileWriter("Classes.facts");
		}
		if(FieldsFile == null){
			FieldsFile = new FileWriter("Fields.facts");
		}
		if(SubclassesFile == null){
			SubclassesFile = new FileWriter("ImmediateSubclass.facts");
		}
		if(InstantiatedFile == null){
			InstantiatedFile = new FileWriter("InstantiatedClasses.facts");
		}
		if(MethodsFile == null){
			MethodsFile = new FileWriter("Methods.facts");
		}
		if(MethodCallsFile == null){
			MethodCallsFile = new FileWriter("MethodCalls.facts");
		}
		if(ParamTypesFile == null){
			ParamTypesFile = new FileWriter("ParamTypes.facts");
		}
		if(ReturnTypesFile == null){
			ReturnTypesFile = new FileWriter("ReturnType.facts");
		}
		if(ReadFieldFile == null){
			ReadFieldFile = new FileWriter("ReadField.facts");
		}
		if(WriteFieldFile == null){
			WriteFieldFile = new FileWriter("WriteField.facts");
		}
	}

	public static void closeFieldHandles() throws IOException{
		ClassesFile.flush();
		ClassesFile.close();
		FieldsFile.flush();
		FieldsFile.close();
		SubclassesFile.flush();
		SubclassesFile.close();
		InstantiatedFile.flush();
		InstantiatedFile.close();
		MethodsFile.flush();
		MethodsFile.close();
		MethodCallsFile.flush();
		MethodCallsFile.close();
		ParamTypesFile.flush();
		ParamTypesFile.close();
		ReturnTypesFile.flush();
		ReturnTypesFile.close();
		ReadFieldFile.flush();
		ReadFieldFile.close();
		WriteFieldFile.flush();
		WriteFieldFile.close();
	}

	public static void gatherClasses(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting Class Names...");
		
		
		for(IClass c:cha){
			String classname = c.getName().toString();
			ClassesFile.write(classname+"\n");	
		}
		
	}

	public static void gatherFields(ClassHierarchy cha) throws IOException {
    	System.err.println("Extracting Fields...");
       	for(IClass c: cha){
        	Collection<IField> fields = c.getAllFields();
	        for(IField f : fields){
	            printFields(f,c);
	        }	
        }
    }

    public static void printFields(IField f, IClass c) throws IOException{
    	
    	String classname = c.getName().toString();
    	String fieldsig = f.getReference().getSignature().toString();
        String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
        String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
        String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
        if(fieldtype.substring(0,1).equals("[")){
            FieldsFile.write(classname + "	" + fieldklass + "	" + fieldname + "	" + "Ljava/util/Arrays\n");
            FieldsFile.write(classname + "	" + fieldklass + "	" + fieldname + "	" +  fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
        }
        else {
    	    FieldsFile.write(classname + "	" + fieldklass + "	" + fieldname + "	" + fieldtype + "\n" );
        }
    }

    public static void gatherSubKlasses(ClassHierarchy cha) throws IOException {
	    System.err.println("Extracting SubClasses...");
	   		for(IClass c:cha){
  			String classname = c.getName().toString();
  			Collection<IClass> subklass = cha.getImmediateSubclasses(c);
	   		for(IClass s : subklass){
	      		SubclassesFile.write(classname + "	" + s.getName().toString()+"\n" );
  			}
	   	}
  	}

  	public static void gatherInstantiatedClasses(ClassHierarchy cha) throws IOException {
        System.err.println("Extracting InstantiatedClasses...");
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
    }

    public static void printInstantiatedClasses(SSAInstruction instruction, IMethod m) throws IOException{
    	
    	String methodsig = m.getSignature();
        String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
        String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
    	String instklass =  ((SSANewInstruction) instruction).getConcreteType().getName().toString();  	
        if(instklass.substring(0,1).equals("[") ){
            InstantiatedFile.write( methodklass + "	" + methodselector + "	" + "Ljava/util/Arrays"+"\n");
            InstantiatedFile.write( methodklass + "	" + methodselector + "	" +  instklass.substring(instklass.lastIndexOf('[')+1)+"\n");    
        }
        else{
        	InstantiatedFile.write( methodklass + "	" + methodselector + "	" + instklass+"\n");
        }
    }

    public static void gatherMethods(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting Methods...");
			//File file = new File("Methods.facts");
			for(IClass c:cha){
				Collection<IMethod> methods = c.getAllMethods();
				for(IMethod m : methods){
					printMethod(m,c);					
				}
			}
	}

	public static void printMethod(IMethod m, IClass c) throws IOException {
		String classname = c.getName().toString();
		String methodsig = m.getSignature().toString();
		String methodklass = "L"+ methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
		String methodselector = methodsig.substring(methodsig.lastIndexOf('.') + 1 );
		MethodsFile.write(classname + "	"  + methodklass + "	" + methodselector +"\n");
		
	}

    public static void gatherMethodCalls(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting MethodCalls...");
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
		String callermethod = m.getSignature();
		String callerklass = "L"+ callermethod.substring(0,callermethod.lastIndexOf('.')).replaceAll("\\.","/");
		String callerselector = callermethod.substring(callermethod.lastIndexOf('.')+1);
		String calledmethod = ((SSAAbstractInvokeInstruction) instruction).getDeclaredTarget().getSignature();
		String calleeklass = "L"+calledmethod.substring(0,calledmethod.lastIndexOf('.')).replaceAll("\\.","/");
		String calleeselector = calledmethod.substring(calledmethod.lastIndexOf('.')+1);
		MethodCallsFile.write(callerklass + "	" + callerselector + "	" + calleeklass + "	" + calleeselector+"\n");
	}
   
    public static void getParameterTypes(ClassHierarchy cha) throws IOException {
      	System.err.println("Extracting Parameter Types...");
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
        String methodsig = m.getSignature();
        String methodklass = "L"+methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
        String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
        if(parameterType.substring(0,1).equals("[")){
            ParamTypesFile.write(methodklass + "	" + methodselector + "	" + "Ljava/util/Arrays"+"\n" );
            ParamTypesFile.write(methodklass + "	" + methodselector + "	" + parameterType.substring(parameterType.lastIndexOf('[')+1) +"\n");  
        }
        else{
            ParamTypesFile.write(methodklass + "	" + methodselector + "	" + parameterType+"\n");    
        }
    }

    public static void gatherReadFieldInstances(ClassHierarchy cha) throws IOException {
	  	System.err.println("Extracting ReadField Instances...");
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
		String methodsig = m.getSignature();
	    String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
	    String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
	    String fieldsig = ((SSAGetInstruction) instruction).getDeclaredField().getSignature().toString();
	    String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
	    String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
	    String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
	    if(fieldtype.substring(0,1).equals("[")){
	        ReadFieldFile.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + "Ljava/util/Arrays"+"\n");
	        ReadFieldFile.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
	    }
	    else {
	        ReadFieldFile.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + fieldtype+"\n");
	    }
	}

  	public static void getReturnTypes(ClassHierarchy cha) throws IOException {
    	System.err.println("Extracting ReturnTypes...");
        for(IClass c : cha){
        	Collection<IMethod> methods = c.getAllMethods();
	        for(IMethod m : methods){
	            String returnType = m.getReturnType().getName().toString();
	            printReturnType(returnType,m);    
	        }
        }    
    }

    public static void printReturnType(String returnType, IMethod m) throws IOException {
    	
		String methodsig = m.getSignature();
        String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
        String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
        if(returnType.substring(0,1).equals("[")){
            ReturnTypesFile.write( methodklass + "	" + methodselector + "	" + "Ljava/util/Arrays" +"\n");
            ReturnTypesFile.write( methodklass + "	" + methodselector + "	" + returnType.substring(returnType.lastIndexOf('[')+1)+"\n" );
        }
        else {
            ReturnTypesFile.write( methodklass + "	" + methodselector + "	" + returnType +"\n");
        }
    }

    public static void gatherWriteFieldInstances(ClassHierarchy cha) throws IOException {
    	System.err.println("Extracting WriteField Instances...");
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
    	String classname = c.getName().toString();
		String methodsig = m.getSignature();
        String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
        String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
        String fieldsig = ((SSAPutInstruction) instruction).getDeclaredField().getSignature().toString();
        String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
        String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
        String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
        if(fieldtype.substring(0,1).equals("[")){
            WriteFieldFile.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + "Ljava/util/Arrays"+"\n");
            WriteFieldFile.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
        }
        else {
        WriteFieldFile.write(methodklass + "	" + methodselector + "	" + fieldklass + "	" + fieldname + "	" + fieldtype+"\n");
        }   
    }
}

