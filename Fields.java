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
import com.ibm.wala.classLoader.IField;



class Fields {
    public static void main(String[] args) throws IOException {
    	System.err.println("Extracting Fields...");
        try{
            String classpath = args[1];
            AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

            // invoke WALA to build a class hierarchy
            ClassHierarchy cha = ClassHierarchyFactory.make(scope);
      
            PrintWriter writer = new PrintWriter("Fields.facts", "UTF-8");
      
            for(IClass c : cha){
                String classname = c.getName().toString();
                Collection<IField> fields = c.getAllFields();
                for(IField f : fields){
                    String fieldsig = f.getReference().getSignature().toString();
                    String fieldklass = fieldsig.substring(0,fieldsig.indexOf('.'));
                    String fieldname = fieldsig.substring(fieldsig.indexOf('.')+1,fieldsig.indexOf(' '));
                    String fieldtype = fieldsig.substring(fieldsig.lastIndexOf(' ')+1);
                    if(fieldtype.substring(0,1).equals("[")){
                        writer.println(classname + "	" + fieldklass + "	" + fieldname + "	" + "Ljava/util/Arrays");
                        writer.println(classname + "	" + fieldklass + "	" + fieldname + "	" +  fieldtype.substring(fieldtype.lastIndexOf('[')+1));
                    }
                    else {
                    //writer.println(c.getName().toString()+"   "+ w );
                    //if(classname.length() < 8){
                        writer.println(classname + "	" + fieldklass + "	" + fieldname + "	" + fieldtype );
                    }
                    
                }
            }
            writer.close();
    
      
      
        } catch (WalaException e) {
        e.printStackTrace();
        }

    }
}