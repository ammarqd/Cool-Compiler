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
            class_node.add((FeatureNode) visitFeature(f));
        }
        return class_node;
    }

    @Override
    public Tree visitFeature(CoolParser.FeatureContext ctx) {

        if (ctx.PARENT_OPEN() != null) {

            Symbol name = new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine());
            List<FormalNode> formals = new ArrayList<>();
            if (ctx.formalList() != null) {
                for (CoolParser.FormalContext formal_ctx : ctx.formalList().formal()) {
                    formals.add((FormalNode) visitFormal(formal_ctx));
                }
            }
            Symbol return_type = new Symbol(ctx.TYPEID().getText(), ctx.getStart().getLine());
            ExpressionNode expr = (ExpressionNode) visitExpr(ctx.expr());
            return new MethodNode(1, name, formals, return_type, expr);
        }
        return null;
    }

    @Override
    public Tree visitFormal(CoolParser.FormalContext ctx) {
        Symbol name = new Symbol(ctx.OBJECTID().getText(), ctx.getStart().getLine());
        Symbol type_decl = new Symbol(ctx.TYPEID().getText(), ctx.getStart().getLine());
        return new FormalNode(1, name, type_decl);
    }
}
