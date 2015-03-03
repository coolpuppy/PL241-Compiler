package pl241.uci.edu.ir;


import pl241.uci.edu.middleend.Result;
import sun.jvm.hotspot.runtime.ResultTypeFinder;

/*
Data:2015/03/03
This class is used to store the two variable of a pre-SSA form instruction.
 */
public class ExpressionNode {
    private Result result1;

    private Result result2;

    public ExpressionNode(Result result1,Result result2)
    {
        this.result1 = result1;
        this.result2 = result2;
    }

    //set a hash code for each result
    //if result is a variable, then we return the hash value for its value
    //if not, we return the hash value for its address and SSA reference ID
    @Override
    public int hashCode() {
        int hashcode1;
        int hashcode2;
        if(result1.kind == Result.Type.var)
            hashcode1 =  result1.address * 17 + result1.ssa.hashCode() * 31;
        else
            hashcode1 =  result1.value * 61;

        if(result2.kind == Result.Type.var)
            hashcode2 =  result2.address * 41 + result2.ssa.hashCode() * 59;
        else
            hashcode2 =  result2.value * 61;
        return hashcode1 + hashcode2;
    }

    public boolean equals(Object object){
        return isEqualResult(this.result1,((ExpressionNode)object).result1) && (isEqualResult(this.result2,((ExpressionNode)object).result2));
    }

    public String toString(){
        return result1.address + "_" + result1.ssa.getID() + " "
                + result2.address + "_" + result2.ssa.getClass();
    }

    private boolean isEqualResult(Result temp1, Result temp2)
    {
        if(temp1.kind != temp2.kind)
            return false;

        if(temp1.kind == Result.Type.var)
        {
            if(temp1.address != temp2.address || !temp1.ssa.equals(temp2.ssa))
                return false;
        }
        else if(temp1.kind == Result.Type.instr)
        {
            if(temp1.instrId != temp2.instrId)
                return false;
        }
        else
        {
            if(temp1.value != temp2.value)
                return false;
        }
        return true;
    }
}
