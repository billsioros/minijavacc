
package semantic.detail;

import semantic.options.*;

import utility.*;

import java.util.*;

public class Base implements Context
{
    protected String identifier;

    protected Table<Variable> variables;
    protected Table<Function> functions;

    public Base(String identifier)
    {
        this.identifier = identifier;

        this.variables = new Table<Variable>(0);
        this.functions = new Table<Function>(0);
    }

    public Base(String identifier, Base base)
    {
        this.identifier = identifier;

        this.variables = new Table<Variable>(base.variables.getOffset());
        this.functions = new Table<Function>(base.functions.getOffset());
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public int functionCount()
    {
        return functions.size();
    }

    public boolean isSubclassOf(Base base)
    {
        return this.identifier == base.identifier;
    }

    @Override
    public Pair<Variable, Integer> acquireVariable(String identifier) throws Exception
    {
        Pair<Variable, Integer> pair = variables.get(identifier);

        if (pair == null)
            throw new Exception("'" + identifier + "' cannot be resolved or is not a field");

        return pair;
    }

    @Override
    public void register(Variable variable) throws Exception
    {
        Variable other = variables.register(variable);

        if (other != null)
            throw new Exception("Multiple declarations of field '" + variable.getIdentifier() + "' in type '" + this.identifier + "'");
    }

    @Override
    public Pair<Function, Integer> acquireFunction(String identifier) throws Exception
    {
        Pair<Function, Integer> pair = functions.get(identifier);

        if (pair == null)
            throw new Exception("The method '" + identifier + "' is undefined for the type '" + this.identifier + "'");

        return pair;
    }

    @Override
    public void register(Function function) throws Exception
    {
        Function other = functions.register(function);

        if (other != null)
            throw new Exception("Multiple definitions of function '" + function.getIdentifier() + "' in type '" + this.identifier + "'");
    }

    protected String getVariablesString()
    {
        String content = "";

        if (!Options.JAVA_FORMAT)
        {
            content += "--Variables---\n";

            for (Map.Entry<String, Pair<Variable, Integer>> entry : variables.entrySet())
            {
                Pair<Variable, Integer> pair = entry.getValue();

                content += this.identifier + "." + pair.first.getIdentifier() + " : " + pair.second + "\n";
            }

            return content;
        }

        for (Map.Entry<String, Pair<Variable, Integer>> entry : variables.entrySet())
        {
            Pair<Variable, Integer> pair = entry.getValue();

            content += "\n\t" + pair.second + ": " + pair.first.toString() + ";\n";
        }

        return content;
    }

    public LinkedList<Function> getFunctions()
    {
        LinkedList<Function> functions = new LinkedList<Function>();

        for (Pair<Function, Integer> pair : this.functions.values())
            functions.add(pair.first);

        return functions;
    }

    protected String getFunctionsString()
    {
        String content = "";

        if (!Options.JAVA_FORMAT)
        {
            content += "---Methods---\n";

            for (Map.Entry<String, Pair<Function, Integer>> entry : functions.entrySet())
            {
                Pair<Function, Integer> pair = entry.getValue();

                content += this.identifier + "." + pair.first.getIdentifier() + " : " + pair.second + "\n";
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

        return "class " + this.identifier + "\n{" + getVariablesString() + "\n" + getFunctionsString() + "}\n\n";
    }
}
