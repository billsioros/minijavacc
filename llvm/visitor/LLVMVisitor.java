
package llvm.visitor;

import llvm.detail.*;

import semantic.detail.*;

import utility.*;

import error.*;

import visitor.*;

import syntaxtree.*;

import java.util.*;

public class LLVMVisitor extends GJNoArguDepthFirst<LinkedList<Variable>>
{
    private Scope scope;

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

    private String assertMatchingType(Variable variable, String expected_type)
    {
        String variable_type = variable.getType();
        String identifier    = variable.getIdentifier();

        Pointer varpointer = Pointer.from(variable_type);
        Pointer exppointer = Pointer.from(expected_type);

        if (!varpointer.base.equals(exppointer.base))
        {
            try
            {
                Base base = null, derived = null;

                if (!varpointer.base.equals("i8"))
                    derived = scope.getGlobal().acquireClass(varpointer.base);

                if (!exppointer.base.equals("i8"))
                    base = scope.getGlobal().acquireClass(exppointer.base);

                if (base != null && derived != null && !derived.isSubclassOf(base))
                    throw new Exception();
            }
            catch (Exception ignore)
            {
                throw new UnrecoverableError("'" + identifier + "' is of type '" + variable_type + "' instead of '" + expected_type + "'");
            }
        }

        if (varpointer.degree != exppointer.degree)
        {
            if (varpointer.degree != exppointer.degree + 1)
                throw new UnrecoverableError("'" + identifier + "' is of type '" + variable_type + "' instead of '" + expected_type + "'");

            String register = LLVM.getRegister();

            LLVM.emit(register + " = load " + expected_type + ", " + expected_type + "* " + identifier);

            identifier = register;
        }

        return identifier;
    }

