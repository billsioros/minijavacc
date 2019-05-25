
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

    public boolean isSubclassOf(Base base)
    {
        return this.base.getIdentifier() == base.getIdentifier() || this.base.isSubclassOf(base);
    }

    @Override
    public Variable acquireVariable(String identifier) throws Exception
    {
        Variable variable = variables.acquire(identifier);

        if (variable == null)
        {
            try
            {
                variable = base.acquireVariable(identifier);
            }
            catch (Exception ex)
            {
                throw new Exception("'" + identifier + "' cannot be resolved or is not a field");
            }
        }

        return variable;
    }

    @Override
    public Function acquireFunction(String identifier) throws Exception
    {
        Function function = functions.acquire(identifier);

        if (function == null)
        {
            try
            {
                function = base.acquireFunction(identifier);
            }
            catch (Exception ex)
            {
                throw new Exception("The method '" + identifier + "' is undefined for the type '" + this.identifier + "'");
            }
        }

        return function;
    }

    @Override
    public void registerFunction(Function function) throws Exception
    {
        String key = function.getIdentifier();

        Function other = null;

        try
        {
            other = base.acquireFunction(key);
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
            return "-----------Class " + this.identifier + "-----------\n" + getVariables() + getFunctions() + "\n";

        return "class " + this.identifier + " extends " + base.getIdentifier() + "\n{" + getVariables() + "\n" + getFunctions() + "}\n\n";
    }
}
