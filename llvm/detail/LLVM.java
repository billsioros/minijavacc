
package llvm.detail;

import semantic.detail.*;

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
        String className = base.getIdentifier();

        LinkedList<Function> functions = base.getFunctions();

        LLVM.emit(VTableOf(base) + " = global [" + functions.size() + " x i8*]");

        LLVM.emit("[");

        for (Function function : functions)
        {
            String identifier = function.getIdentifier();
            String type       = function.getType();

            String[] argtypes = function.getArguementTypes();

            LLVM.emit(String.format("i8* bitcast (%s (%s)* %s to i8*)" + (function != functions.getLast() ? "," : ""), to(type), to(argtypes), "@" + className + "." + identifier));
        }

        LLVM.emit("]");
    }
}
