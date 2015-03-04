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
    //if not, we return the hash value for its varAddress and SSA reference ID
    @Override
    public int hashCode() {
        int hashcode1;
        int hashcode2;
        if(result1.type == Result.Type.variable)
            hashcode1 =  result1.varAddress * 17 + result1.ssaVersion.hashCode() * 31;
        else
            hashcode1 =  result1.value * 61;

        if(result2.type == Result.Type.variable)
            hashcode2 =  result2.varAddress * 41 + result2.ssaVersion.hashCode() * 59;
        else
            hashcode2 =  result2.value * 61;
        return hashcode1 + hashcode2;
    }

    public boolean equals(Object object){
        return isEqualResult(this.result1,((ExpressionNode)object).result1) && (isEqualResult(this.result2,((ExpressionNode)object).result2));
    }

    public String toString(){
        return result1.varAddress + "_" + result1.ssaVersion.getVersion() + " "
                + result2.varAddress + "_" + result2.ssaVersion.getVersion();
    }

    private boolean isEqualResult(Result temp1, Result temp2)
    {
        if(temp1.type != temp2.type)
            return false;

        if(temp1.type == Result.Type.variable)
        {
            if(temp1.varAddress != temp2.varAddress || !temp1.ssaVersion.equals(temp2.ssaVersion))
                return false;
        }
        else if(temp1.type == Result.Type.instruction)
        {
            if(temp1.instrRef != temp2.instrRef)
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
