package dijkstra.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import dijkstra.unify.ScopedSet;
import dijkstra.unify.TypeUnificationTable;

public class AssignmentAST implements AST
{
	private final LinkedList<AST> assignTo = new LinkedList<>();
	private final LinkedList<AST> assignFrom = new LinkedList<>();
	
	public AssignmentAST(Stream<AST> vars, Stream<AST> xpressions)
	{
		vars.forEach(a -> assignTo.push(a));
		xpressions.forEach(a -> assignFrom.push(a));
		
		if (assignFrom.size() != assignTo.size())
		{
			throw new RuntimeException("mismatched assignment stament");
		}
	}
	
	@Override
	public String toString()
	{
		LinkedList<String> left = new LinkedList<>();
		LinkedList<String> right = new LinkedList<>();
		
		assignTo.forEach(a -> left.push(a.toString()));
		assignFrom.forEach(a -> right.push(a.toString()));
		
		return String.join(",", left) + " <- " + String.join(",", right);
	}
	
	@Override
	public AST renameVars(Set<VarBind> s)
	{	
		return new AssignmentAST(assignTo.stream().map(a -> a.renameVars(s)),
								 assignFrom.stream().map(a -> a.renameVars(s)));
	}
	
	@Override
	public AST renameScoping(ScopedSet<VarBind> vb)
	{
		List<AST> afr = new ArrayList<>();
		List<AST> ato = new ArrayList<>();
		for(AST a : assignFrom)
		{
			a = a.renameVars(vb.getScopeVars(a));
			a = a.renameVars(vb.getScopeVars(this));
			a = a.renameScoping(vb);
			afr.add(a);
		}
		
		for(AST a : assignTo)
		{
			a = a.renameVars(vb.getScopeVars(a));
			a = a.renameVars(vb.getScopeVars(this));
			a = a.renameScoping(vb);
			ato.add(a);
		}
		
		return new AssignmentAST(ato.stream(), afr.stream());
	}
	
	@Override
	public void buildTUT(TypeUnificationTable tut)
	{
		Iterator<AST> to = assignTo.iterator();
		Iterator<AST> from = assignFrom.iterator();
		for(int i=0; i<assignTo.size(); i++)
		{
			AST t = to.next();
			AST f = from.next();
			t.buildTUT(tut);
			f.buildTUT(tut);
			tut.register(t, f);
		}
	}
}
