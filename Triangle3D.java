//package b_object3D_collision;
package c;

import javafx.geometry.Point3D;

// This triangle/matrix manipulation package is incomplete 
//  and in some places lacking approaches to handle numerical
public class Triangle3D {
    float [] a = new float[3];
    float [] b = new float[3];
    float [] c = new float[3];

    Triangle3D() { }
    
    Triangle3D(
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz
            ) 
    {
        this.a[0] = ax;  this.a[1] = ay;  this.a[2] = az;
        this.b[0] = bx;  this.b[1] = by;  this.b[2] = bz;
        this.c[0] = cx;  this.c[1] = cy;  this.c[2] = cz;
    }
    
    Triangle3D(
            Point3D a, // These are row vectors
            Point3D b,
            Point3D c
            ) 
    {
        this.a[0] = (float)a.getX();  this.a[1] = (float)a.getY();  this.a[2] = (float)a.getZ();
        this.b[0] = (float)b.getX();  this.b[1] = (float)b.getY();  this.b[2] = (float)b.getZ();
        this.c[0] = (float)c.getX();  this.c[1] = (float)c.getY();  this.c[2] = (float)c.getZ();
    }

    Triangle3D(
            Vctr3D a, // These are row vectors
            Vctr3D b,
            Vctr3D c
            ) 
    {
        this.a[0] = a.x;  this.a[1] = a.y;  this.a[2] = a.z;
        this.b[0] = b.x;  this.b[1] = b.y;  this.b[2] = b.z;
        this.c[0] = c.x;  this.c[1] = c.y;  this.c[2] = c.z;
    }

    public class Pair3D { // Simply a pair of triplets
        Vctr3D a;
        Vctr3D b;
        Pair3D () {
            a = new Vctr3D();
            b = new Vctr3D();
        }
    }

    boolean isCollision(Triangle3D t) { // Check for collision between two triangles
        Vctr3D p0  = new Vctr3D(  a[0]       ,  a[1]       ,  a[2]       );
        Vctr3D v10 = new Vctr3D(  b[0]-  a[0],  b[1]-  a[1],  b[2]-  a[2]);
        Vctr3D v20 = new Vctr3D(  c[0]-  a[0],  c[1]-  a[1],  c[2]-  a[2]);

        Vctr3D p3  = new Vctr3D(t.a[0]       ,t.a[1]       ,t.a[2]       );
        Vctr3D v43 = new Vctr3D(t.b[0]-t.a[0],t.b[1]-t.a[1],t.b[2]-t.a[2]);
        Vctr3D v53 = new Vctr3D(t.c[0]-t.a[0],t.c[1]-t.a[1],t.c[2]-t.a[2]);

        if ( p0.isCollision(v10, p3, v43, v53) ) return true;
        if ( p0.isCollision(v20, p3, v43, v53) ) return true;
        if ( p3.isCollision(v43, p0, v10, v20) ) return true;
        if ( p3.isCollision(v53, p0, v10, v20) ) return true;

        // Need to check one more edge
        Vctr3D p1  = new Vctr3D(  b[0]       ,  b[1]       ,  b[2]       );
        Vctr3D v21 = new Vctr3D(  c[0]-  b[0],  c[1]-  b[1],  c[2]-  b[2]);

        if ( p1.isCollision(v21, p3, v43, v53) ) return true;

        return false; 
    }

