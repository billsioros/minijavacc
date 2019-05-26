
package semantic.detail;

import utility.*;

import java.util.*;

public class Table<Declaration extends Variable> extends LinkedHashMap<String, Pair<Declaration, Integer>>
{
    private static final long serialVersionUID = 1L;

    private int offset;

    public Table(int offset)
    {
        super();

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
