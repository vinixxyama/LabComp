// Vinicius Yamamoto    490105

package ast;

public class WriteStatement extends Statement {
	public WriteStatement(ExprList e){
		this.e = e;
	}
	
	@Override
	public void genC(PW pw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void genKra(PW pw) {
		pw.printIdent("write(");
		e.genKra(pw);
		pw.println(");");
	}

	private ExprList e;
}
