
package semantic.error;

import semantic.visitor.detail.*;

import syntaxtree.*;

public class SemanticError
{
    private String msg;

    public SemanticError(Scope scp, Node node, String msg) throws InternalError
    {
        if (scp == null)
            throw new InternalError("SemanticError.SemanticError.scp is null");

        if (node == null)
            throw new InternalError("SemanticError.SemanticError.node is null");

        if (msg == null)
            throw new InternalError("SemanticError.SemanticError.msg is null");

        this.msg = scp.toString() + (scp.isEmpty() ? "" : ":") + LineNumberInfo.get(node).toString() + " " + msg;
    }

    public String getMessage()
    {
        return msg;
    }
}
