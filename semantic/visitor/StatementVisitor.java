
package semantic.visitor;

import error.*;

import semantic.error.*;

import utility.*;

import semantic.visitor.detail.*;

import visitor.*;

import syntaxtree.*;

import java.util.*;

/*
* A visitor who expands every statement associated grammar node in a depth
* first fashion and performs static analysis
*/
public class StatementVisitor extends GJDepthFirst<String, Scope>
{
    private Global global;

    public StatementVisitor(Global global)
    {
        this.global = global;
    }

    public void visit(Pending pending) throws UnrecoverableError
    {
        if (pending == null)
            throw new UnrecoverableError("StatementVisitor.visit.pending is null");

        for (Pair<Scope, Node> entry : pending)
            entry.second.accept(this, entry.first);
    }

    @Override
    public String visit(NodeList n, Scope scope)
    {
        if (n.size() == 1)
            return n.elementAt(0).accept(this, scope);

        String str = "";
        
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
        {
            String current = e.nextElement().accept(this, scope);

            str += current != null ? current : "";
        }
        
        return str;
    }
  
    @Override
    public String visit(NodeListOptional n, Scope scope)
    {
        if (n.present())
        {
            if (n.size() == 1)
                return n.elementAt(0).accept(this, scope);

            String str = "";

            for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
            {
                String current = e.nextElement().accept(this, scope);

                str += current != null ? current : "";
            }

            return str;
        }
        
        return null;
    }
  
    @Override
    public String visit(NodeOptional n, Scope scope)
    {
        if (n.present())
            return n.node.accept(this, scope);
        
        return null;
    }

    @Override
    public String visit(NodeSequence n, Scope scope)
    {
        if (n.size() == 1)
            return n.elementAt(0).accept(this, scope);

        String str = "";

        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) 
        {
            String current = e.nextElement().accept(this, scope);

            str += current != null ? current : "";
        }

