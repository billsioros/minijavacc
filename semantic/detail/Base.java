
package semantic.detail;

import semantic.options.*;

import utility.*;

import java.util.*;

public class Base implements Context
{
    protected class Table<Declaration extends Variable> extends LinkedHashMap<String, Pair<Declaration, Integer>>
    {
        private static final long serialVersionUID = 1L;

        private int offset;

        public Table(int offset)
        {
            this.offset = offset;
        }

        public int getOffset()
        {
            return offset;
        }

        public Declaration register(Declaration declaration)
        {
            Pair<Declaration, Integer> pair = putIfAbsent(declaration.getIdentifier(), new Pair<Declaration, Integer>(declaration, offset));

            if (pair == null)
                offset += declaration.size();

            return (pair != null ? pair.first : null);
        }

        public Declaration acquire(String identifier)
        {
            Pair<Declaration, Integer> pair = get(identifier);

            return (pair != null ? pair.first : null);
        }
    }

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

    public int size()
    {
        return variables.getOffset();
    }

    public boolean isSubclassOf(Base base)
    {
        return this.identifier == base.identifier;
    }

    @Override
    public Variable acquireVariable(String identifier) throws Exception
    {
        Variable variable = variables.acquire(identifier);

        if (variable == null)
            throw new Exception("'" + identifier + "' cannot be resolved or is not a field");

        return variable;
    }

    @Override
    public void registerVariable(Variable variable) throws Exception
    {
        Variable other = variables.register(variable);

        if (other != null)
            throw new Exception("Multiple declarations of field '" + variable.getIdentifier() + "' in type '" + this.identifier + "'");
    }

    @Override
    public Function acquireFunction(String identifier) throws Exception
    {
        Function function = functions.acquire(identifier);

        if (function == null)
            throw new Exception("The method '" + identifier + "' is undefined for the type '" + this.identifier + "'");

        return function;
    }

    @Override
    public void registerFunction(Function function) throws Exception
    {
        Function other = functions.register(function);

        if (other != null)
            throw new Exception("Multiple definitions of function '" + function.getIdentifier() + "' in type '" + this.identifier + "'");
    }

    protected String getVariables()
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

    protected String getFunctions()
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
            return "-----------Class " + this.identifier + "-----------\n" + getVariables() + getFunctions() + "\n";

        return "class " + this.identifier + "\n{" + getVariables() + "\n" + getFunctions() + "}\n\n";
    }
}
