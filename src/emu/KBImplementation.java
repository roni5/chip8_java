package emu;

import org.lwjgl.input.Keyboard;

import chip8.Chip8Core;

public class KBImplementation {

	/*
	 * Polls input on each frame, and implements the mapping of CHIP8 keypad based on pressed/released keys.
	 */
	public static void pollInput(Chip8Core chip8core)
	{
		while(Keyboard.next())
		{
			if(Keyboard.getEventKeyState())//Keyboard.getEventKeyState() returned true, meaning the key in context (Keyboard.getEventKey()) was PRESSED.
			{
				
				switch(Keyboard.getEventKey())
				{
				//Implement ESC for Exit
				case Keyboard.KEY_ESCAPE:
					System.out.println("\nESCAPE pressed. Exiting...");
					System.exit(0);
					break;
					/*
					 * Implement keypad mapping
					 * If corresponding key pressed, set keypad[key] to 1
					 * else, if corresponding key released, set keypad[key] to 0
					 * 
					 * Mapping 
					 * --------
					 * ORIGINAL:
					 * 1 2 3 C
					 * 4 5 6 D
					 * 7 8 9 E
					 * A 0 B F
					 *
					 * MAPPED:
					 * 1 2 3 4
					 * Q W E R
					 * A S D F
					 * Z X C V
					 */
		 
				case Keyboard.KEY_1:
					chip8core.keypad[0x1] = 1;
					break;
				case Keyboard.KEY_2:
					chip8core.keypad[0x2] = 1;
					break;
				case Keyboard.KEY_3:
					chip8core.keypad[0x3] = 1;
					break;
				case Keyboard.KEY_4:
					chip8core.keypad[0xC] = 1;
					break;
					
				case Keyboard.KEY_Q:
					chip8core.keypad[0x4] = 1;
					break;
				case Keyboard.KEY_W:
					chip8core.keypad[0x5] = 1;
					break;
				case Keyboard.KEY_E:
					chip8core.keypad[0x6] = 1;
					break;
				case Keyboard.KEY_R:
					chip8core.keypad[0xD] = 1;
					break;
					
				case Keyboard.KEY_A:
					chip8core.keypad[0x7] = 1;
					break;
				case Keyboard.KEY_S:
					chip8core.keypad[0x8] = 1;
					break;
				case Keyboard.KEY_D:
					chip8core.keypad[0x9] = 1;
					break;
				case Keyboard.KEY_F:
					chip8core.keypad[0xE] = 1;
					break;
					
				case Keyboard.KEY_Z:
					chip8core.keypad[0xA] = 1;
					break;
				case Keyboard.KEY_X:
					chip8core.keypad[0x0] = 1;
					break;
				case Keyboard.KEY_C:
					chip8core.keypad[0xB] = 1;
					break;
				case Keyboard.KEY_V:
					chip8core.keypad[0xF] = 1;
					break;
		
				}//end switch block for pressed keys
				
			}//end if for pressed key
			else //Keyboard.getEventKeyState() returned false. The key in context was RELEASED instead.
			{
				switch(Keyboard.getEventKey())
				{
				case Keyboard.KEY_1:
					chip8core.keypad[0x1] = 0;
					break;
				case Keyboard.KEY_2:
					chip8core.keypad[0x2] = 0;
					break;
				case Keyboard.KEY_3:
					chip8core.keypad[0x3] = 0;
					break;
				case Keyboard.KEY_4:
					chip8core.keypad[0xC] = 0;
					break;
					
				case Keyboard.KEY_Q:
					chip8core.keypad[0x4] = 0;
					break;
				case Keyboard.KEY_W:
					chip8core.keypad[0x5] = 0;
					break;
				case Keyboard.KEY_E:
					chip8core.keypad[0x6] = 0;
					break;
				case Keyboard.KEY_R:
					chip8core.keypad[0xD] = 0;
					break;
					
				case Keyboard.KEY_A:
					chip8core.keypad[0x7] = 0;
					break;
				case Keyboard.KEY_S:
					chip8core.keypad[0x8] = 0;
					break;
				case Keyboard.KEY_D:
					chip8core.keypad[0x9] = 0;
					break;
				case Keyboard.KEY_F:
					chip8core.keypad[0xE] = 0;
					break;
					
				case Keyboard.KEY_Z:
					chip8core.keypad[0xA] = 0;
					break;
				case Keyboard.KEY_X:
					chip8core.keypad[0x0] = 0;
					break;
				case Keyboard.KEY_C:
					chip8core.keypad[0xB] = 0;
					break;
				case Keyboard.KEY_V:
					chip8core.keypad[0xF] = 0;
					break;
				}//end swtich block for released keys
			}//end else for released keys
		}//end while loop
	}//end pollInput()

}
