package dijkstra.ast.expr;

import static org.objectweb.asm.Opcodes.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import dijkstra.ast.AST;
import dijkstra.type.Type;
import dijkstra.unify.TypeUnificationTable;

public class GreaterThanExpr extends ExprAST
{
	private final ExprAST f, l;
	
	public GreaterThanExpr(ExprAST first, ExprAST last)
	{
		f = first;
		l = last;
	}
	
	@Override
	protected List<AST> getChildren() {
		List<AST> t = new LinkedList<>();
		t.add(f);
		t.add(l);
		return t;
	}
	
	@Override
	public void buildTUT(TypeUnificationTable tut)
	{
		f.buildTUT(tut);
		l.buildTUT(tut);
		
		tut.register(f, Type.NUMERIC_GENERAL);
		tut.register(l, Type.NUMERIC_GENERAL);
		tut.register(this, Type.BOOLEAN);
	}
	
	@Override
	public ExprAST renameVars(Set<VarBind> s)
	{
		return new GreaterThanExpr(f.renameVars(s), l.renameVars(s));
	}
	
	@Override
	public String toString()
	{
		return f + ">" + l;
	}
	
	@Override
	public void generateCode(ClassWriter writer, MethodVisitor mv, TypeUnificationTable tut)
	{
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
		if (ff == Type.INT)
		{
			mv.visitInsn(I2F);
		}
		
		l.generateCode(writer, mv, tut);
		if (ll == Type.INT)
		{
			mv.visitInsn(I2F);
		}
		mv.visitInsn(FCMPL);
		
		
		
		Label lab1 =  new Label();
		mv.visitJumpInsn(IFGT, lab1);
		mv.visitInsn(ICONST_0);		// left = right
		Label lab2 = new Label();
		mv.visitJumpInsn(GOTO, lab2);
		mv.visitLabel(lab1);
		mv.visitInsn(ICONST_1);		// left ~= right
		mv.visitLabel(lab2);
	}
}