    private Variable getVariable(String identifier)
    {
        try
        {
            Pair<Variable, Integer> pair = scope.acquireVariable(identifier);

            String type = LLVM.to(pair.first.getType()) + " *";

            identifier = "%" + pair.first.getIdentifier();

            if (pair.second >= 0)
            {
                String i8Pointer = LLVM.getRegister();

                LLVM.emit(i8Pointer + " = getelementptr i8, i8 * %.this, i32 " + pair.second);

                LLVM.emit(identifier + " = bitcast i8 * " + i8Pointer + " to " + type);
            }

            if (type.startsWith("i8"))
                type = Pointer.raw(pair.first.getType(), Pointer.from(type).degree);

            return new Variable(type, identifier);
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }

    public LLVMVisitor(Global global)
    {
        this.scope = new Scope(global);
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
            scope.push(new Function("void", "main", "? " + n.f11.f0.toString()));

            LLVM.emit("define i32 @main()");
            // n.f13.accept(this); f13 -> "{"
            LLVM.emit("{");

            n.f14.accept(this); // f14 -> ( VarDeclaration() )*
            n.f15.accept(this); // f15 -> ( Statement() )*

            // n.f16.accept(this); f16 -> "}"
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

            LLVM.emit(LLVM.to(base));

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

            LLVM.emit(LLVM.to(base));

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
        String type       = LLVM.to(assertIsSingleton(n.f1.accept(this)).getType()); // f0 -> Type()
        String identifier = "%" + n.f1.f0.toString(); // f1 -> Identifier()
        // n.f2.accept(this); f2 -> ";"

        LLVM.emit(identifier + " = alloca " + type);

        return asSingleton(identifier, type + "*");
    }

    @Override
    public LinkedList<Variable> visit(MethodDeclaration n)
    {
        String local = scope.getLocal().getIdentifier();

        // n.f0.accept(this); f0 -> "public"
        String type       = LLVM.to(assertIsSingleton(n.f1.accept(this)).getType()); // f1 -> Type()
        String identifier = n.f2.f0.toString(); // f2 -> Identifier()

        // n.f3.accept(this); f3 -> "("
        LinkedList<Variable> parameters = null;
        if (n.f4.present())
            parameters = n.f4.accept(this); // f4 -> ( FormalParameterList() )?
        // n.f5.accept(this); f5 -> ")"

        String parametersString = "i8 * %.this";
        if (parameters != null)
            parametersString += ", " + LLVM.to(parameters);

        LLVM.emit("define " + type + " @" + local + "." + identifier + "(" + parametersString + ")");
        // n.f6.accept(this); f6 -> "{"

        try
        {
            scope.push(scope.acquireFunction(identifier).first);
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }

        LLVM.emit("{");

        if (n.f7.present())
            n.f7.accept(this); // f7 -> ( VarDeclaration() )*

        if (n.f8.present())
            n.f8.accept(this); // f8 -> ( Statement() )*

        // n.f9.accept(this); f9 -> "return"
        Variable expression = assertIsSingleton(n.f10.accept(this)); // f10 -> Expression()
        // n.f11.accept(this); f11 -> ";"

        String register = assertMatchingType(expression, type);

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
        String type       = LLVM.to(assertIsSingleton(n.f1.accept(this)).getType()); // f0 -> Type()
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
            Variable variable = getVariable(n.f0.f0.toString()); // f0 -> Identifier()

            // n.f1.accept(this); f1 -> "="
            Variable expression = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()

            Pointer pointer = Pointer.from(variable.getType());

            String type = Pointer.raw(pointer.base, pointer.degree - 1);

            String register = assertMatchingType(expression, type);

            LLVM.emit("store " + type + " " + register + ", " + variable.getType() + variable.getIdentifier());
            // n.f3.accept(this); f3 -> ";"

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
            Variable array = getVariable(n.f0.f0.toString()); // f0 -> Identifier()

            String i32Array = assertMatchingType(array, "i32 *");

            // n.f1.accept(this); f1 -> "["
            Variable index = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()

            String i32Index = assertMatchingType(index, "i32");

            String i32AugmentedIndex = LLVM.getRegister();

            LLVM.emit(i32AugmentedIndex + " = add i32 " + i32Index + ", i32 1");

            String i32Pointer = LLVM.getRegister();

            LLVM.emit(i32Pointer + " = getelementptr i32, i32 * " + i32Array + ", i32 " + i32AugmentedIndex);
            // n.f3.accept(this); f3 -> "]"

            // n.f4.accept(this); f4 -> "="
            Variable value = assertIsSingleton(n.f5.accept(this)); // f5 -> Expression()

            String i32Value = assertMatchingType(value, "i32");

            LLVM.emit("store i32 " + i32Value + ", i32 * " + i32Pointer);
            // n.f6.accept(this); f6 -> ";"

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
        String labelIf = LLVM.getLabel(), labelElse = LLVM.getLabel();

        // n.f0.accept(this); f0 -> "if"
        // n.f1.accept(this); f1 -> "("
        Variable condition = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()
        // n.f3.accept(this); f3 -> ")"

        String i1Condition = assertMatchingType(condition, "i1");

        LLVM.emit("br i1 " + i1Condition + ", label %" + labelIf + ", label %" + labelElse);

        LLVM.emit(labelIf + ":");

        n.f4.accept(this); // f4 -> Statement()

        // n.f5.accept(this); f5 -> "else"

        LLVM.emit(labelElse + ":");

        n.f6.accept(this); // f6 -> Statement()

        return null;
    }

    @Override
    public LinkedList<Variable> visit(WhileStatement n)
    {
        String labelBeg = LLVM.getLabel(), labelNext = LLVM.getLabel(), labelEnd = LLVM.getLabel();

        LLVM.emit("br label %" + labelBeg);

        LLVM.emit(labelBeg + ":");

        // n.f0.accept(this); f0 -> "while"
        // n.f1.accept(this); f1 -> "("
        Variable condition = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()
        // n.f3.accept(this); f3 -> ")"

        String i1Condition = assertMatchingType(condition, "i1");

        LLVM.emit("br i1 " + i1Condition + ", label %" + labelNext + ", label %" + labelEnd);

        LLVM.emit(labelNext + ":");

        n.f4.accept(this); // f4 -> Statement()

        LLVM.emit("br label %" + labelBeg);

        LLVM.emit(labelEnd + ":");

        return null;
    }

    @Override
    public LinkedList<Variable> visit(PrintStatement n)
    {
        // n.f0.accept(this); f0 -> "System.out.println"
        // n.f1.accept(this); f1 -> "("
        Variable expression = assertIsSingleton(n.f2.accept(this)); // f2 -> Expression()
        // n.f3.accept(this); f3 -> ")"
        // n.f4.accept(this); f4 -> ";"

        String type       = expression.getType();
        String identifier = expression.getIdentifier();

        if (type.startsWith("i1"))
            LLVM.emit("call void @print_i1(i1 " + assertMatchingType(expression, "i1") + ")");
        else if (type.startsWith("i32"))
            LLVM.emit("call void @print_i32(i32 " + assertMatchingType(expression, "i32") + ")");
        else
            throw new UnrecoverableError("'" + identifier + "' is of type '" + type + "'");

        return null;
    }

    @Override
    public LinkedList<Variable> visit(Expression n)
    {
        Variable expression = assertIsSingleton(n.f0.accept(this)); // f0 -> AndExpression()

        return asSingleton(expression.getIdentifier() == null ? getVariable(expression.getType()) : expression);
    }

    @Override
    public LinkedList<Variable> visit(AndExpression n)
    {
        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i1LFlag = assertMatchingType(lhs, "i1");

        // n.f1.accept(this); f1 -> "&&"

        String label0 = LLVM.getLabel(), label1 = LLVM.getLabel();

        LLVM.emit("br i1 " + i1LFlag + ", label %" + label1 + ", label %" + label0);

        LLVM.emit(label1 + ":");

        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i1RFlag = assertMatchingType(rhs, "i1");

        LLVM.emit(label0 + ":");

        String i1Result = LLVM.getRegister();

        LLVM.emit(i1Result + " = phi i1 [" + i1LFlag + ", %" + label0 + "], [" + i1RFlag + ", %" + label1 + "]");

        return asSingleton("i1", i1Result);
    }

    @Override
    public LinkedList<Variable> visit(CompareExpression n)
    {
        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32LTerm = assertMatchingType(lhs, "i32");

        // n.f1.accept(this); f1 -> "<"
        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32RTerm = assertMatchingType(rhs, "i32");

        String i32Result = LLVM.getRegister();

        LLVM.emit(i32Result + " = icmp slt i32 " + i32LTerm + ", " + i32RTerm);

        return asSingleton("i1", i32Result);
    }

    @Override
    public LinkedList<Variable> visit(PlusExpression n)
    {
        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32LTerm = assertMatchingType(lhs, "i32");

        // n.f1.accept(this); f1 -> "+"
        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32RTerm = assertMatchingType(rhs, "i32");

        String i32Result = LLVM.getRegister();

        LLVM.emit(i32Result + " = add i32 " + i32LTerm + ", " + i32RTerm);

        return asSingleton("i32", i32Result);
    }

    @Override
    public LinkedList<Variable> visit(MinusExpression n)
    {
        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32LTerm = assertMatchingType(lhs, "i32");

        // n.f1.accept(this); f1 -> "-"
        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32RTerm = assertMatchingType(rhs, "i32");

        String i32Result = LLVM.getRegister();

        LLVM.emit(i32Result + " = sub i32 " + i32LTerm + ", " + i32RTerm);

        return asSingleton("i32", i32Result);
    }

    @Override
    public LinkedList<Variable> visit(TimesExpression n)
    {
        Variable lhs = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32LTerm = assertMatchingType(lhs, "i32");

        // n.f1.accept(this); f1 -> "*"
        Variable rhs = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32RTerm = assertMatchingType(rhs, "i32");

        String i32Result = LLVM.getRegister();

        LLVM.emit(i32Result + " = mul i32 " + i32LTerm + ", " + i32RTerm);

        return asSingleton("i32", i32Result);
    }

    @Override
    public LinkedList<Variable> visit(ArrayLookup n)
    {
        Variable array = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32Array = assertMatchingType(array, "i32 *");

        // n.f1.accept(this); f1 -> "["
        Variable index = assertIsSingleton(n.f2.accept(this)); // f2 -> PrimaryExpression()

        String i32Index = assertMatchingType(index, "i32");
        // n.f3.accept(this); f3 -> "]"

        String i32LengthPointer = LLVM.getRegister();

        LLVM.emit(i32LengthPointer + " = getelementptr i32, i32 * " + i32Array + ", i32 0");

        String i32Length = LLVM.getRegister();

        LLVM.emit(i32Length + " = load i32, i32 * " + i32LengthPointer);

        String i1OutOfBounds = LLVM.getRegister();

        LLVM.emit(i1OutOfBounds + " = icmp ule i32 " + i32Length + ", " + i32Index);

        String label0 = LLVM.getLabel(), label1 = LLVM.getLabel();

        LLVM.emit("br i1 " + i1OutOfBounds + ", label %" + label1 + ", label %" + label0);

        LLVM.emit(label1 + ":");

        LLVM.emit("call void @throw_oob()");

        LLVM.emit(label0 + ":");

        String i32AugmentedIndex = LLVM.getRegister();

        LLVM.emit(i32AugmentedIndex + " = add i32 " + i32Index + ", i32 1");

        String i32ValuePointer = LLVM.getRegister();

        LLVM.emit(i32ValuePointer + " = getelementptr i32, i32 * " + i32Array + ", i32 " + i32AugmentedIndex);

        return asSingleton("i32 *", i32ValuePointer);
    }

    @Override
    public LinkedList<Variable> visit(ArrayLength n)
    {
        Variable pexpression = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String i32Array = assertMatchingType(pexpression, "i32 *");

        // n.f1.accept(this); f1 -> "."
        String i32LengthPointer = LLVM.getRegister();

        LLVM.emit(i32LengthPointer + " = getelementptr i32, i32 * " + i32Array + ", i32 0");

        // n.f2.accept(this); f2 -> "length"
        String i32Length = LLVM.getRegister();

        LLVM.emit(i32Length + " = load i32, i32 * " + i32LengthPointer);

        return asSingleton("i32", i32Length);
    }

    @Override
    public LinkedList<Variable> visit(MessageSend n)
    {
        Variable caller = assertIsSingleton(n.f0.accept(this)); // f0 -> PrimaryExpression()

        String identifier = assertMatchingType(caller, "i8 *");

        try
        {
            Base base = scope.getGlobal().acquireClass(Pointer.from(caller.getType()).base);
            // n.f1.accept(this); f1 -> "."
            Pair<Function, Integer> pair = base.acquireFunction(n.f2.f0.toString()); // f2 -> Identifier()

            String arguements = "i8 * " + identifier;

            // n.f3.accept(this); f3 -> "("
            if (n.f4.present())
                arguements += ", " + LLVM.to(n.f4.accept(this)); // f4 -> ( ExpressionList() )?
            // n.f5.accept(this); f5 -> ")"

            String i8CastedPointer = LLVM.getRegister();

            LLVM.emit(i8CastedPointer + " = bitcast i8 * " + identifier + " to i8 ***");

            String i8LoadPointer = LLVM.getRegister();

            LLVM.emit(i8LoadPointer + " = load i8 **, i8 *** " + i8CastedPointer);

            String i8FunctionPointer = LLVM.getRegister();

            LLVM.emit(i8FunctionPointer + " = getelementptr i8 *, i8 ** " + i8LoadPointer + ", i32 " + pair.second);

            String i8LoadFunctionPointer = LLVM.getRegister();

            LLVM.emit(i8LoadFunctionPointer + " = load i8 *, i8 ** " + i8FunctionPointer);

            String functionPointer = LLVM.getRegister();

            LLVM.emit(functionPointer + " = bitcast i8 * " + i8FunctionPointer + " to " + LLVM.to(pair.first));

            String register = LLVM.getRegister();

            String type = pair.first.getType(), llvm_type = LLVM.to(type);

            LLVM.emit(register + " = call " + llvm_type + " " + functionPointer + "(" + arguements + ")");

            return asSingleton(llvm_type.equals("i8 *") ? type + " *" : llvm_type, register);
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

        return asSingleton(pexpression.getIdentifier() == null ? getVariable(pexpression.getType()) : pexpression);
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
        return asSingleton(Pointer.raw(scope.getOuter().getIdentifier(), 1), "%.this"); // f0 -> "this"
    }

    @Override
    public LinkedList<Variable> visit(ArrayAllocationExpression n)
    {
        // n.f0.accept(this); f0 -> "new"
        // n.f1.accept(this); f1 -> "int"
        // n.f2.accept(this); f2 -> "["
        Variable expression = assertIsSingleton(n.f3.accept(this)); // f3 -> Expression()
        // n.f4.accept(this); f4 -> "]"

        String i32Length = assertMatchingType(expression, "i32");

        String i1OutOfBounds = LLVM.getRegister();

        String label0 = LLVM.getLabel(), label1 = LLVM.getLabel();

        LLVM.emit(i1OutOfBounds + " = icmp sle i32 " + i32Length + ", i32 0");

        LLVM.emit("br i1 " + i1OutOfBounds + ", label %" + label1 + ", label %" + label0);

        LLVM.emit(label1 + ":");

        LLVM.emit("call void @throw_oob()");

        LLVM.emit(label0 + ":");

        String i32AugmentedSize = LLVM.getRegister();

        LLVM.emit(i32AugmentedSize + " = add i32 " + i32Length + ", i32 1");

        String i8Array = LLVM.getRegister();

        LLVM.emit(i8Array + " = call i8 * @calloc(i32 4, i32 " + i32AugmentedSize + ")");

        String i32Array = LLVM.getRegister();

        LLVM.emit(i32Array + " = bitcast i8 * " + i8Array + " to i32 *");

        String i32LengthPointer = LLVM.getRegister();

        LLVM.emit(i32LengthPointer + " = getelementptr i32, i32 * " + i32Array + ", i32 0");

        LLVM.emit("store i32 " + i32Length + ", i32 * " + i32LengthPointer);

        return asSingleton("i32 *", i32Array);
    }

    @Override
    public LinkedList<Variable> visit(AllocationExpression n)
    {
        // n.f0.accept(this); f0 -> "new"
        String identifier = n.f1.f0.toString(); // f1 -> Identifier()
        // n.f2.accept(this); f2 -> "("
        // n.f3.accept(this); f3 -> ")"

        try
        {
            Base base = scope.getGlobal().acquireClass(identifier);

            String i8Pointer = LLVM.getRegister();

            LLVM.emit(i8Pointer + " = call i8* @calloc(i32 1, i32 8)");

            String i8CastedPointer = LLVM.getRegister();

            LLVM.emit(i8CastedPointer + " = bitcast i8 * " + i8Pointer + " to i8 ***");

            String i8VTable = LLVM.getRegister();

            int size = base.functionCount();

            LLVM.emit(i8VTable + " = getelementptr [" + size + " x i8 *], [" + size + " x i8 *] * " + LLVM.VTableOf(base) + ", i32 0, i32 0");

            LLVM.emit("store i8 ** " + i8VTable + ", i8 *** " + i8CastedPointer);

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

        String i1Clause = assertMatchingType(clause, "i1");

        String i1NotClause = LLVM.getRegister();

        LLVM.emit(i1NotClause + " = xor i1 1, " + i1Clause);

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
