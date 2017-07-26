package main;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Main {
	
	static Robot r;
	
	// this is the color threshold for an obstacle
	static final int threshold = 165;

	// these are the pixel coordinates for sampling the background color
	static final int backgroundX = 165;
	static final int backgroundY = 337;
	
	// the lower obstacle tracking coordinates 
	static final int lowX = 475;
	static final int lowY = 215;
	
	// the higher obstacle tracking coordinates 
	static final int highX = 475;
	static final int highY = 190;
	
	// how large do we want the obstacle detection boxes to be (the
	// boxes are centered on the tracking coordinates)
	static final int width = 40;
	static final int height = 5;
	
	// these define the amount of time (in ms) that the buttons are held for jumps
	static final int shortJumpTime = 100;
	static final int duckTime = 400;
	static final int longJumpTime = 200;
	
	// the current speed of the game (increases as the game progresses)
	static int speed = 0;

	public static void main(String[] args) {
		try {
			// this allows us to press buttons
			r = new Robot();
		} catch (AWTException e) {
			System.err.println("Could not create robot");
			System.exit(1);
		}

		playGame();
	}
	
	/*
	 * this is to be used to configure the tracking points- move your
	 * mouse to where you'd like the tracking points to be and then change
	 * their values in the code
	 */
	public static void trackMouse() {
		while (true) {
			Point pt = MouseInfo.getPointerInfo().getLocation();
			System.out.println("(" + pt.getX() + ", " + pt.getY() + ")");
			
			Color color = r.getPixelColor((int) pt.getX(), (int) pt.getY());
			System.out.println(color.getRed());
			
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	/*
	 * the main method for playing the game. It will run indefinitely, even if it loses
	 */
	public static void playGame() {
		
		boolean wasDay = true;
		
		while (true) {
			
			boolean isDay = isDay();

			/*
			 * if the night just changed to day, increase the speed
			 */
			if (!wasDay && isDay) {
				speed = Math.min(speed + 1, 3);
			}
			wasDay = isDay;

			// first we get the high obstacle detection zone
			BufferedImage highImg = r.createScreenCapture(new Rectangle(highX, highY, width + speed * 7, height));
			boolean highObstical = isObstical(highImg);
			
			/*
			 *  this prevents the dino from ducking when it encounters certain cacti. Consider the case where the
			 *  high detection zone triggers because of the high part of the cactus, but the low zone doesn't because
			 *  the center of the cactus is recessed. Now, iff the high zone triggers, the low zone looks ahead more
			 *  in order to prevent this problem 
			 */
			int lowLookAhead = 0;
			if (highObstical) {
				lowLookAhead = 10;
			}
			
			BufferedImage lowImg = r.createScreenCapture(new Rectangle(lowX, lowY, width + speed * 7 + lowLookAhead, height));
			boolean lowObstical = isObstical(lowImg);

			// use the results to determine whether or not to dodge
			dodge(lowObstical, highObstical);
			
		}
		
	}
	
	public static void dodge(boolean lowObstical, boolean highObstical) {
		if (lowObstical && highObstical) {
			System.out.println("long jump");
			jump(longJumpTime, 0);
			return;
		}
		if (lowObstical && !highObstical) {
			System.out.println("short jump");
			jump(shortJumpTime, speed);
			return;
		}
		if (!lowObstical && highObstical) {
			System.out.println("duck");
			duck(duckTime);
			return;
		}
		if (!lowObstical && !highObstical) {
			return;
		}
	}
	
	/*
	 * here we jump
	 * 
	 * time is the amount of time for which to hold the up key, and
	 * downWaitTime is the amount of time for which to wait before
	 * pressing the down key. If downWaitTime == 0, the down key
	 * is not pressed
	 */
	public static void jump(int time, int downWaitTime) {
		r.keyPress(KeyEvent.VK_UP);
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			return;
		}
		r.keyRelease(KeyEvent.VK_UP);

		if (downWaitTime > 0) {
			try {
				Thread.sleep(time + downWaitTime);
			} catch (InterruptedException e) {
				return;
			}

			r.keyPress(KeyEvent.VK_DOWN);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
			r.keyRelease(KeyEvent.VK_DOWN);
		}
	}

	// more or less same as jump
	public static void duck(int time) {
		r.keyPress(KeyEvent.VK_DOWN);
		try {
			int newSpeed = Math.min(speed, 2);
			Thread.sleep((long) (time/(1 + 0.5 * newSpeed)));
		} catch (InterruptedException e) {
			return;
		}
		r.keyRelease(KeyEvent.VK_DOWN);
	}
	
	// compares the background color to the threshold in order to determine whether
	// it's night or day
	public static boolean isDay() {
		return r.getPixelColor(backgroundX, backgroundY).getRed() > threshold;
	}
	
	// determines whether there's an obstacle in img
	public static boolean isObstical(BufferedImage img) {
		boolean obstical = false;
		int background = r.getPixelColor(backgroundX, backgroundY).getRed();
		for (int x = 0; x < img.getWidth() && !obstical; x++) {
			for (int y = 0; y < img.getHeight() && !obstical; y++) {
				obstical |= Math.abs(new Color(img.getRGB(x, y)).getRed() - background) > 10;
			}
		}
		
		return obstical;
	}

}