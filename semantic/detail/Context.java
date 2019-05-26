
package semantic.detail;

import error.*;

public interface Context
{
    public String getIdentifier();

    default public Variable acquireVariable(String identifier) throws Exception, InternalError
    {
        throw new InternalError("No implementation of the 'acquireVariable' method");
    }

    default public void register(Variable variable) throws Exception, InternalError
    {
        throw new InternalError("No implementation of the 'register(Variable)' method");
    }

    default public Function acquireFunction(String identifier) throws Exception, InternalError
    {
        throw new InternalError("No implementation of the 'acquireFunction' method");
    }

    default public void register(Function function) throws Exception, InternalError
    {
        throw new InternalError("No implementation of the 'register(Function)' method");
    }

    default public Base acquireClass(String identifier) throws Exception, InternalError
    {
        throw new InternalError("No implementation of the 'acquireClass' method");
    }

    default public void register(Base base) throws Exception, InternalError
    {
        throw new InternalError("No implementation of the 'register(Base)' method");
    }
}
