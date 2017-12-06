//Vinicius Yamamoto 	490105
package ast;
import java.util.ArrayList;

public class CompositeStatement extends Statement{
	public CompositeStatement(ArrayList<Statement> st){
		this.st = st;
	}
	@Override
	public void genC(PW pw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void genKra(PW pw) {
		for(int i=0;i<st.size();i++) {
			if (st.get(i) instanceof StmtVar) {
				st.get(i).genKra(pw);
			}else if (st.get(i) instanceof Stmtlocadec) {
				st.get(i).genKra(pw);
			}else if(st.get(i) instanceof Ifstatement) {
				pw.add();
				st.get(i).genKra(pw);
				pw.sub();
			}else if(st.get(i) instanceof WhileStatement) {
				pw.printIdent("");
				st.get(i).genKra(pw);
			}else if(st.get(i) instanceof WriteStatement) {
				pw.printIdent("");
				st.get(i).genKra(pw);
			}else if(st.get(i) instanceof WritelnStatement) {
				pw.printIdent("");
				st.get(i).genKra(pw);
			}else if(st.get(i) instanceof ReadStatement) {
				pw.printIdent("");
				st.get(i).genKra(pw);
			}else if(st.get(i) instanceof ReturnStatement) {
				pw.printIdent("");
				st.get(i).genKra(pw);
			}
		}
	}
	
	private ArrayList<Statement> st;
}
