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

// Alternative versions without explicit denotation interfaces
class AltPrint implements ExpAlg<String> {
	@Override public String lit(int n) {
		return Integer.toString(n);
	}
	@Override public String add(String lhs, String rhs) {
		return lhs + " + " + rhs;
	}
	@Override public String mul(String lhs, String rhs) {
		return lhs + " * " + rhs;
	}
}

class AltPrintSub extends AltPrint implements SubAlg<String> {
	@Override
	public String sub(String lhs, String rhs) {
		return lhs + " - " + rhs;
	}
}

class AltEval implements ExpAlg<Integer> {
	@Override
	public Integer lit(int n) {
		return n;
	}
	@Override
	public Integer add(Integer lhs, Integer rhs) {
		return lhs + rhs;
	}
	@Override
	public Integer mul(Integer lhs, Integer rhs) {
		return lhs * rhs;
	}
}

class AltEvalSub extends AltEval implements SubAlg<Integer> {
	@Override
	public Integer sub(Integer lhs, Integer rhs) {
		return lhs - rhs;
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

		// Just showing the alternative versions
		Integer eExp3 = makeExp(new AltEval());
		System.out.println("eExp3 = " + eExp3);

		String pExp3 = makeExp(new AltPrint());
		System.out.println("pExp3 = " + pExp3);

		Integer eExp4 = makeSubExp(new AltEvalSub());
		System.out.println("eExp4 = " + eExp4);

		String pExp4 = makeSubExp(new AltPrintSub());
		System.out.println("pExp4 = " + pExp4);
	}

	private static <E> E makeExp(ExpAlg<E> e) {
		return e.add(e.lit(2), e.mul(e.lit(3), e.lit(4)));
	}

	private static <E> E makeSubExp(SubAlg<E> e) {
		return e.add(e.lit(2), e.sub(e.lit(3), e.lit(4)));
	}
}
