
package llvm.detail;

import semantic.detail.*;

import utility.*;

import error.*;

public class Scope extends semantic.detail.Scope
{
    private static final long serialVersionUID = 1L;

	public Scope(Global global)
    {
        super(global);
    }

    @Override
    public Variable acquireVariable(String identifier)
    {
        String type = null;

        try
        {
            Variable variable = getLocal().acquireVariable(identifier).first;

            type = LLVM.to(variable.getType()) + " *";

            identifier = "%" + variable.getIdentifier();

            if (type.startsWith("i8"))
                type = Pointer.raw(variable.getType(), Pointer.from(type).degree);
        }
        catch (Exception ex)
        {
            try
            {
                Pair<Variable, Integer> pair = getOuter().acquireVariable(identifier);

                type = LLVM.to(pair.first.getType()) + " *";

                identifier = "%" + pair.first.getIdentifier();

                String i8Pointer = LLVM.getRegister();

                LLVM.emit(i8Pointer + " = getelementptr i8, i8 * %.this, i32 " + pair.second);

                LLVM.emit(identifier + " = bitcast i8 * " + i8Pointer + " to " + type);

                if (type.startsWith("i8"))
                    type = Pointer.raw(pair.first.getType(), Pointer.from(type).degree);
            }
            catch (Exception ignore)
            {
                throw new UnrecoverableError(ex.getMessage());
            }
        }

        return new Variable(type, identifier);
    }

    @Override
    public Function acquireFunction(String identifier)
    {
        try
        {
            Pair<Function, Integer> pair = getLocal().acquireFunction(identifier);

            String i8CastedVTable = LLVM.getRegister();

            LLVM.emit(i8CastedVTable + " = bitcast i8 * " + identifier + " to i8 ***");

            String i8LoadPointer = LLVM.getRegister();

            LLVM.emit(i8LoadPointer + " = load i8 **, i8 *** " + i8CastedVTable);

            String i8FunctionPointer = LLVM.getRegister();

            LLVM.emit(i8FunctionPointer + " = getelementptr i8 *, i8 ** " + i8LoadPointer + ", i32 " + pair.second);

            String i8LoadFunctionPointer = LLVM.getRegister();

            LLVM.emit(i8LoadFunctionPointer + " = load i8 *, i8 ** " + i8FunctionPointer);

            String functionPointer = LLVM.getRegister();

            LLVM.emit(functionPointer + " = bitcast i8 * " + i8FunctionPointer + " to " + LLVM.to(pair.first));

            return new Function(pair.first.getType(), functionPointer, null);
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }
}
