package dijkstra.ast.expr;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import dijkstra.ast.AST;
import dijkstra.type.Type;
import dijkstra.unify.Constraint;
import dijkstra.unify.Term;
import dijkstra.unify.TypeUnificationTable;

public class AdditionExpr extends ExprAST
{
	private final ExprAST f, l;
	
	public AdditionExpr(ExprAST first, ExprAST last)
	{
		f = first;
		l = last;
	}

	@Override
	public void buildTUT(TypeUnificationTable tut)
	{
		f.buildTUT(tut);
		l.buildTUT(tut);
		
		Term t1 = tut.register(f, Type.NUMERIC_GENERAL);
		Term t2 = tut.register(l, Type.NUMERIC_GENERAL);
		
		Constraint c = new Constraint(t1, t2);
		tut.register(this, c.important());
	}

	@Override
	protected List<AST> getChildren() {
		List<AST> t = new LinkedList<>();
		t.add(f);
		t.add(l);
		return t;
	}
	
	@Override
	public ExprAST renameVars(Set<VarBind> s)
	{
		return new AdditionExpr(f.renameVars(s), l.renameVars(s));
	}
	
	@Override
	public String toString()
	{
		return f + "+" + l;
	}
	
	@Override
	public void generateCode(ClassWriter writer, MethodVisitor mv, TypeUnificationTable tut)
	{
		Type me = tut.getTypeByName(this);
		Type ff, ll;
		if (f instanceof FunctionCallExpr)
		{
			ff = tut.getTypeByName(new TerminalAST(((FunctionCallExpr)f).getName()));
		}
		else
		{
			ff = tut.getTypeByName(f);
		}
		
		if (l instanceof FunctionCallExpr)
		{
			ll = tut.getTypeByName(new TerminalAST(((FunctionCallExpr)l).getName()));
		}
		else
		{
			ll = tut.getTypeByName(l);
		}
		
		f.generateCode(writer, mv, tut);
		if (me==Type.INT && ff==Type.FLOAT) {
			mv.visitInsn(F2I);
		} else if (me==Type.FLOAT && ff==Type.INT) {
			mv.visitInsn(I2F);
		}
		
		l.generateCode(writer, mv, tut);
		if (me==Type.INT && ll==Type.FLOAT) {
			mv.visitInsn(F2I);
		} else if (me==Type.FLOAT && ll==Type.INT) {
			mv.visitInsn(I2F);
		}
		
		
		switch(me)
		{
		case INT:
			mv.visitInsn(IADD);
			break;
		case FLOAT:
			mv.visitInsn(FADD);
			break;
		default:
			break;
		}
		
	}
}
