# XTA Implementation using WALA for extracting DataFlow Facts


## Dependencies
### WALA
Clone and Build Wala from https://github.com/wala/WALA

There is no need to install Eclipse.


### SOUFFLE
SOUFFLE is a DATALOG variant.

For installing SOUFFLE refer https://souffle-lang.org/docs/build/


## Build

To build

```
make build
```


## Analyze

To analyze

```
make analyze JARFILE=<JARFILE> WALA_ROOTDIR=<WALA_ROOTDIR>
```
where `<JARFILE>` is the path to .JAR to be analyzed
and `<WALA_ROOTDIR>` is the path to root directory where WALA libraries are located


## Test
To run the test cases
```
make test WALA_ROOTDIR=<WALA_ROOTDIR>
```


## Facts

**1. Classes.facts** : Lists all classes

|Class Name|
|----------|

**2. ImmediateSubclass.facts**: Lists immediate inheritance relation
 
| Class Name | Immediate Subclass Name |
|------------|-------------------------|

**3. Methods.facts**: Lists methods applicable for each class. Inherited methods are repeated.

| Class | Method Class | Method Name |
|-------|--------------|-------------|

**4. MethodCall.facts**: Includes Caller-Callee relation

| Caller Method Class | Caller Method Name | Callee Method Class |  Callee Method Name |       
|-------------------|------------------|-------------------|-------------------------------|

**5. Instantiated.facts**: Lists instantiated classes across methods, ie. each occurance of "new()" in method M,C (method with name M defined in class C)

| Method Class |  Method Name | Instantiated Class |
|------------- |--------------|--------------------|


**6. Fields.facts**: Lists all fields against their classes


| ClassName | Field Class | Field Name |  Field's Static Type |
|-----------|-------------|------------|----------------------|

**7. ParamTypes.facts**: For each method with parameters, the static type of parameter is listed

| Method Class | Method Name | Parameter Type |
|--------------|-------------|----------------|


**8. ReadField.facts**: All read field operations are listed.

|Reading Method Class | Reading Method Name | Field Object Class | Field Name | Field Type |
|-------------------|-------------------|------------------|-----------|---------|

**9. ReturnType.facts**: For all methods with return, enlists the declared return type.
 
|Method Class | Method Name | Declared Return Type|
|-------------|-------------|---------------------|

**10. WriteField.facts**: Lists all instances of writing of field

|Writing Method Class | Writing Method Name  | Field Object Class | Field Name | Field Type
 ---------------------|----------------------|--------------------|----------------|-----------|







## Output
The final output are the Reachable Methods obtained on application of XTA algorithm. The test result is stored in 
`Test-Output` folder.








