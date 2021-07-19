package com.mime.minefront;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.mime.minefront.graphics.Render;
import com.mime.minefront.graphics.Render3D;
import com.mime.minefront.graphics.Screen;
import com.mime.minefront.gui.Launcher;
import com.mime.minefront.gui.Pause;
import com.mime.minefront.input.PlayerController;
import com.mime.minefront.input.InputHandler;

public class Display extends Canvas implements Runnable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	public static final int WIDTH = 800;
//	public static final int HEIGHT = 600;
	public static int selection = 1;
	public static  int WIDTH=800, HEIGHT=600;
	//public int WIDTH1, HEIGHT1;
	//public int test;
	
	public static final String TITLE = "Minefront Pre-Alpha 0.05";
	public static Point WindowLocation;
	public static int mouseSpeed;
	public static int MouseSpeed;
	public static Graphics g;

	private Thread thread;
	private Screen screen;
	private BufferedImage img;
	private Game game;
	private boolean running = false;
	public static boolean onceDid=false;
	private int[] pixels;
	private Render render;
	public InputHandler input, inputEsc; //protected
	protected JFrame frame;
	private int newX=0, newY=0, oldX=0, oldY=0;
	private int fpsInnerText;
	Robot robot;
	static Launcher launcher = null;
	//Launcher launcher = new Launcher(0, new Display()); //it's own thread not same as game in will be hard and fail
	
	public Display() {
		//WIDTH = WIDTH1;
		//HEIGHT = HEIGHT1;
		//int test;
		Dimension size = new Dimension(getGameWidth(), getGameHeight());
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		screen = new Screen(getGameWidth(),getGameHeight());
		input = new InputHandler();
		game = new Game(input);
		img = new BufferedImage(getGameWidth(), getGameHeight(), BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		
		
		//input.key[KeyEvent.VK_ESCAPE]=true;
		//inputEsc = new InputHandler();
		addKeyListener(input);
		addFocusListener(input);
		addMouseListener(input);
		addMouseMotionListener(input);
		//System.out.println(test);
		//System.out.println("Constructor Runs AGAINN");
	}
	
	public static Launcher getLauncherInstance() {
		if (launcher == null) {
			launcher = new Launcher(0);
		}
		return launcher;//start the launcher from here !
	}
	
	public static void setLauncherInstance(Launcher launcher) {
		Display.launcher = launcher;
	}
	
	public static Display getGameInstance(Display display) {
		return display;
	}
	
	
	public static int getGameWidth() {
//		if(selection == 0 ) {
//			WIDTH = 640;
//		}
//		
//		if(selection == 1 || selection == -1) {
//			WIDTH = 800;
//		}
//		
//		if(selection == 2) {
//			WIDTH = 1024;
//		}
		return WIDTH;
	}
	
	public static int getGameHeight() {
//		if(selection == 0 ) {
//			HEIGHT = 480;
//		}
//		
//		if(selection == 1 || selection == -1) {
//			HEIGHT = 600;
//		}
//		
//		if(selection == 2) {
//			HEIGHT = 768;
//		}
		return HEIGHT;
	}
	
	public synchronized void start() {
		if (running) return;
		running = true;
		thread = new Thread(this, "game");
		thread.start();
		//System.out.println(Render3D.test);
	}
	
	public synchronized void stop() {
		if (!running) return;
		running=false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public synchronized void paused() {
		try {
			//System.out.println("wait");
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public synchronized void continued() {
			//System.out.println("notify");
			notify();
			input.key[KeyEvent.VK_ESCAPE]=false;//you could use also AtomicBoolean
			//Pause.getPauseInstance().stopPauseMenu();
			//if(Pause.getPauseInstance() != null)Pause.getPauseInstance().stopPauseMenu();
			//if(Controller.Pause_Menu != null) {Controller.Pause_Menu.stopPauseMenu(); Controller.Pause_Menu = null;}
	}
	
//	public synchronized void stopPause() {
//		try {
//			thread.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//}
	

	@Override
	public void run() {

		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1_000_000_000.0 / 60.0 ;//60 times per second
		double delta = 0;
		int frames=0;
		int updates = 0;

		requestFocus();
		//game loop
		while (running) { //cpu cycles
			long now = System.nanoTime();
			
			{//together only work perfect
				for(int ii=0; ii <= 1; ii++) {
					/*Atomic Boolean or input.key[KeyEvent.VK_ESCAPE] = false; better than this?*/ // YEAP!!! time & space complexity reduced + 100% solid result instead of this yacky but working
				for (int i =0; i <= 20; i ++) {
				if(onceDid) {
				try {robot = new Robot();} catch (AWTException e) {e.printStackTrace();}
				robot.keyPress(KeyEvent.VK_ESCAPE);
				robot.keyRelease(KeyEvent.VK_ESCAPE);
				}}onceDid=false;}
				
				if(onceDid) {
					input.key[KeyEvent.VK_ESCAPE] = false;
				onceDid=false;
				}
			}
			
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {//only 60 times per second..delta >= ns ..1/60 if passed?	
				tick();
				updates++;
				delta--;
			
			}
			render();
			frames++;
			
			if (System.currentTimeMillis() - timer > 1000) {//while == if in this kind of "endless LOOPS?"
				PlayerController.timeJ1 = System.currentTimeMillis() - timer;
				timer += 1000;
				fpsInnerText = frames;
				frame.setTitle(TITLE + "    |    " + updates + " ups, " + frames + " fps");
				updates=0;
				frames=0;
				PlayerController.jumped = false;
				if(PlayerController.timeJ < 7500) PlayerController.timeJ++; else PlayerController.timeJ = 0;
			}
			//System.out.println("Game Thread");
			//System.out.println(Thread.activeCount());
			//System.out.println("Thread is "+Pause.statusThread.isAlive());
//			long currentTime = System.nanoTime();
//			long passedTime = currentTime - previousTime;
//			previousTime = currentTime;
//			unprocessedSeconds += passedTime/1000000000.0;//nanoseconds to seconds
			//launcher.updateFrame();//it's own thread not same as game in will be hard and fail
	
//			while (unprocessedSeconds > secondsPetTick) {
//				tick();
//				unprocessedSeconds -= secondsPetTick;
//				ticked = true;
//				tickCount++;
//				if (tickCount % 60 == 0) {//60 ups not fps
//					//frame.setTitle(TITLE + " FPS: " + frames);
//					fpsInnerText = frames;
//					//System.out.println(frames + "fps");
//					previousTime += 1000;
//					frames=0;
////					var temp = Controller.jumped;
////					Controller.jumped = !temp;
//					//timer++;
//					//System.out.println(System.currentTimeMillis() - timer);
//					
//					//ThreadTest tt1 = new ThreadTest();
//					
//
//					//tt1.pause();
//					//tt1.run();
//					//tt1.start();
//				}
////				if (ticked) {
////					render();
////					//wont play here renderMenu()
////					/*renderMenu();*///Limited FPS here but run in own thread independedly went to Launcher Class
////					frames++;
////				}
//			}
//			
//			//update game state logic of game
//			//monitor updates match game updates? e.g. 60 tick 60Hz monitor
//			if (ticked) {//update keep static(not java static) every cpu cycle steady updates
//				render();
//				frames++;
//				//if(Pause.ID_PAUSED_THREAD==1) {Pause.pausedThread.stopPauseMenu(); Pause.ID_PAUSED_THREAD=0;}
//				//if(Pause.getPauseInstance()!=null) {Pause.getPauseInstance().stopPauseMenu(); Pause.pausedThreadTry2=null;}
//			}
//			if (System.currentTimeMillis() - timer > 1000) {
//			Controller.timeJ1 = System.currentTimeMillis() - timer;
//			timer += 1000;
//			//var temp = Controller.jumped;
//			//Controller.jumped = !temp;
//			//System.out.println(timer);
//			Controller.jumped = false;
//			if(Controller.timeJ < 7500) Controller.timeJ++; else Controller.timeJ = 0;
//			//Controller.timeJ = timer;
//		}
			
//			if (System.currentTimeMillis() - timer > 2000) {
//				Controller.timeJ1 = System.currentTimeMillis() - timer;
//				timer += 2000;
//				//var temp = Controller.jumped;
//				//Controller.jumped = !temp;
//				//System.out.println(timer);
//				Controller.jumped = false;
//				if(Controller.timeJ < 7500) Controller.timeJ++; else Controller.timeJ = 0;
//				//Controller.timeJ = timer;
//			}
			
//			if (ticked) {
//				//render();
//				renderMenu();//not in loop run as same below put in above
//				frames++;
//			}
//			render();
//			frames++;
			//renderMenu(); too much fps here squling GPU not good put in in Limit FPS mode
			//Mouse Position Track Debug
			//System.out.println("X: " + InputHandler.MouseX + " Y: " + InputHandler.MouseY);
			//System.out.println(InputHandler.WindowX); //WindowY

		/**************/
		}
	}
	
	
	public void renderMenu() {
		//put it later in different thread? than Main Game?
		BufferStrategy bs = this.getBufferStrategy();
		if(bs==null) {
			createBufferStrategy(3);
			return;
		}

		
		Graphics g = bs.getDrawGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 800, 400);
		try {
			g.drawImage(ImageIO.read(Display.class.getResource("/wallpapers/launcher_menu.jpg")),0,0, 800, 400, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", 0, 30));
		g.drawString("Play", 720, 90);
		g.dispose();
		bs.show();
		
	}

	public void render() {
		if(!PlayerController.not_paused) {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs==null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.setColor(Color.BLACK);
		//g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", Font.BOLD, 50));//0,1,2,3 bold italics bold and italics
		g.drawString("PAUSED", WIDTH/2-100, HEIGHT/2);//drawString me +"" oxi sketo int tha baraei error
		g.dispose();
		bs.show();
		} else {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs==null) {
			createBufferStrategy(3);
			return;
		}
		
		screen.render(game);
		
		for (int i = 0; i <getGameWidth() * getGameHeight(); i++) {
			pixels[i] = screen.PIXELS[i];
		}
		
		//Graphics2D g = (Graphics2D) bs.getDrawGraphics();//GRAPHICS Graphics2D
		 g = bs.getDrawGraphics();
		//g.drawImage(img, 0, 0, WIDTH*20, HEIGHT*20, null);
		//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, getGameWidth(), getGameHeight(), null);
		g.setFont(new Font("Verdana", 0, 50));//0,1,2,3 bold italics bold and italics
		g.setColor(Color.WHITE);
		g.drawString(fpsInnerText + " fps", 20, 50);//drawString me +"" oxi sketo int tha baraei error
		g.dispose();
		bs.show();
		}
	}

	//update method
	private void tick() {
		input.tick();
		//System.out.println(input.key[KeyEvent.VK_UP]);
		try {robot = new Robot();} catch (AWTException e) {e.printStackTrace();}
		//robot.keyPress(KeyEvent.VK_H);
		//robot.keyRelease(KeyEvent.VK_H);
		//System.out.println(Arrays.toString(input.key));//cant overriden because its point to array as whole not for each element! thats why for loop must
		//System.out.println(input.key);
		for (int i =0; i < input.key.length; i++) {
			if(input.key[i] == true) { 
				//System.out.println(i); System.out.println(Arrays.toString(input.key));
			}}
		//game.tick(input.key);
		game.tick();
		//System.out.println("break");
		
		newX = InputHandler.MouseX;
		newY = InputHandler.MouseY;
		int winX=InputHandler.WindowX;
		int winY=InputHandler.WindowY;
		
		{
		//mouse Speed below calc
			/*
			 * Pythagore theorem
				The distance travelled by the mouse between 2 calls to the mouseMotionListener is

				squareRoot(deltaX + deltaY)

				where:
				deltaX is (oldX - newX) at the power of 2
				deltaY is (oldY - newY) at the power of 2

				your mouseMotionListener can also store the time in milli if you want a really exact "speed"
			 */
		//System.out.println("X: " + (oldX-newX) + " Y: " + (oldY-newY));
		//System.out.println((oldX-newX) + (oldY-newY));
		//mouseSpeed = Math.abs((oldX-newX) + (oldY-newY));
		double mouseSpeedX_temp = Math.abs((oldX-newX));
		double mouseSpeedY_temp = Math.abs((oldY-newY));
		mouseSpeed = (int)Math.sqrt(mouseSpeedX_temp*mouseSpeedX_temp + mouseSpeedY_temp*mouseSpeedY_temp);
		}
		
		try {robot = new Robot();} catch (AWTException e) {e.printStackTrace();}
		//System.out.println(WindowLocation);
		
//		if(newX<0 || newX>WIDTH) robot.mouseMove((int)WindowLocation.getX()+500, (int)WindowLocation.getY()+500);//robot.mouseMove(WIDTH/2+500, HEIGHT/2);
//		if(newY<0 || newY>HEIGHT) robot.mouseMove((int)WindowLocation.getX()+500, (int)WindowLocation.getY()+500);//robot.mouseMove(WIDTH/2+500, HEIGHT/2);

		if(newX<0 || newX>WIDTH) robot.mouseMove(winX+500, winY+500);
		if(newY<0 || newY>HEIGHT) robot.mouseMove(winX+500, winY+500);
		//System.out.println(newY);
		if(newX<0) robot.mouseMove(winX+ getGameWidth() -20, winY + newY);
		if (newX>=getGameWidth()-20) robot.mouseMove(winX+20, winY + newY);
		if(newY<15) robot.mouseMove(winX + newX, winY + getGameHeight()-20);
		if(newY>=getGameHeight()-60) robot.mouseMove(winX + newX, winY + 40);

		if(newY < oldY && PlayerController.rotationUp <= 2.8) {PlayerController.turnUpM = true;}
		if(newY < oldY && PlayerController.rotationUp >= 2.8) {PlayerController.turnUpM = false;}
		if(newY == oldY) {PlayerController.turnUpM = false; PlayerController.turnDownM = false;}
		if(newY > oldY && PlayerController.rotationUp >= -0.8) {PlayerController.turnDownM = true;}
		if(newY > oldY && PlayerController.rotationUp <= -0.8) {PlayerController.turnDownM = false;}

		//String temp = newX < oldX ? System.out.println("Left"); : "";
		if (newX > oldX) {
			//System.out.println("Right");
			PlayerController.turnRightM = true;
		}
		if ( newX == oldX) {
			//System.out.println("Still X");
			PlayerController.turnLeftM = false;
			PlayerController.turnRightM = false;
		}
		//if(newX == WIDTH/2 || newX < WIDTH/2)//goes with direct below if
		if (newX < oldX) {
			//System.out.println("Left");
			PlayerController.turnLeftM = true;
		}
//		if (newY == oldY) {
//			//System.out.println("Still Y");
//		}
		double mouseSpeedX_temp = Math.abs((oldX-newX));
		double mouseSpeedY_temp = Math.abs((oldY-newY));
		MouseSpeed = (int)Math.sqrt(mouseSpeedX_temp*mouseSpeedX_temp + mouseSpeedY_temp*mouseSpeedY_temp);
				//MouseSpeed = Math.abs((newX - oldX) + (newY - oldY));//no negative value return!
				//if (MouseSpeed < 0) { MouseSpeed *= -1;}
				oldX = newX;
				oldY = newY;
				//oldY = newY;
		
	}

	public static void main(String[] args) {
		//Display display = new Display();
		//new Launcher(0, display );//
		getLauncherInstance();
	}


}
