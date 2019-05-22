
package semantic.error;

public class InternalError extends Error
{
    private static final long serialVersionUID = 1L;
    
    private String message;

    public InternalError(String message)
    {
        this.message = message;
    }

    @Override
    public String getMessage()
    {
        return message;
    }
}
