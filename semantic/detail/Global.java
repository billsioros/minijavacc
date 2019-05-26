
package semantic.detail;

import java.util.*;

public class Global implements Context
{
    private LinkedHashMap<String, Base> classes;

    public Global()
    {
        this.classes = new LinkedHashMap<String, Base>();
    }

    @Override
    public String getIdentifier()
    {
        return "";
    }

    @Override
    public Base acquireClass(String identifier) throws Exception
    {
        Base base = classes.get(identifier);

        if (base == null)
            throw new Exception("'" + identifier + "' cannot be resolved to a type");

        return base;
    }

    @Override
    public void register(Base base) throws Exception
    {
        if (classes.putIfAbsent(base.getIdentifier(), base) != null)
            throw new Exception("Multiple definitions of type '" + base.getIdentifier() + "'");
    }
}
