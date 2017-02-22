package breakingoa.model;

// Exp """metamodel"""
abstract class Exp {}
abstract class BinExp extends Exp {
	private Exp lhs;
	private Exp rhs;
	public BinExp(Exp l, Exp r) { lhs = l ; rhs = r ; }
	public Exp getLhs() { return lhs; }
	public Exp getRhs() { return rhs; }
}
class Lit extends Exp {
	private int n;
	public Lit(int v) { n = v; }
	public int getN() { return n; }
}
class Add extends BinExp {
	public Add(Exp l, Exp r) { super(l, r); }
}
class Mul extends BinExp {
	public Mul(Exp l, Exp r) { super(l, r); }
}

// Exp OA + PP
interface ExpAlg<E> {
	E lit(Lit l);
	E add(Add a);
	E mul(Mul m);
	default E $(Exp e) {
		if (e instanceof Lit)
			return lit((Lit) e);
		if (e instanceof Add)
			return add((Add) e);
		if (e instanceof Mul)
			return mul((Mul) e);
		throw new RuntimeException("Oh, Snap! " + e);
	}
}

interface IPrint {
	String print();
}

class PrintExp implements ExpAlg<IPrint> {
	@Override public IPrint lit(Lit l) {
		return () -> Integer.toString(l.getN());
	}
	@Override public IPrint add(Add a) {
		return () -> $(a.getLhs()).print() + " + " + $(a.getRhs()).print();
	}
	@Override public IPrint mul(Mul m) {
		return () -> $(m.getLhs()).print() + " * " + $(m.getRhs()).print();
	}
}

// Syn Extension
class Sub extends BinExp {
	public Sub(Exp l, Exp r) { super(l, r); }
}

interface SubAlg<E> extends ExpAlg<E> {
	E sub(Sub s);
	default E $(Exp e) {
		if (e instanceof Sub)
			return sub((Sub) e);
		else
			return ExpAlg.super.$(e);
	}
}

class PrintSub extends PrintExp implements SubAlg<IPrint> {
	@Override public IPrint sub(Sub s) {
		return () -> $(s.getLhs()).print() + " - " + $(s.getRhs()).print();
	}
}

// Sem Extension
interface IEval {
	int eval();
}

class EvalExp implements ExpAlg<IEval> {
	@Override public IEval lit(Lit l) {
		return () -> l.getN();
	}
	@Override public IEval add(Add a) {
		return () -> $(a.getLhs()).eval() + $(a.getRhs()).eval();
	}
	@Override public IEval mul(Mul m) {
		return () -> $(m.getLhs()).eval() * $(m.getRhs()).eval();
	}
}

class EvalSub extends EvalExp implements SubAlg<IEval> {
	@Override public IEval sub(Sub s) {
		return () -> $(s.getLhs()).eval() - $(s.getRhs()).eval();
	}
}

public class BreakingOA {
	public static void main(String[] args) {
		// On a base model...
		Exp e = makeModel();

		// ... applying the base algebra is fine
		IEval eExp = wrap(e, new EvalExp());
		IPrint pExp = wrap(e, new PrintExp());
		System.out.println("eExp.eval  = " + eExp.eval());
		System.out.println("pExp.print = " + pExp.print());
		
		// ... applying the extended algebra is fine as well
		IEval eExp2 = wrap(e, new EvalSub());
		IPrint pExp2 = wrap(e, new PrintSub());
		System.out.println("eExp2.eval  = " + eExp2.eval());
		System.out.println("pExp2.print = " + pExp2.print());
		
		// On an extended model...
		Exp subE = makeSubModel();

		// ... applying the extended algebra is fine
		IEval eSubE = wrap(subE, new EvalSub());
		IPrint pSubE = wrap(subE, new PrintSub());
		System.out.println("eSubE.eval = " + eSubE.eval());
		System.out.println("pSubE.print = " + pSubE.print());
		
		// ... applying the base algebra fails,
		// and isn't rejected statically
		IEval eSubE2 = wrap(subE, new EvalExp());
		IPrint pSubE2 = wrap(subE, new PrintExp());

		try {
			eSubE2.eval(); // Sub not handled by EvalExp
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
		}

		try {
			pSubE2.print(); // Sub not handled by PrintExp
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	private static Exp makeModel() {
		return new Add(new Lit(2), new Mul(new Lit(3), new Lit(4)));
	}
	
	private static Exp makeSubModel() {
		return new Add(new Lit(2), new Sub(new Lit(3), new Lit(4)));
	}

	/**
	 * The problem arises from the wrap function, as there's
	 * no way to strongly link Exp to the *Alg<E> that has
	 * to handle it. So, we may end up with a SubExp being
	 * evaluated with a ExpAlg<E> (and not SubAlg<E>) => crash!
	 * 
	 * The problem is solved if we can make the distinction
	 * between Exp and Exp' => which is the case if they
	 * belong to different type groups.
	 */
	private static <E> E wrap(Exp e, ExpAlg<E> alg) {
		return alg.$(e);
	}

	private static <E> E wrap(Exp e, SubAlg<E> alg) {
		return alg.$(e);
	}
}
