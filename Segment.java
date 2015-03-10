package c;

public class Segment implements Comparable<Segment>{
	SegmentedPath segment;
	Vctr3D endpoint;
	boolean isLeft;
	boolean isBlack; //Is this segment part of the original shapes (black)
					 //Or is it an offset path (blue)
	int index; //Index of segment in paths variable in the class SegmentedPaths.
	
	Segment(SegmentedPath segment, Vctr3D endpoint, boolean isLeft, int index, boolean isBlack){
		this.segment = segment;
		this.endpoint = endpoint;
		this.isLeft = isLeft;
		this.index = index;
		this.isBlack = isBlack;
	}

	float getYgivenX(float x){
		//Get y value at a given x value using equation of the segment in interest.
		Vctr3D pointA = segment.getStart();
		Vctr3D pointB = segment.get(0);
		
		float slope = (pointA.y - pointB.y) / (pointA.x - pointB.x);
		
		float intercept = pointA.y - (slope*pointA.x);
		
		float Y = (slope * x) + intercept;
		
		return Y;
	}
	
	@Override
	public int compareTo(Segment o) {
		if(endpoint.x == o.endpoint.x){
			if(endpoint.y < o.endpoint.y){
				return -1;
			}
			else if(endpoint.y > o.endpoint.y){
				return 1;
			}
			else{
				return 0;
			}
		}
		else if (endpoint.x < o.endpoint.x){
			return -1;
		}
		else{
			return 1;
		}
	}
	
}
