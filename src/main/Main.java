package main;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

public class Main {
	
	Robot r;

	public static void main(String[] args) {
		
		while (true) {

			Point pt = MouseInfo.getPointerInfo().getLocation();
			System.out.println("(" + pt.getX() + ", " + pt.getY() + ")");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
				
		}
	}

}
