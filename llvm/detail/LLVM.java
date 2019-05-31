
package llvm.detail;

import semantic.detail.*;

import java.util.*;

import error.*;

public class LLVM
{
    public static class Pointer
    {
        private static final String error = "'%s' is not 'LLVM.Pointer' compatible";

        public String base;

        public int degree;

        public Pointer(String base, int degree)
        {
            if (base == null)
                throw new UnrecoverableError(String.format(error, ""));

            this.base = base.trim();

            if (this.base.isEmpty())
                throw new UnrecoverableError(String.format(error, ""));

            if (degree < 0)
                throw new UnrecoverableError("Negative pointer degree supplied");

            this.degree = degree;
        }

        public static String raw(String base, int degree)
        {
            return raw(new Pointer(base, degree));
        }

        public static String raw(Pointer pointer)
        {
            String raw = pointer.base + " ";

            for (int i = 0; i < pointer.degree; ++i)
                raw += '*';

            return raw;
        }

        public static Pointer from(String raw)
        {
            int pivot = raw.indexOf('*');

            if (pivot < 0)
                return new Pointer(raw.trim(), 0);

            if (pivot == 0)
                throw new UnrecoverableError(String.format(error, raw));

            String[] elements =
            {
                raw.substring(0, pivot - 1).trim(),
                raw.substring(pivot, raw.length()).replace(" ", "")
            };

            for (int i = 0; i < elements[1].length(); ++i)
            {
                char c = elements[1].charAt(i);

                if (c != '*')
                    throw new UnrecoverableError(String.format(error, raw));
            }

            return new Pointer(elements[0], elements[1].length());
        }
    }

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
        switch (type)
        {
            case "int":     type = "i32";   break;
            case "int[]":   type = "i32 *"; break;
            case "boolean": type = "i1";    break;
            default:        type = "i8 *";  break;
        }

        return type;
    }

    public static String to(String[] types)
    {
        String llvm = "i8 *";

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
                llvm += ", " + variable.getType() + " " + variable.getIdentifier();

        return llvm;
    }

    public static String to(Function function)
    {
        String type       = function.getType();

        String[] argtypes = function.getArguementTypes();

        return String.format("%s (%s) *", to(type), to(argtypes));
    }

    public static String VTableOf(Base base)
    {
        return "@" + base.getIdentifier() + ".VTable";
    }

    public static String to(Base base)
    {
        String llvm = "";

        String className = base.getIdentifier();

        LinkedList<Function> functions = base.getFunctions();

        llvm += VTableOf(base) + " = global [" + functions.size() + " x i8 *]";

        llvm += "\n[";

        for (Function function : functions)
        {
            String identifier = function.getIdentifier();
            String type       = function.getType();

            String[] argtypes = function.getArguementTypes();

            llvm += String.format("\ni8 * bitcast (%s (%s) %s * to i8 *),", to(type), "@" + className + "." + identifier, to(argtypes));
        }

        llvm += "\n]";

        return llvm;
    }

    public static void emit(String llvm)
    {
        System.out.println(llvm);
    }
}
