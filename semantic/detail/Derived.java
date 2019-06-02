
package semantic.detail;

import semantic.options.*;

import utility.*;

public class Derived extends Base
{
    private Base base;

    public Derived(String identifier, Base base)
    {
        super(identifier, base);

        this.base = base;
    }

    @Override
    public Base getBase()
    {
        return base;
    }

    @Override
    public int functionCount()
    {
        return functions.size() + base.functionCount();
    }

    @Override
    public boolean isSubclassOf(Base base)
    {
        return this.base.getIdentifier() == base.getIdentifier() || this.base.isSubclassOf(base);
    }

    @Override
    public Pair<Variable, Integer> acquireVariable(String identifier) throws Exception
    {
        Pair<Variable, Integer> pair = variables.get(identifier);

        if (pair == null)
        {
            try
            {
                pair = base.acquireVariable(identifier);
            }
            catch (Exception ex)
            {
                throw new Exception("'" + identifier + "' cannot be resolved or is not a field");
            }
        }

        return pair;
    }

    @Override
    public Pair<Function, Integer> acquireFunction(String identifier) throws Exception
    {
        Pair<Function, Integer> pair = functions.get(identifier);

        if (pair == null)
        {
            try
            {
                pair = base.acquireFunction(identifier);
            }
            catch (Exception ex)
            {
                throw new Exception("The method '" + identifier + "' is undefined for the type '" + this.identifier + "'");
            }
        }

        return pair;
    }

    @Override
    public void register(Function function) throws Exception
    {
        String key = function.getIdentifier();

        Function other = null;

        try
        {
            other = base.acquireFunction(key).first;
        }
        catch (Exception ex)
        {
            if (functions.register(function) != null)
                throw new Exception("Multiple definitions of function '" + key + "' in type '" + this.identifier + "'");
        }

        if (other != null && !other.equals(function))
            throw new Exception("Multiple definitions of function '" + key + "' in type '" + this.identifier + "'");
    }

    @Override
    public String toString()
    {
        if (!Options.JAVA_FORMAT)
            return "-----------Class " + this.identifier + "-----------\n" + getVariablesString() + getFunctionsString() + "\n";

        return "class " + this.identifier + " extends " + base.getIdentifier() + "\n{" + getVariablesString() + "\n" + getFunctionsString() + "}\n\n";
    }
}
