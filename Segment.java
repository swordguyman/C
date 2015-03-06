package c;

public class Segment implements Comparable<Segment>{
	SegmentedPath segment;
	Vctr3D endpoint;
	boolean isLeft;
	int index; //Index of segment in paths variable in the class SegmentedPaths.
	
	Segment(SegmentedPath segment, Vctr3D endpoint, boolean isLeft, int index){
		this.segment = segment;
		this.endpoint = endpoint;
		this.isLeft = isLeft;
		this.index = index;
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
