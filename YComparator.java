package c;

import java.util.Comparator;

public class YComparator implements Comparator<Segment> {
	//sorts segments by either maxY or minY
	//like the SweepLineComparator, but more elaborate 
	private float offset; //holds offset, if any
	
	public YComparator(float off){
		offset = off;
	}

	@Override
	public int compare(Segment arg0, Segment arg1) {
		float Y1 = arg0.endpoint.y;
		float Y2 = arg1.endpoint.y;
		if(!arg0.isBlack){
			if(arg0.isLeft){
				//if we are sorting the maxes, we add the offset
				Y1 -= offset;
				Y2 -= offset;
			}else{
				Y1 += offset;
				Y2 += offset;
			}
		}
		if(!arg1.isBlack){
			//repeat for arg1 because the order of these can change
			if(arg1.isLeft){
				//if we are sorting the maxes, we add the offset
				Y1 -= offset;
				Y2 -= offset;
			}else{
				Y1 += offset;
				Y2 += offset;
			}
		}
		//now we are either comparing the mins or the maxes, and if we have a blue segment, we've "baked" the offset into the comparison.
		if(Y1 == Y2){
			//we have a problem, we should look at the slope
			//ideally we never get here
			float[] slopes = new float[]{arg0.getSlope(),arg1.getSlope()};

			if(slopes[0] > slopes[1])
				return 1;
			else if(slopes[0] < slopes[1])
				return -1;
			else
				return 0; //We should never get here, but include it for safety.
		}else{
			if(Y1 < Y2) return -1;
			else return 1;
		}
	}
	
	public Segment getMaxY(Segment a, Segment b){
		int compareValue = compare(a,b);
		if(compareValue == 1){
			return a;
			//should never return 0
		}else{
			return b;
		}
	}
	
	public Segment getMinY(Segment a, Segment b){
		int compareValue = compare(a,b);
		if(compareValue == -1){
			return a;
		}else{
			return b;
		}
	}
}
