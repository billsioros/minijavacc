
package llvm.detail;

import semantic.detail.*;

import utility.*;

import error.*;

import java.util.*;

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

            LLVM.debug("Acquired Local Variable '" + identifier + "'");

            type = LLVM.to(variable.getType()) + "*";

            identifier = "%" + variable.getIdentifier();

            if (type.startsWith("i8"))
                type = Pointer.raw(variable.getType(), Pointer.from(type).degree);
        }
        catch (Exception ex)
        {
            try
            {
                Pair<Variable, Integer> pair = getOuter().acquireVariable(identifier);

                LLVM.debug("Acquired field '" + pair.first.getIdentifier() + "' of '" + getOuter().getIdentifier() +"'");

                type = LLVM.to(pair.first.getType()) + "*";

                identifier = LLVM.getRegister();

                String i8Pointer = LLVM.getRegister();

                LLVM.debug("Accessing offset " + pair.second + " of '" + getOuter().getIdentifier() + "'");

                LLVM.emit(i8Pointer + " = getelementptr i8, i8* %this, i32 " + pair.second);

                LLVM.emit(identifier + " = bitcast i8* " + i8Pointer + " to " + type);

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
        String[] elements = identifier.split("\\s+");

        if (elements.length != 2)
            throw new UnrecoverableError("Identifier '" + identifier + "' consists of " + elements.length + " tokens instead of 2");

        try
        {
            Context local = getLocal();

            Pair<Function, Integer> pair = local.acquireFunction(elements[1]);

            LLVM.debug("Acquired function '" + pair.first.getIdentifier() + "' of '" + local.getIdentifier() + "'");

            String i8CastedVTable = LLVM.getRegister();

            LLVM.emit(i8CastedVTable + " = bitcast i8* " + elements[0] + " to i8***");

            String i8LoadPointer = LLVM.getRegister();

            LLVM.emit(i8LoadPointer + " = load i8**, i8*** " + i8CastedVTable);

            String i8FunctionPointer = LLVM.getRegister();

            LLVM.debug("Accessing offset " + pair.second + " of '" + local.getIdentifier() + "'");

            LLVM.emit(i8FunctionPointer + " = getelementptr i8*, i8** " + i8LoadPointer + ", i32 " + pair.second / pair.first.size());

            String i8LoadFunctionPointer = LLVM.getRegister();

            LLVM.emit(i8LoadFunctionPointer + " = load i8*, i8** " + i8FunctionPointer);

            String functionPointer = LLVM.getRegister();

            LLVM.emit(functionPointer + " = bitcast i8* " + i8LoadFunctionPointer + " to " + LLVM.to(pair.first));

            LinkedList<Variable> args = pair.first.getArguements();

            String arguements = "i8* %this";

            if (args != null)
                for (Variable arg : args)
                    arguements += ", " + LLVM.to(arg.getType()) + " %." + arg.getIdentifier();

            return new Function(pair.first.getType(), functionPointer, arguements);
        }
        catch (Exception ex)
        {
            throw new UnrecoverableError(ex.getMessage());
        }
    }
}
