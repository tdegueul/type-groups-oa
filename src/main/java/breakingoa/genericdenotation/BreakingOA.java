package breakingoa.genericdenotation;

// Exp OA + PP
interface ExpAlg<E> {
	E lit(int n);
	E add(E lhs, E rhs);
	E mul(E lhs, E rhs);
}

interface IPrint {
	String print();
}

interface PrintExp extends ExpAlg<IPrint> {
	@Override public default IPrint lit(int n) {
		return () -> Integer.toString(n);
	}
	@Override public default IPrint add(IPrint lhs, IPrint rhs) {
		return () -> lhs.print() + " + " + rhs.print();
	}
	@Override public default IPrint mul(IPrint lhs, IPrint rhs) {
		return () -> lhs.print() + " * " + rhs.print();
	}
}

// Syn Extension
interface SubAlg<E> extends ExpAlg<E> {
	E sub(E lhs, E rhs);
}

interface PrintSub extends PrintExp, SubAlg<IPrint> {
	@Override public default IPrint sub(IPrint lhs, IPrint rhs) {
		return () -> lhs.print() + " - " + rhs.print();
	}
}

// Sem Extension
interface IEval {
	int eval();
}

interface EvalExp extends ExpAlg<IEval> {
	@Override public default IEval lit(int n) {
		return () -> n;
	}
	@Override public default IEval add(IEval lhs, IEval rhs) {
		return () -> lhs.eval() + rhs.eval();
	}
	@Override public default IEval mul(IEval lhs, IEval rhs) {
		return () -> lhs.eval() * rhs.eval();
	}
}

interface EvalSub extends EvalExp, SubAlg<IEval> {
	@Override public default IEval sub(IEval lhs, IEval rhs) {
		return () -> lhs.eval() - rhs.eval();
	}
}

// Another lang: Boolean expressions
interface BoolAlg<E> {
	E ttrue();
	E ffalse();
	E and(E lhs, E rhs);
	E not(E e);
}

// Ned to redefine this.
// . Or make it generic for <T> ?
interface IBoolEval {
	boolean eval();
}

interface EvalBool extends BoolAlg<IBoolEval> {
	@Override public default IBoolEval ttrue() {
		return () -> true;
	}
	@Override public default IBoolEval ffalse() {
		return () -> false;
	}
	@Override public default IBoolEval and(IBoolEval lhs, IBoolEval rhs) {
		return () -> lhs.eval() && rhs.eval();
	}
	@Override public default IBoolEval not(IBoolEval e) {
		return () -> !e.eval();
	}
}

interface PrintBool extends BoolAlg<IPrint> {
	@Override public default IPrint ttrue() {
		return () -> "true";
	}
	@Override public default IPrint ffalse() {
		return () -> "false";
	}
	@Override public default IPrint and(IPrint lhs, IPrint rhs) {
		return () -> lhs.print() + " && " + rhs.print();
	}
	@Override public default IPrint not(IPrint e) {
		return () -> "¬" + e.print();
	}
}

// Fair enough, let's compose those
interface CompAlg<E, F> extends ExpAlg<E>, BoolAlg<F> {
	E bToI(F b);
	F iToB(E i);
}

interface PrintComp extends PrintExp, PrintBool, CompAlg<IPrint, IPrint> {
	@Override public default IPrint bToI(IPrint b) {
		return () -> "(<int> " + b.print() + ")";
	}
	@Override public default IPrint iToB(IPrint i) {
		return () -> "(<bool> " + i.print() + ")";
	}
}

interface EvalComp extends EvalExp, EvalBool, CompAlg<IEval, IBoolEval> {
	@Override public default IEval bToI(IBoolEval b) {
		return () -> b.eval() ? 1 : 0;
	}
	@Override public default IBoolEval iToB(IEval i) {
		return () -> i.eval() != 0 ? true : false;
	}
}

// Yet again
interface ProgAlg<I, W, P, F, G> extends CompAlg<F, G> {
	I ifelse(G cond, F then, F els);
	W wwhile(G cond, P block);
	P printStr(String s);
	P printInt(F i);
}

interface IVoidEval {
	void eval();
}

interface PrintProg extends PrintComp, ProgAlg<IPrint, IPrint, IPrint, IPrint, IPrint> {
	@Override public default IPrint ifelse(IPrint cond, IPrint then, IPrint els) {
		return () -> "if (" + cond.print() + ")\n\t" + then.print() + "\nelse\n\t" + els.print();
	}
	@Override public default IPrint wwhile(IPrint cond, IPrint block) {
		return () -> "while (" + cond.print() + ")\n\t" + block.print() + "\n";
	}
	@Override public default IPrint printStr(String s) {
		return () -> "printStr(" + s + ")";
	}
	@Override public default IPrint printInt(IPrint i) {
		return () -> "printInt(" + i.print() + ")";
	}
}

