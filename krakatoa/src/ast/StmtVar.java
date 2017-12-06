// Vinicius Yamamoto    490105

package ast;

import java.util.ArrayList;

public class StmtVar extends Statement{
	
	public StmtVar(ArrayList<Variable> varlist) {
		this.varlist = varlist;
	}
	
	@Override
	public void genC(PW pw) {
		
	}
	@Override
	public void genKra(PW pw) {
		pw.add();
		pw.printIdent(varlist.get(0).getType().getName()+ " ");
		for(int i = 0; i<varlist.size();i++) {
			if(varlist.get(i).getType() instanceof KraClass) {
				pw.print("*");
			}
			pw.print(varlist.get(i).getName());
			if(i != varlist.size()-1) {
				pw.print(", ");
			}
		}
		pw.println(";");
		pw.sub();
	}
	private ArrayList<Variable> varlist;
}
