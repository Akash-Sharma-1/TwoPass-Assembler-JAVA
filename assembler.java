import java.util.*;
import java.io.*;
//AKASH SHARMA -2017327
//TANYA KAPUR -2017366
class symbol
{
	String name;
	int address;
	int indicator;

	symbol(String s, int add, int indi)
	{
		name=s;
		address=add;
		indicator=indi;
	}
}

class literal
{
	String name;
	int address;
	literal(String s, int add)
	{
		name=s;
		address=add;
	}
}

class intermediate
{
	int opcode;
	int[] symtab; 
	int littab;


	intermediate(int i, int[] s, int l)
	{
		opcode=i;
		symtab=s;
		littab=l;

	}
}

class mot
{
	int opno;
	int address;
	String operandtype;
}


public class assembler
{

	public static boolean checkopcode(ArrayList<String> opcodetable,String x)
	{
	
		for(int i=0;i<opcodetable.size();i++)
		{
			if((opcodetable.get(i)).equals(x))
			{
				
				return true; 
			}
		}
		return false;

	}

	public static boolean checksymbol(ArrayList<symbol> symtable,String x)
	{
	
		for(int i=0;i<symtable.size();i++)
		{
			if((symtable.get(i).name).equals(x))
			{
				return true; 
			}
		}
		return false;
	}

