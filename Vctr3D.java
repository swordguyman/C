//package b_object3D_collision;
package c;

public class Vctr3D{  // This a row vector by the convention of this file

    // At times is used as a column vector. I.e., (x,y,z) is not T(a,b,c)
    float x; float y; float z;
    static final float EPSILON = 1e-6f; // Guessing. What should this be?
    static final float EPSILON_B = EPSILON*100; // Guessing. What should this be?

    Vctr3D() { }
    Vctr3D(float a, float b, float c) { this.x=  a; this.y=  b; this.z=  c; }
    Vctr3D(Vctr3D v)                  { this.x=v.x; this.y=v.y; this.z=v.z; }

    Vctr3D minus(         Vctr3D v2) { return new Vctr3D(x-  v2.x,y-  v2.y,z-  v2.z); }
    Vctr3D  plus(         Vctr3D v2) { return new Vctr3D(x+  v2.x,y+  v2.y,z+  v2.z); }
    Vctr3D  plus(float m, Vctr3D v2) { return new Vctr3D(x+m*v2.x,y+m*v2.y,z+m*v2.z); }

    float distL1(         Vctr3D p2) { 
        return    Math.abs(x - p2.x) 
                + Math.abs(y - p2.y) 
                + Math.abs(z - p2.z);
    }

    Vctr3D times( Triangle3D m ) { return new Vctr3D( // this is a row vector and we are doing this*m
            this.x*m.a[0] + this.y*m.b[0] + this.z*m.c[0],
            this.x*m.a[1] + this.y*m.b[1] + this.z*m.c[1],
            this.x*m.a[2] + this.y*m.b[2] + this.z*m.c[2]  ); }

    Vctr3D solve( Triangle3D m ) { // This is the row vector version
        Vctr3D r = new Vctr3D(
                m.b[1]*m.c[2]-m.c[1]*m.b[2],
                m.c[1]*m.a[2]-m.a[1]*m.c[2],
                m.a[1]*m.b[2]-m.b[1]*m.a[2] );
        float det = m.a[0]*r.x + m.b[0]*r.y + m.c[0]*r.z;
        if (det != 0.f) {
            r.x = (r.x * x - (m.b[0]*m.c[2]-m.c[0]*m.b[2]) * y + (m.b[0]*m.c[1]-m.c[0]*m.b[1]) * z) / det;
            r.y = (r.y * x - (m.c[0]*m.a[2]-m.a[0]*m.c[2]) * y + (m.c[0]*m.a[1]-m.a[0]*m.c[1]) * z) / det;
            r.z = (r.z * x - (m.a[0]*m.b[2]-m.b[0]*m.a[2]) * y + (m.a[0]*m.b[1]-m.b[0]*m.a[1]) * z) / det;
        } else { // The matrix is not invertible
            r.x = Float.MAX_VALUE;
        }
        return r;
        //float a[0],a[1],a[2];
        //float b[0],b[1],b[2];
        //float c[0],c[1],c[2];
    }

    Vctr3D solveT( Triangle3D m ) { // Column vector version
        assert(false); // We need a check for whether the matrix is ill conditioned as in solve();
        Vctr3D r = new Vctr3D(
                m.b[1]*m.c[2]-m.b[2]*m.c[1],
                m.b[2]*m.c[0]-m.b[0]*m.c[2],
                m.b[0]*m.c[1]-m.b[1]*m.c[0] );
        float det = m.a[0]*r.x + m.a[1]*r.y + m.a[2]*r.z;
        if (det != 0.f) {
            r.x = (r.x * x - (m.a[1]*m.c[2]-m.a[2]*m.c[1]) * y + (m.a[1]*m.b[2]-m.a[2]*m.b[1]) * z) / det;
            r.y = (r.y * x - (m.a[2]*m.c[0]-m.a[0]*m.c[2]) * y + (m.a[2]*m.b[0]-m.a[0]*m.b[2]) * z) / det;
            r.z = (r.z * x - (m.a[0]*m.c[1]-m.a[1]*m.c[0]) * y + (m.a[0]*m.b[1]-m.a[1]*m.b[0]) * z) / det;

            // Check the result
            Vctr3D chck = times(m).minus(this);
            if ( ! (-EPSILON_B < chck.x && chck.x < EPSILON_B &&
                    -EPSILON_B < chck.y && chck.y < EPSILON_B &&
                    -EPSILON_B < chck.z && chck.z < EPSILON_B    ) ) {
                r.x = Float.MAX_VALUE/2; // The matrix is ill conditioned enough to cause numerical issues
            }
        } else { // The matrix is not invertible
            r.x = Float.MAX_VALUE;
        }
        return r;
    }

