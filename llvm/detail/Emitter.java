
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

        instance = this;

        emit("declare i8 * @calloc(i32, i32)");
        emit("declare i32 @printf(i8 *, ...)");
        emit("declare void @exit(i32)");

        emit("@_i32fmt = constant [4 x i8] c\"%d\\0a\\00\"");

        emit("@_true  = constant [6 x i8] c\"true\0a\00\"");
        emit("@_false = constant [7 x i8] c\"false\0a\00\"");

        emit("@_oob_exception = constant [15 x i8] c\"Out of bounds\0a\00\"");

        emit("define void @print_i32(i32 %var)");
        emit("{");
        emit("%_str = bitcast [4 x i8] * @_i32fmt to i8 *");
        emit("call i32 (i8 *, ...) @printf(i8 * %_str, i32 %var)");
        emit("ret void");
        emit("}");

        emit("define void @print_i1(i1 %var)");
        emit("{");
        emit("br i1 %var, label %isTrue, label %isFalse");
        emit("isTrue:");
        emit("%_true_str = bitcast [15 x i8] * @_true to i8 *");
        emit("isFalse:");
        emit("%_false_str = bitcast [15 x i8] * @_false to i8 *");
        emit("%_str = phi i8 * [%_true_str, %isTrue], [%_false_str, %isFalse]");
        emit("call i32 (i8 *, ...) @printf(i8 * %_str)");
        emit("ret void");
        emit("}");

        emit("define void @throw_oob()");
        emit("{");
        emit("%_str = bitcast [15 x i8] * @_oob_exception to i8 *");
        emit("call i32 (i8 *, ...) @printf(i8 * %_str)");
        emit("call void @exit(i32 1)");
        emit("ret void");
        emit("}");
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

        if (string.matches("define.*"))
            instance.println("\n" + string);
        else if (string.matches("(@|\\{|\\[).*"))
            instance.println(string);
        else if (string.matches("(declare|\\}|\\]).*"))
            instance.println(string + "\n");
        else
            instance.println("\t" + string);
    }
}
