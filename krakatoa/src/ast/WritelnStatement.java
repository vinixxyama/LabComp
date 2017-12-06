// Vinicius Yamamoto    490105

package ast;

public class WritelnStatement extends Statement{
	public WritelnStatement(ExprList e) {
		this.e = e;
	}
	
	@Override
	public void genC(PW pw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void genKra(PW pw) {
		pw.printIdent("writeln(");
		e.genKra(pw);
		pw.println(");");
	}

	private ExprList e;
}
