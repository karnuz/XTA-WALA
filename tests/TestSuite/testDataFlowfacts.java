package tests.TestSuite;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.hamcrest.CoreMatchers;
import static org.junit.matchers.JUnitMatchers.*;


//import org.hamcrest.collection.IsEmptyCollection;


// import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
// import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
// import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
// import static org.hamcrest.MatcherAssert.assertThat;

public class testDataFlowfacts {

    public static List<String> readFileInList(String filename){
	List<String> lines = Collections.emptyList();
	try{
	    lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
	}
	catch(IOException e){
	    e.printStackTrace();
	}
	return lines;
    }

    
    @Test
    public void testgatherClasses() {	
	List l = readFileInList("Calculator-DataFlowfacts/Classes.facts");
	System.out.println("Inside testgatherClasses()");    
	assertThat(l, hasItems("LCalculator","LMult","LAddSub","LsumofCube","Lhypo","LdiffSub"));     
    }

    @Test
    public void testgatherFields() {
	List l = readFileInList("Calculator-DataFlowfacts/Fields.facts");
	System.out.println("Inside testgatherFields()");
	assertThat(l, hasItems("LtestFields\tLtestFields\ttestfield\tI","LtestFields\tLtestFields\tvar1\tI","LtestFields\tLtestFields\tvar2\tI","LtestFields\tLtestFields\tvar3\tI","LtestFieldssubklass\tLtestFields\ttestfield\tI","LtestFieldssubklass\tLtestFields\tvar1\tI","LtestFieldssubklass\tLtestFields\tvar2\tI","LtestFieldssubklass\tLtestFields\tvar3\tI","LtestFieldssubklass\tLtestFieldssubklass\ttestfield2\tD","LmatrixTesting\tLmatrixTesting\tdemo\tLtestFields","LmatrixTesting\tLmatrixTesting\tmatrix\tLjava/util/Arrays","LmatrixTesting\tLmatrixTesting\tmatrix\tD","LmatrixTesting\tLmatrixTesting\tmatrixtrans\tLjava/util/Arrays","LmatrixTesting\tLmatrixTesting\tmatrixtrans\tD","LAddI\tLAddI\tinterfacetestfield\tI"));
    }

    
    @Test
    public void testgatherImmediateSubklass() {	
	List l = readFileInList("Calculator-DataFlowfacts/ImmediateSubclass.facts");
	System.out.println("Inside testImmediateSubklass()");    
	assertThat(l, hasItems("LAddSub\tLMult","LMult\tLsumofCube","LsumofCube\tLhypo","LMult\tLdiffSub","LtestFields\tLtestFieldssubklass"));     
    }


    @Test
    public void testgatherInterfaceFields() {	
	List l = readFileInList("Calculator-DataFlowfacts/InterfaceFields.facts");
	System.out.println("Inside testgatherInterfaceFields()");    
	assertThat(l, hasItems("LAddSub\tLAddI\tinterfacetestfield\tI","LMult\tLAddI\tinterfacetestfield\tI","LsumofCube\tLAddI\tinterfacetestfield\tI","Lhypo\tLAddI\tinterfacetestfield\tI","LdiffSub\tLAddI\tinterfacetestfield\tI"));     
    }

    @Test
    public void testgatherMethods() {	
	List l = readFileInList("Calculator-DataFlowfacts/Methods.facts");
	System.out.println("Inside testgatherMethods()");
	assertThat(l, hasItems("LCalculator\tLCalculator\tmain([Ljava/lang/String;)V","LmatrixTesting\tLmatrixTesting\tmatrixtrans()V","LVarReadWrite\tLVarReadWrite\tadditionfix()V","LAddSub\tLAddI\taddition(II)I","LAddSub\tLAddSub\tSubtraction(II)V","LMult\tLAddSub\tSubtraction(II)V","LMult\tLAddI\taddition(II)I","LMult\tLMult\tmultiplication(II)I","LsumofCube\tLAddSub\tSubtraction(II)V","LsumofCube\tLAddI\taddition(II)I","LsumofCube\tLMult\tmultiplication(II)I","LsumofCube\tLsumofCube\tcubesum(II)V","Lhypo\tLAddSub\tSubtraction(II)V","Lhypo\tLAddI\taddition(II)I","Lhypo\tLMult\tmultiplication(II)I","Lhypo\tLsumofCube\tcubesum(II)V","Lhypo\tLhypo\thypot(II)V","LdiffSub\tLdiffSub\tSubtraction(II)V","LdiffSub\tLAddI\taddition(II)I","LdiffSub\tLMult\tmultiplication(II)I"));     
    }

}
