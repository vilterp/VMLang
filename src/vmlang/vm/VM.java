package vmlang.vm;

import java.io.IOException;

import vmlang.common.Opcodes;

public class VM {
	
	// registers
	private int A, B, SP, BP;
	private int counter;
	
	// memory
	private byte[] program;
	private byte[] memory;
	
	private static final Opcodes[] opcodes = Opcodes.values(); // inefficient? 
	
	public VM(byte[] prog, int mem_size) {
		program = prog;
		memory = new byte[mem_size];
	}
	
	public void run() {
		int nextAddr;
		while(true) {
			switch(opcodes[progReadByte()]) {
				
				// FLOW CONTROL
				
				// basic
				case STOP:
					return;
				case GOTO:
					counter = progReadInt();
					break;
				case GOTO_A:
					counter = A;
					break;
				// conditional jumps: based on post-SUB state of register A
				case IF_EQ:
					nextAddr = progReadInt();
					if(A != 0)
						counter = nextAddr;
					break;
				case IF_LT:
					nextAddr = progReadInt();
					if(A >= 0)
						counter = nextAddr;
					break;
				
				// LOAD & STORE (Von-Neumann bottleneck)
				
				// int constants
				case I_CONST_A:
					A = progReadInt();
					break;
				case I_CONST_B:
					B = progReadInt();
					break;
				// byte constants
				case B_CONST_A:
					A = progReadByte();
					break;
				case B_CONST_B:
					B = progReadByte();
					break;
				// load int
				case I_LOAD_A_SP:
					A = memReadInt(SP);
					break;
				case I_LOAD_B_SP:
					B = memReadInt(SP);
					break;
				case I_LOAD_SP_SP:
					SP = memReadInt(SP);
					break;
				// load byte
				case B_LOAD_A_SP:
					A = memReadByte(SP);
					break;
				case B_LOAD_B_SP:
					B = memReadByte(SP);
					break;
				// store int
				case I_STORE_A_SP:
					memWriteInt(SP,A);
					break;
				// store byte
				case B_STORE_A_SP:
					memWriteByte(SP,(byte)A);
					break;
				// moves
				case MOVE_COUNTER_A:
					A = counter;
					break;
				case MOVE_SP_A:
					A = SP;
					break;
				case MOVE_BP_A:
					A = BP;
					break;
				case MOVE_A_SP:
					SP = A;
					break;
				case MOVE_A_BP:
					BP = A;
					break;
				
				// REGISTER OPERATIONS
				
				// arithmetic
				case I_ADD:
					A = A + B;
					break;
				case I_SUB:
					A = A - B;
					break;
				case I_MUL:
					A = A * B;
					break;
				case I_DIV:
					A = A / B;
					break;
				case I_MOD:
					A = A * B;
					break;
				case INC_A:
					A++;
					break;
				// inc/dec
				case INC_B:
					B++;
					break;
				case INC_SP:
					SP++;
					break;
				case DEC_A:
					A--;
					break;
				case DEC_B:
					B--;
					break;
				case DEC_SP:
					SP--;
					break;
				// logic (0: false; non-0: true)
				case NEG_A:
					if(A == 0)
						A = 1;
					else
						A = 0;
					break;
				case NEG_LT_A:
					if(A < 0)
						A = 1;
					else
						A = 0;
				case AND:
					if(A != 0 && B != 0)
						A = 1;
					else
						A = 0;
					break;
				case OR:
					if(A != 0 || B != 0)
						A = 1;
					else
						A = 0;
					break;
				
				// io
				case PRINT_CHAR_A:
					System.out.print((char)A);
					break;
				case READ_CHAR_A:
					try {
						A = System.in.read();
					} catch(IOException e) {
						System.out.println(e.getMessage());
					}
					break;
			}
		}
	}
	
	public byte progReadByte() {
		byte result = program[counter];
		counter++;
		return result;
	}
	
	public int progReadInt() {
		return ((progReadByte() & 0xff) << 24) |
		       ((progReadByte() & 0xff) << 16) |
		       ((progReadByte() & 0xff) << 8) |
			    (progReadByte() & 0xff);
	}
	
	public byte memReadByte(int addr) {
		return memory[addr];
	}
	
	public int memReadInt(int addr) {
		return ((memReadByte(addr) & 0xff) << 24) |
			   ((memReadByte(addr+1) & 0xff) << 16) |
			   ((memReadByte(addr+2) & 0xff) << 8) |
				(memReadByte(addr+3) & 0xff);
	}
	
	public void memWriteByte(int addr, byte val) {
		memory[addr] = val;
	}
	
	public void memWriteInt(int addr, int val) {
		memWriteByte(addr  ,(byte)(0xff & (val >> 24)));
		memWriteByte(addr+1,(byte)(0xff & (val >> 16)));
		memWriteByte(addr+2,(byte)(0xff & (val >> 8)));
		memWriteByte(addr+3,(byte)(0xff &  val));
	}
	
}