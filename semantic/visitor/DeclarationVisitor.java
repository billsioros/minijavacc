
package semantic.visitor;

import error.*;

import semantic.error.*;

import semantic.detail.*;

import visitor.*;

import syntaxtree.*;

import java.util.*;

/**
* A visitor who expands every declaration associated grammar node
* in a depth first fashion and feels up the symbol table of the context at hand
*/
public class DeclarationVisitor extends GJNoArguDepthFirst<String>
{
    private Scope scope;

    private Pending pending;

    public DeclarationVisitor()
    {
        super();

        this.scope = new Scope();

        this.pending = new Pending();
    }

    public Global getGlobal()
    {
        return scope.getGlobal();
    }

    public Pending getPending()
    {
        return pending;
    }

    @Override
    public String visit(NodeList n)
    {
        if (n.size() == 1)
            return n.elementAt(0).accept(this);

        String str = "";
        
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
        {
            String current = e.nextElement().accept(this);

            str += current != null ? current : "";
        }
        
        return str;
    }
  
    @Override
    public String visit(NodeListOptional n)
    {
        if (n.present())
        {
            if (n.size() == 1)
                return n.elementAt(0).accept(this);

            String str = "";

            for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
            {
                String current = e.nextElement().accept(this);

                str += current != null ? current : "";
            }

            return str;
        }
        
        return null;
    }
  
    @Override
    public String visit(NodeOptional n)
    {
        if (n.present())
            return n.node.accept(this);
        
        return null;
    }

    @Override
    public String visit(NodeSequence n)
    {
        if (n.size() == 1)
            return n.elementAt(0).accept(this);

        String str = "";

        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) 
        {
            String current = e.nextElement().accept(this);

            str += current != null ? current : "";
        }

        return str;
    }
  
    @Override
    public String visit(NodeToken n) { return null; }

    @Override
    public String visit(Goal n)
    {
        // f0 -> MainClass()
        n.f0.accept(this);

        // f1 -> ( TypeDeclaration() )*
        if (n.f1.present())
            return n.f1.accept(this);

        return "";
    }

    @Override
    public String visit(MainClass n)
    {
        // f0 -> "class"
        // f1 -> Identifier()
        try
        {
            scope.registerClass(new Base(n.f1.accept(this)));
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f1, ex.getMessage());

            return null;
        }

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
        try
        {
            scope.push(new Function("void", "main", "? " + n.f11.accept(this)));
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f6, ex.getMessage());

            try
            {
                scope.pop();
            }
            catch (EmptyStackException emptyStackException)
            {
                throw new UnrecoverableError("DeclarationVisitor.visit(MainClass).scope failed to pop()");
            }

            return null;
        }
        // f13 -> "{"

        // f14 -> ( VarDeclaration() )*
        if (n.f14.present())
            n.f14.accept(this);

        // f15 -> ( Statement() )*
        if (n.f15.present())
            pending.insert((Scope)scope.clone(), n);

        // f16 -> "}"
        scope.pop();
        // f17 -> "}"
        scope.pop();

        return null;
    }

    @Override
    public String visit(TypeDeclaration n)
    {
        // f0 -> ClassDeclaration()
        return n.f0.accept(this);
    }

    @Override
    public String visit(ClassDeclaration n)
    {
        // f0 -> "class"
        // f1 -> Identifier()
        try
        {
            scope.registerClass(new Base(n.f1.accept(this)));
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f1, ex.getMessage());

            return "";
        }
        // f2 -> "{"
        // f3 -> ( VarDeclaration() )*
        if (n.f3.present())
            n.f3.accept(this);

        // f4 -> ( MethodDeclaration() )*
        if (n.f4.present())
            n.f4.accept(this);

        // f5 -> "}"
        return scope.pop().toString();
    }

    @Override
    public String visit(ClassExtendsDeclaration n)
    {
        // f0 -> "class"
        // f1 -> Identifier()
        String identifier = n.f1.accept(this);
        // f2 -> "extends"
        // f3 -> Identifier()
        try
        {
            Base base = scope.acquireClass(n.f3.accept(this));

            try
            {
                scope.registerClass(new Derived(identifier, base));
            }
            catch (Exception ex)
            {
                SemanticErrorManager.register(scope, n.f1, ex.getMessage());

                return "";
            }
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f3, ex.getMessage());

            return "";
        }

        // f4 -> "{"
        // f5 -> ( VarDeclaration() )*
        if (n.f5.present())
            n.f5.accept(this);

        // f6 -> ( MethodDeclaration() )*
        if (n.f6.present())
            n.f6.accept(this);

        // f7 -> "}"
        return scope.pop().toString();
    }

    @Override
    public String visit(VarDeclaration n)
    {
        // f0 -> Type()
        String type       = n.f0.accept(this);
        // f1 -> Identifier()
        String identifier = n.f1.accept(this);

        try
        {
            scope.registerVariable(new Variable(type, identifier));
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f1, ex.getMessage());

            return null;
        }

        // f2 -> ";"

        return null;
    }

    @Override
    public String visit(MethodDeclaration n)
    {
        // f0 -> "public"
        // f1 -> Type()
        String type       = n.f1.accept(this);
        // f2 -> Identifier()
        String identifier = n.f2.accept(this);
        // f3 -> "("

        // f4 -> ( FormalParameterList() )?
        String arguements = n.f4.present() ? n.f4.accept(this) : null;

        try
        {
            scope.registerFunction(new Function(type, identifier, arguements));
        }
        catch (Exception ex)
        {
            SemanticErrorManager.register(scope, n.f2, ex.getMessage());

            return null;
        }

        // f5 -> ")"
        // f6 -> "{"
        // f7 -> ( VarDeclaration() )*
        if (n.f7.present())
            n.f7.accept(this);

        // f8 -> ( Statement() )*
        // f9 -> "return"
        // f10 -> Expression()
        pending.insert((Scope)scope.clone(), n);
        
        // f11 -> ";"
        // f12 -> "}"
        scope.pop();

        return null;
    }

    @Override
    public String visit(FormalParameterList n)
    {
        // f0 -> FormalParameter()
        String head = n.f0.accept(this);
        // f1 -> FormalParameterTail()
        String tail = n.f1.accept(this);

        return head + tail;
    }

    @Override
    public String visit(FormalParameter n)
    {
        // f0 -> Type()
        String type       = n.f0.accept(this);
        // f1 -> Identifier()
        String identifier = n.f1.accept(this);

        return type + " " + identifier;
    }

    @Override
    public String visit(FormalParameterTail n)
    {
        // f0 -> ( FormalParameterTerm() )*
        if (n.f0.present())
            return n.f0.accept(this);

        return "";
    }

    @Override
    public String visit(FormalParameterTerm n)
    {
        // f0 -> ","
        // f1 -> FormalParameter()
        return ", " + n.f1.accept(this);
    }

    @Override
    public String visit(Type n)
    {
        // f0 -> ArrayType()
        return n.f0.accept(this);
    }

    @Override
    public String visit(ArrayType n)
    {
        // f0 -> "int"
        // f1 -> "["
        // f2 -> "]"

        return "int[]";
    }

    @Override
    public String visit(BooleanType n)
    {
        // f0 -> "boolean"

        return "boolean";
    }

    @Override
    public String visit(IntegerType n)
    {
        // f0 -> "int"

        return "int";
    }

    @Override
    public String visit(Identifier n)
    {
        // f0 -> <IDENTIFIER>
        return n.f0.toString();
    }
}
