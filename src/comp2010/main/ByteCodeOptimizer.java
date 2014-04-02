package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;
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
		  Method[] methods = gen.getMethods();

		  for (int i = 0; i < methods.length; i ++)
		  {
			  String method = methods[i].getCode().toString();
			  if (method.contains("goto"))
			  {
				  MethodGen mg = new MethodGen(methods[i], original.getClassName(), gen.getConstantPool());

				  InstructionList oldInstructionList = mg.getInstructionList();
				  InstructionList newInstructionList = new InstructionList();

				  ArrayList<Integer> gotoIndices = new ArrayList<Integer>();

				  InstructionHandle[] ih = oldInstructionList.getInstructionHandles();   

				  int size = 0;
				  int additional = 0;
				  for(int j = 0; j < ih.length; j ++)
				  {

					  if((ih[j].getInstruction().toString().contains("goto")) && (gotoIndices.contains(ih[j].getPosition())) )
					  {
						  additional++;
					  }
					  else if (ih[j].getInstruction().toString().contains("goto"))
					  {
						  InstructionHandle current = ih[j];

						  while(current.getInstruction().toString().contains("goto"))
						  {
							  current = current.getNext();
							  gotoIndices.add(current.getPosition());
							  size++;
						  }

						  for (int c = 0; c < size-1 + additional; c ++)
						  {
							  current = current.getPrev();
						  }

						  GOTO newGoto = new GOTO(current);
						  newInstructionList.append(newGoto);
						  size = 0;
					  }
					  else
					  {
						  newInstructionList.append(ih[j].getInstruction());

					  }
				  }
				  mg.setInstructionList(newInstructionList);
				  Method updated = mg.getMethod();
				  gen.removeMethod(methods[i]);
				  gen.addMethod(updated);
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