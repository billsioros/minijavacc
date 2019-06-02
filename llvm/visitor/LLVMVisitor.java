
package llvm.visitor;

import llvm.detail.*;

import semantic.detail.*;

import error.*;

import visitor.*;

import syntaxtree.*;

import java.util.*;

public class LLVMVisitor extends GJNoArguDepthFirst<LinkedList<Variable>>
{
    private llvm.detail.Scope scope;

    private LinkedList<Variable> asSingleton(String singleton)
    {
        return new LinkedList<Variable>(Arrays.asList(new Variable(singleton, null)));
    }

    private LinkedList<Variable> asSingleton(String type, String register)
    {
        return new LinkedList<Variable>(Arrays.asList(new Variable(type, register)));
    }

    private LinkedList<Variable> asSingleton(Variable variable)
    {
        return new LinkedList<Variable>(Arrays.asList(variable));
    }

    private Variable assertIsSingleton(LinkedList<Variable> variables)
    {
        if (variables.size() != 1)
            throw new UnrecoverableError("LinkedList contains " + variables.size() + " items instead of 1");

        return variables.getFirst();
    }

    public LLVMVisitor(Global global)
    {
        this.scope = new llvm.detail.Scope(global);
    }

    @Override
    public LinkedList<Variable> visit(NodeList n)
    {
        if (n.size() == 1)
            return n.elementAt(0).accept(this);

        LinkedList<Variable> total = new LinkedList<Variable>();

        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
        {
            LinkedList<Variable> partial = e.nextElement().accept(this);

            if (partial != null)
                total.addAll(partial);
        }

        return total;
    }

    @Override
    public LinkedList<Variable> visit(NodeListOptional n)
    {
        if (n.present())
        {
            if (n.size() == 1)
                return n.elementAt(0).accept(this);

            LinkedList<Variable> total = new LinkedList<Variable>();

            for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
            {
                LinkedList<Variable> partial = e.nextElement().accept(this);

                if (partial != null)
                    total.addAll(partial);
            }

            return total;
        }

        return null;
    }

    @Override
    public LinkedList<Variable> visit(NodeOptional n)
    {
        if (n.present())
            return n.node.accept(this);

        return null;
    }

    @Override
    public LinkedList<Variable> visit(NodeSequence n)
    {
        if (n.size() == 1)
            return n.elementAt(0).accept(this);

        LinkedList<Variable> total = new LinkedList<Variable>();

        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
        {
            LinkedList<Variable> partial = e.nextElement().accept(this);

            if (partial != null)
                total.addAll(partial);
        }

        return total;
    }

    @Override
    public LinkedList<Variable> visit(NodeToken n) { return null; }

    @Override
    public LinkedList<Variable> visit(Goal n)
    {
        n.f0.accept(this); // f0 -> MainClass()

        if (n.f1.present())
            n.f1.accept(this); // f1 -> ( TypeDeclaration() )*

        return null;
    }

