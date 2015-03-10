package c;

public class YContainer {
	//contains a point's y value, and the index in the paths variable
	//offset logic is handled in SegmentedPaths
	float y;
	float slope; //for comparisons
	int index;
	
	public YContainer(float point, float sl, int ind){
		y = point;
		slope = sl;
		index = ind;
	}
	
	public String toString(){
		return "y: " + y + " , index of segment: " + index;
	}

}