        return str;
    }
  
    @Override
    public String visit(NodeToken n, Scope scope) { return null; }

    @Override
    public String visit(MainClass n, Scope scope)
    {
        // f0 -> "class"
        // f1 -> Identifier()
        // f2 -> "{"
        // f3 -> "public"
        // f4 -> "static"
        // f5 -> "void"
        // f6 -> "main"
        // f7 -> "("
        // f8 -> "String"
        // f9 -> "["
        // f10 -> "]"
        // f11 -> Identifier()
        // f12 -> ")"
        // f13 -> "{"
        // f14 -> ( VarDeclaration() )*
        // f15 -> ( Statement() )*
        if (n.f15.present())
            n.f15.accept(this, scope);
        // f16 -> "}"
        // f17 -> "}"

        return null;
    }

    @Override
    public String visit(MethodDeclaration n, Scope scope)
    {
        // f0 -> "public"
        // f1 -> Type()
        // f2 -> Identifier()
        // f3 -> "("
        // f4 -> ( FormalParameterList() )?
        // f5 -> ")"
        // f6 -> "{"
        // f7 -> ( VarDeclaration() )*
        // f8 -> ( Statement() )*
        if (n.f8.present())
            n.f8.accept(this, scope);

        // f9 -> "return"
        // f10 -> Expression()
        String type1 = ((Function)scope.getLocal()).getType();
        String type2 = n.f10.accept(this, scope);

        if (!(type1.equals(type2)))
            SemanticErrorManager.register(scope, n.f10, "Cannot convert '" + type2 + "' to '" + type1 + "'");

        // f11 -> ";"
        // f12 -> "}"

        return null;
    }

    @Override
    public String visit(Statement n, Scope scope)
    {
        // f0 -> Block()
        return n.f0.accept(this, scope);
    }

    @Override
    public String visit(Block n, Scope scope)
    {
        // f0 -> "{"
        // f1 -> ( Statement() )*

        if (n.f1.present())
            return n.f1.accept(this, scope);
        // f2 -> "}"

        return null;
    }

    @Override
    public String visit(AssignmentStatement n, Scope scope)
    {
        try
        {
            // f0 -> Identifier()
            String type1 = scope.acquireVariable(n.f0.f0.toString()).getType();

            // f1 -> "="
            // f2 -> Expression()
            // f3 -> ";"
            String type2 = n.f2.accept(this, scope);

            if (!type1.equals(type2))
            {
                try
                {
                    Base base    = global.acquireClass(type1);
                    Base derived = global.acquireClass(type2);

                    if (!derived.isSubclassOf(base))
                        SemanticErrorManager.register(scope, n, "Cannot convert '" + type2 + "' to '" + type1 + "'");
                }
                catch (Exception ex)
                {
                    SemanticErrorManager.register(scope, n, "Cannot convert '" + type2 + "' to '" + type1 + "'");
                }
            }
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f0, ex.getMessage());
        }

        return null;
    }

    @Override
    public String visit(ArrayAssignmentStatement n, Scope scope)
    {
        try
        {
            // f0 -> Identifier()
            String variableType = scope.acquireVariable(n.f0.f0.toString()).getType();

            if (!variableType.equals("int[]"))
                SemanticErrorManager.register(scope, n.f0, "The type of the expression must be an array type but it resolved to '" + variableType);

            // f1 -> "["
            // f2 -> Expression()
            String indexType = n.f2.accept(this, scope);

            if (!indexType.equals("int"))
                SemanticErrorManager.register(scope, n.f2, "Cannot convert '" + indexType + "' to 'int'");

            // f3 -> "]"
            // f4 -> "="
            // f5 -> Expression()
            String expressionType = n.f5.accept(this, scope);

            if (!expressionType.equals("int"))
                SemanticErrorManager.register(scope, n.f5, "Cannot convert '" + expressionType + "' to 'int'");

            // f6 -> ";"
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f0, ex.getMessage());
        }

        return null;
    }

    @Override
    public String visit(IfStatement n, Scope scope)
    {
        // f0 -> "if"
        // f1 -> "("
        // f2 -> Expression()
        String conditionType = n.f2.accept(this, scope);

        if (!conditionType.equals("boolean"))
            SemanticErrorManager.register(scope, n.f2, "Cannot convert '" + conditionType + "' to 'boolean'");

        // f3 -> ")"
        // f4 -> Statement()
        n.f4.accept(this, scope);
        // f5 -> "else"
        // f6 -> Statement()
        n.f6.accept(this, scope);

        return null;
    }

    @Override
    public String visit(WhileStatement n, Scope scope)
    {
        // f0 -> "while"
        // f1 -> "("
        // f2 -> Expression()
        String conditionType = n.f2.accept(this, scope);

        if (!conditionType.equals("boolean"))
            SemanticErrorManager.register(scope, n.f2, "Cannot convert '" + conditionType + "' to 'boolean'");

        // f3 -> ")"
        // f4 -> Statement()
        n.f4.accept(this, scope);

        return null;
    }

    @Override
    public String visit(PrintStatement n, Scope scope)
    {
        // f0 -> "System.out.println"
        // f1 -> "("
        // f2 -> Expression()
        String expressionType = n.f2.accept(this, scope);

        if (!expressionType.equals("int") && !expressionType.equals("boolean"))
            SemanticErrorManager.register(scope, n, "System.out.println is not capable of printing an expression of '" + expressionType + "' type");

        // f3 -> ")"
        // f4 -> ";"

        return null;
    }

    @Override
    public String visit(Expression n, Scope scope)
    {
        // f0 -> AndExpression()
        String expression = n.f0.accept(this, scope);

        return expression;
    }

    @Override
    public String visit(AndExpression n, Scope scope)
    {
        // f0 -> Clause()
        String type1 = n.f0.accept(this, scope);
        if (!type1.equals("boolean"))
            SemanticErrorManager.register(scope, n.f0, "Cannot convert '" + type1 + "' to 'boolean'");

        // f1 -> "&&"
        // f2 -> Clause()
        String type2 = n.f2.accept(this, scope);
        if (!type2.equals("boolean"))
            SemanticErrorManager.register(scope, n.f2, "Cannot convert '" + type2 + "' to 'boolean'");

        return "boolean";
    }

    @Override
    public String visit(CompareExpression n, Scope scope)
    {
        // f0 -> PrimaryExpression()
        String type1 = n.f0.accept(this, scope);
        // f1 -> "<"
        // f2 -> PrimaryExpression()
        String type2 = n.f2.accept(this, scope);

        if (!type1.equals("int") && !type2.equals("int"))
            SemanticErrorManager.register(scope, n, "The operator < is undefined for the argument type(s) '" + type1 + "','" + type2 + "'");

        return "boolean";
    }

    @Override
    public String visit(PlusExpression n, Scope scope)
    {
        // f0 -> PrimaryExpression()
        String type1 = n.f0.accept(this, scope);
        // f1 -> "+"
        // f2 -> PrimaryExpression()
        String type2 = n.f2.accept(this, scope);

        if (!type1.equals("int") && !type2.equals("int"))
            SemanticErrorManager.register(scope, n, "The operator + is undefined for the argument type(s) '" + type1 + "','" + type2 + "'");

        return "int";
    }

    @Override
    public String visit(MinusExpression n, Scope scope)
    {
        // f0 -> PrimaryExpression()
        String type1 = n.f0.accept(this, scope);
        // f1 -> "-"
        // f2 -> PrimaryExpression()
        String type2 = n.f2.accept(this, scope);

        if (!type1.equals("int") && !type2.equals("int"))
            SemanticErrorManager.register(scope, n, "The operator - is undefined for the argument type(s) '" + type1 + "','" + type2 + "'");

        return "int";
    }

    @Override
    public String visit(TimesExpression n, Scope scope)
    {
        // f0 -> PrimaryExpression()
        String type1 = n.f0.accept(this, scope);
        // f1 -> "*"
        // f2 -> PrimaryExpression()
        String type2 = n.f2.accept(this, scope);

        if (!type1.equals("int") && !type2.equals("int"))
            SemanticErrorManager.register(scope, n, "The operator * is undefined for the argument type(s) '" + type1 + "','" + type2 + "'");

        return "int";
    }

    @Override
    public String visit(ArrayLookup n, Scope scope)
    {
        // f0 -> PrimaryExpression()
        String arrayType = n.f0.accept(this, scope);
        if (!arrayType.equals("int[]"))
            SemanticErrorManager.register(scope, n.f0, "The type of the expression must be an array type but it resolved to '" + arrayType + "'");

        // f1 -> "["
        // f2 -> PrimaryExpression()
        String indexType = n.f2.accept(this, scope);
        if (!indexType.equals("int"))
            SemanticErrorManager.register(scope, n.f2, "Cannot convert '" + indexType + "' to 'int'");
        // f3 -> "]"

        return "int";
    }

    @Override
    public String visit(ArrayLength n, Scope scope)
    {
        // f0 -> PrimaryExpression()
        String type = n.f0.accept(this, scope);
        if (!type.equals("int[]"))
            SemanticErrorManager.register(scope, n, "length cannot be resolved or is not a field");

        // f1 -> "."
        // f2 -> "length"

        return "int";
    }

    @Override
    public String visit(MessageSend n, Scope scope)
    {
        // f0 -> PrimaryExpression()
        String expression = n.f0.accept(this, scope);

        if (expression == "?")
            return "?";

        // f1 -> "."
        // f2 -> Identifier()
        String identifier = n.f2.f0.toString();
        // f3 -> "("
        // f4 -> ( ExpressionList() )?
        // f5 -> ")"
        String arguementTypes = n.f4.present() ? n.f4.accept(this, scope) : "";

        if (expression == "int" || expression == "boolean" || expression == "int[]")
        {
            SemanticErrorManager.register(scope, n, "Cannot invoke '" + identifier + "(" + arguementTypes + ")' on the primitive type '" + expression + "'");

            return "?";
        }

        Context context = null; Function function = null;

        try
        {
            context = global.acquireClass(expression);

            try
            {
                function = context.acquireFunction(identifier);
            }
            catch (Exception ex)
            {
                SemanticErrorManager.register(scope, n.f2, ex.getMessage());

                return "?";
            }
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f0, ex.getMessage());

            return "?";
        }

        if (!function.isApplicable(!arguementTypes.isEmpty() ? arguementTypes.split(",") : null, global))
            SemanticErrorManager.register(scope, n.f2, "The method '" + function.toString() + "' in the type '" + context.getIdentifier() + "' is not applicable for the arguments (" + arguementTypes + ")");

        return function.getType();
    }

    @Override
    public String visit(ExpressionList n, Scope scope)
    {
        // f0 -> Expression()
        String head = n.f0.accept(this, scope);
        // f1 -> ExpressionTail()
        String tail = n.f1.accept(this, scope);

        return head + tail;
    }

    @Override
    public String visit(ExpressionTail n, Scope scope)
    {
        // f0 -> ( ExpressionTerm() )*
        if (n.f0.present())
        {
            String terms = n.f0.accept(this, scope);

            return terms; 
        }

        return "";
    }

    @Override
    public String visit(ExpressionTerm n, Scope scope)
    {
        // f0 -> ","
        // f1 -> Expression()
        String term = n.f1.accept(this, scope);

        return "," + term;
    }

    @Override
    public String visit(Clause n, Scope scope)
    {
        // f0 -> NotExpression()
        return n.f0.accept(this, scope);
    }

    @Override
    public String visit(PrimaryExpression n, Scope scope)
    {
        // f0 -> IntegerLiteral()
        return n.f0.accept(this, scope);
    }

    @Override
    public String visit(IntegerLiteral n, Scope scope)
    {
        // f0 -> <INTEGER_LITERAL>
        return "int";
    }

    @Override
    public String visit(TrueLiteral n, Scope scope)
    {
        // f0 -> "true"
        return "boolean";
    }

    @Override
    public String visit(FalseLiteral n, Scope scope)
    {
        // f0 -> "false"
        return "boolean";
    }

    @Override
    public String visit(Identifier n, Scope scope)
    {
        // f0 -> <IDENTIFIER>

        try
        {
            return scope.acquireVariable(n.f0.toString()).getType();
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n, ex.getMessage());
        }

        return "?";
    }

    @Override
    public String visit(ThisExpression n, Scope scope)
    {
        // f0 -> "this"
        return scope.getOuter().getIdentifier();
    }

    @Override
    public String visit(ArrayAllocationExpression n, Scope scope)
    {
        // f0 -> "new"
        // f1 -> "int"
        // f2 -> "["
        // f3 -> Expression()
        String type = n.f3.accept(this, scope);
        if (!type.equals("int"))
            SemanticErrorManager.register(scope, n, "Cannot convert '" + type + "' to 'int'");
        // f4 -> "]"

        return "int[]";
    }

    @Override
    public String visit(AllocationExpression n, Scope scope)
    {
        // f0 -> "new"
        // f1 -> Identifier()
        return n.f1.f0.toString();
        // f2 -> "("
        // f3 -> ")"
    }

    @Override
    public String visit(NotExpression n, Scope scope)
    {
        // f0 -> "!"
        // f1 -> Clause()
        String type = n.f1.accept(this, scope);

        if (!type.equals("boolean"))
            SemanticErrorManager.register(scope, n.f1, "Cannot convert '" + type + "' to 'boolean'");

        return "boolean";
    }

    @Override
    public String visit(BracketExpression n, Scope scope)
    {
        // f0 -> "("
        // f1 -> Expression()
        return n.f1.accept(this, scope);
        // f2 -> ")"
    }
}
