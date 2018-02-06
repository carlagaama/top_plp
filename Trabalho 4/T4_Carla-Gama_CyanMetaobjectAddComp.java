/*
 * Carla Sim�es Gama 613843
 * T�picos Avan�ados em Paradigmas de Linguagens de Programa��o
 * prof: Z�
 * */
package meta.cyanLang;

import java.util.ArrayList;
import java.util.Iterator;

import ast.ASTVisitor;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.MethodSignature;
import ast.ObjectDec;
import ast.Statement;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.ICheckProgramUnit_ati3;
import meta.ICompiler_ati;
import meta.IInstanceVariableDec_ati;
import saci.Tuple2;
import ast.StatementIf;
import ast.StatementWhile;
import ast.StatementFor;

public class CyanMetaobjectAddComp extends CyanMetaobjectWithAt
		implements IActionProgramUnit_ati, ICheckProgramUnit_ati3 {

	public CyanMetaobjectAddComp() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "addComp";
	}

	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_codeToAddToPrototypes(
			ICompiler_ati compiler) {

		CyanMetaobjectWithAtAnnotation annot = this.getMetaobjectAnnotation();

		// ObjectDec � a classe da AST que representa prot�tipos que n�o s�o
		// interfaces
		if ( !(annot.getDeclaration() instanceof ObjectDec) ) {
			this.addError("N�o pode ser interface! Necessita ser objeto.");

		}
		ObjectDec prototype = (ObjectDec) annot.getDeclaration();
		ArrayList<Tuple2<String, StringBuffer>> array = new ArrayList<>();
		
		String prototypeName = prototype.getName();
		String methodName = "<1";
		ArrayList<MethodSignature> mList;
		MethodDec lessThanMethod = null;
		mList = ((ObjectDec) compiler.getProgramUnit())
				.searchMethodPrivateProtectedPublic(methodName);
		if ( mList != null && mList.size() > 0 ) {
			// h� um m�todo < declarado
			if ( mList.get(0).getParameterList().get(0).getType() != compiler
					.getProgramUnit() ) {
				// erro
				this.addError("Tipo de par�metro errado para m�todo <");
				return null;
			}
			else {
				lessThanMethod = mList.get(0).getMethod();
			}
		}
		else {
			// n�o h� <, sinalize um erro
			this.addError("Este prot�tipo deveria ter um m�todo < ");
			return null;
		}
		StringBuffer out = new StringBuffer();

		out.append("    override\n");
		out.append("    func == Dyn other -> Boolean {\n");
		out.append("        type other\n");
		out.append("            case " + prototypeName + " proto {\n");
		out.append("                return ");
		ArrayList<IInstanceVariableDec_ati> ivList = compiler
				.getInstanceVariableList();
		if ( ivList == null || ivList.size() == 0 ) {
			this.addError("no iv");
			return null;
		}
		int size = ivList.size();
		for (IInstanceVariableDec_ati iv : ivList) {
			String getMethod = iv.getName();
			if ( getMethod.startsWith("_") ) {
				getMethod = getMethod.substring(1);
			}
			else {
				getMethod = "get" + Character.toUpperCase(getMethod.charAt(0))
						+ getMethod.substring(1);
			}
			out.append(" " + iv.getName() + " == proto " + getMethod);
			if ( --size > 0 ) {
				out.append(" && ");
			}
		}
		out.append("\n            }\n");
		out.append("            case Dyn d {\n");
		out.append("                return false\n");

		out.append("            }\n");
		out.append("    }\n");

		/*
		 func <= Student other -> Boolean  
		 */
		out.append("    func <= "+ prototypeName +" other -> Boolean { return self == other || self < other; }\n");
		
		/*
		func > Student other -> Boolean 
		*/
		out.append("    func > "+ prototypeName +" other -> Boolean { return !(self < other) && self != other; }\n");
		
		/*
		 func >= Student other -> Boolean 
		 */
		out.append("    func >= "+ prototypeName +" other -> Boolean { return !(self < other); }\n");
		array.add(new Tuple2<String, StringBuffer>(
				annot.getPrototypeOfAnnotation(), out));
		return array;
	}

	@Override
	public DeclarationKind[] mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind[] decKindList = new DeclarationKind[] {
			DeclarationKind.INSTANCE_VARIABLE_DEC, DeclarationKind.METHOD_DEC,
			DeclarationKind.PROTOTYPE_DEC, DeclarationKind.PACKAGE_DEC,
			DeclarationKind.PROGRAM_DEC };

	@Override
	public void ati3_checkProgramUnit(ICompiler_ati compiler) {
		CyanMetaobjectWithAtAnnotation annot = this.getMetaobjectAnnotation();


		String methodName = "<1";
		ArrayList<MethodSignature> mList;
		MethodDec lessThanMethod = null;
		mList = ((ObjectDec) annot.getDeclaration())
				.searchMethodPrivateProtectedPublic(methodName);
		lessThanMethod = mList.get(0).getMethod();
		lessThanMethod.accept(new ASTVisitor() {
			// esta parte est� obviamente errada -ok
			@Override
			public void visit(MethodDec node) {
				Iterator <Statement> i = node.getStatementList().getStatementList().iterator();
				Statement lista = null;
				
				while(i.hasNext()) {
					lista = i.next();
					
					if (lista instanceof StatementFor || lista instanceof StatementIf || lista instanceof StatementWhile) {
						CyanMetaobjectAddComp.this.addError("O m�todo < n�o � simples.");
					}
				}
				
			}
		});

	}

}
