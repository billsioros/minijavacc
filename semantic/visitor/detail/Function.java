
package semantic.visitor.detail;

import java.util.*;

public class Function extends Variable implements Context
{
    private LinkedHashMap<String, String> arguements;

    private LinkedHashMap<String, String> variables;

    public Function(String type, String identifier, String arguements) throws Exception
    {
        super(type, identifier);

        this.arguements = null;
        
        if (arguements != null)
        {
            this.arguements = new LinkedHashMap<String, String>();

            for (String arguement : arguements.split(","))
            {
                String token[] = arguement.trim().split("\\s+");

                String key = (token.length > 1 ? token[1] : "");

                String other = this.arguements.putIfAbsent(key, token[0]);

                if (other != null)
                    throw new Exception("Multiple declarations of local variable '" + key + "'");
            }
        }

        this.variables = new LinkedHashMap<String, String>();
    }

    @Override
    public int size()
    {
        return 8;
    }

    public boolean isApplicable(String[] arguementTypes, Global global)
    {
        if (arguements == null)
            return arguementTypes == null;

        if (arguementTypes == null)
            return arguements == null;

        if (arguementTypes.length != arguements.size())
            return false;

        int index = 0;
        for (Map.Entry<String, String> entry : arguements.entrySet())
        {
            String parameterType = entry.getValue(), arguementType = arguementTypes[index++];

            if (!arguementType.equals(parameterType.trim()))
            {
                try
                {
                    Base base    = global.acquireClass(parameterType);
                    Base derived = global.acquireClass(arguementType);

                    if (!derived.isSubclassOf(base))
                        return false;
                }
                catch (Exception ex)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public String[] getArguementTypes()
    {
        return arguements == null ? null : arguements.values().toArray(new String[0]);
    }

    @Override
    public Variable acquireVariable(String identifier) throws Exception
    {
        String type = variables.get(identifier);

        if (type == null && arguements != null)
            type = arguements.get(identifier);

        if (type == null)
            throw new Exception("'" + identifier + "' cannot be resolved to a variable");
        
        return new Variable(type, identifier);
    }
    
    @Override
    public void registerVariable(Variable variable) throws Exception
    {
        String key = variable.getIdentifier();
        
        if (arguements != null && arguements.get(key) != null)
            throw new Exception("Multiple declarations of local variable '" + key + "'");

        if (variables.putIfAbsent(key, variable.getType()) != null)
            throw new Exception("Multiple declarations of local variable '" + key + "'");
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this)
            return true;

        if (!(object instanceof Function))
            return false;

        Function other = (Function)object;

        if (!getType().equals(other.getType()))
            return false;

        if (!getIdentifier().equals(other.getIdentifier()))
            return false;

        if (!Arrays.equals(getArguementTypes(), other.getArguementTypes()))
            return false;

        return true;
    }

    @Override
    public String toString()
    {
        String arguementTypes[] = getArguementTypes();

        return getType() + " " + getIdentifier() + "(" + (arguementTypes != null ? String.join(", ", arguementTypes) : "") + ")";
    }
}
