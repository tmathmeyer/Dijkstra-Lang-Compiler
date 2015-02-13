package dijkstra.ast;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import dijkstra.lexparse.DijkstraBaseVisitor;
import dijkstra.lexparse.DijkstraParser;
import dijkstra.lexparse.DijkstraParser.AlternativeStatementContext;
import dijkstra.lexparse.DijkstraParser.ArrayDeclarationContext;
import dijkstra.lexparse.DijkstraParser.AssignStatementContext;
import dijkstra.lexparse.DijkstraParser.DijkstraTextContext;
import dijkstra.lexparse.DijkstraParser.ExprContext;
import dijkstra.lexparse.DijkstraParser.ExpressionListContext;
import dijkstra.lexparse.DijkstraParser.FunctionDeclarationContext;
import dijkstra.lexparse.DijkstraParser.GuardContext;
import dijkstra.lexparse.DijkstraParser.GuardedStatementListContext;
import dijkstra.lexparse.DijkstraParser.IdListContext;
import dijkstra.lexparse.DijkstraParser.InputStatementContext;
import dijkstra.lexparse.DijkstraParser.IterativeStatementContext;
import dijkstra.lexparse.DijkstraParser.OutputStatementContext;
import dijkstra.lexparse.DijkstraParser.ParameterContext;
import dijkstra.lexparse.DijkstraParser.ParameterListContext;
import dijkstra.lexparse.DijkstraParser.ProgramContext;
import dijkstra.lexparse.DijkstraParser.TypeContext;
import dijkstra.lexparse.DijkstraParser.TypeListContext;
import dijkstra.lexparse.DijkstraParser.VarContext;
import dijkstra.lexparse.DijkstraParser.VarListContext;
import dijkstra.lexparse.DijkstraParser.VariableDeclarationContext;

public class ASTBuilder extends DijkstraBaseVisitor<AST>
{
	private final DijkstraParser p;
	
	public ASTBuilder(DijkstraParser b)
	{
		p = b;
	}

	@Override
	public AST visitInputStatement(InputStatementContext ctx)
	{
		return new InputAST(ctx);
	}
	
	@Override
	public AST visitDijkstraText(DijkstraTextContext ctx)
	{
		return ctx.program().accept(this);
	}
	
	@Override
	public AST visitOutputStatement(OutputStatementContext ctx)
	{
		return new OutputAST(ctx.expr().accept(this));
	}
	
	@Override
	public AST visitProgram(ProgramContext ctx)
	{
		return new ProgramAST(ctx.children.stream().filter(c -> !(c instanceof TerminalNodeImpl)).map(e -> e.accept(this)));
	}
	
	@Override
	public AST visitAssignStatement(AssignStatementContext ctx)
	{
		return new AssignmentAST(getVarsFromList(ctx.varList()), getExprsFromList(ctx.expressionList()).stream().map(e -> e.accept(this)));
	}
	
	@Override
	public AST visitExpr(ExprContext ctx)
	{
		return new ExprAST(ctx.children.stream().map(e -> examineExpr(e)));
	}
	
	@Override
	public AST visitArrayDeclaration(ArrayDeclarationContext ctx)
	{
		return new ArrayDecAST(ctx.type(), ctx.expr().accept(this), getIDsFromList(ctx.idList()));
	}
	
	
	@Override
	public AST visitAlternativeStatement(AlternativeStatementContext ctx)
	{
		return new ConditionalAST(ctx.guard().stream().map(e -> e.accept(this)));
	}
	
	@Override
	public AST visitIterativeStatement(IterativeStatementContext ctx)
	{
		return ctx.guardedStatementList().accept(this);
	}
	
	@Override
	public AST visitGuard(GuardContext ctx)
	{
		return new GuardedAST(ctx.expr().accept(this), ctx.statement().accept(this));
	}
	
	@Override
	public AST visitVariableDeclaration(VariableDeclarationContext ctx)
	{
		return new VariableDeclarationAST(ctx.type(), getIDsFromList(ctx.idList()));
	}
	
	@Override
	public AST visitFunctionDeclaration(FunctionDeclarationContext ctx)
	{
		return new FunctionAST(
				ctx.ID().getText(),
				getParams(ctx.parameterList()).stream().map(e -> e.accept(this)),
				getParams(ctx.typeList()).stream().map(e -> e.getText()),
				ctx.compoundStatement().accept(this));
	}
	
	@Override
	public AST visitParameter(ParameterContext ctx)
	{
		return new FunctionAST.Param(ctx.type(), ctx.ID());
	}
	
	
	@Override
	public AST visitGuardedStatementList(GuardedStatementListContext ctx)
	{
		throw new RuntimeException("not implemented");
	}
	
	
	
	
	
	private AST examineExpr(ParseTree tree)
	{
		if (tree instanceof TerminalNodeImpl)
		{
			return new TerminalAST((TerminalNodeImpl)tree);
		}
		
		return tree.accept(this);
	}
	
	private List<VarContext> getVarsFromList(VarListContext vct)
	{
		List<VarContext> vc = new LinkedList<>();
		while(vct != null  &&  vct.var() != null)
		{
			vc.add(vct.var());
			vct = vct.varList();
		}
		return vc;
	}
	
	private List<ExprContext> getExprsFromList(ExpressionListContext elc)
	{
		List<ExprContext> vc = new LinkedList<>();
		while(elc != null  &&  elc.expr() != null)
		{
			vc.add(elc.expr());
			elc = elc.expressionList();
		}
		return vc;
	}
	
	private List<TerminalNode> getIDsFromList(IdListContext elc)
	{
		List<TerminalNode> vc = new LinkedList<>();
		while(elc != null  &&  elc.ID() != null)
		{
			vc.add(elc.ID());
			elc = elc.idList();
		}
		return vc;
	}
	
	private List<ParameterContext> getParams(ParameterListContext plc)
	{
		List<ParameterContext> vc = new LinkedList<>();
		while(plc != null  &&  plc.parameter() != null)
		{
			System.out.println(plc.parameter().toStringTree(p));
			vc.add(plc.parameter());
			plc = plc.parameterList();
		}
		return vc;
	}
	
	private List<TypeContext> getParams(TypeListContext plc)
	{
		List<TypeContext> vc = new LinkedList<>();
		while(plc != null  &&  plc.type() != null)
		{
			vc.add(plc.type());
			plc = plc.typeList();
		}
		return vc;
	}
}
