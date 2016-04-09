package chip8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class Chip8Core
{
	
	/*
	Core of the CHIP-8 CPU. Contains the fetch/decode/execute cycle.

	CHIP8 Technical specifications
	------------------------------
	# 4 KB of memory (4096 bytes).
	# 16 x 8-bit (1-byte) data registers (V0 - VF).
		VF isn't a data register, per se. It's used only for storing carry/borrow/collision detection flag
	# 1 x 16 bit (2-byte) address register (I).
	# Opcodes are 16 bits long.
	# Hexadecimal keypad for input.
	# Resolution of 64 x 32, with all graphics rendered in XOR mode.
		All graphics are 8-bits long sprites.
		Fontset is also composed of sprites. 
		XOR also facilitates collision detection.
	# Program Counter that starts from 0x200 since 0x0 - 0x1FF is taken up by the memory resident CHIP8 interpreter.
		Fontset space starts from 0x50
	# Stack of 16 levels
	# 2 timers (delay and sound) which count down at 60 Hz.
		This essentially means the CHIP-8 runs at 60 FPS, or 60 updates per second.
	*/

	private char[]  Memory;
	private char[]  V;
	private char	I;
	private char	pc;
	private char[]	stack;
	private int stackPointer;
	public byte[]	keypad;
	private byte[]	display;
	private int delayTimer;
	private int soundTimer;
	
	public boolean drawFlag;
	/*
	Constructor for init
	*/
	public Chip8Core()
	{
		Memory 			= new char[4096];
		V      			= new char[16];
		I 				= 0;
		pc 				= 0x200;
		stack 			= new char[16];
		stackPointer 	= 0;	 
		keypad			= new byte[16];
		display 		= new byte[64*32];
		delayTimer 		= 0;
		soundTimer 		= 0;

		loadFontset();
	}
	public byte[] getDisplay()	{	return display;	}
//############################################################################################################################################################
	/*
	Loads fontset into memory, starting at 0x50
	*/
	private void loadFontset()
	{
		char[] fontset = 
			{ 
			  0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
			  0x20, 0x60, 0x20, 0x20, 0x70, // 1
			  0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
			  0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
			  0x90, 0x90, 0xF0, 0x10, 0x10, // 4
			  0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
			  0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
			  0xF0, 0x10, 0x20, 0x40, 0x40, // 7
			  0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
			  0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
			  0xF0, 0x90, 0xF0, 0x90, 0x90, // A
			  0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
			  0xF0, 0x80, 0x80, 0x80, 0xF0, // C
			  0xE0, 0x90, 0x90, 0x90, 0xE0, // D
			  0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
			  0xF0, 0x80, 0xF0, 0x80, 0x80  // F
			};
		
		for (int i = 0; i < fontset.length ; i++)
		{
			Memory[ 0x50 + i] = fontset[i];
		}
	}
//############################################################################################################################################################
	/*
	A cycle of the emulator
	Fetch / Decode / execute
	*/
	private void cycle()
	{
		int nnn;	// 12 bits, address.
		int x;		// index of register array V[]. Basically, V[x] = the VXth register, where x = 0 to 15
		int kk;		// 8 bits, constant.
		int y;		// index of register array V[]. Basically, V[y] = the VYth register, where y = 0 to 15.
		
		/* Minor problem : CHIP8 instructions are 8 bits each, while the registers to store them in are 16 bits wide. 
		 * The solution is to merge two instructions into one to create the opcode; shifting the first one to the left by 8 bits, then logical ORing the result with the next.
		 * 		We can then logical AND the opcode with 0xf000, 0x0f00, 0x00f0, or 0x000f depending on which nibble we need to "extract" and compare for further processing.
		 * Note that the program counter needs to be incremented by 2 each time, as with the merge into one opcode, we're reading 2 instructions at a time.
		 */
		char opcode = (char) ((Memory[pc] << 8) | Memory[pc + 1]);
		System.out.print("\n"+ Integer.toHexString(opcode).toUpperCase() + " : ");
		switch(opcode & 0xf000)	//isolate first nibble, and compare
		{
			//-------------------------------------------------------------------------------------
			case	0x0000: 			//all instructions beginning with 0
				switch(opcode & 0x000f) //isolate last nibble, and compare
				{
					case 0x0:			//00E0 CLS : Clears the screen.
						System.out.print("CLS");
						for(int i = 0 ; i < display.length ; i++)	{	display[i] = 0;	 }
						pc+=2;
						break;
					case 0xe:			//00EE RET : Returns from a subroutine.
						System.out.print("RET");
						pc = (char) (stack[--stackPointer] + 2);
						break;
					default:
						System.err.println("Unsupported opcode!");
						break;
				}
			break;
//			//-------------------------------------------------------------------------------------
			case	0x1000:				//1NNN JP addr : Jumps to given 12bit address
				
				nnn = opcode & 0x0fff;
				System.out.print("JP "+nnn);
				pc = (char) nnn;
				break;
			//-------------------------------------------------------------------------------------
			case	0x2000:				//2NNN CALL addr : Calls subroutine at given 12bit address
			
				nnn = opcode & 0xfff;
				System.out.print("CALL "+nnn);
				stack[stackPointer] = pc;
				stackPointer++;
				pc = (char) nnn;
				break;
			//-------------------------------------------------------------------------------------
			case	0x3000:				//3XKK SE Vx, byte : Skip next instruction if Vx = kk
				
				x 	= (opcode & 0x0f00) >> 8;
				kk 	= (char) opcode & 0x00ff;
				System.out.print("SE V"+x+", "+kk);
				if( V[x] == kk)	{	pc+=4;	}
				else            {	pc+=2;	}
				break;
			//-------------------------------------------------------------------------------------
			case	0x4000:				//4XKK SNE Vx, byte : Skip next instruction if Vx != kk
					
				x = (opcode & 0x0f00) >> 8;
				kk = opcode & 0xff;
				System.out.print("SNE V"+x+", "+kk);
				if( V[x] != kk)	{	pc+=4;	}
				else			{	pc+=2;	}
				break;
			//-------------------------------------------------------------------------------------
			case	0x5000:				//5xy0 - SE Vx, Vy : Skip next instruction if Vx = Vy.
				
				x = (opcode & 0x0f00) >> 8;
				y = (opcode & 0x00f0) >> 4;
				System.out.print("SE V"+x+", V"+y);
				if(V[x] == V[y])	{	pc+=2;	}
				pc+=2;
				break;
			//-------------------------------------------------------------------------------------
			case	0x6000:				//6xkk - LD Vx, byte : Set Vx = kk.
				
				x = (opcode & 0x0f00) >> 8;
				kk = (char)(opcode & 0x00ff);
					System.out.print("LD V"+x+", "+kk);
				V[x] = (char) kk;
				pc+=2;
				break;
			//-------------------------------------------------------------------------------------
			case	0x7000:				//7xkk - ADD Vx, byte : Set Vx = Vx + kk.
				
				x = (opcode & 0x0f00) >> 8;
				kk = (char) opcode & 0xff;
				System.out.print("ADD V"+x+", " + kk);
				V[x] += kk;	// logical AND with 255, to account for overflow
				V[x] &= 0xff;
				pc+=2;
				break;
			//-------------------------------------------------------------------------------------
			case	0x8000:				//all instructions beginning with 8
				switch (opcode & 0x000f)
				{
					case 0x0:			//8xy0 : LD Vx, Vy : Set Vx = Vy.
						
						x = (opcode & 0x0f00) >> 8;
						y = (opcode & 0x00f0) >> 4;
						System.out.print("LD V"+x+", V"+y);
						V[x] = V[y];
						pc+=2;
						break;
					case 0x1:			//8xy1 : OR Vx, Vy : Set Vx = Vx OR Vy

						x = (opcode & 0x0f00) >> 8;
						y = (opcode & 0x00f0) >> 4;
						System.out.print("OR V"+x+", V"+y);
						V[x] = (char) ((V[x] | V[y]) & 0xff);//account for overflow
						pc+=2;
						break;
					case 0x2:			//8xy2 : AND Vx, Vy : Set Vx = Vx AND Vy

						x = (opcode & 0x0f00) >> 8;
						y = (opcode & 0x00f0) >> 4;
						System.out.print("AND V"+x+", V"+y);
						V[x] = (char) (V[x] & V[y]);
						pc+=2;
						break;
					case 0x3:			//8xy3 : XOR Vx, Vy : Set Vx = Vx XOR Vy

						x = (opcode & 0x0f00) >> 8;
						y = (opcode & 0x00f0) >> 4;
						System.out.print("XOR V"+x+", V"+y);
						V[x] = (char) ((V[x] ^ V[y]) & 0xff);//account for overflow
						pc+=2;
						break;
					case 0x4:			//8xy4 : ADD Vx, Vy. Set Vx = Vx + Vy. Set VF = carry
						
						x = (opcode & 0x0f00) >> 8;
						y = (opcode & 0x00f0) >> 4;
						System.out.print("ADD V"+x+", V"+y);
						V[0xf] = (char) (((V[x] + V[y]) > 0xff)? 1:0);
						V[x] = (char) ((V[x] + V[y]) & 0xff);//account for overflow
						pc+=2;
						break;
					case 0x5:			//8xy5 : SUB Vx, Vy. Set Vx = Vx - Vy. Set VF = NOT borrow
						
						x = (opcode & 0x0f00) >> 8;
						y = (opcode & 0x00f0) >> 4;
						System.out.print("SUB V"+x+", V"+y);
						V[0xf] = (char) ((V[y] > V[x])? 0:1);
						V[x] = (char) (V[x] - V[y]);
						pc+=2;
						break;
					case 0x6:			//8xy6 : SHR Vx, {,Vy} : Set Vx = Vx >> 1. Set VF = MSB of Vx before shift.
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("SHR V"+x);
						V[0xf]  = (char) (V[x] & 0x80); //MSB of V[x]
						V[x]  = (char) (V[x] >> 1);
						pc+=2;
						break;
					case 0x7:			//8xy7 : SUBN Vx, Vy. Set Vx = Vy - Vx. Set VF = NOT borrow
						
						x = (opcode & 0x0f00) >> 8;
						y = (opcode & 0x00f0) >> 4;
						System.out.print("SUBN V"+x+", V"+y);
						V[0xf] = (char) ((V[x] > V[y])? 0:1);
						V[x] = V[y] = V[x];
						pc+=2;
 						break;
					case 0xe:			//8xye : SHL Vx, Vy. Set Vx = Vx << 1. Set VF = LSB of Vx before shift.
			
						x = (opcode & 0x0f00) >> 8;
						y = (opcode & 0x00f0) >> 4;
						System.out.print("SHL V"+x+", V"+y);
						V[0xf] = (char) (V[x] & 0x1); //LSB of Vx
						V[x]  = (char) (V[x] << 1);
						pc+=2;
						break;
					default:
						System.err.println("Unsupported opcode");
						break;
				}
				break;
			//-------------------------------------------------------------------------------------
			case 0x9000: 				//9xy0 : SNE Vx, Vy. Skip next instruction if Vx != Vy.
				
				x = (opcode & 0x0f00) >> 8;
				y = (opcode & 0x00f0) >> 4;
				System.out.print("SNE V"+x+", V"+y);
				if(V[x]!=V[y])	{	pc+=2;	}
				pc+=2;
				break;
			//-------------------------------------------------------------------------------------
			case 0xA000:				//Annn : LD I, addr. Set I = 12 bit address.
				
				nnn = (char)opcode & 0x0fff;
				System.out.print("LD I, "+nnn);
				I = (char)nnn;
				pc+=2;
				break;
			//-------------------------------------------------------------------------------------
			case 0xB000:				//Bnnn : JP V0, addr. Jump to location (nnn + V0)
				
				nnn = opcode & 0x0fff;
				System.out.print("JP V0, "+nnn);
				pc = (char) ((V[0x0] + nnn) & 0xff);
				break;
			//-------------------------------------------------------------------------------------
			case 0XC000:				//Cxkk : RND Vx, byte. Set Vx = random byte AND kk
				
				x = (opcode & 0x0f00) >> 8;
				kk = opcode & 0x00ff;
				System.out.print("RND V"+x+", "+kk);
				Random random = new Random();
				int rnd = random.nextInt(255);
				V[x] = (char) (rnd & kk);
				pc+=2;
				break;
			//-------------------------------------------------------------------------------------
			case 0xD000:				//Dxyn - DRW Vx, Vy, nibble. VF = 1 if collision, 0 if not.
				
				x = (opcode & 0x0f00) >> 8;
				y = (opcode & 0x00f0) >> 4;
				char xcoord = (char) V[x];
				char ycoord = (char) V[y];
				//where n = pixel height
				int n = (char)(opcode & 0x000f);
				System.out.print("DRW V"+x+", V"+y+", "+n);
				char pixelData;
				//collision marker
				V[0xf] = 0;
				for(int ycounter = 0; ycounter < n ; ycounter++)
				{
					pixelData = Memory[I + ycounter];
					//since all CHIP8 sprites are 8 bits in width...
					for (int xcounter = 0; xcounter < 8 ; xcounter++)
					{
						if( (pixelData & (0x80 >> xcounter)) != 0)
						{
							int totalX = xcoord + xcounter;
							int totalY = ycoord + ycounter;
							totalX %= 64;	//wraparound
							totalY %= 32;	//wraparound
							int finalIndex = totalY * 64 + totalX;
							if (display[finalIndex] == 1)	{	V[0xf] =1;	}
							//draw the pixel, finally
							display[finalIndex] ^=1;
						}
					}//end xcounter loop
				}//end ycounter loop
				pc+=2;
				drawFlag = true;
				break;
			//-------------------------------------------------------------------------------------
			case 0xE000:				//All instructions beginning with E
				switch(opcode & 0xf)
				{
					case 0x000e:		//Ex9E : SKP Vx. Skip next instruction if key stored in V[x] is pressed.
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("SKP V"+x);
						if(keypad[V[x]] == 1)	{	pc+=4;	}//skip
						else					{	pc+=2;	}//don't skip
					case 0x0001:		//ExA1 : SKNP Vx. Skip next instruction if key stored in V[x] is NOT pressed.
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("SKNP V"+x);
						if(keypad[V[x]] == 1)	{	pc+=2;	}//dont skip
						else					{	pc+=4;	}//skip
				}
				break;
			//-------------------------------------------------------------------------------------
			case 0xF000:				//All instructions beginning with F
				switch(opcode & 0x00ff)
				{
					case 0x0007:		//Fx07 : LD Vx, DT. Set Vx = value of Delay Timer.
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("LD V"+x+", DT");
						V[x] = (char) delayTimer;
						pc+=2;
						break;
					case 0x000a:		//Fx0A : LD Vx, K. Await a keypress, and then set Vx = key which was pressed.
						
						x = (opcode & 0x0f00) >> 8;
						int keyPressed = 0;//this is purely for the print statement
						for(int i = 0 ; i < keypad.length; i++)
						{
							if(keypad[i] == 1)
							{
								keyPressed = i;
								V[x] = (char) i;
							}
							System.out.println(" Awaiting keypress...");
						}
						System.out.print("LD V"+x+", "+keyPressed);
						pc+=2;
						break;
					case 0x0015:		//FX15 : LD DT, Vx. Set delayTimer = value stored in Vx
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("LD DT, V"+x);
						delayTimer = V[x];
						pc+=2;
						break;
					case 0x0018:		//FX18 : LD ST, Vx. Set soundTimer = value stored in Vx
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("LD ST, V"+x);
						soundTimer = V[x];
						pc+=2;
						break;
					case 0x001e:		//FX1E : ADD I, Vx. Set I = I + Value in Vx
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("ADD I, V"+x);
						I = (char) (I + V[x]);
						pc+=2;
						break;
					case 0x0029:		//FX29 : LD F, Vx. Set I = location of sprite for character stored in Vx.
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("LD F, V"+x);
						int character = V[x];
						I = (char) (0x50 + (character * 5));//character sprites in fontset are 5 bits long
						pc+=2;
						break;
					case 0x0033:		//FX33 : LD B, VX. Store BCD representation of Vx in Mem[I, I+1, and I+2]
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("LD B, V"+x);
						int hundreds = V[x] / 100;
						int tens = (V[x] /10) % 10;
						int ones = (V[x] % 10) ;

						Memory[I] = (char) hundreds;
						Memory[I + 1] = (char) tens;
						Memory[I + 2] = (char) ones;

						pc+=2;
						break;
					case 0x0055:		//FX55 : LD [I], Vx. Stores V0 to Vx in memory starting at location I
						
						
						x = (opcode & 0x0f00) >> 8;
						System.out.print("LD [I], V"+x);
						for(int i = 0; i <= x ; i++)
						{
							Memory[I + i] = V[i];
						}
						pc+=2;
						break;
					case 0x0065:		//FX65 : LD Vx, [I]. LoadS V0 to Vx with contents of memory starting at location I
						
						x = (opcode & 0x0f00 ) >> 8;
						System.out.print("LD V"+x+", [I]");
						for(int i = 0; i <= x ; i ++)
						{
							V[i] = Memory[I + i];
						}
						pc+=2;
						break;
				}
				break;
			//-------------------------------------------------------------------------------------
			default:
				System.err.print("Unsupported opcode");
				break;
			}
			
			/*
			 * Timers have to updated at the end of every CPU cycle
			 */
		if(delayTimer > 0)	{	delayTimer--;	}
		if (soundTimer >0)	{	soundTimer--;	}
		
	}//end of a cycle
//############################################################################################################################################################
	/*
	 * Publicly visible function (let's call it a wrapper function) that calls the actual private cycle() without revealing its inner workings.
	 */
	public void run()
	{
		cycle();
	}
//############################################################################################################################################################
	/*
	 * Loads a file at given filepath into CHIP8 memory starting at address 0x200.
	 */
	public void loadProgram(String filename)
	{
		try{
			//read all bytes from file at given filepath
			byte[] fileBuffer = Files.readAllBytes(new File(filename).toPath());
			for(int i = 0 ; i < fileBuffer.length; i++)
			{
				//copy buffer over to actual CHIP8 Memory, skipping 0x0 - 0x1FF
				Memory[0x200 + i] = (char) (fileBuffer[i] & 0xff);
			}
		}catch (IOException e)
		{
			e.printStackTrace();
		}
		
	
	}
//############################################################################################################################################################
	/*
	 * Return if screen needs a refresh/redraw
	 */
	public boolean needsRefresh() {
		
		if(drawFlag == false)
			{
			return false;
			
			}
		else
		{
			return true;
			
		}
	}
//############################################################################################################################################################
}