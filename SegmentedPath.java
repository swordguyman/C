//package b_object3D_collision;
package c;

import java.util.ArrayList;

public class SegmentedPath {
    
    private ArrayList<Vctr3D> path;
    private boolean isRemoveLast = false;
    
    SegmentedPath() {
        path = new ArrayList<Vctr3D>();
    }

    SegmentedPath(Vctr3D startingPoint) {
        this(); // Call the default constructor first
        path.add(startingPoint);
    }

    void addNextPoint(Vctr3D nextPoint) {
        path.add(nextPoint);
    }

    private void removePoint(int iPoint) {
        path.remove(iPoint);
    }
    
    void addPath(SegmentedPath pathB) {
        // ToDo: assert that pathA.end matches pathB.start
        for( int iSegmentB = 0; iSegmentB < pathB.size(); iSegmentB++ ) {
            addNextPoint(pathB.get(iSegmentB));
        }
        isRemoveLast = pathB.isRemoveLast;
    }
    
    SegmentedPath splitPath(int iPoint) {
        SegmentedPath pathB = new SegmentedPath( new Vctr3D(path.get(iPoint+1)) );
        for( int iSegmentB = iPoint+2; iSegmentB < path.size(); iSegmentB++ ) {
            pathB.addNextPoint(path.get(iSegmentB));
        }
        for( int iSegmentB = path.size()-1; iSegmentB > iPoint+1; iSegmentB-- ) {
            path.remove(iSegmentB);
        }
        if ( isRemoveLast && pathB.size()>1 ) pathB.setRemoveLast();
        isRemoveLast = false;
        return pathB;
    }
    
    protected void removeMinisculeSegments() {
        Vctr3D pointA = path.get(0);
        Vctr3D pointB;
        for (int iSegment = 1; iSegment < path.size(); iSegment++ ) {
            pointB = path.get(iSegment);
            if (pointA.distL1(pointB)<.2) {
                if (iSegment<path.size()-1) removePoint(iSegment  );
                else                        removePoint(iSegment-1);
                iSegment--;// Bad form to modify iterator
            } else {
                pointA = pointB;
            }
        }
    }
    
    int size() { return path.size()-1; } // There is one less segment than # of points
    Vctr3D getStart() { return path.get(  0); }
    Vctr3D get(int i) { return path.get(i+1); }
    void set(int i, Vctr3D p) { path.set(i+1, p); }
    void removeLast() { path.remove(path.size()-1); }
    void setRemoveLast() { isRemoveLast = true; }
    boolean isRemoveLast() { return isRemoveLast; }
    boolean addNextRemovablePointChecked(Vctr3D nextPoint) {
        if (path.get(path.size()-1).distL1(nextPoint) > .1) {
            path.add(nextPoint);
            isRemoveLast = true;
            return true;
        } else {
            isRemoveLast = false;
            return false;
        }
    }
}
