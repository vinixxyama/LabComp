// Vinicius Yamamoto    490105

package ast;

public class VariableExpr extends Expr {
    
    public VariableExpr( Variable v ) {
        this.v = v;
    }
    
    public void genC( PW pw, boolean putParenthesis ) {
        pw.print( v.getName() );
    }
    
    public void genKra(PW pw) {
    	pw.print(v.getName());
    }
    
    public Variable getVariable() {
    	return v;
    }
    
    public Type getType() {
        return v.getType();
    }
    
    private Variable v;
}