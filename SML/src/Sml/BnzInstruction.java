package Sml;

/**
 * BnzInstruction register label
 * If the register is non-zero branch to label
 * 
 * @author K. Dolan
 */

public class BnzInstruction extends Instruction {

	private int op1;
	private String op2;
	
	public BnzInstruction(String label, String op) {
		super(label, op);
	}
	
	public BnzInstruction(String label, int op1, String op2) {
		this(label, "bnz");
		this.op1 = op1;
		this.op2 = op2;
	}

	@Override
	public void execute(Machine m) {
		if (m.getRegisters().getRegister(op1) != 0) {
			int index = m.getLabels().indexOf(op2);
			if (index != -1) {
				m.setPc(index);
			}
		}
	}

	@Override
	public String toString() {
		return super.toString() + " " + op1 + " to label " + op2;
	}

}