	public static int findsymbol(ArrayList<symbol> symtable,String x)
	{
		for(int i=0;i<symtable.size();i++)
		{
			if((symtable.get(i).name).equals(x))
			{
				return i; 
			}
		}
		return -1;

	}
	public static int findliteral(ArrayList<literal> symtable,String x)
	{
		for(int i=0;i<symtable.size();i++)
		{
			if((symtable.get(i).name).equals(x))
			{
				return i; 
			}
		}
		return -1;

	}
	public static int findopcode(ArrayList<String> table,String x)
	{
		for(int i=0;i<table.size();i++)
		{
			if((table.get(i)).equals(x))
			{
				return i; 
			}
		}
		return -1;

	}
	public static ArrayList<intermediate> passone(BufferedReader br, ArrayList<String> opcodetable, ArrayList<symbol> symboltable, ArrayList<literal> literaltable,int start,ArrayList<mot> pseudoopcodetable,HashMap<String, Integer> varvalues)
	{ try
		{
			ArrayList<intermediate> code=new ArrayList<intermediate>();
			String s;
			
			while((s=br.readLine())!=null)
			{
				start++;
				int cntsymbol=0;
				int[] symno={-1,-1};
				int literalvalue=-1;
				String[] a=s.split("\t");
				int len=a.length;
				String label=a[0];
				String opcode=a[1];
				String operand=a[2];
				String comment;
				if(len>3) comment=a[3];
				symbol sym;
//LABEL CHECK
				if(!(label.equals(""))) //IF THERE IS A LABEL
				{

					// IF THE LABEL IS ALREADY NOT USED
					if(!checksymbol(symboltable,label))
					{
						if(!checkopcode(opcodetable,label))
						{
							sym= new symbol(label,start,0);
							symboltable.add(sym);
							symno[cntsymbol]=findsymbol(symboltable,label);
							cntsymbol++;
						}
						else
						{
							System.out.println("INVALID LABEL , ITS AN OPCODE ..... "+"("+" "+label+" "+")");
								return null;
						}
						

					}
					else
					{
						int no=findsymbol(symboltable,label);
						symbol fakelabel=symboltable.get(no);
						//declared symbol has address as -1
						if(fakelabel.address!=-1 && fakelabel.indicator==0) 
						{
								System.out.println("LABEL ALREADY USED.....");
								return null;
						}	
						else
						{
							if(findopcode(opcodetable,opcode)!=13)
							{
								System.out.println("ALREADY USED AS A VARIABLE..... "+"("+" "+label+" "+")");
								return null;
							}

							
						}


					}	
				}
//CHECKING IF THE OPCODE REALLY EXISTS
				mot m=new mot();
				if(!checkopcode(opcodetable,opcode))
				{
						System.out.println("NO SUCH OPCODE EXISTS....");
						return null;
				}
				if(checkopcode(opcodetable,opcode))
				{
						m.opno=findopcode(opcodetable,opcode);
						m.address=start;
				}
//OPERAND CHECK
				symbol variable;
				
				//NO NEED FOR OPERANDS FOR CLA AND STP
				if(((opcode.equals("CLA")) || (opcode.equals("STP"))))
				{
					if(!operand.equals(""))
					{
						System.out.println("WRONG OPERAND GIVEN...");
						return null;
					}
					m.operandtype="-";
					if(opcode.equals("STP"))
					{
						pseudoopcodetable.add(m);
						return code;
					}
				}
				else
				{
					// DIRECT VALUES NEEDED FOR DC (DECLARATION OF VARIABLES)
					if(opcode.equals("DC"))
					{
						//ADD ADDRESSES TO THE SYMBOL IN THE TABLE	
						
						int index=findsymbol(symboltable,label);
						if(index!=-1)
						{
							//allocate address to its respective symbol 
							variable=symboltable.get(index);
							if(variable.indicator==1)
							{
								if(variable.address==-1)variable.address=start;
								else System.out.println("ALREADY DEFINED..");
							}
							else
							{
								System.out.println("ITS A LABEL.. CAN'T DECLARE");
								return null;
							}
							

						} 
						else
						{

							//create a new symbol
							symbol declared= new symbol(label,start,1);
							symboltable.add(declared);
						}
						literalvalue=Integer.parseInt(operand);
						m.operandtype="value";
						varvalues.put(label,literalvalue);

					}
					//BRANCH STATEMENTS USES LABELS ONLY
					if(opcode.equals("BRN") || (opcode.equals("BRP")) || (opcode.equals("BRZ")))
					{

						int index=findsymbol(symboltable,operand);
						
						if(index!=-1)
						{
							variable=symboltable.get(index);
							if(variable.indicator!=0)
							{
								System.out.println("ONLY LABELS ARE ALLOWED FOR THE BRANCH STATEMENTS");
								return null;
							}
						}
						else
						{
							symbol nondeclared= new symbol(operand,-1,0);
							symboltable.add(nondeclared);
							int symbolnumber=findsymbol(symboltable,operand);
							symno[cntsymbol]=symbolnumber;
						}


						
					}
					else //CHECK IF OTHER OPCODES HAVE A OPERAND OR NOT -> HANDLE SYMBOLS & LITERALS  
					{
						int index=findsymbol(symboltable,operand);
						if(index!=-1)
						{
							variable=symboltable.get(index);
							if(variable.indicator!=1 )
							{
								System.out.println("LABELS ARE NOT ARE ALLOWED FOR THE BRANCH STATEMENTS");
								return null;
							}
						} 
						
						//symbol (cnt++)
						if(operand.charAt(0)!='=')
						{
							int symbolnumber=findsymbol(symboltable,operand);
							if(symbolnumber!=-1)
							{
								//exists
								symno[cntsymbol]=symbolnumber;
							}
							else
							{
								//doesnt exists
								if(opcode.equals("DC"))
								{
									// dont add to symbols- the values
								}
								else
								{
									symbol nondeclared= new symbol(operand,-1,1);
									symboltable.add(nondeclared);
									symbolnumber=findsymbol(symboltable,operand);
									symno[cntsymbol]=symbolnumber;
								}
								

							}
							m.operandtype="Reg";

						}
						//literal
						else 
						{
							literal l=new literal(operand,start);
							literaltable.add(l);
							String valuelit=operand.substring(1);
						    try
						    {
						        Integer.parseInt(valuelit);
						    }
						    catch(Exception e){
						        System.out.println("Invalid Literal Given");
						        return null;
						    }

							//index of literal on literal table
							literalvalue= findliteral(literaltable,operand);
							m.operandtype="Immed";

						}
					}
				}
				pseudoopcodetable.add(m);
				int opcodenumber=findopcode(opcodetable,opcode);
				intermediate inter=new intermediate(opcodenumber,symno,literalvalue);
				code.add(inter);

			}
			// for(symbol sxs:symboltable)
			// {
			// 	System.out.println(sxs.name+""+sxs.address);
			// }
			// for(literal sxs:literaltable)
			// {
			// 	System.out.println(sxs.name+""+sxs.address);
			// }
			return code;
		}
		catch(IOException e)
		{
			System.out.println("no file found");
		}

		return null;
	}
	public static String binaryconverter(int i)
	{
		return String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
	}
	public static String opbinaryconverter(int i)
	{
		return String.format("%4s", Integer.toBinaryString(i)).replace(' ', '0');
	}
	public static void passtwo(ArrayList<intermediate> code,ArrayList<symbol> symboltable, ArrayList<literal> literaltable, int start,ArrayList<mot> pseudoopcodetable,ArrayList<String> opcodetable,HashMap<String, Integer> varvalues)
	{
		try
		{
			FileWriter fw= new FileWriter("output1.txt");
			PrintWriter pw= new PrintWriter(fw);
			for(int i=0;i<code.size();i++)
			{
			start++;
			int opnumber=code.get(i).opcode;

			//LINE NUMBER
			pw.print("("+start+")"+"\t");
			//OPCODE PRINT
			pw.print(opbinaryconverter(opnumber)+"\t");
			//Symbol Print

			if(code.get(i).symtab[1]==-1)
			{
				if(code.get(i).symtab[0]!=-1)
				{
					pw.print(binaryconverter((symboltable.get(code.get(i).symtab[0])).address)+"\t");
				}
				else
				{
					if(code.get(i).opcode==13)
					{
						pw.print(binaryconverter(code.get(i).littab)+"\t");

					}
					else if(code.get(i).littab!=-1)
					{
						pw.print(binaryconverter((literaltable.get(code.get(i).littab)).address)+"\t");
					}
					
				}
			}
			else
			{
				pw.print(binaryconverter(symboltable.get(code.get(i).symtab[1]).address)+"\t");

			}
			pw.println();
			}
			pw.println("---------------------------------------------------------------------");
			pw.println("GENERATING SYMBOL TABLE");
			pw.println("Index"+"\t"+"Name"+"\t"+"Address"+"\t"+"Label or Var");
			int o=0;
			for(symbol s:symboltable)
			{
				if(s.indicator==0)pw.println(o+"\t"+s.name+"\t"+s.address+"\t"+"label");
				if(s.indicator==1)pw.println(o+"\t"+s.name+"\t"+s.address+"\t"+"variable- "+varvalues.get(s.name));

				o++;
			}
			pw.println("---------------------------------------------------------------------");
			pw.println("GENERATING LITERAL TABLE");
			pw.println("Index"+"\t"+"Name"+"\t"+"Address"+"\t");
			 o=0;
			for(literal s:literaltable)
			{
				pw.println(o+"\t"+s.name+"\t"+s.address);
				o++;
			}
			pw.println("---------------------------------------------------------------------");
			pw.println("GENERATING OPCODE TABLE");
			pw.println("Index"+"\t"+"Opcode"+"\t"+"Binary"+"\t"+"OperandType"+"\t"+"Address"+"\t"+"INSTR. LENGTH");
			o=0;
			for(mot s:pseudoopcodetable)
			{
				pw.println(o+"\t"+opcodetable.get(s.opno)+"\t"+opbinaryconverter(s.opno)+"\t"+s.operandtype+"\t"+"\t"+s.address+"\t"+"12 bits");
				o++;
			}	
			pw.close();

		}
		catch(IOException e)
		{
			System.out.println("no file found");
		}
	}