    boolean isCollisionBoundingBoxExhaustive( 
            float [] minP, float [] maxP   ) { // 3D coordinates 
                           // of the min and max of the bounding box

        // This is the exhaustive check for whether a triangle 
        //   intersects any faces of a bounding box.
        
        // The code is meant to be more demonstrative than optimal
        //   Thus, there is much more that one can do to speed this up
        
        // There are three edges of the triangle that we will check
        //   for intersections with the six bounding box sides.
        //   Thus, there are eighteen checks
        
        float[] vBA = {  b[0]-  a[0],  b[1]-  a[1],  b[2]-  a[2]};
        if (isCollisionSegmentToBoxFace(a,vBA,0,minP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,0,maxP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,1,minP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,1,maxP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,2,minP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        if (isCollisionSegmentToBoxFace(a,vBA,2,maxP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        float[] vCB = {  c[0]-  b[0],  c[1]-  b[1],  c[2]-  b[2]};
        if (isCollisionSegmentToBoxFace(b,vCB,0,minP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,0,maxP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,1,minP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,1,maxP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,2,minP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        if (isCollisionSegmentToBoxFace(b,vCB,2,maxP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        float[] vAC = {  a[0]-  c[0],  a[1]-  c[1],  a[2]-  c[2]};
        if (isCollisionSegmentToBoxFace(c,vAC,0,minP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,0,maxP[0],minP[1],minP[2],maxP[1],maxP[2])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,1,minP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,1,maxP[1],minP[2],minP[0],maxP[2],maxP[0])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,2,minP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;
        if (isCollisionSegmentToBoxFace(c,vAC,2,maxP[2],minP[0],minP[1],maxP[0],maxP[1])) return true;

        return false;
    }
    
    static boolean isCollisionSegmentToBoxFace( float [] point, float [] vector, int dimension0, 
            float level, float min1, float min2, float max1, float max2) {
        // dimension0 - 0 for x, 1 for y, 2 for z
        // level - location of the bounding box in the dimension above
        // min1, min2, max1, max2 - other bounds of the bounding box shape in question

        if (vector[dimension0]<=0) return false; // There is no thickness in this dimension

        // This is the proportion of distance to appropriate face plane from point.
        float alpha = (level - point[dimension0]) / vector[dimension0];
        if (alpha < 0 || 1 < alpha) return false; // The bounding box face is too far
        
        int dimension1 = (dimension0+1)%3; // enumerate the other dimensions
        float value1 = point[dimension1]+alpha*vector[dimension1];
        if (value1 < min1 || max1 < value1) return false;
        
        int dimension2 = (dimension0+2)%3; // enumerate the other dimensions
        float value2 = point[dimension2]+alpha*vector[dimension2];
        if (value2 < min2 || max2 < value2) return false;

        return true;
    }

    Pair3D intersectPlaneZ( float zLevel ) {
        // Point A vs. B1+B2 on different sides where A,B1,B2 in cross-section CW order
        float [] pA = null, pB1 = null, pB2 = null; // 

        if (     a[2]>zLevel && b[2]> zLevel && c[2]> zLevel) { return null;        }
        else if (a[2]<zLevel && b[2]< zLevel && c[2]< zLevel) { return null;        }
        else if (a[2]>zLevel && b[2]<=zLevel && c[2]<=zLevel) { pA=a; pB1=b; pB2=c; }
        else if (b[2]>zLevel && c[2]<=zLevel && a[2]<=zLevel) { pA=b; pB1=c; pB2=a; }
        else if (c[2]>zLevel && a[2]<=zLevel && b[2]<=zLevel) { pA=c; pB1=a; pB2=b; }
        else if (a[2]<zLevel && b[2]>=zLevel && c[2]>=zLevel) { pA=a; pB2=b; pB1=c; }
        else if (b[2]<zLevel && c[2]>=zLevel && a[2]>=zLevel) { pA=b; pB2=c; pB1=a; }
        else if (c[2]<zLevel && a[2]>=zLevel && b[2]>=zLevel) { pA=c; pB2=a; pB1=b; }
        else 
            { return null; }; // Logically, this should not happen, but if it did, not a big deal.

        // This is the proportion of distance to appropriate pB vertex from pA.
        float alpha1 = (zLevel - pA[2]) / (pB1[2] - pA[2]);
        float alpha2 = (zLevel - pA[2]) / (pB2[2] - pA[2]);

        Pair3D pair = new Pair3D();
        pair.a.x = pA[0] + alpha1 * (pB1[0] - pA[0]);
        pair.b.x = pA[0] + alpha2 * (pB2[0] - pA[0]);
        pair.a.y = pA[1] + alpha1 * (pB1[1] - pA[1]);
        pair.b.y = pA[1] + alpha2 * (pB2[1] - pA[1]);
        pair.a.z = zLevel;
        pair.b.z = zLevel;

        return pair;
    }

    Triangle3D inverse3x3() // This method returns an inverse of a 
    {                       // 3x3 matrix ((a[0],a[1],a[2]),(b[0],b[1],b[2]),(c[0],c[1],c[2]))
        // NOTE, THIS CODE IS UNTESTED!!!
        Triangle3D t = new Triangle3D();
        t.a[0] =      b[2]*c[1] - b[1]*c[2];
        t.b[0] =      b[0]*c[2] - b[2]*c[0];
        t.c[0] =      b[1]*c[0] - b[0]*c[1];
        float det = a[0]*t.a[0] + a[1]*t.b[0] + a[2]*t.c[0];
        if (det != 0.f) {
            t.a[0] /= det;
            t.b[0] /= det;
            t.c[0] /= det;
            t.a[1] = (c[2]*a[1] - c[1]*a[2])/det;
            t.b[1] = (c[0]*a[2] - c[2]*a[0])/det;
            t.c[1] = (c[1]*a[0] - c[0]*a[1])/det;
            t.a[2] = (a[2]*b[1] - a[1]*b[2])/det;
            t.b[2] = (a[0]*b[2] - a[2]*b[0])/det;
            t.c[2] = (a[1]*b[0] - a[0]*b[1])/det;
        }

        return t;
    }
    
    Triangle3D multiply3x3(Triangle3D t) // This method returns a product of two 
    {                       // 3x3 matrices ((a[0],a[1],a[2]),(b[0],b[1],b[2]),(c[0],c[1],c[2]))
        // NOTE, THIS CODE IS UNTESTED!!!
        Triangle3D r = new Triangle3D();
        r.a[0] = a[0]*t.a[0] + a[1]*t.b[0] + a[2]*t.c[0];
        r.b[0] = b[0]*t.a[0] + b[1]*t.b[0] + b[2]*t.c[0];
        r.c[0] = c[0]*t.a[0] + c[1]*t.b[0] + c[2]*t.c[0];
        r.a[1] = a[0]*t.a[1] + a[1]*t.b[1] + a[2]*t.c[1];
        r.b[1] = b[0]*t.a[1] + b[1]*t.b[1] + b[2]*t.c[1];
        r.c[1] = c[0]*t.a[1] + c[1]*t.b[1] + c[2]*t.c[1];
        r.a[2] = a[0]*t.a[2] + a[1]*t.b[2] + a[2]*t.c[2];
        r.b[2] = b[0]*t.a[2] + b[1]*t.b[2] + b[2]*t.c[2];
        r.c[2] = c[0]*t.a[2] + c[1]*t.b[2] + c[2]*t.c[2];

        return r;
    }

    public static void main0(String[] args) {
        // Unit test
        Triangle3D m1 = new Triangle3D(
                1,2,1,
                0,0,1,
                1,1,0 );
        Vctr3D   v1 = new Vctr3D(0,2,1);
        Vctr3D   v2 = v1.solve(m1);
        System.out.println(v2.print()); // v1 used as a row vector
        Vctr3D   v3 = v1.solveT(m1);
        System.out.println(v3.print()); // v1 used as a column vector
        
        Vctr3D p0 = new Vctr3D(1,1,-1);
        Vctr3D v0 = new Vctr3D(0,0,5);
        Vctr3D p1 = new Vctr3D(0,0,0);
        Vctr3D vA = new Vctr3D(3,0,0);
        Vctr3D vB = new Vctr3D(0,1.5f,0);
        if ( p0.isCollision(v0, p1, vA, v1) ) System.out.println("isCollision() worked!");
        else                                  System.out.println("isCollision() failed!");
        if ( p0.isCollision(v0, p1, vA, vB) ) System.out.println("isCollision() worked!");
        else                                  System.out.println("isCollision() failed!");
        if ( p0.isCollision(v0, p1, v1, vB) ) System.out.println("isCollision() failed!");
        else                                  System.out.println("isCollision() worked!");
    }
}
