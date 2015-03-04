package pl241.uci.edu.cfg;

import pl241.uci.edu.middleend.SSAValue;

import java.util.*;

/*
Date:2015/03/03
This class is used to store the hash map of global variable and SSA reference.
*/
public class VariableTable {
    //store the address of global variables
    private static HashSet<Integer> GlobalVariableAddress;

    //store the SSA form variables which have the same address
    private static HashMap<Integer,ArrayList<SSAValue>> SSAUseChain;

    public VariableTable()
    {
        GlobalVariableAddress = new HashSet<Integer>();

        SSAUseChain = new HashMap<Integer,ArrayList<SSAValue>>();
    }

    public static void addGlobalVariable(int address)
    {
        GlobalVariableAddress.add(address);
    }

    public static SSAValue getLatestVersion(int address)
    {
        if(!SSAUseChain.containsKey(address))
        {
            Error("Cannot find address " + address + " ! get latest version of SSA failed!");
            return null;
        }
        else
            return SSAUseChain.get(address).get(SSAUseChain.get(address).size()-1);
    }

    public static void addSSAUseChain(int address,int version)
    {
        if(!SSAUseChain.containsKey(address))
            SSAUseChain.put(address,new ArrayList<SSAValue>());
        SSAUseChain.get(address).add(new SSAValue(version));
    }

    public static void addSSAUseChain(int address,SSAValue ssa)
    {
        if(!SSAUseChain.containsKey(address))
            SSAUseChain.put(address, new ArrayList<SSAValue>());
        SSAUseChain.get(address).add(ssa);
    }

    public static HashMap<Integer,ArrayList<SSAValue>> cloneSSAUseChain()
    {
        HashMap<Integer,ArrayList<SSAValue>> clone = new HashMap<Integer,ArrayList<SSAValue>>();
        for(Map.Entry<Integer, ArrayList<SSAValue>> entry : SSAUseChain.entrySet()){
            clone.put(entry.getKey(), new ArrayList<SSAValue>(entry.getValue()));
        }
        return clone;
    }

    public static HashSet<Integer> cloneGlobalVariableAddress()
    {
        Iterator iter = GlobalVariableAddress.iterator();
        HashSet<Integer> clone = new HashSet<Integer>();
        while(iter.hasNext())
            clone.add((Integer)iter.next());
        return clone;
    }

    public static HashSet<Integer> getGlobalVariableAddress()
    {
        return GlobalVariableAddress;
    }

    public static void setGlobalVariableAddress(HashSet<Integer> set)
    {
        GlobalVariableAddress = set;
    }

    public static HashMap<Integer,ArrayList<SSAValue>> getSSAUseChain()
    {
        return SSAUseChain;
    }

    public static void setSSAUseChain(HashMap<Integer,ArrayList<SSAValue>> set)
    {
        SSAUseChain = set;
    }

    private static void Error(String msg)
    {
        System.out.println("VariableTable Error! " + msg);
    }

}
