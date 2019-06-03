
package semantic.detail;

import utility.*;

import error.*;

import java.util.*;

public class Main extends Base
{
    public Main(String identifier)
    {
        super(identifier);
    }

    public Main(String identifier, Base base)
    {
        super(identifier);
    }

    @Override
    public Pair<Variable, Integer> acquireVariable(String identifier) throws Exception
    {
        throw new Exception("'" + identifier + "' cannot be resolved or is not a field");
    }

    @Override
    public void register(Variable variable) throws Exception
    {
        throw new UnrecoverableError("Attempting to register variable '" + variable.getIdentifier() + "' in main class '" + this.identifier + "'");
    }

    @Override
    public void register(Function function) throws Exception
    {
        if (!function.getType().matches("static.*"))
            throw new UnrecoverableError("Attempting to register function '" + function.getIdentifier() + "' in main class '" + this.identifier + "'");

        Function other = functions.register(function);

        if (other != null)
            throw new Exception("Multiple definitions of function '" + function.getIdentifier() + "' in type '" + this.identifier + "'");
    }

    @Override
    public int getFunctionsOffset()
    {
        return 0;
    }

    @Override
    public int functionCount()
    {
        return 0;
    }

    @Override
    public TreeMap<Integer, Pair<String, Function>> getFunctions()
    {
        return new TreeMap<Integer, Pair<String, Function>>();
    }
}
