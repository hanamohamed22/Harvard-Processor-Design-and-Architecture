import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class McHarvard {
	static short instructionMemory[];
	static byte dataMemory[];
	static byte registerFile[];
	static byte statusRegister;
	static int temp[];

	static short pc;
	static int clock;
	 static Queue decodequeue;
	 static Queue executequeue;
	 static boolean flag;
	 static boolean branch;
	 static short oldp;
	
	public McHarvard() {
		instructionMemory=new short[1024];
		dataMemory=new byte[2048];
		registerFile=new byte[64];
		statusRegister=0;
		temp=new int[8];
		temp[0]=0;
		temp[1]=0;
		temp[2]=0;
		pc=0;
		clock=0;
		decodequeue=new LinkedList();
		executequeue=new LinkedList();
		flag=false;
		branch=false;
	}
	public static void readfile() throws NumberFormatException {
		try {
			int i=0;
			int n;
			String r;
			int t;
			String s="Program.txt";
			
			int in;

			File file=new File(s);    //creates a new file instance  
			FileReader fr=new FileReader(file);   //reads the file  
			BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream  
			StringBuffer sb=new StringBuffer();    //constructs a string buffer with no characters  
			String line;  
			while((line=br.readLine())!=null)  
			{  
			String inst="";
			sb.append(line);      //appends line to string buffer  
			sb.append("\n"); //line feed   
			//count++; 
			String[] x = line.split(" ");
			switch(x[0]) {
			case "ADD":inst+="0000";break;
			case "SUB":inst+="0001";break;
			case "MUL":inst+="0010";break;
			case "LDI":inst+="0011";break;
			case "BEQZ":inst+="0100";break;
			case "AND":inst+="0101";break;
			case "OR":inst+="0110";break;
			case "JR":inst+="0111";break;
			case "SLC":inst+="1000";break;
			case "SRC":inst+="1001";break;
			case "LB":inst+="1010";break;
			case "SB":inst+="1011";break;
			default: inst+="1111";break;
			}
			if(x[1].length()==2) {
				 n=Integer.parseInt(x[1].charAt(1)+"");
				 r=Integer.toBinaryString(n);
				
				 t=6-r.length();
				for(int j=0;j<t;j++) {
					r="0"+r;
				}
				inst+=r;
			}
			else {
				String str=""+x[1].charAt(1)+""+x[1].charAt(2)+"";
				n=Integer.parseInt(str);
				r=Integer.toBinaryString(n);
				t=6-r.length();
				for(int j=0;j<t;j++) {
					r="0"+r;
				}
				inst+=r;	
			}
			if(x[2].charAt(0)=='R') {
				if(x[2].length()==2) {
					 n=Integer.parseInt(x[2].charAt(1)+"");
					 r=Integer.toBinaryString(n);
					
					 t=6-r.length();
					for(int j=0;j<t;j++) {
						r="0"+r;
					}
					inst+=r;
				}
				else {
					String str=""+x[2].charAt(1)+""+x[2].charAt(2)+"";
					n=Integer.parseInt(str);
					r=Integer.toBinaryString(n);
					t=6-r.length();
					for(int j=0;j<t;j++) {
						r="0"+r;
					}
					inst+=r;	
				}
			}else {
				n=Integer.parseInt(x[2]);
				if(n<0) {            //lw negative
					n=n & 0b00000000000000000000000000111111;	
				}
				r=Integer.toBinaryString(n);
				//System.out.println(r);
				t=6-r.length();
				for(int j=0;j<t;j++) {
					r="0"+r;
				}
				inst+=r;
			}
			
			//System.out.println(inst);
			in=Integer.parseInt(inst,2);
			//System.out.println(in);
		
			instructionMemory[i]=(short)in;
			//System.out.println(instructionMemory[i]);
			i++;
			
			//list.add(line);
			}
			fr.close();
			clock++;
			while(true) {
				System.out.println("Clock Cycle: "+ clock);
				System.out.println("PC: "+pc);
				if(!executequeue.isEmpty()) {
					byte o=(byte) executequeue.remove();
					int reg1 =(int) executequeue.remove();
					int reg2=(int) executequeue.remove();
					byte op1=(byte) executequeue.remove();
					byte op2=(byte) executequeue.remove();
					byte im=(byte) executequeue.remove();
					short p=(short)executequeue.remove();
				
					execute(o,reg1,reg2,op1,op2,im,p);
					
				}
				else if(flag==true && branch==false) {
					System.out.println("The program is done!");
					break;
				}
				if(!decodequeue.isEmpty()) {
					short instr=(short)decodequeue.remove();
					short p=(short)decodequeue.remove();
					decode(instr,p);
				}
				if(instructionMemory[pc]!=0) {
					if(branch==true) {
					   fetch(oldp);
					   flag=false;
					}
					else
						fetch(pc);
					System.out.println("PC updated: "+pc);
				}
				else {
					flag=true;
				}
				if(branch==true) {
					branch=false;
					if(!executequeue.isEmpty()) {
					executequeue.remove();
					executequeue.remove();
					executequeue.remove();
					executequeue.remove();
					executequeue.remove();
					executequeue.remove();
					executequeue.remove();
					}
					if(!decodequeue.isEmpty()) {
					decodequeue.remove();
					decodequeue.remove();
					}
				}
				clock++;
				System.out.println("-------------------------------------------------------------");
			}
			
			
			}  
			catch(IOException e)  
			{  
			e.printStackTrace();  
			} 
		
		
	}
	public static void fetch(short p) {
	
		short instruction=instructionMemory[p];
		int in=(int) instruction;
		if(in<0) {            //lw negative
			in=in & 0b00000000000000001111111111111111;	
		}
		
		String x=Integer.toBinaryString(in);
		int t=16-x.length();
		for(int j=0;j<t;j++) {
			x="0"+x;
		}
	
		System.out.println("Fetching: "+x);
		if(branch==false)
			pc++;
		decodequeue.add(instruction);
		decodequeue.add(pc);
		
		
		
	}
	
	public static void decode(short instruction,short p) {
		int in=(int) instruction;
		if(in<0) {            //lw negative
			in=in & 0b00000000000000001111111111111111;	
		}
		
		String x=Integer.toBinaryString(in);
		int t=16-x.length();
		for(int j=0;j<t;j++) {
			x="0"+x;
		}
		System.out.println("Decoding: "+ x);
		byte opcode = (byte) ((instruction & 0b1111000000000000)>>12);
		int rs= ((instruction & 0b0000111111000000)>>6);
		int rt= ((instruction & 0b0000000000111111));
		byte immediate=(byte) ((instruction & 0b0000000000111111));
		byte valueRT=0;
		byte valueRS=0;
		System.out.println("Decoded instruction for next cycle:");
		System.out.println("OPcode: "+opcode);
		System.out.println("R1: "+rs);
		System.out.println("R2/IMM/OFF: "+rt);
		
		
		 if(rs<registerFile.length&&rt<registerFile.length) {
			 
		for(int i=0; i<registerFile.length;i++){
			if(i==rt){
			valueRT=registerFile[i];}

			if(i==rs){
			valueRS=registerFile[i];}


			}
		}
		 System.out.println("Inputs for next cycle: ");
		 System.out.println("Value of R1: "+valueRS);
		 System.out.println("Value of R2: "+valueRT);
		 
		 executequeue.add(opcode);
		 executequeue.add(rs);
		 executequeue.add(rt);
		 executequeue.add(valueRS);
		 executequeue.add(valueRT);
		 executequeue.add(immediate);
		 executequeue.add(p);
		
	}
	
	public static void execute(byte opcode, int rs,int rt,byte valRs,byte valRt, byte imm,short p ) {
		String s;
		int regs;
		int regt;
		boolean R=true;
		switch(opcode) {
		case 0:s="ADD ";break;
		case 1:s="SUB ";break;
		case 2:s="MUL ";break;
		case 3:s="LDI ";R=false; break;
		case 4:s="BEQZ ";R=false;break;
		case 5:s="AND ";break;
		case 6:s="OR ";break;
		case 7:s="JR ";break;
		case 8:s="SLC ";R=false;break;
		case 9:s="SRC ";R=false;break;
		case 10:s="LB ";R=false;break;
		case 11:s="SB ";R=false;break;
		default:s="";break;
		}
		int op=(int) opcode;
		if(op<0) {            //lw negative
			op=op & 0b00000000000000000000000000001111;	
		}
		
		String o=Integer.toBinaryString(op);
		int t=4-o.length();
		for(int j=0;j<t;j++) {
			o="0"+o;
		}
		regs=rs;
		if(regs<0) {
			regs=regs & 0b00000000000000000000000000111111;
		}
		String r=Integer.toBinaryString(regs);
		int t1=6-r.length();
		for(int j=0;j<t1;j++) {
			r="0"+r;
		}
		regt=rt;
		if(regt<0) {
			regt=regt & 0b00000000000000000000000000111111;
		}
		String im=Integer.toBinaryString(regt);
		int t2=6-im.length();
		for(int j=0;j<t2;j++) {
			im="0"+im;
		}
		byte ims=imm;
		if((imm& 32)==32)
			ims=(byte)(ims|192);
		
		
		if(R)
			System.out.println("Executing: "+s+"R"+regs+" "+"R"+rt+" "+o+""+r+""+im);
		else
			System.out.println("Executing: "+s+"R"+regs+" "+ims+" "+o+""+r+""+im);
		int C=0;
		int V=0;
		int N=0;
		int S=0;
		int Z=0;
		temp[7]=Z;
		temp[6]=S;
		temp[5]=N;
		temp[4]=V;
		temp[3]=C;
		int unRs=valRs&0x000000FF;
		int unRt=valRt&0x000000FF;
		int maskcarry=256;
		byte maskof=(byte)128;
		byte res;

		switch(opcode) {
		case 0: 
		res=(byte)(valRs+valRt);
		if(((unRs +unRt)&maskcarry)==maskcarry)
			C=1;
		else
			C=0;
		System.out.println("Carry Flag Updated to: "+ C);
		temp[3]=C;
		if((valRs & maskof)==(valRt & maskof)) {
			
			if((res &maskof)==(valRs &maskof))
				V=0;
			else
				V=1;
		}
		System.out.println("Overflow Flag Updated to: "+ V);
		temp[4]=V;
		if(res<0) 
			N=1;
		else
			N=0;
		System.out.println("Negative Flag Updated to: "+ N);
		temp[5]=N;
		S=N^V;
		System.out.println("Sign Flag Updated to: "+ S);
		temp[6]=S;
		if(res==0)
			Z=1;
		else
			Z=0;
		System.out.println("Zero Flag Updated to: "+ Z);
		temp[7]=Z;
		System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + res);
		valRs= (byte) (valRs+valRt);
		
		break;
		case 1:
			res=(byte)(valRs-valRt);
			if((valRs & maskof)!=(valRt & maskof)) {
				
				if((res &maskof)==(valRt &maskof))
					V=1;
				else
					V=0;
			}
			temp[4]=V;
			System.out.println("OverFlow Flag Updated to: "+ V);
			if(res<0) 
				N=1;
			else
				N=0;
			temp[5]=N;
			System.out.println("Negative Flag Updated to: "+ N);
			S=N^V;
			temp[6]=S;
			System.out.println("Sign Flag Updated to: "+ S);
			if(res==0)
				Z=1;
			else
				Z=0;
			temp[7]=Z;
			System.out.println("Zero Flag Updated to: "+ Z);
			System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + res);
			valRs=(byte) (valRs-valRt);
			break;
		case 2: res=(byte) (valRs*valRt);
		
		if(res<0) 
			N=1;
		else
			N=0;
		temp[5]=N;
		System.out.println("Negative Flag Updated to: "+ N);
		if(res==0)
			Z=1;
		else
			Z=0;
		temp[7]=Z;
		System.out.println("Zero Flag Updated to: "+ Z);
		System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + res);
		valRs=res;
		break;
		case 3:
			if((imm& 32)==32)
				imm=(byte)(imm|192);
			System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + imm); 
			valRs=(byte) imm;break;
		case 4: 
			if((imm& 32)==32)
				imm=(byte)(imm|192);
			if(valRs==0) {
				oldp=pc;
			pc =(short)(p+imm);
			
		branch=true;
		}
		break;
		case 5:res=(byte) (valRs&valRt);
		
		if(res<0) 
			N=1;
		else
			N=0;
		temp[5]=N;
		System.out.println("Negative Flag Updated to: "+ N);
		if(res==0)
			Z=1;
		else
			Z=0;
		temp[7]=Z;
		System.out.println("Zero Flag Updated to: "+ Z);
		System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + res);
		valRs=res;
		break;
		case 6:res=(byte) (valRs|valRt);
		
		if(res<0) 
			N=1;
		else
			N=0;
		temp[5]=N;
		System.out.println("Negative Flag Updated to: "+ N);
		if(res==0)
			Z=1;
		else
			Z=0;
		temp[7]=Z;
		System.out.println("Zero Flag Updated to: "+ Z);
		System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + res);
		valRs=res;
		break;
		case 7:
			oldp=pc;
		pc=(short)((short)(valRs << 8) | valRt);

		branch =true;
		break;
		case 8:res = (byte) ((valRs << imm) | (valRs >>> (8-imm)));
		
		if(res<0) 
			N=1;
		else
			N=0;
		temp[5]=N;
		System.out.println("Negative Flag Updated to: "+ N);
		if(res==0)
			Z=1;
		else
			Z=0;
		temp[7]=Z;
		System.out.println("Zero Flag Updated to: "+ Z);
		System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + res);
		valRs=res;
		break;
		case 9:res = (byte)((valRs >>> imm) | (valRs << (8-imm)));
		
		if(res<0) 
			N=1;
		else
			N=0;
		temp[5]=N;
		System.out.println("Negative Flag Updated to: "+ N);
		if(res==0)
			Z=1;
		else
			Z=0;
		temp[7]=Z;
		System.out.println("Zero Flag Updated to: "+ Z);
		System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + res);
		valRs=res;
		break;
		case 10:System.out.println("Register "+rs +" Content updated from "+ valRs+" to " + dataMemory[imm]);
		valRs=dataMemory[imm];
		break;
		case 11:System.out.println("Data Memory index "+imm +" Content updated from "+ dataMemory[imm]+" to " + valRs);
			dataMemory[imm]=valRs;break;
		}
		String te="";
		for(int i=0;i<temp.length;i++) {
			te+=temp[i];
		}
		System.out.println("Status Register: " + te +" C="+C+" V="+ V+" N="+N+" S="+S +" Z="+Z);
	
		 statusRegister=Byte.parseByte(te,2);
	       
	      registerFile[rs]=valRs;
	      
		 
		 
		 
		 
		
		
	}
	
	
	public static void main(String args []) {
		McHarvard m=new McHarvard();
		for(byte i=0;i<registerFile.length;i++) {
			registerFile[i]=i;
		}
		
		
		readfile();
		System.out.println("Registers content:");
		for(int i=0;i<registerFile.length;i++) {
			System.out.println("R"+i+" : "+ registerFile[i]);
		}
		System.out.println();
		System.out.println("Instruction Memory content: ");
		
		
		
		for(int i=0;i<instructionMemory.length;i++) {
			//if(instructionMemory[i]!=0) {
				System.out.println(i+" : " +instructionMemory[i]);
			//}
		}
		System.out.println();
		System.out.println("Data Memory content: ");
		
		for(int i=0;i<dataMemory.length;i++) {
			//if(dataMemory[i]!=0) {
				System.out.println(i+" : "+dataMemory[i]);
			//}
		}
		
	
	}
	

}
