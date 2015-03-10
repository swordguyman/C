package c;

import java.util.ArrayList;
import java.util.Comparator;

public class SweepLineComparator implements Comparator<Segment>{
	//this class sorts the points so we can use them for the more efficient sweep line (check only above and below)
	ArrayList<SegmentedPath> paths; //so we can compute slopes...only use in worst case!
	
	public SweepLineComparator(ArrayList<SegmentedPath> allpaths){
		paths = allpaths;
	}
	
	
	public int compare(Segment a, Segment b){
		//Idea is to get the y values where the sweep line is at this.x so we can sort activeSegments accordingly.
		float current_y = a.endpoint.y;
		//a.getYgivenX(x);
		float other_y = b.endpoint.y;
		//getYgivenX(x); 
		
		if(current_y == other_y){
			//we're in trouble. this is why we include paths - sort by slope. 
			float slope0 = a.getSlope();
			float slope1 = b.getSlope();
			
			if(slope0 > slope1)
				return 1;
			else if(slope0 < slope1)
				return -1;
			else
				return 0; //We should never get here, but include it for safety.
		}
		
		else if(current_y > other_y){
			return 1; //reverse so that the largest y value at x is at index 0
		}
		else{
			return -1;
		}
	}
			
	/*
	public int compare(Segment arg0, Segment arg1) {
		//sort by y. If y is equal, sort by slope.
		float y1 = arg0.endpoint.y;
		float y2 = arg1.endpoint.y;
		
		if(y1 == y2){
			//we're in trouble. this is why we include paths - sort by slope. 
			SegmentedPath lineA = arg0.segment;
			SegmentedPath lineB = arg1.segment;
			//get the points for each of the two segments
			Vctr3D pointA1 = lineA.getStart();
			Vctr3D pointA2 = lineA.get(0);
			float slope0 = (pointA2.y - pointA1.y) / (pointA2.x - pointA1.x);
			Vctr3D pointB1 = lineB.getStart();
			Vctr3D pointB2 = lineB.get(0);
			float slope1 = (pointB2.y - pointB1.y) / (pointB2.x - pointB1.x);
			if(slope0 < slope1)
				return -1;
			else if(slope0 > slope1)
				return 1;
			else
				return 0; //We should never get here, but include it for safety.
			
		}
		return 0;
	}
	*/
}