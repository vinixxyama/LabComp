// Vinicius Yamamoto    490105

package ast;

import java.util.*;

public class ExprList {

    public ExprList() {
        exprList = new ArrayList<Expr>();
    }

    public void addElement( Expr expr ) {
        exprList.add(expr);
    }
    
    public ArrayList<Expr> getArray(){
    	return exprList;
    }

    public void genC( PW pw ) {

        int size = exprList.size();
        for ( Expr e : exprList ) {
        	e.genC(pw, false);
            if ( --size > 0 )
                pw.print(", ");
        }
    }
    
    public void genKra(PW pw) {
    	if(exprList != null) {
	    	for(int i = 0;i<exprList.size();i++) {
	    		if(exprList.get(i) instanceof ExprClass) {
	    			exprList.get(i).genKra(pw);
				}else if(exprList.get(i) instanceof LiteralInt){
					exprList.get(i).genKra(pw);
				}else if(exprList.get(i) instanceof LiteralBoolean) {
					exprList.get(i).genKra(pw);
				}else if(exprList.get(i) instanceof LiteralString) {
					exprList.get(i).genKra(pw);
				}else if(exprList.get(i) instanceof VariableExpr) {
					exprList.get(i).genKra(pw);
				}else if(exprList.get(i) instanceof CompositeExpr) {
					exprList.get(i).genKra(pw);
				}else if(exprList.get(i) instanceof UnaryExpr) {
					exprList.get(i).genKra(pw);
				}else if(exprList.get(i) instanceof PrimaryExpr) {
					pw.sub();
					exprList.get(i).genKra(pw);
					pw.add();
				}
	    		if(exprList.size() > 0&& i != exprList.size()-1) {
	    			pw.print(", ");
	    		}
	    	}
    	}
    }
    
    private ArrayList<Expr> exprList;

}
