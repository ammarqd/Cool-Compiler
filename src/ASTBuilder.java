import ast.ClassNode;
import ast.ProgramNode;
import ast.*;
import java.util.List;
import java.util.ArrayList;

public class ASTBuilder extends CoolParserBaseVisitor<Tree> {

    @Override
    public Tree visitProgram(CoolParser.ProgramContext ctx) {
        ProgramNode p = new ProgramNode(ctx.getStart().getLine());
        for (CoolParser.CoolClassContext c : ctx.coolClass()) {
            p.add((ClassNode) visitCoolClass(c));
        }
        return p;
    }

    @Override
    public Tree visitCoolClass(CoolParser.CoolClassContext ctx) {
        Symbol name = new Symbol(ctx.TYPEID(0).getText(), ctx.getStart().getLine());
        Symbol parent = new Symbol(
                ctx.INHERITS() != null ? ctx.TYPEID(1).getText() : "Object",
                ctx.getStart().getLine()
        );
        Symbol filename = new Symbol(ctx.start.getInputStream().getSourceName(), ctx.getStart().getLine());

        ClassNode class_node = new ClassNode(1, name, parent, filename);
        for (CoolParser.FeatureContext f : ctx.feature()) {
            class_node.add((FeatureNode) visit(f));
        }
        return class_node;
    }

