package ast;

public class ReturnStatement extends Statement{
		
	public ReturnStatement(Expr e){
		this.e = e;
	}
	
	@Override
	public void genC(PW pw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void genKra(PW pw) {
		pw.printIdent("return ");
		if(e != null) {
			pw.sub();
			if(e instanceof ExprClass) {
				pw.print("new ");
				e.genKra(pw);
			}else
				e.genKra(pw);
			pw.add();
		}
		pw.println(";");
	}
	private Expr e;
}
