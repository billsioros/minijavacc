
package semantic.visitor.detail;

import error.*;

public interface Context
{
    public String getIdentifier();

    default public Variable acquireVariable(String identifier) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'acquireVariable' method");
    }
    
    default public void registerVariable(Variable variable) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'registerVariable method'");
    }
    
    default public Function acquireFunction(String identifier) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'acquireFunction' method");
    }
    
    default public void registerFunction(Function function) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'registerFunction method'");
    }
    
    default public Base acquireClass(String identifier) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'acquireClass' method");
    }

    default public void registerClass(Base base) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'registerClass method'");
    }
}