    @Override
    public LinkedList<Variable> visit(MainClass n)
    {
        try
        {
            // n.f0.accept(this); f0 -> "class"
            scope.push(scope.acquireClass(n.f1.f0.toString())); // f1 -> Identifier()
             // n.f2.accept(this); f2 -> "{"
            // n.f3.accept(this); f3 -> "public"
            // n.f4.accept(this); f4 -> "static"
            // n.f5.accept(this); f5 -> "void"
            // n.f6.accept(this); f6 -> "main"
            // n.f7.accept(this); f7 -> "("
            // n.f8.accept(this); f8 -> "String"
            // n.f9.accept(this); f9 -> "["
            // n.f10.accept(this); f10 -> "]"
            // n.f11.accept(this); // f11 -> Identifier()
            // n.f12.accept(this); f12 -> ")"
            scope.push(scope.getLocal().acquireFunction("main").first);

            LLVM.emit("define i32 @main()");
            // n.f13.accept(this); f13 -> "{"
            LLVM.emit("{");

            LLVM.debug("Entering @main");

            n.f14.accept(this); // f14 -> ( VarDeclaration() )*
            n.f15.accept(this); // f15 -> ( Statement() )*

            // n.f16.accept(this); f16 -> "}"
            LLVM.debug("Exiting @main");

            LLVM.emit("ret i32 0");

            LLVM.emit("}");

            scope.pop();

            // n.f17.accept(this); f17 -> "}"
            scope.pop();

            return null;
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }

    @Override
    public LinkedList<Variable> visit(TypeDeclaration n)
    {
        return n.f0.accept(this); // f0 -> ClassDeclaration()
    }

    @Override
    public LinkedList<Variable> visit(ClassDeclaration n)
    {
        try
        {
            // n.f0.accept(this); f0 -> "class"
            Base base = scope.acquireClass(n.f1.f0.toString()); // f1 -> Identifier()

            LLVM.emit(LLVM.VTableOf(base) + " = global [" + base.functionCount() + " x i8*]");

            LLVM.emit("[");

            LLVM.emit(base);

            LLVM.emit("]");

            scope.push(base);

            // n.f2.accept(this); f2 -> "{"
            // n.f3.accept(this); // f3 -> ( VarDeclaration() )*
            n.f4.accept(this); // f4 -> ( MethodDeclaration() )*
            // n.f5.accept(this); f5 -> "}"

            scope.pop();

            return null;
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }

    @Override
    public LinkedList<Variable> visit(ClassExtendsDeclaration n)
    {
        try
        {
            // n.f0.accept(this); f0 -> "class"
            Base base = scope.acquireClass(n.f1.f0.toString()); // f1 -> Identifier()

            LLVM.emit(LLVM.VTableOf(base) + " = global [" + base.functionCount() + " x i8*]");

            LLVM.emit("[");

            LLVM.emit(base);

            LLVM.emit("]");

            scope.push(base);

            // n.f2.accept(this); f2 -> "extends"
            // n.f3.accept(this); // f3 -> Identifier()
            // n.f4.accept(this); f4 -> "{"
            // n.f5.accept(this); // f5 -> ( VarDeclaration() )*
            n.f6.accept(this); // f6 -> ( MethodDeclaration() )*
            // n.f7.accept(this); f7 -> "}"

            scope.pop();

            return null;
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }

    @Override
    public LinkedList<Variable> visit(VarDeclaration n)
    {
        String type       = LLVM.to(assertIsSingleton(n.f0.accept(this)).getType()); // f0 -> Type()
        String identifier = "%" + n.f1.f0.toString(); // f1 -> Identifier()
        // n.f2.accept(this); f2 -> ";"

        LLVM.emit(identifier + " = alloca " + type);

        return asSingleton(identifier, type + "*");
    }

    @Override
    public LinkedList<Variable> visit(MethodDeclaration n)
    {
        Context local = scope.getLocal();

        // n.f0.accept(this); f0 -> "public"
        String type       = LLVM.to(assertIsSingleton(n.f1.accept(this)).getType()); // f1 -> Type()
        String identifier = n.f2.f0.toString(); // f2 -> Identifier()

        // n.f3.accept(this); f3 -> "("
        LinkedList<Variable> parameters = null;
        if (n.f4.present())
            parameters = n.f4.accept(this); // f4 -> ( FormalParameterList() )?
        // n.f5.accept(this); f5 -> ")"

        String parametersString = "i8* %this";
        if (parameters != null)
            parametersString += LLVM.to(parameters);

        LLVM.emit("define " + type + " @" + local.getIdentifier() + "." + identifier + "(" + parametersString + ")");
        // n.f6.accept(this); f6 -> "{"
        LLVM.emit("{");

        LLVM.debug("Entering Method @" + local.getIdentifier() + "." + identifier);

        if (parameters != null)
        {
            for (Variable parameter : parameters)
            {
                String register = parameter.getIdentifier().replace(".", "");

                LLVM.emit(register + " = alloca " + parameter.getType());

                LLVM.emit("store " + parameter.getType() + " " + parameter.getIdentifier() + ", " + parameter.getType() + "* " + register);
            }
        }

        try
        {
            scope.push(local.acquireFunction(identifier).first);
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }

        if (n.f7.present())
            n.f7.accept(this); // f7 -> ( VarDeclaration() )*

        if (n.f8.present())
            n.f8.accept(this); // f8 -> ( Statement() )*

        // n.f9.accept(this); f9 -> "return"
        Variable expression = assertIsSingleton(n.f10.accept(this)); // f10 -> Expression()
        // n.f11.accept(this); f11 -> ";"

        String register = LLVM.assertMatchingType(scope, expression, type);

        LLVM.debug("Exiting Method @" + local.getIdentifier() + "." + identifier);

        LLVM.emit("ret " + type + " " + register);

        // n.f12.accept(this); f12 -> "}"
        scope.pop();

        LLVM.emit("}");

        return null;
    }

    @Override
    public LinkedList<Variable> visit(FormalParameterList n)
    {
        LinkedList<Variable> head = n.f0.accept(this); // f0 -> FormalParameter()

        LinkedList<Variable> tail = n.f1.accept(this); // f1 -> FormalParameterTail()

        if (tail == null)
            return asSingleton(assertIsSingleton(head));

        tail.addFirst(assertIsSingleton(head));

        return tail;
    }

    @Override
    public LinkedList<Variable> visit(FormalParameter n)
    {
        String type       = LLVM.to(assertIsSingleton(n.f0.accept(this)).getType()); // f0 -> Type()
        String identifier = n.f1.f0.toString(); // f1 -> Identifier()

        return asSingleton(type, "%." + identifier);
    }

    @Override
    public LinkedList<Variable> visit(FormalParameterTail n)
    {
        if (n.f0.present())
            return n.f0.accept(this); // f0 -> ( FormalParameterTerm() )*

        return null;
    }

    @Override
    public LinkedList<Variable> visit(FormalParameterTerm n)
    {
        // n.f0.accept(this); f0 -> ","
        LinkedList<Variable> parameterList = n.f1.accept(this); // f1 -> FormalParameter()

        assertIsSingleton(parameterList);

        return parameterList;
    }

    @Override
    public LinkedList<Variable> visit(Statement n)
    {
        return n.f0.accept(this); // f0 -> Block()
    }

    @Override
    public LinkedList<Variable> visit(Block n)
    {
        // n.f0.accept(this); f0 -> "{"
        if (n.f1.present())
            n.f1.accept(this); // f1 -> ( Statement() )*
        // n.f2.accept(this); f2 -> "}"

        return null;
    }

    @Override
    public LinkedList<Variable> visit(AssignmentStatement n)
    {
        try
        {
            Variable variable = scope.acquireVariable(n.f0.f0.toString()); // f0 -> Identifier()

            // n.f1.accept(this); f1 -> "="
            Variable expression = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()

            Pointer pointer = Pointer.from(variable.getType());

            String type = Pointer.raw(pointer.base.matches("(i32|i8|i1).*") ? pointer.base : "i8", pointer.degree - 1);

            String register = LLVM.assertMatchingType(scope, expression, type);

            LLVM.debug("Assigning " + type + " " + register + " to " + type + "* " + variable.getIdentifier());

            LLVM.emit("store " + type + " " + register + ", " + type + "* " + variable.getIdentifier());
            // n.f3.accept(this); f3 -> ";"

            LLVM.emit("");

            return null;
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }

    @Override
    public LinkedList<Variable> visit(ArrayAssignmentStatement n)
    {
        try
        {
            Variable array = scope.acquireVariable(n.f0.f0.toString()); // f0 -> Identifier()

            String i32Array = LLVM.assertMatchingType(scope, array, "i32*");

            Variable index = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()

            String i32Index = LLVM.assertMatchingType(scope, index, "i32");

            LLVM.debug("Indexing array '" + array.getIdentifier() + "' with '" + index.getIdentifier() + "'");

            // n.f1.accept(this); f1 -> "["
            String i32LengthPointer = LLVM.getRegister();

            LLVM.emit(i32LengthPointer + " = getelementptr i32, i32* " + i32Array + ", i32 0");

            String i32Length = LLVM.getRegister();

            LLVM.emit(i32Length + " = load i32, i32* " + i32LengthPointer);

            String i1OutOfBounds = LLVM.getRegister();

            LLVM.emit(i1OutOfBounds + " = icmp ule i32 " + i32Length + ", " + i32Index);

            String labelFalse = LLVM.getLabel(), labelTrue = LLVM.getLabel();

            LLVM.emit("br i1 " + i1OutOfBounds + ", label %" + labelTrue + ", label %" + labelFalse);

            LLVM.emit(labelTrue + ":");

            LLVM.emit("call void @throw_oob()");

            LLVM.emit("br label %" + labelFalse);

            LLVM.emit(labelFalse + ":");

            String i32AugmentedIndex = LLVM.getRegister();

            LLVM.emit(i32AugmentedIndex + " = add i32 " + i32Index + ", 1");

            String i32Pointer = LLVM.getRegister();

            LLVM.emit(i32Pointer + " = getelementptr i32, i32* " + i32Array + ", i32 " + i32AugmentedIndex);
            // n.f3.accept(this); f3 -> "]"

            // n.f4.accept(this); f4 -> "="
            Variable value = assertIsSingleton(n.f5.accept(this)); // f5 -> Expression()

            String i32Value = LLVM.assertMatchingType(scope, value, "i32");

            LLVM.debug("Assigning i32 '" + i32Value + "' to i32* '" + i32Pointer + "'");

            LLVM.emit("store i32 " + i32Value + ", i32* " + i32Pointer);
            // n.f6.accept(this); f6 -> ";"

            LLVM.emit("");

            return null;
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }

    @Override
    public LinkedList<Variable> visit(IfStatement n)
    {
        LLVM.debug("Entering If Statement");

        String labelIf = LLVM.getLabel(), labelElse = LLVM.getLabel(), labelEnd = LLVM.getLabel();

        // n.f0.accept(this); f0 -> "if"
        // n.f1.accept(this); f1 -> "("
        Variable condition = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()
        // n.f3.accept(this); f3 -> ")"

        LLVM.debug("Evaluating Condition " + condition.getIdentifier());

        String i1Condition = LLVM.assertMatchingType(scope, condition, "i1");

        LLVM.emit("br i1 " + i1Condition + ", label %" + labelIf + ", label %" + labelElse);

        LLVM.emit(labelIf + ":");

        LLVM.debug("Condition Evaluated True");

        n.f4.accept(this); // f4 -> Statement()

        LLVM.emit("br label %" + labelEnd);

        // n.f5.accept(this); f5 -> "else"

        LLVM.emit(labelElse + ":");

        LLVM.debug("Condition Evaluated False");

        n.f6.accept(this); // f6 -> Statement()

        LLVM.emit("br label %" + labelEnd);

        LLVM.emit(labelEnd + ":");

        return null;
    }

    @Override
    public LinkedList<Variable> visit(WhileStatement n)
    {
        LLVM.debug("Entering While Statement");

        String labelBeg = LLVM.getLabel(), labelNext = LLVM.getLabel(), labelEnd = LLVM.getLabel();

        LLVM.emit("br label %" + labelBeg);

        LLVM.emit(labelBeg + ":");

        // n.f0.accept(this); f0 -> "while"
        // n.f1.accept(this); f1 -> "("
        Variable condition = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()
        // n.f3.accept(this); f3 -> ")"

        LLVM.debug("Evaluating Condition " + condition.getIdentifier());

        String i1Condition = LLVM.assertMatchingType(scope, condition, "i1");

        LLVM.emit("br i1 " + i1Condition + ", label %" + labelNext + ", label %" + labelEnd);

        LLVM.emit(labelNext + ":");

        LLVM.debug("Entering While Statement Body");

        n.f4.accept(this); // f4 -> Statement()

        LLVM.emit("br label %" + labelBeg);

        LLVM.emit(labelEnd + ":");

        LLVM.debug("Exiting While Statement");

        return null;
    }

    @Override
    public LinkedList<Variable> visit(PrintStatement n)
    {
        LLVM.debug("Entering Print Statement");

        // n.f0.accept(this); f0 -> "System.out.println"
        // n.f1.accept(this); f1 -> "("
        Variable expression = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()
        // n.f3.accept(this); f3 -> ")"
        // n.f4.accept(this); f4 -> ";"

        String type       = expression.getType();
        String identifier = expression.getIdentifier();

        if (type.startsWith("i1"))
            LLVM.emit("call void @print_i1(i1 " + LLVM.assertMatchingType(scope, expression, "i1") + ")");
        else if (type.startsWith("i32"))
            LLVM.emit("call void @print_i32(i32 " + LLVM.assertMatchingType(scope, expression, "i32") + ")");
        else
            throw new UnrecoverableError("'" + identifier + "' is of type '" + type + "'");

        LLVM.debug("Exiting Print Statement");

        LLVM.emit("");

        return null;
    }

    @Override
    public LinkedList<Variable> visit(Expression n)
    {
        Variable expression = assertIsSingleton(n.f0.accept(this)); // f0 -> AndExpression()

        return asSingleton(expression.getIdentifier() == null ? scope.acquireVariable(expression.getType()) : expression);
    }

    @Override
    public LinkedList<Variable> visit(AndExpression n)
    {
        LLVM.debug("Entering And Expression");

        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i1LFlag = LLVM.assertMatchingType(scope, lhs, "i1");

        // n.f1.accept(this); f1 -> "&&"

        String labelFalse = LLVM.getLabel(), labelTrue = LLVM.getLabel(), labelEnd = LLVM.getLabel();

        LLVM.debug("Evaluating Left Hand Side Of And Expression");

        LLVM.emit("br i1 " + i1LFlag + ", label %" + labelTrue + ", label %" + labelFalse);

        LLVM.emit(labelTrue + ":");

        LLVM.debug("Evaluating Right Hand Side of And Expression");

        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i1RFlag = LLVM.assertMatchingType(scope, rhs, "i1");

        LLVM.emit("br label %" + labelEnd);

        LLVM.emit(labelFalse + ":");

        LLVM.emit("br label %" + labelEnd);

        LLVM.emit(labelEnd + ":");

        String i1Result = LLVM.getRegister();

        LLVM.emit(i1Result + " = phi i1 [" + i1LFlag + ", %" + labelFalse + "], [" + i1RFlag + ", %" + labelTrue + "]");

        LLVM.debug("Exiting And Expression");

        return asSingleton("i1", i1Result);
    }

    @Override
    public LinkedList<Variable> visit(CompareExpression n)
    {
        LLVM.debug("Entering Compare Expression");

        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32LTerm = LLVM.assertMatchingType(scope, lhs, "i32");

        // n.f1.accept(this); f1 -> "<"
        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32RTerm = LLVM.assertMatchingType(scope, rhs, "i32");

        String i32Result = LLVM.getRegister();

        LLVM.debug("Comparing i32 '" + i32LTerm + "' with i32 '" + i32RTerm + "'");

        LLVM.emit(i32Result + " = icmp slt i32 " + i32LTerm + ", " + i32RTerm);

        LLVM.debug("Exiting Compare Expression");

        return asSingleton("i1", i32Result);
    }

    @Override
    public LinkedList<Variable> visit(PlusExpression n)
    {
        LLVM.debug("Entering Plus Expression");

        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32LTerm = LLVM.assertMatchingType(scope, lhs, "i32");

        // n.f1.accept(this); f1 -> "+"
        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32RTerm = LLVM.assertMatchingType(scope, rhs, "i32");

        String i32Result = LLVM.getRegister();

        LLVM.debug("Performing Addition on i32 '" + i32LTerm + "' and i32 '" + i32RTerm +"'");

        LLVM.emit(i32Result + " = add i32 " + i32LTerm + ", " + i32RTerm);

        LLVM.debug("Exiting Plus Expression");

        return asSingleton("i32", i32Result);
    }

    @Override
    public LinkedList<Variable> visit(MinusExpression n)
    {
        LLVM.debug("Entering Minus Expression");

        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32LTerm = LLVM.assertMatchingType(scope, lhs, "i32");

        // n.f1.accept(this); f1 -> "-"
        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32RTerm = LLVM.assertMatchingType(scope, rhs, "i32");

        String i32Result = LLVM.getRegister();

        LLVM.debug("Performing Subtraction on i32 '" + i32LTerm + "' and i32 '" + i32RTerm +"'");

        LLVM.emit(i32Result + " = sub i32 " + i32LTerm + ", " + i32RTerm);

        LLVM.debug("Exiting Minus Expression");

        return asSingleton("i32", i32Result);
    }

    @Override
    public LinkedList<Variable> visit(TimesExpression n)
    {
        LLVM.debug("Entering Times Expression");

        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32LTerm = LLVM.assertMatchingType(scope, lhs, "i32");

        // n.f1.accept(this); f1 -> "*"
        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32RTerm = LLVM.assertMatchingType(scope, rhs, "i32");

        String i32Result = LLVM.getRegister();

        LLVM.debug("Performing Multiplication on i32 '" + i32LTerm + "' and i32 '" + i32RTerm +"'");

        LLVM.emit(i32Result + " = mul i32 " + i32LTerm + ", " + i32RTerm);

        LLVM.debug("Exiting Times Expression");

        return asSingleton("i32", i32Result);
    }

    @Override
    public LinkedList<Variable> visit(ArrayLookup n)
    {
        Variable array = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32Array = LLVM.assertMatchingType(scope, array, "i32*");

        // n.f1.accept(this); f1 -> "["
        Variable index = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        LLVM.debug("Indexing array '" + array.getIdentifier() + "' with '" + index.getIdentifier() + "'");

        String i32Index = LLVM.assertMatchingType(scope, index, "i32");
        // n.f3.accept(this); f3 -> "]"

        String i32LengthPointer = LLVM.getRegister();

        LLVM.emit(i32LengthPointer + " = getelementptr i32, i32* " + i32Array + ", i32 0");

        String i32Length = LLVM.getRegister();

        LLVM.emit(i32Length + " = load i32, i32* " + i32LengthPointer);

        String i1OutOfBounds = LLVM.getRegister();

        LLVM.emit(i1OutOfBounds + " = icmp ule i32 " + i32Length + ", " + i32Index);

        String labelFalse = LLVM.getLabel(), labelTrue = LLVM.getLabel();

        LLVM.emit("br i1 " + i1OutOfBounds + ", label %" + labelTrue + ", label %" + labelFalse);

        LLVM.emit(labelTrue + ":");

        LLVM.emit("call void @throw_oob()");

        LLVM.emit("br label %" + labelFalse);

        LLVM.emit(labelFalse + ":");

        String i32AugmentedIndex = LLVM.getRegister();

        LLVM.emit(i32AugmentedIndex + " = add i32 " + i32Index + ", 1");

        String i32ValuePointer = LLVM.getRegister();

        LLVM.emit(i32ValuePointer + " = getelementptr i32, i32* " + i32Array + ", i32 " + i32AugmentedIndex);

        return asSingleton("i32*", i32ValuePointer);
    }

    @Override
    public LinkedList<Variable> visit(ArrayLength n)
    {
        Variable pexpression = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        LLVM.debug("Attempting to acquire the 'length' attribute of '" + pexpression.getIdentifier() + "'");

        String i32Array = LLVM.assertMatchingType(scope, pexpression, "i32*");

        // n.f1.accept(this); f1 -> "."
        String i32LengthPointer = LLVM.getRegister();

        LLVM.emit(i32LengthPointer + " = getelementptr i32, i32* " + i32Array + ", i32 0");

        // n.f2.accept(this); f2 -> "length"
        String i32Length = LLVM.getRegister();

        LLVM.emit(i32Length + " = load i32, i32* " + i32LengthPointer);

        return asSingleton("i32", i32Length);
    }

    @Override
    public LinkedList<Variable> visit(MessageSend n)
    {
        Variable caller = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        LLVM.debug("Entering Message Send Expression on '" + caller.getIdentifier() + "'");

        String identifier = LLVM.assertMatchingType(scope, caller, "i8*");

        try
        {
            Base base = scope.getGlobal().acquireClass(Pointer.from(caller.getType()).base);

            scope.push(base);

            // n.f1.accept(this); f1 -> "."
            Function function = scope.acquireFunction(identifier + " " + n.f2.f0.toString()); // f2 -> Identifier()

            scope.pop();

            // n.f3.accept(this); f3 -> "("
            LinkedList<Variable> arguements = asSingleton("i8* ", identifier);
            if (n.f4.present())
                arguements.addAll(n.f4.accept(this)); // f4 -> ( ExpressionList() )?
            // n.f5.accept(this); f5 -> ")"

            String arguementsString = "";

            String[] argtypes = function.getArguementTypes();

            for (int i = 0; i < argtypes.length; i++)
                arguementsString += argtypes[i] + " " + LLVM.assertMatchingType(scope, arguements.get(i), argtypes[i]) + (i < argtypes.length - 1 ? ", " : "");

            String register = LLVM.getRegister();

            String type = function.getType(), llvm_type = LLVM.to(type);

            LLVM.debug("Invoking '" + function.getIdentifier() + "(" + arguementsString + ")'");

            LLVM.emit(register + " = call " + llvm_type + " " + function.getIdentifier() + "(" + arguementsString + ")");

            LLVM.debug("Exiting Message Send Expression on '" + caller.getIdentifier() + "'");

            return asSingleton(llvm_type.equals("i8*") ? type + "*" : llvm_type, register);
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }

    @Override
    public LinkedList<Variable> visit(ExpressionList n)
    {
        LinkedList<Variable> head = n.f0.accept(this); // f0 -> Expression()

        LinkedList<Variable> tail = n.f1.accept(this); // f1 -> ExpressionTail()

        if (tail == null)
            return asSingleton(assertIsSingleton(head));

        tail.addFirst(assertIsSingleton(head));

        return tail;
    }

    @Override
    public LinkedList<Variable> visit(ExpressionTail n)
    {
        if (n.f0.present())
            return n.f0.accept(this); // f0 -> ( ExpressionTerm() )*

        return null;
    }

    @Override
    public LinkedList<Variable> visit(ExpressionTerm n)
    {
        // n.f0.accept(this); f0 -> ","
        LinkedList<Variable> termList = n.f1.accept(this); // f1 -> Expression()

        assertIsSingleton(termList);

        return termList;
    }

    @Override
    public LinkedList<Variable> visit(Clause n)
    {
        return n.f0.accept(this); // f0 -> NotExpression()
    }

    @Override
    public LinkedList<Variable> visit(PrimaryExpression n)
    {
        Variable pexpression = assertIsSingleton(n.f0.accept(this)); // f0 -> IntegerLiteral()

        return asSingleton(pexpression.getIdentifier() == null ? scope.acquireVariable(pexpression.getType()) : pexpression);
    }

    @Override
    public LinkedList<Variable> visit(IntegerLiteral n)
    {
        return asSingleton("i32", n.f0.toString()); // f0 -> <INTEGER_LITERAL>
    }

    @Override
    public LinkedList<Variable> visit(TrueLiteral n)
    {
        return asSingleton("i1", "1"); // f0 -> "true"
    }

    @Override
    public LinkedList<Variable> visit(FalseLiteral n)
    {
        return asSingleton("i1", "0"); // f0 -> "false"
    }

    @Override
    public LinkedList<Variable> visit(ThisExpression n)
    {
        return asSingleton(Pointer.raw(scope.getOuter().getIdentifier(), 1), "%this"); // f0 -> "this"
    }

    @Override
    public LinkedList<Variable> visit(ArrayAllocationExpression n)
    {
        LLVM.debug("Entering an Array Allocation Expression");

        // n.f0.accept(this); f0 -> "new"
        // n.f1.accept(this); f1 -> "int"
        // n.f2.accept(this); f2 -> "["
        Variable expression = assertIsSingleton(n.f3.accept(this)); // f3 -> Expression()
        // n.f4.accept(this); f4 -> "]"

        String i32Length = LLVM.assertMatchingType(scope, expression, "i32");

        String i1OutOfBounds = LLVM.getRegister();

        String labelFalse = LLVM.getLabel(), labelTrue = LLVM.getLabel();

        LLVM.emit(i1OutOfBounds + " = icmp sle i32 " + i32Length + ", 0");

        LLVM.emit("br i1 " + i1OutOfBounds + ", label %" + labelTrue + ", label %" + labelFalse);

        LLVM.emit(labelTrue + ":");

        LLVM.emit("call void @throw_oob()");

        LLVM.emit("br label %" + labelFalse);

        LLVM.emit(labelFalse + ":");

        String i32AugmentedSize = LLVM.getRegister();

        LLVM.emit(i32AugmentedSize + " = add i32 " + i32Length + ", 1");

        String i8Array = LLVM.getRegister();

        LLVM.emit(i8Array + " = call i8* @calloc(i32 4, i32 " + i32AugmentedSize + ")");

        String i32Array = LLVM.getRegister();

        LLVM.emit(i32Array + " = bitcast i8* " + i8Array + " to i32*");

        String i32LengthPointer = LLVM.getRegister();

        LLVM.emit(i32LengthPointer + " = getelementptr i32, i32* " + i32Array + ", i32 0");

        LLVM.emit("store i32 " + i32Length + ", i32* " + i32LengthPointer);

        LLVM.debug("Exiting an Array Allocation Expression");

        return asSingleton("i32*", i32Array);
    }

    @Override
    public LinkedList<Variable> visit(AllocationExpression n)
    {
        // n.f0.accept(this); f0 -> "new"
        String identifier = n.f1.f0.toString(); // f1 -> Identifier()
        // n.f2.accept(this); f2 -> "("
        // n.f3.accept(this); f3 -> ")"

        LLVM.debug("Entering an Allocation Expression on '" + identifier + "'");

        try
        {
            Base base = scope.getGlobal().acquireClass(identifier);

            String i8Pointer = LLVM.getRegister();

            LLVM.emit(i8Pointer + " = call i8* @calloc(i32 1, i32 " + base.size() + ")");

            String i8CastedPointer = LLVM.getRegister();

            LLVM.emit(i8CastedPointer + " = bitcast i8* " + i8Pointer + " to i8***");

            String i8VTable = LLVM.getRegister();

            int size = base.functionCount();

            LLVM.emit(i8VTable + " = getelementptr [" + size + " x i8*], [" + size + " x i8*]* " + LLVM.VTableOf(base) + ", i32 0, i32 0");

            LLVM.emit("store i8** " + i8VTable + ", i8*** " + i8CastedPointer);

            LLVM.debug("Exiting an Allocation Expression on '" + identifier + "'");

            return asSingleton(Pointer.raw(identifier, 1), i8Pointer);
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }

    @Override
    public LinkedList<Variable> visit(NotExpression n)
    {
        // n.f0.accept(this); f0 -> "!"
        Variable clause = assertIsSingleton(n.f1.accept(this)); // f1 -> Clause()

        LLVM.debug("Entering a Not Expression on '" + clause.getIdentifier() + "'");

        String i1Clause = LLVM.assertMatchingType(scope, clause, "i1");

        String i1NotClause = LLVM.getRegister();

        LLVM.emit(i1NotClause + " = xor i1 1, " + i1Clause);

        LLVM.debug("Exiting a Not Expression on '" + clause.getIdentifier() + "'");

        return asSingleton("i1", i1NotClause);
    }

    @Override
    public LinkedList<Variable> visit(BracketExpression n)
    {
        // n.f0.accept(this); f0 -> "("
        return n.f1.accept(this); // f1 -> Expression()
        // n.f2.accept(this); f2 -> ")"
    }

    @Override
    public LinkedList<Variable> visit(Identifier n)
    {
        return asSingleton(n.f0.toString());
    }

    @Override
    public LinkedList<Variable> visit(Type n)
    {
        // f0 -> ArrayType()
        return n.f0.accept(this);
    }

    @Override
    public LinkedList<Variable> visit(ArrayType n)
    {
        // f0 -> "int"
        // f1 -> "["
        // f2 -> "]"

        return asSingleton("int[]");
    }

    @Override
    public LinkedList<Variable> visit(BooleanType n)
    {
        // f0 -> "boolean"

        return asSingleton("boolean");
    }

    @Override
    public LinkedList<Variable> visit(IntegerType n)
    {
        // f0 -> "int"

        return asSingleton("int");
    }
}
