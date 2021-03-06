package dijkstra.ast;

import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import dijkstra.ast.expr.ExprAST;
import dijkstra.type.Type;
import dijkstra.unify.ScopedSet;
import dijkstra.unify.TypeUnificationTable;

public class ReturnAST implements AST
{
	private final ExprAST parts;

	public ExprAST getParts()
	{
		return parts;
	}
	
	public ReturnAST(AST accept)
	{
		parts = (ExprAST) accept;
	}

	public String toString()
	{
		return "return "+parts.toString();
	}
	
	@Override
	public ScopedSet<String> getDeclaredVariables(ScopedSet<String> scope)
	{	
		return scope;
	}
	
	@Override
	public AST renameVars(Set<VarBind> scope)
	{
		return new ReturnAST(parts.renameVars(scope));
	}
	
	@Override
	public void buildTUT(TypeUnificationTable tut)
	{
		parts.buildTUT(tut);
	}
	
	@Override
	public void generateCode(ClassWriter writer, MethodVisitor mv, TypeUnificationTable tut)
	{
		generateCode(writer, mv, tut, tut.getTypeByName(parts));
	}
	
	public void generateCode(ClassWriter writer, MethodVisitor mv, TypeUnificationTable tut, Type t)
	{
		parts.generateCode(writer, mv, tut);
		switch(t)
		{
		case A_BOOL:
			break;
		case A_FLOAT:
			break;
		case A_INT:
			break;
		case BOOLEAN:
			mv.visitInsn(IRETURN);
			return;
		case C_FLOAT:
			break;
		case C_INT:
			break;
		case FLOAT:
			mv.visitInsn(FRETURN);
			return;
		case INT:
			mv.visitInsn(IRETURN);
			return;
		case LOOKUP:
			break;
		case NUMERIC_GENERAL:
			break;
		case UNKNOWN:
			break;
		case VOID:
			break;
		default:
			break;
		}
		
		throw new RuntimeException("need to return a type" + t);
	}
}
