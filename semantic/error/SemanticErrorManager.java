
package semantic.error;

import semantic.visitor.detail.*;

import syntaxtree.*;

import java.util.*;

public class SemanticErrorManager
{
    private static LinkedList<SemanticError> errors = new LinkedList<SemanticError>();
    
    public static final int SUCCESS = 0, FAILURE = 1;

    public static int getStatus()
    {
        return errors.isEmpty() ? SUCCESS : FAILURE;
    }

    public static void register(Scope scp, Node node, String msg)
    {
        errors.add(new SemanticError(scp, node, msg));
    }

    public static void flush(String filename)
    {
        if (errors.isEmpty())
            return;

        for (SemanticError ex : errors)
            System.err.println(filename + ":" + ex.getMessage());

        System.err.println("\n" + errors.size() + " errors\n");

        errors.clear();
    }
}