interface EvalProg extends EvalComp, ProgAlg<IEval, IVoidEval, IVoidEval, IEval, IBoolEval> {
	@Override public default IEval ifelse(IBoolEval cond, IEval then, IEval els) {
		return () -> cond.eval() ? then.eval() : els.eval();
	}
	@Override public default IVoidEval wwhile(IBoolEval cond, IVoidEval block) {
		return () -> {while (cond.eval()) {
			block.eval();
		}};
	}
	@Override public default IVoidEval printStr(String s) {
		return () -> System.out.println(s);
	}
	@Override public default IVoidEval printInt(IEval i) {
		return () -> System.out.println(i.eval());
	}
}

public class BreakingOA {
	public static void main(String[] args) {
		IEval eExp = makeExp(new EvalExp() {});
		System.out.println("eExp.eval = " + eExp.eval());

		IPrint pExp = makeExp(new PrintExp() {});
		System.out.println("pExp.print = " + pExp.print());

		// Following is statically forbidden => Safe!
		//IEval eSubExp = makeSubExp(new EvalExp());
		IEval eSubExp = makeSubExp(new EvalSub() {});
		System.out.println("eSubExp.eval = " + eSubExp.eval());

		// Following is statically forbidden => Safe!
		//IPrint pSubExp = makeSubExp(new PrintExp());
		IPrint pSubExp = makeSubExp(new PrintSub() {});
		System.out.println("pSubExp.print = " + pSubExp.print());
		
		// But we can ofc use the extended algebra
		// on the non-extended Exp
		IEval eExp2 = makeExp(new EvalSub() {});
		System.out.println("eExp2.eval = " + eExp2.eval());

		IPrint pExp2 = makeExp(new PrintSub() {});
		System.out.println("pExp2.print = " + pExp2.print());
		
		// Now moving on to Booleans
		IBoolEval eBool = makeBool(new EvalBool() {});
		System.out.println("eBool.eval = " + eBool.eval());

		IPrint pBool = makeBool(new PrintBool() {});
		System.out.println("pBool.print = " + pBool.print());
		
		// Now moving on to the Comp thing
		IBoolEval eComp = makeComp(new EvalComp() {});
		System.out.println("eComp.eval = " + eComp.eval());
		
		IPrint pComp = makeComp(new PrintComp() {});
		System.out.println("pComp.print = " + pComp.print());
		
		// Now moving on to the ProgAlg
		IPrint pProg = makeProg(new PrintProg() {});
		System.out.println("pProg.print =\n" + pProg.print());

		IEval eProg = makeProg(new EvalProg() {});
		System.out.println("eProg.eval = " + eProg.eval());
		
		IPrint pProg2 = makeProg2(new PrintProg() {});
		System.out.println("pProg2.print =\n" + pProg2.print());

		IVoidEval eProg2 = makeProg2(new EvalProg() {});
		System.out.println("eProg2.eval = ");
		eProg2.eval();
	}

	private static <E> E makeExp(ExpAlg<E> a) {
		return a.add(a.lit(2), a.mul(a.lit(3), a.lit(4)));
	}

	private static <E> E makeSubExp(SubAlg<E> a) {
		return a.add(a.lit(2), a.sub(a.lit(3), a.lit(4)));
	}

	private static <E> E makeBool(BoolAlg<E> a) {
		return a.and(a.ttrue(), a.not(a.ffalse()));
	}

	// How do I decide of the return type there?
	private static <E, F> F makeComp(CompAlg<E, F> a) {
		return a.and(a.iToB(a.add(a.lit(1), a.lit(2))), a.iToB(a.lit(0)));
	}

	private static <I, W, P, F, G> I makeProg(ProgAlg<I, W, P, F, G> a) {
		return a.ifelse(
			a.iToB(a.mul(a.lit(2), a.add(a.lit(3), a.lit(4)))),
			a.add(a.add(a.lit(2), a.lit(3)), a.lit(4)),
			a.bToI(a.and(a.ffalse(), a.ttrue()))
		);
	}
	
	private static <I, W, P, F, G> W makeProg2(ProgAlg<I, W, P, F, G> a) {
		return a.wwhile(
			a.and(a.not(a.ffalse()), a.ffalse()),
			a.printInt(a.add(a.lit(2), a.lit(3)))
		);
	}
}
