class SubclassReturnValue
{
    public static void main(String[] args)
    {
        B b;
        A a;

        b = new B();

        a = b.foo();
    }
}

class A
{

}

class B extends A
{
    public A foo()
    {
        return new B();
    }
}
