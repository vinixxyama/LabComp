// Vinicius Yamamoto    490105

package ast;

import java.util.*;

public class ParamList {

    public ParamList() {
       paramList = new ArrayList<Variable>();
    }

    public void addElement(Variable v) {
       paramList.add(v);
    }

    public Iterator<Variable> elements() {
        return paramList.iterator();
    }

    public int getSize() {
        return paramList.size();
    }
    
    public ArrayList<Variable> getarray() {
    	return paramList;
    }
    
    public void geKra(PW pw) {
		for(int i = 0;i<paramList.size();i++) {
			paramList.get(i).getKra(pw);
		}
	}

    private ArrayList<Variable> paramList;

}
