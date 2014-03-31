package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Visitor;

public class ByteCodeOptimizer
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ByteCodeOptimizer(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void optimize()
	{
		ClassGen gen = new ClassGen(original);
		//array of methods in the supplied class.
		Method[] methods = gen.getMethods();
		
		//cycle through these methods to see if any optimization is needed
		//in the example there are two methods; method one has no GOTOs, but method two does.
		for (int i = 0; i < methods.length; i ++)
		{
			//Check method's code to see if goto exists.
			String method = methods[i].getCode().toString();
			if (method.contains("goto"))
			{
				//The MethodGen object lets you edit a method.
				//Here I am copying the method from the original class into this object.
				MethodGen mg = new MethodGen(methods[i], original.getClassName(), gen.getConstantPool());
				
				//Get list of instructions from method.
				InstructionList il = mg.getInstructionList();
				
				//Get array of instruction handles.
				InstructionHandle[] ih = il.getInstructionHandles();	
				
				//Cycle through instructions to see which one has a goto.
				for(int j = 0; j < ih.length; j ++)
				{
					if (ih[j].getInstruction().toString().contains("goto"))
					{
						//mark first position, we now need to find where the final goto is.
						int first = ih[j].getPosition();
						int second = 0;
						InstructionHandle current = ih[j];
						InstructionHandle next = ih[j].getNext();
						//the algorithm works like this
						//1. get the next instruction from here. use current.getNext(); [i think!]
						//2. check to see if it is a GOTO. if it is a GOTO, mark second as it's index. Get index by using getPosition(). repeat from step 1.
						//3. if it is not a GOTO, get index of this instruction, create a new GOTO at first index which targets the instruction at second.
						//4. somehow delete all the redundant GOTOs...not sure what the best way would be. Perhaps while traversing through the code, you store all the redundant GOTOs indexes in an arraylist or something? not sure.
						//5. done. well...ensure the whole method has been accounted for. as in, all GOTOs in a method have been optimized.
					}
				}
			}
		}
		
		this.optimized = gen.getJavaClass();
	}
	
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		ByteCodeOptimizer optimizer = new ByteCodeOptimizer(args[0]);
		optimizer.write(args[1]);

	}
}