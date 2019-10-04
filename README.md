
# A Compiler for a subset of Java (aka Minijava)

* *MiniJava* is fully object-oriented, like Java. It does not allow global functions, only classes, fields and methods. The basic types are int, boolean, and int [] which is an array of int. You can build classes that contain fields of these basic types or of other classes. Classes contain methods with arguments of basic or class types, etc.
* *MiniJava* supports single inheritance but not interfaces. It does not support function overloading, which means that each method name must be unique. In addition, all methods are inherently polymorphic (i.e., “virtual” in C++ terminology). This means that foo can be defined in a subclass if it has the same return type and argument types (ordered) as in the parent, but it is an error if it exists with other argument types or return type in the parent. Also all methods must have a return type--there are no void methods. Fields in the base and derived class are allowed to have the same names, and are essentially different fields.
* All *MiniJava* methods are _public_ and all fields _protected_. A class method cannot access fields of another class, with the exception of its superclasses. Methods are visible, however. A class's own methods can be called via _this_. E.g., this.foo(5) calls the object's own foo method, a.foo(5) calls the foo method of object a. Local variables are defined only at the beginning of a method. A name cannot be repeated in local variables (of the same method) and cannot be repeated in fields (of the same class). A local variable x shadows a field x of the surrounding class.
* *MiniJava* constructors and destructors are not defined. The new operator calls a default void constructor. In addition, there are no inner classes and there are no static methods or fields. By exception, the pseudo-static method _main_ is handled specially in the grammar. A *MiniJava* program is a file that begins with a special class that contains the main method and specific arguments that are not used. The special class has no fields. After it, other classes are defined that can have fields and methods.
Notably, an A class can contain a field of type B, where B is defined later in the file. But when we have "class B extends A”, A must be defined before B. As you'll notice in the grammar, *MiniJava* offers very simple ways to construct expressions and only allows _<_ comparisons. There are no lists of operations, e.g., 1 + 2 + 3, but a method call on one object may be used as an argument for another method call. In terms of logical operators, *MiniJava* allows the logical and (_&&_) and the logical not (_!_). For int arrays, the _=_ and _[]_ operators are allowed, as well as the _a.length_ expression, which returns the size of array a. We have _while_ and _if_ code blocks. The latter are always followed by an _else_. Finally, the assignment _"A a = new B();"_ when B extends A is correct, and the same applies when a method expects a parameter of type A and a B instance is given instead.
* The *MiniJava* grammar in BNF form can be found [here](http://cgi.di.uoa.gr/~thp06/project_files/minijava-new/minijava.html).


## Successful Translation

```bash
java Main ./examples/positive/subclass-rv.java
```

The following java program is semantically correct and thus compiles successfully

```java
class SubclassReturnValue
{
    public static void main(String[] args)
    {
        B b;
        A a;

        b = new B();

        a = b.foo();
    }
}

class A
{

}

class B extends A
{
    public A foo()
    {
        return new B();
    }
}
```

The resulting llvm code is the following

```llvm
declare i8* @calloc(i32, i32)

declare i32 @printf(i8*, ...)

declare void @exit(i32)


@_i32fmt = constant [4 x i8] c"%d\0a\00"

@_true  = constant [6 x i8] c"true\0a\00"

@_false = constant [7 x i8] c"false\0a\00"

@_oob_exception = constant [73 x i8] c"ArrayIndexOutOfBoundsException:%d: Index %d out of bounds for length %d\0a\00"

@_alc_exception = constant [35 x i8] c"NegativeArraySizeException:%d: %d\0a\00"

@_errors = global [2 x i8*]
[
	i8* bitcast ([73 x i8]* @_oob_exception to i8*),
	i8* bitcast ([35 x i8]* @_alc_exception to i8*)
]


define void @print_i32(i32 %var)
{
	%_str = bitcast [4 x i8]* @_i32fmt to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %var)

	ret void
}


define void @print_i1(i1 %var)
{
	br i1 %var, label %isTrue, label %isFalse

isTrue:

	%_true_str = bitcast [6 x i8]* @_true to i8*
	br label %isDone

isFalse:

	%_false_str = bitcast [7 x i8]* @_false to i8*
	br label %isDone

isDone:

	%_str = phi i8* [%_true_str, %isTrue], [%_false_str, %isFalse]
	call i32 (i8*, ...) @printf(i8* %_str)

	ret void
}


define void @throw(i32 %errno, i32 %line, i32 %index, i32 %length)
{
	%_str_ptr = getelementptr i8*, i8** bitcast ([2 x i8*]* @_errors to i8**), i32 %errno
	%_str = load i8*, i8** %_str_ptr
	call i32 (i8*, ...) @printf(i8* %_str, i32 %line, i32 %index, i32 %length)
	call void @exit(i32 1)

	ret void
}


define i32 @main()
{
	%b = alloca i8*
	%a = alloca i8*
	; Acquired Local Variable 'b'
	%_0 = call i8* @calloc(i32 1, i32 8)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @B.VTable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	store i8* %_0, i8** %b

	; Acquired Local Variable 'a'
	; Acquired Local Variable 'b'
	%_3 = load i8*, i8** %b
	; Acquired function 'foo' of 'B'
	%_4 = bitcast i8* %_3 to i8***
	%_5 = load i8**, i8*** %_4
	%_6 = getelementptr i8*, i8** %_5, i32 0
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i8* (i8*)*
	%_9 = call i8* %_8(i8* %_3)
	store i8* %_9, i8** %a


	ret i32 0
}


@A.VTable = global [0 x i8*]
[
]


@B.VTable = global [1 x i8*]
[
	i8* bitcast (i8* (i8*)* @B.foo to i8*)
]


define i8* @B.foo(i8* %this)
{
	%_10 = call i8* @calloc(i32 1, i32 8)
	%_11 = bitcast i8* %_10 to i8***
	%_12 = getelementptr [1 x i8*], [1 x i8*]* @B.VTable, i32 0, i32 0
	store i8** %_12, i8*** %_11

	ret i8* %_10
}
```

## Unsuccessful Translation

```bash
java Main ./examples/negative/LinearSearch-error.java
```

The following java program is semantically incorrect and thus fails to compile

```java
class BoolPlusInt
{
    public static void main(String[] args)
    {
        int result;

        result = 10 + false;
    }
}
```

The resulting error message(s) are the following

    bool-plus-int.java:BoolPlusInt.main:7:18-27: The operator + is undefined for the argument type(s) 'int','boolean'

    1 errors


## References

* The *MiniJava* grammar in JavaCC form can be found [here](http://cgi.di.uoa.gr/~thp06/project_files/minijava-new/minijava.jj)
* [Java Compiler Compiler](https://javacc.org/) is the most popular parser generator for use with Java™ applications. A parser generator is a tool that reads a grammar specification and converts it to a Java program that can recognize matches to the grammar. In addition to the parser generator itself, JavaCC provides other standard capabilities related to parser generation such as tree building (via a tool called JJTree included with JavaCC), actions, debugging, etc.
* [JTB](http://compilers.cs.ucla.edu/jtb/) is a syntax tree builder to be used with the Java Compiler Compiler (JavaCC) parser generator.  It takes a plain JavaCC grammar file as input and automatically generates the following:
  1. A set of syntax tree classes based on the productions in the grammar, utilizing the Visitor design pattern.
  2. Two interfaces: Visitor and GJVisitor.  Two depth-first visitors: DepthFirstVisitor and GJDepthFirst, whose default methods simply visit the children of the current node.
  3. A JavaCC grammar jtb.out.jj with the proper annotations to build the syntax tree during parsing.
* [LLVM](https://llvm.org/docs/LangRef.html#introduction) is designed to be used in three different forms: as an in-memory compiler IR, as an on-disk bitcode representation (suitable for fast loading by a Just-In-Time compiler), and as a human readable assembly language representation. This allows LLVM to provide a powerful intermediate representation for efficient compiler transformations and analysis, while providing a natural means to debug and visualize the transformations.

