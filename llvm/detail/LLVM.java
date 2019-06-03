
package llvm.detail;

import semantic.detail.*;

import error.*;

import utility.*;

import java.util.*;

public class LLVM
{
    private static int labelCount = 0, variableCount = 0;

    public static String getLabel()
    {
        return "LABEL_" + labelCount++;
    }

    public static String getRegister()
    {
        return "%_" + variableCount++;
    }

    public static String to(String type)
    {
        if (type.matches("(i32|i8|i1).*"))
            return type;

        switch (type)
        {
            case "int":     type = "i32";   break;
            case "int[]":   type = "i32*"; break;
            case "boolean": type = "i1";    break;
            default:        type = "i8*";  break;
        }

        return type;
    }

    public static String to(String[] types)
    {
        String llvm = "i8*";

        if (types != null)
            for (String type : types)
                llvm += ", " + to(type);

        return llvm;
    }

    public static String to(LinkedList<Variable> variables)
    {
        String llvm = "";

        if (variables != null)
            for (Variable variable : variables)
                llvm += ", " + to(variable.getType()) + " " + variable.getIdentifier();

        return llvm;
    }

    public static String to(Function function)
    {
        String type       = function.getType();

        String[] argtypes = function.getArguementTypes();

        return String.format("%s (%s)*", to(type), to(argtypes));
    }

    public static String VTableOf(Base base)
    {
        return "@" + base.getIdentifier() + ".VTable";
    }

    public static void open(String filename)
    {
        Emitter.create(filename);
    }

    public static void close()
    {
        Emitter.destroy();
    }

    public static void emit(String llvm)
    {
        Emitter.emit(llvm);
    }

    public static void emit(Base base)
    {
        Collection<Pair<String, Function>> pairs = base.getFunctions().values();

        LLVM.emit(LLVM.VTableOf(base) + " = global [" + pairs.size() + " x i8*]");

        LLVM.emit("[");

        int count = 0;

        for (Pair<String, Function> pair : pairs)
        {
            String identifier = pair.second.getIdentifier();
            String type       = pair.second.getType();

            String[] argtypes = pair.second.getArguementTypes();

            LLVM.emit(String.format("i8* bitcast (%s (%s)* %s to i8*)" + (++count < pairs.size() ? "," : ""), to(type), to(argtypes), "@" + pair.first + "." + identifier));
        }

        LLVM.emit("]");
    }

    public static String assertMatchingType(Scope scope, Variable variable, String expected_type)
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

    public static void debug(String message)
    {
        Emitter.debug(message);
    }

    public static void comment(String message)
    {
        Emitter.comment(message);
    }
}