    String print() { return "V3D: "+x+", "+y+", "+z+"."; }

    protected boolean isCollision(Vctr3D v, Vctr3D p, Vctr3D vA, Vctr3D vB) {
        // true if point(this) + m * vector(v) == point(p) + a * vector(vA) + b * vector(vB)
        //         for some 0 <= m,a,b <= 1 and a+b <= 1 
        // else false

        // Solve for pd: (this - p) == pd * (v,vA,vB). Consequently, m = -pd.x
        Vctr3D pd = minus(p).solve(new Triangle3D(v,vA,vB));

        if (    pd.x <  EPSILON && (-1f-EPSILON) < pd.x &&
                pd.y > -EPSILON && pd.z > -EPSILON &&
                pd.y+pd.z < (1f+EPSILON) ) 
            return true ;
        else // TODO else if pd.x==Float.MAX_VALUE
            return false;
    }
    
    protected float getDistance2D(Vctr3D pA2) { // "this" is pA1 
        // Note, this is a specialized function to return a point2point distance for 2D polygon growth algorithm
        // It is kind of an ugly design though.

        float vAx = pA2.x-    x;
        float vAy = pA2.y-    y;
        return (float) Math.sqrt( vAx * vAx + vAy * vAy );
    }
    
    protected float getDistance2D(Vctr3D pB1, Vctr3D pB2) { // "this" is pA1 
        // Note, this is a specialized function to return a point2segment distance for 2D polygon growth algorithm
        // It is kind of an ugly design though.
        // One can make this method more efficient.

        float vBx = pB2.x-pB1.x; // vAx =  vBy
        float vBy = pB2.y-pB1.y; // vAy = -vBx

        float crsProduct = vBy * vBy + vBx * vBx ;
        
        if ( crsProduct == 0 ) return getDistance2D(pB1); 

        // Solve: pA1.x + alpha * vAx == pB1.x + beta * vBx
        //        pA1.y + alpha * vAy == pB1.y + beta * vBy
        //float alpha = -((x - pB1.x) * vBy - (y - pB1.y) * vBx) / crsProduct; 
        float beta  =  ((x - pB1.x) * vBx + (y - pB1.y) * vBy) / crsProduct; 
        
        if      (beta <= 0) return getDistance2D(pB1);
        else if (beta >= 1) return getDistance2D(pB2);
        else                return getDistance2D( new Vctr3D( pB1.x + beta * vBx, pB1.y + beta * vBy, pB1.z) );
    }
    
    protected float getDistance2D(Vctr3D pA2, Vctr3D pB1, Vctr3D pB2) { // "this" is pA1 
        // Note, this is a specialized function to return a segment2segment distance for 2D polygon growth algorithm
        // It is kind of an ugly design though.
        // One can make this method more efficient.

        // 
        if (getIntersect2D(pA2, pB1, pB2) != null) return 0.0f; // the two segments intersect
        float dA1 =     getDistance2D( pB1,pB2);
        float dA2 = pA2.getDistance2D( pB1,pB2);
        float dB1 = pB1.getDistance2D(this,pA2);
        float dB2 = pB2.getDistance2D(this,pA2);
        return Math.min( Math.min(dA1, dA2), Math.min(dB1, dB2) );
    }
    
