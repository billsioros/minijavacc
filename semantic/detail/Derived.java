
package semantic.detail;

import semantic.options.*;

import utility.*;

import java.util.*;

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
        return getFunctions().size();
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

        Pair<Function, Integer> other = null;

        try
        {
            other = base.acquireFunction(key);
        }
        catch (Exception ex)
        {
            if (functions.register(function) != null)
                throw new Exception("Multiple definitions of function '" + key + "' in type '" + this.identifier + "'");
        }

        if (other != null && (!other.first.equals(function) || functions.register(new Pair<Function, Integer>(function, other.second)) != null))
            throw new Exception("Multiple definitions of function '" + key + "' in type '" + this.identifier + "'");
    }

    public TreeMap<Integer, Pair<String, Function>> getFunctions()
    {
        TreeMap<Integer, Pair<String, Function>> functions = base.getFunctions();

        for (Pair<Function, Integer> pair : this.functions.values())
            functions.put(pair.second, new Pair<String, Function>(identifier, pair.first));

        return functions;
    }

    @Override
    protected String getFunctionsString()
    {
        String content = "";

        if (!Options.JAVA_FORMAT)
        {
            content += "---Methods---\n";

            for (Map.Entry<String, Pair<Function, Integer>> entry : functions.entrySet())
            {
                Pair<Function, Integer> pair = entry.getValue();

                try
                {
                    base.acquireFunction(pair.first.getIdentifier()); continue;
                }
                catch (Exception ex)
                {
                    content += this.identifier + "." + pair.first.getIdentifier() + " : " + pair.second + "\n";
                }
            }

            return content;
        }

        for (Map.Entry<String, Pair<Function, Integer>> entry : functions.entrySet())
        {
            Pair<Function, Integer> pair = entry.getValue();

            content += "\n\t" + pair.second + ": " + pair.first.toString() + ";\n";
        }

        return content;
    }

    @Override
    public String toString()
    {
        if (!Options.JAVA_FORMAT)
            return "-----------Class " + this.identifier + "-----------\n" + getVariablesString() + getFunctionsString() + "\n";

        return "class " + this.identifier + " extends " + base.getIdentifier() + "\n{" + getVariablesString() + "\n" + getFunctionsString() + "}\n\n";
    }
}
