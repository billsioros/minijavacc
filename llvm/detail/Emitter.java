
package llvm.detail;

import llvm.options.*;

import error.*;

import java.io.*;

class Emitter extends PrintWriter
{
    private static Emitter instance = null;

    private static long lineCount = 1;

    private Emitter(File file) throws FileNotFoundException
    {
        super(file);

        instance = this;

        emit("declare i8* @calloc(i32, i32)");
        emit("declare i32 @printf(i8*, ...)");
        emit("declare void @exit(i32)");

        if (Options.DEBUG)
            emit("@_dbgfmt = constant [9 x i8] c\"[%d] %s\\0a\\00\"");

        emit("@_i32fmt = constant [4 x i8] c\"%d\\0a\\00\"");

        emit("@_true  = constant [6 x i8] c\"true\\0a\\00\"");
        emit("@_false = constant [7 x i8] c\"false\\0a\\00\"");

        emit("@_oob_exception = constant [73 x i8] c\"ArrayIndexOutOfBoundsException:%d: Index %d out of bounds for length %d\\0a\\00\"");
        emit("@_alc_exception = constant [35 x i8] c\"NegativeArraySizeException:%d: %d\\0a\\00\"");

        emit("@_errors = global [2 x i8*]");
        emit("[");
        emit("i8* bitcast ([73 x i8]* @_oob_exception to i8*),");
        emit("i8* bitcast ([35 x i8]* @_alc_exception to i8*)");
        emit("]");

        emit("define void @print_i32(i32 %var)");
        emit("{");
        emit("%_str = bitcast [4 x i8]* @_i32fmt to i8*");
        emit("call i32 (i8*, ...) @printf(i8* %_str, i32 %var)");
        emit("ret void");
        emit("}");

        emit("define void @print_i1(i1 %var)");
        emit("{");
        emit("br i1 %var, label %isTrue, label %isFalse");
        emit("");
        emit("isTrue:");
        emit("%_true_str = bitcast [6 x i8]* @_true to i8*");
        emit("br label %isDone");
        emit("");
        emit("isFalse:");
        emit("%_false_str = bitcast [7 x i8]* @_false to i8*");
        emit("br label %isDone");
        emit("");
        emit("isDone:");
        emit("%_str = phi i8* [%_true_str, %isTrue], [%_false_str, %isFalse]");
        emit("call i32 (i8*, ...) @printf(i8* %_str)");
        emit("ret void");
        emit("}");

        emit("define void @throw(i32 %errno, i32 %line, i32 %index, i32 %length)");
        emit("{");
        emit("%_str_ptr = getelementptr i8*, i8** bitcast ([2 x i8*]* @_errors to i8**), i32 %errno");
        emit("%_str = load i8*, i8** %_str_ptr");
        emit("call i32 (i8*, ...) @printf(i8* %_str, i32 %line, i32 %index, i32 %length)");
        emit("call void @exit(i32 1)");
        emit("ret void");
        emit("}");
    }

    public static void create(String filename)
    {
        if (instance != null)
            throw new UnrecoverableError("Emitter.instance is non null");

        if (lineCount != 1)
            throw new UnrecoverableError("Emitter.lineCount has not been reset");

        filename = filename.replace("\\s+", "");

        if (filename.isEmpty())
            throw new UnrecoverableError("'" + filename + "' does not name a file");

        if (filename.contains(".java"))
            filename = filename.replace(".java", ".ll");
        else if (filename.contains(".mini"))
            filename = filename.replace(".mini", ".ll");
        else
            filename += ".ll";

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

        instance = null; lineCount = 1;
    }

    public static void emit(String string)
    {
        if (instance == null)
            throw new UnrecoverableError("Emitter.instance is null");

        string = string.trim();

        if (string.matches("(define|@).*"))
        {
            instance.println("\n" + string); lineCount += 2;
        }
        else if (string.matches("ret.*"))
        {
            instance.println("\n\t" + string); lineCount += 2;
        }
        else if (string.matches("(\\{|\\[).*"))
        {
            instance.println(string); lineCount++;
        }
        else if (string.matches("(declare|\\}|\\]|.*:).*"))
        {
            instance.println(string + "\n"); lineCount += 2;
        }
        else
        {
            instance.println("\t" + string); lineCount++;
        }
    }

    public static void comment(String message)
    {
        emit("; " + message.trim());
    }

    public static void debug(String message)
    {
        if (Options.DEBUG)
        {
            message = message.trim();

            int size = message.length() + 2;

            comment("-------------------------------------");
            comment(message);

            if (Options.EMBEDDED_LOGGING)
            {
                String i8Array = "%_debug" + lineCount;
                emit(i8Array + String.format(" = alloca [%d x i8]", size));

                emit(String.format("store [%d x i8] c\"%s\\0a\\00\", [%d x i8]* %s", size, message, size, i8Array));

                String i8Pointer = "%_debug" + lineCount;
                emit(i8Pointer + String.format(" = bitcast [%d x i8]* %s to i8*", size, i8Array));

                emit(String.format("call i32 (i8*, ...) @printf(i8* bitcast ([9 x i8]* @_dbgfmt to i8*), i32 %d, i8* %s)", lineCount, i8Pointer));
            }

            comment("-------------------------------------");
        }
    }
}
