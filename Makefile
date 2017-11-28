WALA_CP=$(WALA_ROOTDIR)/com.ibm.wala.core/bin:$(WALA_ROOTDIR)/com.ibm.wala.util/bin:$(WALA_ROOTDIR)/com.ibm.wala.shrike/bin:$(WALA_ROOTDIR)/com.ibm.wala.core.tests/bin


build:
		mkdir -p bin
		javac -cp "$(WALA_CP):bin/" src/Extractor/*.java -d bin/

test: build
		mkdir -p Test-Output
		java -cp "$(WALA_CP):bin/" Extractor.Extractor classpath tests/Calculator.jar
		souffle src/CallGraph.dl -F temp/ -D Test-Output
		

analyze: build
		mkdir -p Test-Output
		java -cp "$(WALA_CP):bin/Extractor/" Extractor classpath $(JARFILE)
		souffle src/CallGraph.dl -F temp/ -D Test-Output

.phony: build test analyze
