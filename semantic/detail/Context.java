
package semantic.detail;

import error.*;

import utility.*;

public interface Context
{
    public String getIdentifier();

    default public Pair<Variable, Integer> acquireVariable(String identifier) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'acquireVariable' method");
    }

    default public void register(Variable variable) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'register(Variable)' method");
    }

    default public Pair<Function, Integer> acquireFunction(String identifier) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'acquireFunction' method");
    }

    default public void register(Function function) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'register(Function)' method");
    }

    default public Base acquireClass(String identifier) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'acquireClass' method");
    }

    default public void register(Base base) throws Exception, UnrecoverableError
    {
        throw new UnrecoverableError("No implementation of the 'register(Base)' method");
    }
}
