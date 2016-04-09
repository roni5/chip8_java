package emu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import chip8.Chip8Core;

public class Main 
{
	private static Chip8Core chip8core = new Chip8Core();
	
	public static void main(String[] args) throws LWJGLException, IOException
	{
		

		/* Menu 
		 * 
		 */
		System.out.println("_______________CHIP8 Emulator________________");
		System.out.println("Choose a .ch8 program to load");
		System.out.println("ESC button quits emulator.");
		/**
		 * ROMS unverified; will be removed if non-public domain
		 */
		System.out.println("Demos :");
		System.out.println("1) Zero");
		System.out.println("2) Maze2");
		System.out.println("3) Stars");
		System.out.println("Games :");
		System.out.println("4) Tron");
		System.out.println("5) Brix");
		System.out.println("6) Pong2");
		System.out.println("_____________________________________________");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String choice = reader.readLine();
		String filePath = "";
		
		/*
		 * Following switch block is fairly self explanatory; set filePath to point to ROM corresponding to menu choice.
		 */
		switch(choice)
		{

		case "1":
			filePath = ("./zerodemo.ch8");
			break;
		case "2":
			filePath = ("./maze2.ch8");
			break;
		case "3":
			filePath = ("./stars.ch8");
			break;
		case "4":
			filePath = ("./tron.ch8");
			break;
		case "5":
			filePath = ("./brix.ch8");
			break;
		case "6":
			filePath = ("./pong2.ch8");
			break;
		
		
	
		default:
			System.err.println("Invalid choice. Try again.");
			main(args);
			break;
		}
		//and load the ROM
		chip8core.loadProgram(filePath);
		
		
		/*
		 * The following section is all about initing OpenGL graphics.
		 */
		//Create new display w/ resolution = 640 x 320
		Display.setDisplayMode(new DisplayMode(640,320));
		Display.create();
		//Set up a orthogonal projection (i.e. no perspective, just 2D)
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();					//reset view, clear any prior projections
		GL11.glOrtho(0, 640, 320, 0, 1, -1);	//params are in the order : (left, right, bottom, top, zNear, zFar);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		//Set size of each pixel to 10x10, since we're upscaling to 640x320 from 64x32 native
		GL11.glPointSize(10);					
		//Enable this for freaking ANTIALIASED POINTS! Hilarious, and pointless.
		//GL11.glEnable(GL11.GL_POINT_SMOOTH); 
		
		
		
		/*
		 * Main game loop is implemented here as a whileLoop that runs until the window is closed (The [X] button of window, obviously)
		 * Put all necessary per-frame computation here.
		 */
		while(!Display.isCloseRequested())
		{
			//Initiate a cycle of the emulator
			chip8core.run();
			
			//Poll keyboard for setting chip8core.keypad[key] to either 1(pressed) or 0(released)
			KBImplementation.pollInput(chip8core);
			
			/*
			 * Check for refresh, and clear (2D) buffer if refresh is needed.
			 */
			if(chip8core.needsRefresh())
			{
				//refresh screen
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				//reset drawFlag
				chip8core.drawFlag = false;
			}
			
			/*
			 * This is the actual rendering loop. 
			 * Loop through the display[] array, 
			 * 		if an element is not == 0, draw a white point at (i%64, i/64)
			 * 		else, draw a black point there.
			 */
			for(int i = 0; i < chip8core.getDisplay().length; i++)
			{
				if(chip8core.getDisplay()[i] == 0)
				{
					//set color = black
					GL11.glColor3f(0, 0, 0);
				}
				else
				{
					//set color = white
					GL11.glColor3f(1, 1, 1);
				}
				
				//Handle wraparounds
				int x = (int) i%64;
				int y = (int) Math.floor(i/64);
				
				//Actually draw the point (x,y), each coordinate scaled up by 10 pixels.
				GL11.glBegin(GL11.GL_POINTS);
					GL11.glVertex2f(x*10, y*10);
				GL11.glEnd();
				
			}//end rendering loop
			
			//one rendering cycle done, now update display
			//This also polls input devices
			Display.update();
			//CHIP8 runs at 60 updates per second, so set the sync delay to 60 fps.
			Display.sync(60);
			
		}//end main game logic loop
		
		//Window was closed by user
		Display.destroy();
	}
//##################################################################################################################################################
}
