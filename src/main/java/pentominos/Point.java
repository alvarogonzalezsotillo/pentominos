package pentominos;

import java.util.Comparator;

import pentominos.Board.PiecePosition;

public class Point implements Comparable<Point>{
	
	public static Comparator<Point> COMPARATOR = new Comparator<Point>(){

		@Override
		public int compare(Point o1, Point o2) {
			if( o1.x() == o2.x() ){
				return o1.y() - o2.y();
			}
			return o1.x() - o2.x();
		}
		
	};
	private static final int DIMCACHEY = 40;
	private static final int DIMCACHEX = 40;

	private int _x, _y;
	private Point _down;
	private Point _left;
	private Point _right;
	private Point _up;

	private static Point _cache[][];
	
	private static Point[][] cache(){
		if( _cache != null ){
			return _cache;
		}
		
		_cache = new Point[DIMCACHEX][];
		for( int x = 0 ; x < _cache.length ; x++ ){
			_cache[x] = new Point[DIMCACHEY];
		}
		
		return _cache;
	}
	
	private Point(int x, int y) {
		_x = x;
		_y = y;
	}
	
	public static Point create(int x, int y){
		int offsetx = DIMCACHEX/2;
		int offsety = DIMCACHEY/2;
		Point ret = cache()[x+offsetx][y+offsety]; 
		if( ret != null ){
			return ret;
		}
		ret = new Point(x,y);
		cache()[x+offsetx][y+offsety] = ret;
		return ret;
	}


	@Override
	public boolean equals(Object obj) {
		if( obj == this ){
			return true;
		}
		Point p = (Point) obj;
		return _x == p._x && _y == p._y;
	}
	
	public boolean nextTo( Point p ){
		if( equals(p) ){
			return true;
		}
		
		if( _x == p._x ){
			if( _y == p._y+1 || _y == p._y-1 ){
				return true;
			}
		}

		if( _y == p._y ){
			if( _x == p._x+1 || _x == p._x-1 ){
				return true;
			}
		}
		
		return false;
	}
	
	public Point move( Direction d ){
		switch( d ){
			case down: if( _down == null ){ _down = create( _x, _y+1 ); } return _down;
			case left: if( _left == null ){ _left = create( _x+1, _y ); } return _left;
			case right: if( _right == null ){ _right = create( _x-1, _y ); } return _right;
			case up: if( _up == null ){ _up = create( _x, _y-1 ); } return _up;
		}
		throw new IllegalArgumentException( d.toString() );
	}
	
	public Point rotate( Direction d, int w, int h  ){
		switch( d ){
			case down: return create( w - _x - 1, h - _y - 1 );
			case flipH: return create( _x, h - _y - 1 );
			case flipV: return create( w - _x - 1, _y );
			case left: return create(  _y, w - _x - 1);
			case right: return create( h - _y -1 , _x );
			case up: return this;
			
		}
		throw new UnsupportedOperationException();
	}

	public int x() {
		return _x;
	}

	public int y() {
		return _y;
	}
	
	@Override
	public String toString() {
		return _x + "," + _y;
	}

	@Override
	public int compareTo(Point o) {
		return COMPARATOR.compare(this, o);
	}
}
