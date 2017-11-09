# Data Flow Facts for Call Graph construction Algorithms using WALA

Extraction of Data Flow Facts


To obtain dataflow fact install WALA and run following commands after making necessary changes in classpath

```
java -cp ".:/home/home/WALA/com.ibm.wala.core/bin:/home/home/WALA/com.ibm.wala.util/bin:/home/home/WALA/com.ibm.wala.shrike/bin:/home/home/WALA/com.ibm.wala.core.tests/bin" Extract classpath /home/home/XTA-WALA/Calculator.jar
```

Further, seprate dataflow facts can be otained one by one using

```
java -cp ".:/home/home/WALA/com.ibm.wala.core/bin:/home/home/WALA/com.ibm.wala.util/bin:/home/home/WALA/com.ibm.wala.shrike/bin:/home/home/WALA/com.ibm.wala.core.tests/bin" <filename> classpath /home/home/XTA-WALA/Calculator.jar
```
\**change library path as required*

Post obtaining dataflow facts, to perform XTA :
```
souffle CallGraph.dl
```

The result (reacable methods) is stored in **Reachable.csv**



## Data Flow FactFile Description



**1. ImmediateSubclass.facts**:  Enlists immediate inheritance relation
 
Format: *#Class #Subclass*


**2. Methods.facts**: Enlists methods across each class. Inherited methods have not been repeated.

Format: *#Class      #Method Class*  *#Method Name*


**3.  MethodCall.facts**: Includes Caller-Callee relation

Format: *#CallerMethodClass       #CallerMethodName       #CalleeMethodClass #CalleeMethodName*       


**4. Instantiated.facts**: Enlists instantiated classes across methods, ie. each occurance of "new()" in method M,C (method with name M defined in class C)

Format: *#MethodClass  #MethodName       #Instantiated Class*



**5. ParamTypes.facts**: For each method with parameters, the static type of parameter is listed

Format: *#MethodClass  #MethodName      #Parameter Type*


**6. ReadField.facts**: All read field operations are listed.

Format: *#ReadingMethodClass     #ReadingMethodName  #FieldObjectClass  #FieldName   #FieldType* 


**7. ReturnType.facts**
 For all methods with return, enlists the declared return type.
 
Format:
#MethodClass #MethodName       #declaredReturnType


**8. WriteField.facts**: Enlists all instances of writing of field

Format: *#WritingMethodClass  #WritingMethodName        #FieldObjectClass #FieldName  #FieldType*


**9. Classes.facts**: Enlists all classes

Format: *#ClassName*


**10. Fields.facts**: Enlists all fields against their classes

Format: *#ClassName #FieldClass #FieldName #staticFieldType*



