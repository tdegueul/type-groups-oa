package breakingoa.classic;

// Exp OA + PP
interface ExpAlg<E> {
	E lit(int n);
	E add(E lhs, E rhs);
	E mul(E lhs, E rhs);
}

interface IPrint {
	String print();
}

class PrintExp implements ExpAlg<IPrint> {
	@Override public IPrint lit(int n) {
		return () -> Integer.toString(n);
	}
	@Override public IPrint add(IPrint lhs, IPrint rhs) {
		return () -> lhs.print() + " + " + rhs.print();
	}
	@Override public IPrint mul(IPrint lhs, IPrint rhs) {
		return () -> lhs.print() + " * " + rhs.print();
	}
}

// Syn Extension
interface SubAlg<E> extends ExpAlg<E> {
	E sub(E lhs, E rhs);
}

class PrintSub extends PrintExp implements SubAlg<IPrint> {
	@Override public IPrint sub(IPrint lhs, IPrint rhs) {
		return () -> lhs.print() + " - " + rhs.print();
	}
}

// Sem Extension
interface IEval {
	int eval();
}

class EvalExp implements ExpAlg<IEval> {
	@Override public IEval lit(int n) {
		return () -> n;
	}
	@Override public IEval add(IEval lhs, IEval rhs) {
		return () -> lhs.eval() + rhs.eval();
	}
	@Override public IEval mul(IEval lhs, IEval rhs) {
		return () -> lhs.eval() * rhs.eval();
	}
}

class EvalSub extends EvalExp implements SubAlg<IEval> {
	@Override public IEval sub(IEval lhs, IEval rhs) {
		return () -> lhs.eval() - rhs.eval();
	}
}

public class BreakingOA {
	public static void main(String[] args) {
		IEval eExp = makeExp(new EvalExp());
		System.out.println("eExp.eval = " + eExp.eval());

		IPrint pExp = makeExp(new PrintExp());
		System.out.println("pExp.print = " + pExp.print());

		// Following is statically forbidden => Safe!
		//IEval eSubExp = makeSubExp(new EvalExp());
		IEval eSubExp = makeSubExp(new EvalSub());
		System.out.println("eSubExp.eval = " + eSubExp.eval());

		// Following is statically forbidden => Safe!
		//IPrint pSubExp = makeSubExp(new PrintExp());
		IPrint pSubExp = makeSubExp(new PrintSub());
		System.out.println("pSubExp.print = " + pSubExp.print());
		
		// But we can ofc use the extended algebra
		// on the non-extended Exp
		IEval eExp2 = makeExp(new EvalSub());
		System.out.println("eExp2.eval = " + eExp2.eval());

		IPrint pExp2 = makeExp(new PrintSub());
		System.out.println("pExp2.print = " + pExp2.print());		
	}

	private static <E> E makeExp(ExpAlg<E> e) {
		return e.add(e.lit(2), e.mul(e.lit(3), e.lit(4)));
	}

	private static <E> E makeSubExp(SubAlg<E> e) {
		return e.add(e.lit(2), e.sub(e.lit(3), e.lit(4)));
	}
}
