package breakingoa.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AS
 */
class Fsm {
	private String name;
	private List<State> states = new ArrayList<>();
	private List<Trans> trans = new ArrayList<>();
	public Fsm(String n) { name = n; }
	public String getName() { return name; }
	public List<State> getStates() { return states; }
	public List<Trans> getTrans() { return trans; }
}

class State {
	private String name;
	public State(String n) { name = n; }
	public String getName() { return name; }
}

class Trans {
	private String event;
	private String output;
	private State from;
	private State to;
	public Trans(String e, String o) { event = e; output = o; }
	public String getEvent() { return event; }
	public String getOutput() { return output; }
	public void setFrom(State s) { from = s; }
	public void setTo(State s) { to = s; }
	public State getFrom() { return from; }
	public State getTo() { return to; }
}

/**
 * OA
 */
interface FsmAlg<E> {
	E f(Fsm f);
	E s(State s);
	E t(Trans t);
	default E $(Trans t) {
		if (t instanceof Trans)
			return t(t);
		else
			throw new RuntimeException("Oh, Snap! " + t);
	}
}

interface IPrint {
	String print();
}

class PrintFsm implements FsmAlg<IPrint> {
	@Override public IPrint f(Fsm f) {
		return () -> f.getName() + ":\n"
				+ f.getStates().stream().map(s -> s(s).print())
				.collect(Collectors.joining("\n")) + "\n"
				+ f.getTrans().stream().map(s -> $(s).print())
				.collect(Collectors.joining("\n"));
	}
	@Override public IPrint s(State s) {
		return () -> s.getName();
	}
	@Override public IPrint t(Trans t) {
		return () -> t.getEvent() + " [" + t.getOutput() + "]";
	}
}

// Syntax extension
class GTrans extends Trans {
	private Guard guard;
	
	GTrans(String e, String o) {
		super(e, o);
	}
	public Guard getGuard() { return guard; }
	public void setGuard(Guard g) { guard = g; }
}

class Guard {
	private boolean okay;
	Guard(boolean o) { okay = o; }
	public boolean getOkay() { return okay; }
}

interface GFsmAlg<E> extends FsmAlg<E> {
	@Override default E $(Trans t) {
		if (t instanceof GTrans)
			return gt((GTrans) t);
		else if (t instanceof Trans)
			return t(t);
		else
			throw new RuntimeException("Oh, Snap! " + t);
	}
	E gt(GTrans t);
	E g(Guard g);
}

class PrintGFsm extends PrintFsm implements GFsmAlg<IPrint> {
	@Override public IPrint gt(GTrans t) {
		// FIXME: How do I override "super"-print from Trans here?
		return () -> t.getEvent() + " [" + t.getOutput() + "] -- " + g(t.getGuard()).print();//TODO
	}
	@Override public IPrint g(Guard g) {
		return () -> "" + g.getOkay();
	}
}

public class BreakingOA {
	public static void main(String[] args) {
		Fsm f1 = makeModel();
		IPrint p1 = wrap(f1, new PrintFsm());
		System.out.println("p1.print = " + p1.print());
		IPrint p2 = wrap(f1, new PrintGFsm());
		System.out.println("p2.print = " + p2.print());
		
		Fsm f2 = makeGuardedModel();
		IPrint p3 = wrap(f2, new PrintFsm());
		System.out.println("p3.print = " + p3.print());
		IPrint p4 = wrap(f2, new PrintGFsm());
		System.out.println("p4.print = " + p4.print());
	}

	private static <E> E wrap(Fsm f, FsmAlg<E> alg) {
		return alg.f(f);
	}
	
	private static Fsm makeModel() {
		Fsm f = new Fsm("myFsm");
		State s1 = new State("s1");
		State s2 = new State("s2");
		Trans t1 = new Trans("a", "1");
		Trans t2 = new Trans("b", "2");
		t1.setFrom(s1);
		t1.setTo(s2);
		t2.setFrom(s2);
		t2.setTo(s1);
		f.getStates().add(s1);
		f.getStates().add(s2);
		f.getTrans().add(t1);
		f.getTrans().add(t2);
		return f;
	}

	private static Fsm makeGuardedModel() {
		Fsm f = new Fsm("myGuardedFsm");
		State s1 = new State("s1");
		State s2 = new State("s2");
		Guard g = new Guard(true);
		GTrans t1 = new GTrans("a", "1");
		t1.setGuard(g);
		Trans t2 = new Trans("b", "2");
		t1.setFrom(s1);
		t1.setTo(s2);
		t2.setFrom(s2);
		t2.setTo(s1);
		f.getStates().add(s1);
		f.getStates().add(s2);
		f.getTrans().add(t1);
		f.getTrans().add(t2);
		return f;
	}
}
