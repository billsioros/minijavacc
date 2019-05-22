
package semantic.visitor.detail;

import semantic.utility.*;

import syntaxtree.*;

import java.util.*;

public class Pending extends LinkedList<Pair<Scope, Node>>
{
    private static final long serialVersionUID = 1L;

    public void insert(Scope scope, Node node) throws InternalError
    {
        if (scope == null)
            throw new InternalError("Pending.insert.scope is null");

        add(new Pair<Scope, Node>((Scope)scope.clone(), node));
    }
}
