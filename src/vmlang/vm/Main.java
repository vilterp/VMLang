package vmlang.vm;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
	
	private static final int MEM_SIZE = 1024; // arbitrary
	
	public static void main(String[] args) {
		if(args.length > 1)
			System.out.println("usage: vmlang [file to run]");
		else {
			try {
				File inFile = new File(args[0]);
				DataInputStream in = new DataInputStream(new FileInputStream(inFile));
				int length = (int)inFile.length();
				byte[] prog = new byte[length];
				for(int i=0; i < length; i++)
					prog[i] = in.readByte();
				VM vm = new VM(prog,MEM_SIZE);
				vm.run(); // what all this hubbub is about...
			} catch (FileNotFoundException e) {
				System.out.printf("couldn't find file \"%s\"\n",args[0]);
			} catch (IOException e) {
				System.out.printf("error reading file \"%s\"\n",args[0]);
			}
		}
	}

}
