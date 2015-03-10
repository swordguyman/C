package c;

import java.util.Comparator;

public class YComparator implements Comparator<YContainer> {
	//sorts y values.

	@Override
	public int compare(YContainer arg0, YContainer arg1) {
		// TODO Auto-generated method stub
		if(arg0.y == arg1.y){
			if(arg0.slope > arg1.slope){
				return 1;
			}else{
				return -1;
			}
		}else{
			if(arg0.y > arg1.y){
				return 1;
			}else{
				return -1;
			}
		}
	}
}
