
package semantic.visitor.detail;

import java.util.*;

public class Scope extends Stack<Context>
{
    private static final long serialVersionUID = 1L;

    public Scope()
    {
        super();

        push(new Global());
    }

    public Context getLocal() throws InternalError
    {
        Context context;
        try
        {
            context = lastElement();
        }
        catch (NoSuchElementException ex)
        {
            throw new InternalError("Scope.getLocal: No 'Context' instance");
        }

        return context;
    }

    public Context getOuter() throws InternalError
    {
        Context context = null;
        try
        {
            context = get(size() - 2);
        }
        catch(ArrayIndexOutOfBoundsException ex)
        {
            throw new InternalError("Failed to fetch the outer context of '" + getLocal().getIdentifier() + "'");
        }

        return context;
    }

    public Global getGlobal() throws InternalError
    {
        Context context;
        
        try
        {
            context = firstElement();
        }
        catch (NoSuchElementException ex)
        {
            throw new InternalError("Scope.getGlobal: No 'Context' instance");
        }

        if (!(context instanceof Global))
            throw new InternalError("Scope.getGlobal.context is not an instance of the 'Global' class");

        return (Global)context;
    }
    
    public Variable acquireVariable(String identifier) throws Exception, InternalError
    {
        try
        {
            return getLocal().acquireVariable(identifier);
        }
        catch (Exception ex)
        {
            try
            {
                return getOuter().acquireVariable(identifier);
            }
            catch (Exception ignore)
            {
                throw ex;
            }
        }
    }
    
    public void registerVariable(Variable variable) throws Exception, InternalError
    {
        getLocal().registerVariable(variable);
    }

    public Function acquireFunction(String identifier) throws Exception, InternalError
    {
        return getLocal().acquireFunction(identifier);
    }
    
    public void registerFunction(Function function) throws Exception, InternalError
    {
        getLocal().registerFunction(function); push(function);
    }

    public Base acquireClass(String identifier) throws Exception, InternalError
    {
        return getLocal().acquireClass(identifier);
    }
    
    public void registerClass(Base base) throws Exception, InternalError
    {
        getLocal().registerClass(base); push(base);
    }
    
    @Override
    public String toString()
    {
        String string = "";

        int count = 0;
        for (Context context : this)
        {
            String identifier = context.getIdentifier();

            if (++count < this.size() && identifier != "")
                string += identifier + ".";
            else
                string += identifier;
        }
        
        return string;
    }
}
