package dijkstra.unify;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;

import dijkstra.ast.AST;
import dijkstra.ast.FunctionAST;
import dijkstra.ast.expr.ExprAST;
import dijkstra.ast.expr.TerminalAST;
import dijkstra.gen.JVMInfo;
import dijkstra.type.Arrow;
import dijkstra.type.Monad;
import dijkstra.type.Type;
import dijkstra.unify.rlist.RList;

public class TypeUnificationTable
{
	private final ReverseNameIndex rni;
	private RList<Term, Constraint> temp = RList.emptyList();
	private Map<String, FunctionAST> fnmap = new HashMap<>();
	private String name;
	
	public void setName(String n)
	{
		name = n;
	}
	
	public TypeUnificationTable(RList<Term, Constraint> cs, ReverseNameIndex r)
	{
		this(r);
		temp = cs;
	}
	
	public TypeUnificationTable(ReverseNameIndex r)
	{
		rni = r;
	}
	
	public Term register(ExprAST tree, Term t)
	{
		Monad<Term> monad = Monad.of();
		temp = temp.setAdd(new Constraint(tree, t), monad);
		return monad.get();
	}
	
	public String toString()
	{
		return rni.sanatize(temp.toString());
	}
	
	public TypeUnificationTable getOnlyTerminalValues()
	{
		RList<Term, Constraint> mew = RList.emptyList();
		RList<Term, Constraint> temp = this.temp;
		
		while(!temp.empty())
		{
			if (temp.first().left() instanceof TerminalAST)
			{
				mew = mew.cons(temp.first());
			}
			temp = temp.rest();
		}
		
		TypeUnificationTable newtut = new TypeUnificationTable(mew, rni);
		newtut.fnmap = fnmap;
		newtut.setName(name);
		return newtut;
	}
	
	
	public static RList<Term, Constraint> unify(RList<Term, Constraint> stack, RList<Term, Constraint> subst)
	{
		if (stack.empty())
		{
			return subst;
		}
		
		Constraint current = stack.first();
		stack = stack.rest();
		
		if (current.same())
		{
			return unify(stack, subst);
		}
		
		if (current.left().isID())
		{
			stack = replaceInList(current.left(), current.right(), stack);
			subst = replaceInList(current.left(), current.right(), subst);
			return unify(stack, subst.cons(current));
		}
		
		if (current.right().isID())
		{
			stack = replaceInList(current.right(), current.left(), stack);
			subst = replaceInList(current.right(), current.left(), subst);
			return unify(stack, subst.cons(current));
		}
		
		if (current.matchingArity())
		{
			RList<Term, Constraint> additions = current.generateAdditions();
			stack = RList.append(additions, stack);
			return unify(stack, subst);
		}
		
		throw new RuntimeException("didn't know what to do with: "+current);
	}

	private static RList<Term, Constraint> replaceInList(Term l, Term r, RList<Term, Constraint> s)
	{
		if (l instanceof Arrow) {
			return replaceInList(((Arrow) l).o, r, s);
		}
		if (r instanceof Arrow) {
			return replaceInList(l, ((Arrow) r).o, s);
		}
		return s.map(a -> new Constraint(a.left(), a.right().replace(l, r)));
	}
	
	public TypeUnificationTable check(RList<Term, Constraint> cons)
	{
		TypeUnificationTable newtut =  new TypeUnificationTable(unify(temp, cons), rni);
		newtut.fnmap = fnmap;
		newtut.setName(name);
		return newtut;
	}

	public Type getTypeByName(AST outputAST)
	{
		RList<Term, Constraint> temp = this.temp;
		while(!temp.empty())
		{
			Constraint c = temp.first();
			
			if (c.left().equals(outputAST))
			{
				if (c.right() instanceof Type)
				{
					return consolidate((Type) c.right());
				}
				if (c.right() instanceof Arrow)
				{
					return (Type) ((Arrow)c.right()).o;
				}
			}
			
			if (c.right().equals(outputAST))
			{
				if (c.left() instanceof Type)
				{
					return consolidate((Type) c.left());
				}
				if (c.left() instanceof Arrow)
				{
					return (Type) ((Arrow)c.left()).o;
				}
			}
			
			temp = temp.rest();
		}
		
		return Type.UNKNOWN;
	}
	
	public Type consolidate(Type t) {
		switch(t)
		{
		case C_INT: case INT: case NUMERIC_GENERAL:
			return Type.INT;
		case C_FLOAT: case FLOAT:
			return Type.FLOAT;
		case BOOLEAN:
			return Type.BOOLEAN;
		case A_INT:
			return Type.A_INT;
		case A_FLOAT:
			return Type.A_FLOAT;
		case A_BOOL:
			return Type.A_BOOL;
			
		default:
			throw new RuntimeException("no "+t);
		}
	}

	public void smush() {
		RList<Term, Constraint> nnn = RList.emptyList();
		
		while(!temp.empty())
		{
			nnn = nnn.setAdd(temp.first(), Monad.of());
			
			temp = temp.rest();
		}
		
		temp = nnn;
	}

	public Constraint getConstraintByName(String arr)
	{
		RList<Term, Constraint> temp = this.temp;
		while(!temp.empty())
		{
			Constraint c = temp.first();
			
			if (c.left().toString().equals(arr))
			{
				return c;
			}
			
			if (c.right().toString().equals(arr))
			{
				return c;
			}
			
			temp = temp.rest();
		}
		return null;
	}

	public void makeFunctions(ClassWriter cw)
	{
		RList<Term, Constraint> temp = this.temp;
		while(!temp.empty())
		{
			Constraint c = temp.first();
			
			if (c.right() instanceof Arrow)
			{
				JVMInfo.reset();
				fnmap.get(c.left().toString()).generateCode(cw, null, this);
			}
			
			
			temp = temp.rest();
		}
	}

	public void registerFunction(String name, FunctionAST functionAST)
	{
		fnmap.put(name, functionAST);
	}

	public String getName()
	{
		return name;
	}
}