    @Override
    public Tree visitMethod(CoolParser.MethodContext ctx) {
        Symbol name = new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine());
        List<FormalNode> formals = new ArrayList<>();
        if (ctx.formal() != null) {
            for (CoolParser.FormalContext f : ctx.formal()) {
                formals.add((FormalNode) visitFormal(f));
            }
        }
        Symbol return_type = new Symbol(ctx.TYPEID().getText(), ctx.getStart().getLine());
        ExpressionNode expr = (ExpressionNode) visitExpr(ctx.expr());
        return new MethodNode(1, name, formals, return_type, expr);
    }

    @Override
    public Tree visitAttribute(CoolParser.AttributeContext ctx) {
        Symbol name = new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine());
        Symbol type_decl = new Symbol(ctx.TYPEID().getText(), ctx.getStart().getLine());
        ExpressionNode expr = ctx.expr() != null ?
                (ExpressionNode) visitExpr(ctx.expr()) :
                new NoExpressionNode(1);
        return new AttributeNode(1, name, type_decl, expr);
    }

    @Override
    public Tree visitFormal(CoolParser.FormalContext ctx) {
        Symbol name = new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine());
        Symbol type_decl = new Symbol(ctx.TYPEID().getText(), ctx.getStart().getLine());
        return new FormalNode(1, name, type_decl);
    }

    @Override
    public Tree visitExpr(CoolParser.ExprContext ctx) {
        if (ctx.comparisonExpr() != null) {
            return visitComparisonExpr(ctx.comparisonExpr());
        }
        return visit(ctx.defaultExpr());
    }

    @Override
    public Tree visitComparisonExpr(CoolParser.ComparisonExprContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.defaultExpr(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.defaultExpr(1));

        if (ctx.LESS_OPERATOR() != null) return new LTNode(1, left, right);
        if (ctx.LESS_EQ_OPERATOR() != null) return new LEqNode(1, left, right);
        if (ctx.EQ_OPERATOR() != null) return new EqNode(1, left, right);

        return null;
    }

    @Override
    public Tree visitStaticDispatch(CoolParser.StaticDispatchContext ctx) {
        List<ExpressionNode> actuals = new ArrayList<>();
        ExpressionNode expr = (ExpressionNode) visit(ctx.defaultExpr());
        for (int i = 0; i < ctx.expr().size(); i++) {
            actuals.add((ExpressionNode) visitExpr(ctx.expr(i)));
        }
        Symbol type_name = new Symbol(ctx.TYPEID().getText(), ctx.getStart().getLine());
        Symbol method_name = new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine());
        return new StaticDispatchNode(1, expr, type_name, method_name, actuals);
    }

    @Override
    public Tree visitDynamicDispatch(CoolParser.DynamicDispatchContext ctx) {
        List<ExpressionNode> actuals = new ArrayList<>();
        ExpressionNode expr = (ExpressionNode) visit(ctx.defaultExpr());
        for (int i = 0; i < ctx.expr().size(); i++) {
            actuals.add((ExpressionNode) visitExpr(ctx.expr(i)));
        }
        return new DispatchNode(1, expr, new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine()), actuals);
    }

    @Override
    public Tree visitMethodCall(CoolParser.MethodCallContext ctx) {
        List<ExpressionNode> actuals = new ArrayList<>();
        ExpressionNode expr = new ObjectNode(1, new Symbol("self", ctx.getStart().getLine()));
        for (int i = 0; i < ctx.expr().size(); i++) {
            actuals.add((ExpressionNode) visitExpr(ctx.expr(i)));
        }
        return new DispatchNode(1, expr, new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine()), actuals);
    }

    @Override
    public Tree visitComplement(CoolParser.ComplementContext ctx) {
        return new NegNode(1, (ExpressionNode) visit(ctx.defaultExpr()));
    }

    @Override
    public Tree visitIsVoid(CoolParser.IsVoidContext ctx) {
        return new IsVoidNode(1, (ExpressionNode) visit(ctx.defaultExpr()));
    }

    @Override
    public Tree visitParenthesis(CoolParser.ParenthesisContext ctx) {
        return visitExpr(ctx.expr());
    }

    @Override
    public Tree visitAddSub(CoolParser.AddSubContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.defaultExpr(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.defaultExpr(1));

        if (ctx.PLUS_OPERATOR() != null) return new PlusNode(1, left, right);
        if (ctx.MINUS_OPERATOR() != null) return new SubNode(1, left, right);

        return null;
    }

    @Override
    public Tree visitMultDiv(CoolParser.MultDivContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.defaultExpr(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.defaultExpr(1));

        if (ctx.MULT_OPERATOR() != null) return new MulNode(1, left, right);
        if (ctx.DIV_OPERATOR() != null) return new DivideNode(1, left, right);

        return null;
    }

    @Override
    public Tree visitNot(CoolParser.NotContext ctx) {
        return new CompNode(1, (ExpressionNode) visitExpr(ctx.expr()));
    }

    @Override
    public Tree visitAssign(CoolParser.AssignContext ctx) {
        return new AssignNode(1, new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine()), (ExpressionNode) visitExpr(ctx.expr()));
    }

    @Override
    public Tree visitLet(CoolParser.LetContext ctx) {
        List<Symbol> identifiers = new ArrayList<>();
        List<Symbol> typeDecls = new ArrayList<>();
        List<ExpressionNode> inits = new ArrayList<>();
        for (int i = 0; i < ctx.OBJECTID().size(); i++) {
            Symbol identifier = new Symbol(ctx.OBJECTID(i).getText(), ctx.getStart().getLine());
            Symbol typeDecl = new Symbol(ctx.TYPEID(i).getText(), ctx.getStart().getLine());
            ExpressionNode init = (ctx.ASSIGN_OPERATOR(i) != null)
                    ? (ExpressionNode) visitExpr(ctx.expr(i))
                    : new NoExpressionNode(1);
            identifiers.add(identifier);
            typeDecls.add(typeDecl);
            inits.add(init);
        }
        ExpressionNode body = (ExpressionNode) visitExpr(ctx.expr(ctx.expr().size() - 1));
        for (int i = identifiers.size() - 1; i >= 0; i--) {
            body = new LetNode(1, identifiers.get(i), typeDecls.get(i), inits.get(i), body);
        }
        return body;
    }

    @Override
    public Tree visitConditional(CoolParser.ConditionalContext ctx) {
        return new CondNode(1, (ExpressionNode) visitExpr(ctx.expr(0)), (ExpressionNode) visitExpr(ctx.expr(1)), (ExpressionNode) visitExpr(ctx.expr(2)));
    }

    @Override
    public Tree visitLoop(CoolParser.LoopContext ctx) {
        return new LoopNode(1, (ExpressionNode) visitExpr(ctx.expr(0)), (ExpressionNode) visitExpr(ctx.expr(1)));
    }

    @Override
    public Tree visitBlock(CoolParser.BlockContext ctx) {
        List<ExpressionNode> expr_list = new ArrayList<>();
        for (CoolParser.ExprContext e : ctx.expr()) {
            expr_list.add((ExpressionNode) visitExpr(e));
        }
        return new BlockNode(1, expr_list);
    }

    @Override
    public Tree visitCase(CoolParser.CaseContext ctx) {
        ExpressionNode expr = (ExpressionNode) visitExpr(ctx.expr(0));
        List<BranchNode> branches = new ArrayList<>();
        int caseBranchCount = ctx.OBJECTID().size();
        for (int i = 0; i < caseBranchCount; i++) {
            Symbol identifier = new Symbol(ctx.OBJECTID(i).getText(), ctx.getStart().getLine());
            Symbol type_decl = new Symbol(ctx.TYPEID(i).getText(), ctx.getStart().getLine());
            ExpressionNode branch_expr = (ExpressionNode) visitExpr(ctx.expr(i + 1));
            branches.add(new BranchNode(1, identifier, type_decl, branch_expr));
        }
        return new CaseNode(1, expr, branches);
    }

    @Override
    public Tree visitNew(CoolParser.NewContext ctx) {
        return new NewNode(1, new Symbol(ctx.TYPEID().getText(), ctx.getStart().getLine()));
    }

    @Override
    public Tree visitObject(CoolParser.ObjectContext ctx) {
        return new ObjectNode(1, new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine()));
    }

    @Override
    public Tree visitInteger(CoolParser.IntegerContext ctx) {
        return new IntConstNode(1, new Symbol(ctx.INT_CONST().getText(), ctx.getStart().getLine()));
    }

    @Override
    public Tree visitString(CoolParser.StringContext ctx) {
        String rawString = ctx.STR_CONST().getText().substring(1, ctx.STR_CONST().getText().length() - 1);
        rawString = rawString.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\r", "\r")
                .replace("\\\\", "\\");
        return new StringConstNode(1, new Symbol(rawString, ctx.getStart().getLine()));
    }

    @Override
    public Tree visitTrue(CoolParser.TrueContext ctx) {
        return new BoolConstNode(1, true);
    }

    @Override
    public Tree visitFalse(CoolParser.FalseContext ctx) {
        return new BoolConstNode(1, false);
    }

}