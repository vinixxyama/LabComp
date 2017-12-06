// Vinicius Yamamoto    490105

package ast;
import java.util.ArrayList;

public class ReadStatement extends Statement{

	public ReadStatement(ArrayList<Expr> e) {
		this.e = e;
	}
	@Override
	public void genC(PW pw) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void genKra(PW pw) {
		pw.printIdent("read(");
		for(int i=0;i<e.size();i++) {
			e.get(i).genKra(pw);
			if(e.size() > 0 && i != e.size()-1) {
				pw.print(", ");
			}
		}
		pw.println(");");
	}

	private ArrayList<Expr> e;
}
