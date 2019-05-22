
package error;

public class UnrecoverableError extends Error
{
    private static final long serialVersionUID = 1L;
    
    private String message;

    public UnrecoverableError(String message)
    {
        this.message = message;
    }

    @Override
    public String getMessage()
    {
        return message;
    }
}
