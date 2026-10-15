/**
 * Authors: Htet, Nyi and Roddy, Hannah
 * CS152 Fall 2026
 */

package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

/**
 * FWJS expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * FWJS constants.
 */
class ValueExpr implements Expression {
    private Value val;
    public ValueExpr(Value v) {
        this.val = v;
    }
    public Value evaluate(Environment env) {
        return this.val;
    }
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
    private String varName;
    public VarExpr(String varName) {
        this.varName = varName;
    }
    public Value evaluate(Environment env) {
        return env.resolveVar(varName);
    }
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
    private Expression exp;
    public PrintExpr(Expression exp) {
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        System.out.println(v.toString());
        return v;
    }
}
/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
    private Op op;
    private Expression e1;
    private Expression e2;
    public BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @SuppressWarnings("incomplete-switch")
    public Value evaluate(Environment env) {
        // evaluate both operands in the current environment
        Value v1 = e1.evaluate(env);
        Value v2 = e2.evaluate(env);

        // only numbers are supported, so reject anything that isn't an IntVal
        if (!(v1 instanceof IntVal) || !(v2 instanceof IntVal)) {
            throw new RuntimeException("Binary operators expect numbers");
        }

        // unwrap the Java ints so we can do the math
        int i = ((IntVal) v1).toInt();
        int j = ((IntVal) v2).toInt();
 
        // arithmetic ops return an IntVal; comparison ops return a BoolVal
        switch (op) {
            case ADD:      return new IntVal(i + j);
            case SUBTRACT: return new IntVal(i - j);
            case MULTIPLY: return new IntVal(i * j);
            case DIVIDE:   return new IntVal(i / j);
            case MOD:      return new IntVal(i % j);
            case GT:       return new BoolVal(i > j);
            case GE:       return new BoolVal(i >= j);
            case LT:       return new BoolVal(i < j);
            case LE:       return new BoolVal(i <= j);
            case EQ:       return new BoolVal(i == j);
        }

        // reached only if the switch didn't handle the operator
        throw new RuntimeException("Unknown operator: " + op);
    }
}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
    private Expression cond;
    private Expression thn;
    private Expression els;
    public IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }
    public Value evaluate(Environment env) {
        // evaluate the condition; it must produce a boolean
        Value v = cond.evaluate(env);
        if (!(v instanceof BoolVal)) {
            throw new RuntimeException("If condition must be a boolean");
        }

        // evaluate only the branch that was chosen and return its value
        if (((BoolVal) v).toBoolean()) {
            return thn.evaluate(env);
        } else {
            return els.evaluate(env);
        }
    }
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
    private Expression cond;
    private Expression body;
    public WhileExpr(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }
    public Value evaluate(Environment env) {
        // re-check the condition before every pass through the loop
        while (true) {
            Value v = cond.evaluate(env);
            if (!(v instanceof BoolVal)) {
                throw new RuntimeException("While condition must be a boolean");
            }

            // stop once the condition is false; otherwise run the body again
            if (!((BoolVal) v).toBoolean()) {
                break;
            }
            body.evaluate(env);
        }
        // the while loop has no meaningful result, so it evaluates to null.
        return new NullVal();
    }
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
    private Expression e1;
    private Expression e2;
    public SeqExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Value evaluate(Environment env) {
        // run e1 for its side effects and discard its value
        // the whole sequence evaluates to e2's value
        e1.evaluate(env);
        return e2.evaluate(env);
    }
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
    private String varName;
    private Expression exp;
    public VarDeclExpr(String varName, Expression exp) {
        this.varName = varName;
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        // evaluate the initial value, then declare the variable in the LOCAL scope

        Value v = exp.evaluate(env);
        env.updateVar(varName, v);
        return v;
    }
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
    private String varName;
    private Expression e;
    public AssignExpr(String varName, Expression e) {
        this.varName = varName;
        this.e = e;
    }
    public Value evaluate(Environment env) {
        // evaluate the new value, then update the nearest scope that has the variable
        // if no scope has it, updateVar adds it to the global scope
        Value eVal = e.evaluate(env);
        env.updateVar(varName, eVal);
        return eVal;
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private List<String> params;
    private Expression body;
    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params;
        this.body = body;
    }
    public Value evaluate(Environment env) {
        // capture the current environment so the closure remembers it
        return new ClosureVal(params, body, env);
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private Expression f;
    private List<Expression> args;
    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args;
    }
    public Value evaluate(Environment env) {
        // evaluate the function expression; it must produce a closure
        Value fv = f.evaluate(env);
        if (!(fv instanceof ClosureVal)) {
            throw new RuntimeException("Attempted to call a non-function");
        }
 
        // evaluate arguments in the caller's environment
        List<Value> argVals = new ArrayList<Value>();
        for (Expression arg : args) {
            argVals.add(arg.evaluate(env));
        }

        // apply() binds params to args in a new scope and runs the body
        return ((ClosureVal) fv).apply(argVals);
    }
}


