
package llvm.detail;

import error.*;

public class Pointer
{
    private static final String error = "'%s' is not 'Pointer' compatible";

    public String base;

    public int degree;

    public Pointer(String base, int degree)
    {
        if (base == null)
            throw new UnrecoverableError(String.format(error, ""));

        this.base = base.trim();

        if (this.base.isEmpty())
            throw new UnrecoverableError(String.format(error, ""));

        if (degree < 0)
            throw new UnrecoverableError("Negative pointer degree supplied");

        this.degree = degree;
    }

    public static String raw(String base, int degree)
    {
        return raw(new Pointer(base, degree));
    }

    public static String raw(Pointer pointer)
    {
        String raw = pointer.base;

        for (int i = 0; i < pointer.degree; ++i)
            raw += '*';

        return raw;
    }

    public static Pointer from(String raw)
    {
        int pivot = raw.indexOf('*');

        if (pivot < 0)
            return new Pointer(raw.trim(), 0);

        if (pivot == 0)
            throw new UnrecoverableError(String.format(error, raw));

        String[] elements =
        {
            raw.substring(0, pivot).replace(" ", ""),
            raw.substring(pivot, raw.length()).replace(" ", "")
        };

        for (int i = 0; i < elements[1].length(); ++i)
        {
            char c = elements[1].charAt(i);

            if (c != '*')
                throw new UnrecoverableError(String.format(error, raw));
        }

        return new Pointer(elements[0], elements[1].length());
    }
}
