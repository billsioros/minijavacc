
package semantic.detail;

public class Variable
{
    private String type, identifier;

    public Variable(String type, String identifier)
    {
        this.type       = type;
        this.identifier = identifier;
    }

    public String getType()
    {
        return type;
    }
    
    public String getIdentifier()
    {
        return identifier;
    }

    public int size()
    {
        return (type == "int" ? 4 : (type == "boolean" ? 1 : 8));
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this)
            return true;

        if (!(object instanceof Variable))
            return false;

        Variable other = (Variable)object;

        return type.equals(other.type) && identifier.equals(other.identifier);
    }

    @Override
    public String toString()
    {
        return type + " " + identifier;
    }
}