	public static void main(String[] args) {
		
		try
		{
			FileReader fr =new FileReader("input.txt");
			BufferedReader br= new BufferedReader(fr);
			ArrayList<String> opcodetable= new ArrayList<String>();
			opcodetable.add("CLA");
			opcodetable.add("LAC");
			opcodetable.add("SAC");
			opcodetable.add("ADD");
			opcodetable.add("SUB");
			opcodetable.add("BRZ");
			opcodetable.add("BRN");
			opcodetable.add("BRP");
			opcodetable.add("INP");
			opcodetable.add("DSP");
			opcodetable.add("MUL");
			opcodetable.add("DIV");
			opcodetable.add("STP");
			opcodetable.add("DC");


			ArrayList<symbol> symboltable= new ArrayList<symbol>();
			ArrayList<literal> literaltable= new ArrayList<literal>();
			ArrayList<mot> pseudoopcodetable= new ArrayList<mot>();
			ArrayList<intermediate> intermediate_code=null;
			HashMap<String, Integer> varvalues = new HashMap<>(); 


			String s;
			s=br.readLine();
			int startingaddress=0;
			String[] a=s.split("\t");
			if(a[0].equals("START")) 
				{
					startingaddress=Integer.parseInt(a[1]); 
					intermediate_code=passone(br,opcodetable,symboltable,literaltable,startingaddress,pseudoopcodetable,varvalues);
				}
			else
			{
				System.out.println("NO START STATEMENT");
			}
			
			//CHECKING IF ALL THE SYMBOLS ARE DECLARED OF NOT
			if(intermediate_code!=null)
			{
				int flag=0;
				for(mot m:pseudoopcodetable)
				{
					if(m.opno==12)
					{
						flag=1;
					}
				}
				if(flag==0)
				{
					System.out.println("NO STOP STATEMENT");
					System.out.println("FIX THE ERROR");
					intermediate_code=null;
				}
				if(intermediate_code!=null)
				{
					for(symbol ss:symboltable)
					{
						if(ss.address==-1)
						{
							System.out.println("UNDEFINED SYMBOL..  "+"("+" "+ss.name+" "+")");
						}
					
					}
					passtwo(intermediate_code, symboltable, literaltable, startingaddress,pseudoopcodetable,opcodetable,varvalues);
				}

				
			}
			else
			{
					System.out.println("FIX THE ERROR");
			}
			
			
		}
		catch(IOException e)
		{
			System.out.println("no file found");
		}
	}
}