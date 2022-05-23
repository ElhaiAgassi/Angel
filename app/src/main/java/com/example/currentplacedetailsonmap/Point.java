package com.example.currentplacedetailsonmap;

import android.location.Location;


public class Point{
    public static Location lastKnownLocation;
    static double loc_x =  lastKnownLocation.getLatitude();
    static double loc_y =  lastKnownLocation.getLongitude();

    public double x = 0;
    public double y = 0;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static boolean inDanger(Point p1,Point p2,double radius)
    {
        double x_res = p2.x - p1.x;
        double y_res = p2.y - p1.y;
        double res = Math.sqrt(Math.pow(x_res,2)+Math.pow(y_res,2));
        if (res<radius)
        {
            System.out.println("you are in danger place");
            return true;
        }
        return false;
    }
}
