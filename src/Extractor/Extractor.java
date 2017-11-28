package Extractor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.io.PrintWriter;
import java.util.Iterator;
import java.io.FileWriter;
import java.nio.file.Paths;  


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

	static FileWriter classesWriter;
	static FileWriter fieldsWriter;
	static FileWriter subclassesWriter;
	static FileWriter instantiatedWriter;
	static FileWriter methodsWriter;
	static FileWriter methodCallsWriter;
	static FileWriter paramTypesWriter;
	static FileWriter returnTypesWriter;
	static FileWriter readFieldWriter;
	static FileWriter writeFieldWriter;
	static FileWriter ifieldsWriter;



	public static void main(String args[]) throws IOException {
		try{    
			String jarfile = args[1].substring(args[1].lastIndexOf('/')+1,args[1].lastIndexOf('.'))+"-DataFlowfacts";
			System.err.println(jarfile);
			File  f = new File(jarfile);
			f.mkdir();
			String classpath = args[1];
			AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath,null);
			ClassHierarchy cha = ClassHierarchyFactory.make(scope);
			
			openFileWriters(jarfile);
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
			closeFileWriters();

		}catch (WalaException e) {
		e.printStackTrace();
		}finally{
			closeFileWriters();
		}
		
	}

	//initializes files for recording dataflow facts
	static void openFileWriters(String outputDir) throws IOException{
		
		classesWriter = new FileWriter(Paths.get(outputDir, "Classes.facts").toString());
		fieldsWriter = new FileWriter(Paths.get(outputDir, "Fields.facts").toString());
		subclassesWriter = new FileWriter(Paths.get(outputDir, "ImmediateSubclass.facts").toString());
		instantiatedWriter = new FileWriter(Paths.get(outputDir, "InstantiatedClasses.facts").toString());
		methodsWriter = new FileWriter(Paths.get(outputDir, "Methods.facts").toString());
		methodCallsWriter = new FileWriter(Paths.get(outputDir, "MethodCalls.facts").toString());
		paramTypesWriter = new FileWriter(Paths.get(outputDir, "ParamTypes.facts").toString());
		returnTypesWriter = new FileWriter(Paths.get(outputDir, "ReturnType.facts").toString());
		readFieldWriter = new FileWriter(Paths.get(outputDir, "ReadField.facts").toString());
		writeFieldWriter = new FileWriter(Paths.get(outputDir, "WriteField.facts").toString());
		ifieldsWriter = new FileWriter(Paths.get(outputDir, "InterfaceFields.facts").toString());
	}

	//closes the dataflowfact files initialized in openFileWriters()
	static void closeFileWriters() throws IOException{
		classesWriter.flush();
		classesWriter.close();
		fieldsWriter.flush();
		fieldsWriter.close();
		subclassesWriter.flush();
		subclassesWriter.close();
		instantiatedWriter.flush();
		instantiatedWriter.close();
		methodsWriter.flush();
		methodsWriter.close();
		methodCallsWriter.flush();
		methodCallsWriter.close();
		paramTypesWriter.flush();
		paramTypesWriter.close();
		returnTypesWriter.flush();
		returnTypesWriter.close();
		readFieldWriter.flush();
		readFieldWriter.close();
		writeFieldWriter.flush();
		writeFieldWriter.close();
		ifieldsWriter.close();
	}

	//record all classes in Classes.facts
	static void gatherClasses(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting Class Names...");
		
		for(IClass c:cha){
			String classname = c.getName().toString();
			classesWriter.write(classname+"\n");    
		}       
	}

	//gather fields against their classes
	static void gatherFields(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting Fields...");
		
		for(IClass c: cha){
			Collection<IField> fields = c.getAllFields();
			for(IField f : fields){
				printFields(f,c);
			}

			Collection<IClass> interfacelist = c.getAllImplementedInterfaces();
            for(IClass i : interfacelist){
                Collection<IField> ifields = i.getAllFields();
                for (IField f : ifields){
                	printIFields(f,c,i);
                    }

            }   
		}
	}

	static void printIFields(IField f, IClass c, IClass i) throws IOException{
		String classname = c.getName().toString();
		String intfname = i.getName().toString();
		String fieldsig = f.getReference().getSignature().toString();
		String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
		String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
		String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
		
		if(fieldtype.substring(0,1).equals("[")){
			ifieldsWriter.write(classname + "\t" + fieldklass + "\t" + fieldname + "\t" + "Ljava/util/Arrays\n");
			ifieldsWriter.write(classname + "\t" + fieldklass + "\t" + fieldname + "\t" +  fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
		}
		else {
			ifieldsWriter.write(classname + "\t" + fieldklass + "\t" + fieldname + "\t" + fieldtype + "\n" );
		}
	}


	//check if fieldtype is an array and print the fieldtype into Fields.facts
	static void printFields(IField f, IClass c) throws IOException{
		String classname = c.getName().toString();
		String fieldsig = f.getReference().getSignature().toString();
		String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
		String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
		String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
		
		if(fieldtype.substring(0,1).equals("[")){
			fieldsWriter.write(classname + "\t" + fieldklass + "\t" + fieldname + "\t" + "Ljava/util/Arrays\n");
			fieldsWriter.write(classname + "\t" + fieldklass + "\t" + fieldname + "\t" +  fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
		}
		else {
			fieldsWriter.write(classname + "\t" + fieldklass + "\t" + fieldname + "\t" + fieldtype + "\n" );
		}
	}

	//gather subclass information and print in ImmediateSubclass.facts
	static void gatherSubKlasses(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting SubClasses...");
		
		for(IClass c:cha){
			String classname = c.getName().toString();
			Collection<IClass> subklass = cha.getImmediateSubclasses(c);
				for(IClass s : subklass){
					subclassesWriter.write(classname + "\t" + s.getName().toString()+"\n" );
			}
		}
	}

	//for each method gather classes instantiated in the method
	static void gatherInstantiatedClasses(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting InstantiatedClasses...");
		AnalysisOptions options = new AnalysisOptions();
		IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
		int n;
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

	//check if instantiated class is an array of class and print the information in Instantiated.facts
	static void printInstantiatedClasses(SSAInstruction instruction, IMethod m) throws IOException{
		
		String methodsig = m.getSignature();
		String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
		String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
		String instklass =  ((SSANewInstruction) instruction).getConcreteType().getName().toString();   
		
		if(instklass.substring(0,1).equals("[") ){
			instantiatedWriter.write( methodklass + "\t" + methodselector + "\t" + "Ljava/util/Arrays"+"\n");
			instantiatedWriter.write( methodklass + "\t" + methodselector + "\t" +  instklass.substring(instklass.lastIndexOf('[')+1)+"\n");    
		}
		else{
			instantiatedWriter.write( methodklass + "\t" + methodselector + "\t" + instklass+"\n");
		}
	}

	//gather all methods agains the class they are applicable in
	static void gatherMethods(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting Methods...");
		for(IClass c:cha){
			Collection<IMethod> methods = c.getAllMethods();
			for(IMethod m : methods){
				printMethod(m,c);                   
			}
		}
	}

	//print the methods against the applicable classes into Methods.facts
	static void printMethod(IMethod m, IClass c) throws IOException {
		String classname = c.getName().toString();
		String methodsig = m.getSignature().toString();
		String methodklass = "L"+ methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
		String methodselector = methodsig.substring(methodsig.lastIndexOf('.') + 1 );
		methodsWriter.write(classname + "\t"  + methodklass + "\t" + methodselector +"\n");
	}

	//gather all Method Calls
	static void gatherMethodCalls(ClassHierarchy cha) throws IOException {
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

	//print method calls in MethodCalls.facts
	static void printMethodCalls(SSAInstruction instruction, IMethod m) throws IOException {
		String callermethod = m.getSignature();
		String callerklass = "L"+ callermethod.substring(0,callermethod.lastIndexOf('.')).replaceAll("\\.","/");
		String callerselector = callermethod.substring(callermethod.lastIndexOf('.')+1);
		String calledmethod = ((SSAAbstractInvokeInstruction) instruction).getDeclaredTarget().toString();
		String calleeselector = calledmethod.substring(calledmethod.lastIndexOf(',')+2,calledmethod.lastIndexOf('>')-1);
		String calleeklass_ = calledmethod.substring( calledmethod.indexOf(',')+2,calledmethod.lastIndexOf(',') );
		String calleeklass$;
		String calleeklass;
		if (calleeklass_.substring(0,1).equals("[")){
			calleeklass$ = calleeklass_.substring(calleeklass_.lastIndexOf('[')+1);
		}
		else {
			calleeklass$=calleeklass_;
		}
		
		if(calleeklass$.equals("Z")){
			calleeklass="Ljava/lang/Byte";
		}
		else if(calleeklass$.equals("B")){
			calleeklass="Ljava/lang/Boolean";
		}
		else if(calleeklass$.equals("C")){
			calleeklass="Ljava/lang/Character";
		}
		else if(calleeklass$.equals("D")){
			calleeklass="Ljava/lang/Double";
		}
		else if(calleeklass$.equals("F")){
			calleeklass="Ljava/lang/Float";
		}
		else if(calleeklass$.equals("I")){
			calleeklass="Ljava/lang/Integer";
		}
		else if(calleeklass$.equals("J")){
			calleeklass="Ljava/lang/Long";
		}
		else if(calleeklass$.equals("S")){
			calleeklass="Ljava/lang/Short";
		}
		else{
			calleeklass=calleeklass$;
		}


		methodCallsWriter.write(callerklass + "\t" + callerselector + "\t" + calleeklass + "\t" + calleeselector+"\n");
	}
	
	//gather ParameterTypes of each method   
	static void getParameterTypes(ClassHierarchy cha) throws IOException {
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

	//print Parameter types of methods into ParamTypes.facts
	static void printParameterTypes(String parameterType, IMethod m) throws IOException {
		String methodsig = m.getSignature();
		String methodklass = "L"+methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
		String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
		if(parameterType.substring(0,1).equals("[")){
			paramTypesWriter.write(methodklass + "\t" + methodselector + "\t" + "Ljava/util/Arrays"+"\n" );
			paramTypesWriter.write(methodklass + "\t" + methodselector + "\t" + parameterType.substring(parameterType.lastIndexOf('[')+1) +"\n");  
		}
		else{
			paramTypesWriter.write(methodklass + "\t" + methodselector + "\t" + parameterType+"\n");    
		}
	}

	//gather ReadField Instances in each method
	static void gatherReadFieldInstances(ClassHierarchy cha) throws IOException {
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

	//print Read Field instance information in ReadField.facts
	static void printReadFieldInstances(IMethod m, SSAInstruction instruction) throws IOException {
		String methodsig = m.getSignature();
		String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
		String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
		String fieldsig = ((SSAGetInstruction) instruction).getDeclaredField().getSignature().toString();
		String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
		String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
		String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
		if(fieldtype.substring(0,1).equals("[")){
			readFieldWriter.write(methodklass + "\t" + methodselector + "\t" + fieldklass + "\t" + fieldname + "\t" + "Ljava/util/Arrays"+"\n");
			readFieldWriter.write(methodklass + "\t" + methodselector + "\t" + fieldklass + "\t" + fieldname + "\t" + fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
		}
		else {
			readFieldWriter.write(methodklass + "\t" + methodselector + "\t" + fieldklass + "\t" + fieldname + "\t" + fieldtype+"\n");
		}
	}

	//gather ReturnType of each method
	static void getReturnTypes(ClassHierarchy cha) throws IOException {
		System.err.println("Extracting ReturnTypes...");
		for(IClass c : cha){
			Collection<IMethod> methods = c.getAllMethods();
			for(IMethod m : methods){
				String returnType = m.getReturnType().getName().toString();
				printReturnType(returnType,m);    
			}
		}    
	}

	//print Return type of methods into ReturnTypes.facts
	static void printReturnType(String returnType, IMethod m) throws IOException {
		
		String methodsig = m.getSignature();
		String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
		String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
		if(returnType.substring(0,1).equals("[")){
			returnTypesWriter.write( methodklass + "\t" + methodselector + "\t" + "Ljava/util/Arrays" +"\n");
			returnTypesWriter.write( methodklass + "\t" + methodselector + "\t" + returnType.substring(returnType.lastIndexOf('[')+1)+"\n" );
		}
		else {
			returnTypesWriter.write( methodklass + "\t" + methodselector + "\t" + returnType +"\n");
		}
	}

	//gather WriteField Instances in each method
	static void gatherWriteFieldInstances(ClassHierarchy cha) throws IOException {
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

	//print Write Field instance information in ReadField.facts
	static void printWriteFieldInstances(SSAInstruction instruction, IMethod m, IClass c) throws IOException {
		String classname = c.getName().toString();
		String methodsig = m.getSignature();
		String methodklass = "L" + methodsig.substring(0,methodsig.lastIndexOf('.') ).replaceAll("\\.","/");
		String methodselector = methodsig.substring(methodsig.lastIndexOf('.')+1);
		String fieldsig = ((SSAPutInstruction) instruction).getDeclaredField().getSignature().toString();
		String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
		String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
		String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
		if(fieldtype.substring(0,1).equals("[")){
			writeFieldWriter.write(methodklass + "\t" + methodselector + "\t" + fieldklass + "\t" + fieldname + "\t" + "Ljava/util/Arrays"+"\n");
			writeFieldWriter.write(methodklass + "\t" + methodselector + "\t" + fieldklass + "\t" + fieldname + "\t" + fieldtype.substring(fieldtype.lastIndexOf('[')+1)+"\n");
		}
		else {
		writeFieldWriter.write(methodklass + "\t" + methodselector + "\t" + fieldklass + "\t" + fieldname + "\t" + fieldtype+"\n");
		}   
	}
}

