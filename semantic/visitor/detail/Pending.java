
package semantic.visitor.detail;

import error.*;

import utility.*;

import syntaxtree.*;

import java.util.*;

public class Pending extends LinkedList<Pair<Scope, Node>>
{
    private static final long serialVersionUID = 1L;

    public void insert(Scope scope, Node node) throws UnrecoverableError
    {
        if (scope == null)
            throw new UnrecoverableError("Pending.insert.scope is null");

        add(new Pair<Scope, Node>((Scope)scope.clone(), node));
    }
}
