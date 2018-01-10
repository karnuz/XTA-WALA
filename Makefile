WALA_CP=$(WALA_ROOTDIR)/com.ibm.wala.core/target/classes:$(WALA_ROOTDIR)/com.ibm.wala.util/target/classes:$(WALA_ROOTDIR)/com.ibm.wala.shrike/target/classes:$(WALA_ROOTDIR)/com.ibm.wala.core.tests/target/classes


build:
		mkdir -p bin
		javac -cp "$(WALA_CP):bin/" src/Extractor/*.java -d bin/

test: build
		mkdir -p Test-Output
		java -cp "$(WALA_CP):bin/" Extractor.Extractor classpath tests/Calculator.jar
		souffle src/CallGraph.dl -F Calculator-DataFlowfacts/ -D Test-Output

analyze: build
		mkdir -p Test-Output
		java -cp "$(WALA_CP):bin/" Extractor.Extractor classpath $(JARFILE)
		souffle src/CallGraph.dl -F DataFlowFacts/ -D Test-Output

testsuite: test
		javac -cp .:/home/karnuz/XTA-WALA/Junit tests/TestSuite/testDataFlowfacts.java
		javac -cp .:/home/karnuz/XTA-WALA/Junit tests/TestSuite/TestSuite.java
		javac -cp .:/home/karnuz/XTA-WALA/Junit tests/TestSuite/TestRunner.java 
		java -cp ".:/home/karnuz/XTA-WALA/Junit" tests.TestSuite.TestRunner

.phony: build test analyze
