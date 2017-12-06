// Vinicius Yamamoto    490105

package ast;

import java.util.*;

public class InstanceVariableList {

    public InstanceVariableList() {
       instanceVariableList = new ArrayList<InstanceVariable>();
    }

    public void addElement(InstanceVariable instanceVariable) {
       instanceVariableList.add( instanceVariable );
    }

    public Iterator<InstanceVariable> elements() {
    	return this.instanceVariableList.iterator();
    }

    public int getSize() {
        return instanceVariableList.size();
    }
    
	public InstanceVariable getInstanceVar(String instVarName){
		for(int i=0;i<instanceVariableList.size();i++){
			if(instanceVariableList.get(i).getName().equals(instVarName)) {
				return instanceVariableList.get(i);
			}
		}
		return null;
	}
	
	public void genKra(PW pw) {
		pw.add();
		for(int i=0;i < instanceVariableList.size();i++){
			pw.printlnIdent(instanceVariableList.get(i).getType().getName() + " " + instanceVariableList.get(i).getName()+";");
		}
		pw.sub();
	}
	
    private ArrayList<InstanceVariable> instanceVariableList;


}
