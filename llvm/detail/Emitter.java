
package llvm.detail;

import llvm.options.*;

import error.*;

import java.io.*;

class Emitter extends PrintWriter
{
    private static Emitter instance = null;

    private Emitter(File file) throws FileNotFoundException
    {
        super(file);

        println("declare i8 * @calloc(i32, i32)");
        println("declare i32 @printf(i8 *, ...)");
        println("declare void @exit(i32)");

        println("@_i32fmt = constant [4 x i8] c\"%d\\0a\\00\"");

        println("@_true  = constant [6 x i8] c\"true\0a\00\"");
        println("@_false = constant [7 x i8] c\"false\0a\00\"");

        println("@_oob_exception = constant [15 x i8] c\"Out of bounds\0a\00\"");

        println("define void @print_i32(i32 %var)");
        println("{");
        println("\t%_str = bitcast [4 x i8] * @_i32fmt to i8 *");
        println("\tcall i32 (i8 *, ...) @printf(i8 * %_str, i32 %var)");
        println("\tret void");
        println("}");

        println("define void @print_i1(i1 %var)");
        println("{");
        println("\tbr i1 %var, label %isTrue, label %isFalse");
        println("\tisTrue:");
        println("\t%_true_str = bitcast [15 x i8] * @_true to i8 *");
        println("\tisFalse:");
        println("\t%_false_str = bitcast [15 x i8] * @_false to i8 *");
        println("\t%_str = phi i8 * [%_true_str, %isTrue], [%_false_str, %isFalse]");
        println("\tcall i32 (i8 *, ...) @printf(i8 * %_str)");
        println("\tret void");
        println("}");

        println("define void @throw_oob()");
        println("{");
        println("\t%_str = bitcast [15 x i8] * @_oob_exception to i8 *");
        println("\tcall i32 (i8 *, ...) @printf(i8 * %_str)");
        println("\tcall void @exit(i32 1)");
        println("\tret void");
        println("}");

        instance = this;
    }

    public static void create(String filename)
    {
        if (instance != null)
            throw new UnrecoverableError("Emitter.instance is non null");

        filename = filename.replace("\\s+", "");

        if (filename.isEmpty())
            throw new UnrecoverableError("'" + filename + "' does not name a file");

        if (filename.contains(".java"))
            filename = filename.replace(".java", ".llvm");
        else if (filename.contains(".mini"))
            filename = filename.replace(".mini", ".llvm");
        else
            filename += ".llvm";

        File file = new File(filename);

        if (!Options.OVERWRITE_OUTPUT_FILE)
            if (file.exists())
                throw new UnrecoverableError("'" + filename + "' already exists");

        try
        {
            instance = new Emitter(file);
        }
        catch (FileNotFoundException ex)
        {
            throw new UnrecoverableError("'" + filename + "' does not name a file");
        }
    }

    public static void destroy()
    {
        if (instance == null)
            throw new UnrecoverableError("Emitter.instance is null");

        instance.close();

        instance = null;
    }

    public static void emit(String string)
    {
        if (instance == null)
            throw new UnrecoverableError("Emitter.instance is null");

        if (string.matches("(@|\\{|\\}|\\[|\\]|define).*"))
            instance.println(string);
        else
            instance.println("\t" + string);
    }
}
