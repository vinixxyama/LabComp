// Vinicius Yamamoto    490105

package ast;

public class NullExpr extends Expr {
    
   public void genC( PW pw, boolean putParenthesis ) {
      pw.printIdent("NULL");
   }
   public void genKra(PW pw){
	   pw.print("null");
   }
   
   public Type getType() {
      //# corrija
      return null;
   }
}