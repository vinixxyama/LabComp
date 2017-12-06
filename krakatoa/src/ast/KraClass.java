// Vinicius Yamamoto    490105

package ast;

import java.util.ArrayList;

/*
 * Krakatoa Class
 */
public class KraClass extends Type {
	
   public KraClass( String name ) {
      super(name);
      publicMethodList = new ArrayList<>();
      instanceVariableList = new InstanceVariableList();
   }
   
   public String getCname() {
      return getName();
   }
   
   public boolean isSubclassOf(Type other) {
	   KraClass current = this;
	   while(current != other) {
		   current = current.getSuperclass();
		   if(current == null) {
			   return false;
		   }
	   }
	   return true;
   }
   
   public KraClass getSuperclass() {
	   return superclass;
   }
   
   public void setSuperclass(KraClass superclass) {
	   this.superclass = superclass;
   }
   
   public void addMethod(MethodDec aMethod) {
	   publicMethodList.add(aMethod);
   }
   
   public void remMethod(MethodDec aMethod) {
	   publicMethodList.remove(aMethod);
   }
   public MethodDec searchPublicMethod(String methodName) {
	   for(int i = 0;i < publicMethodList.size();i++) {
		   if(publicMethodList.get(i).getName().equals(methodName)) {
			   return publicMethodList.get(i);
		   }
	   }
	   return null;
   }

   public void addInstanceVariable(InstanceVariable instanceVariable) {
		instanceVariableList.addElement(instanceVariable);
   }
   
   public InstanceVariableList getInstanceVariableList() {
	   return instanceVariableList;
   }
   
   public void genKra(PW pw) {
	   if(getSuperclass() != null) {
		   pw.println("Class " + getCname() +" Extends "+ superclass.getCname() + " {");   
	   }else {
		   pw.println("Class " + getCname() + " {");
	   }
	   if(instanceVariableList != null) {
		   instanceVariableList.genKra(pw);
	   }
	   pw.add();
	   if(publicMethodList != null) {
		   for(int i=0;i<publicMethodList.size();i++) {
			   if(publicMethodList.get(i).getQualifier().toString().equals("private")) {
				   pw.printIdent("virtual ");
				   pw.print(publicMethodList.get(i).getReturnType().getName() + " ");
				   pw.print(publicMethodList.get(i).getName());
				   pw.print("(");
				   if(publicMethodList.get(i).getParametros() != null) {
					   publicMethodList.get(i).getParametros().geKra(pw);
				   }
				   pw.println("){");
				   if(publicMethodList.get(i).getStlist() != null) {
					   publicMethodList.get(i).genKra(pw); 
				   }
				   pw.printlnIdent("}");
			   }
		   }
		   pw.printlnIdent("public:");
		   for(int i=0;i<publicMethodList.size();i++) {
			   if(publicMethodList.get(i).getQualifier().toString().equals("public")) {
				   pw.printIdent("virtual ");
				   pw.print(publicMethodList.get(i).getReturnType().getName() + " ");
				   pw.print(publicMethodList.get(i).getName());
				   pw.print("(");
				   if(publicMethodList.get(i).getParametros() != null) {
					   publicMethodList.get(i).getParametros().geKra(pw);
				   }
				   pw.println("){");
				   if(publicMethodList.get(i).getStlist() != null) {
					   publicMethodList.get(i).genKra(pw); 
				   }
				   pw.printlnIdent("}");
			   }
		   }
	   }
	   pw.sub();
	   pw.println("}");
   }
   
   private String name;
   private KraClass superclass;
   private InstanceVariableList instanceVariableList;
   private ArrayList<MethodDec> publicMethodList;
   // private MethodList publicMethodList, privateMethodList;
   // métodos públicos get e set para obter e iniciar as variáveis acima,
   // entre outros métodos


}
