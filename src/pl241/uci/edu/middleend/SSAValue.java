package pl241.uci.edu.middleend;

/*
Data:2015/03/02
This is used to store the reference ID of a value in SSA form.
 */
public class SSAValue {
    private int referenceID;

    public SSAValue(int id)
    {
        this.referenceID = id;
    }

    public int getID()
    {
        return this.referenceID;
    }

    public void changeID(int id)
    {
        this.referenceID = id;
    }
}