    protected float [] getIntersect2D(Vctr3D pA2, Vctr3D pB1, Vctr3D pB2 ) { // "this" is pA1 
        // Note, this is a specialized function to return an intersect for 2D polygon growth algorithm
        // It is kind of an ugly design though.
        // returns (null) when no intersect, (x,y,crossproduct) when there is an intersect
        // 

        if ( pA2.distL1( pB1) < .1 ) return null; // one segment continues the other
        if ( pA2.distL1( pB2) < .1 ) return null; // segments terminate at the same point
        if ( pB1.distL1(this) < .1 ) return null; // segments originate at the same point
        if ( pB2.distL1(this) < .1 ) return null; // one segment continues the other

        float vAx = pA2.x-    x;
        float vAy = pA2.y-    y;
        float vBx = pB2.x-pB1.x;
        float vBy = pB2.y-pB1.y;
        float dotProduct = vAx * vBx + vAy * vBy ;
        float crsProduct = vAx * vBy - vAy * vBx ;
        
        float crsLimit = Math.max(1e-8f, dotProduct*0.0001f);
        if ( -crsLimit < crsProduct && crsProduct < crsLimit ) return null; // null if parallel
        
        // Solve: pA1.x + alpha * vAx == pB1.x + beta * vBx
        //        pA1.y + alpha * vAy == pB1.y + beta * vBy
        float alpha = -((x - pB1.x) * vBy - (y - pB1.y) * vBx) / crsProduct; 
        float beta  = -((x - pB1.x) * vAy - (y - pB1.y) * vAx) / crsProduct; 

        if (    0<=alpha && alpha<=1 && 
                0<=beta  && beta <=1    ) 
            return new float [] {x+alpha*vAx,y+alpha*vAy,crsProduct};
        else 
            return null;
    }
    
    protected float [] getExtrapolateIntersect2D(Vctr3D pB2, // "this" is pB1 
            float v1x, float v1y, float v2x, float v2y, float crossProduct) { // these vectors are in the direction of motion
        // Note, this is a specialized function to return an extrapolated intersect for 2D polygon growth algorithm
        // It is kind of an ugly design though.
        // 
        // Solve: pB1x + alpha * v1x == pB2x - beta * v2x
        //        pB1y + alpha * v1y == pB2y - beta * v2y
        float alpha = -((x - pB2.x) * v2y - (y - pB2.y) * v2x) / crossProduct; 

        return new float [] {x + alpha * v1x, y + alpha * v1y};
    }
    
    protected float [] getExtrapolateTwoIntersect2D(Vctr3D pB2, Vctr3D pB, float offset, // "this" is pB1 
            float v1x, float v1y, float v2x, float v2y, float crossProduct) { // these vectors are in the direction of motion
        // Note, this is a specialized function to return an intersect for 2D polygon growth algorithm
        // It is kind of an ugly design though.
        // This one is specialized for a convex sharp turn
        // 
        Vctr3D pOffsetB = new Vctr3D(pB);
        float vMidX = pB2.x - x;
        float vMidY = pB2.y - y;
        float dist = (float) Math.sqrt(vMidX*vMidX+vMidY*vMidY);
        pOffsetB.x +=  vMidY/dist*offset;
        pOffsetB.y += -vMidX/dist*offset;
        
        float [] point1 =          getExtrapolateIntersect2D(pOffsetB,   v1x,   v1y, vMidX, vMidY,   v1x * vMidY -   v1y * vMidX);
        float [] point2 = pOffsetB.getExtrapolateIntersect2D(pB2     , vMidX, vMidY,   v2x,   v2y, vMidX *   v2y - vMidY *   v2x);

        return new float [] {point1[0], point1[1], point2[0], point2[1]};
    }
    
    public static void main0(String[] args) {
        // Unit test
        Vctr3D   pA1 = new Vctr3D(0,0,1);
        Vctr3D   pA2 = new Vctr3D(2,2,1);
        Vctr3D   pB1 = new Vctr3D(0,2,1);
        Vctr3D   pB2 = new Vctr3D(2,0,1);
        float [] intersect = pA1.getIntersect2D(pA2, pB1, pB2);
        System.out.println("Intersect: "+intersect[0]+" "+intersect[1]+" "+intersect[2]);
        float [] intersect2 = pA1.getIntersect2D(pB1, pA2, pB2);
        System.out.println("Intersect: "+intersect2);
    }
}
