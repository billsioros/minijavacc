
package semantic.error;

import error.*;

import semantic.detail.*;

import syntaxtree.*;

public class SemanticError
{
    private String msg;

    public SemanticError(Scope scp, Node node, String msg) throws UnrecoverableError
    {
        if (scp == null)
            throw new UnrecoverableError("SemanticError.SemanticError.scp is null");

        if (node == null)
            throw new UnrecoverableError("SemanticError.SemanticError.node is null");

        if (msg == null)
            throw new UnrecoverableError("SemanticError.SemanticError.msg is null");

        this.msg = scp.toString() + (scp.isEmpty() ? "" : ":") + LineNumberInfo.get(node).toString() + " " + msg;
    }

    public String getMessage()
    {
        return msg;
    }
}
