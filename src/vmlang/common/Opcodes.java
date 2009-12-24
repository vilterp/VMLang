package vmlang.common;

public enum Opcodes {

	STOP,
	GOTO,
	GOTO_A,
	IF_EQ,
	IF_LT,
	I_CONST_A,
	I_CONST_B,
	B_CONST_A,
	B_CONST_B,
	I_LOAD_A_SP,
	I_LOAD_B_SP,
	I_LOAD_SP_SP,
	B_LOAD_A_SP,
	B_LOAD_B_SP,
	I_STORE_A_SP,
	B_STORE_A_SP,
	MOVE_COUNTER_A,
	MOVE_SP_A,
	MOVE_BP_A,
	MOVE_A_SP,
	MOVE_A_BP,
	I_ADD,
	I_SUB,
	I_MUL,
	I_DIV,
	I_MOD,
	INC_A,
	INC_B,
	INC_SP,
	DEC_A,
	DEC_B,
	DEC_SP,
	NEG_A,
	NEG_LT_A,
	AND,
	OR,
	PRINT_CHAR_A,
	READ_CHAR_A,
	STACK_START;

	public byte toByte() {
		return (byte)ordinal();
	}
}
