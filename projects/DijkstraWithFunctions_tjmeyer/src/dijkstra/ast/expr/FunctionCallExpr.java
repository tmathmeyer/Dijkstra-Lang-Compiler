package dijkstra.ast.expr;

import static org.objectweb.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import dijkstra.ast.AST;
import dijkstra.type.MethodSignitureGenerator;
import dijkstra.unify.Constraint;
import dijkstra.unify.Term;
import dijkstra.unify.TypeUnificationTable;

public class FunctionCallExpr extends ExprAST
{
	private final String fname;
	private final ArrayList<AST> args = new ArrayList<>();
	
	public FunctionCallExpr(String id, Stream<AST> map)
	{
		fname = id;
		map.forEach(a -> args.add(a));
	}

	public String toString()
	{
		LinkedList<String> argsS = new LinkedList<>();
		args.stream().forEach(e -> argsS.add(e.toString()));
		return fname + "(" + String.join(",", argsS) + ")";
	}
	
	@Override
	public ExprAST renameVars(Set<VarBind> scope)
	{
		String name = fname;
		for(VarBind b : scope)
		{
			if (b.old.equals(name))
			{
				name = b.New;
			}
		}
		
		return new FunctionCallExpr(name, args.stream().map(a -> {return a.renameVars(scope); }));
	}

	@Override
	public void buildTUT(TypeUnificationTable tut)
	{
		// huh
	}

	@Override
	protected List<AST> getChildren()
	{
		return args;
	}

	public String getName()
	{
		return fname;
	}
	
	@Override
	public Term replace(Term l, Term r)
	{
		if (this.equals(l))
		{
			return r;
		}
		
		return this;
	}
	
	@Override
	public void generateCode(ClassWriter writer, MethodVisitor mv, TypeUnificationTable tut)
	{
		Constraint cons = tut.getConstraintByName(fname);
		MethodSignitureGenerator msg = new MethodSignitureGenerator(cons.right());
		
		args.forEach(e -> e.generateCode(writer, mv, tut));
		mv.visitMethodInsn(INVOKESTATIC, "djkcode/"+tut.getName(), fname, msg.toString(), false);
	}
}
