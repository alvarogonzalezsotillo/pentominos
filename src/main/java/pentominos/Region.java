package pentominos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Region implements Comparable<Region>{
	private static final int MAXPOINTS = 200;
	private Point[] _points = new Point[MAXPOINTS];
	private int _minx, _miny, _maxx, _maxy;
	
	private static final boolean SORTPOINTS = false;
	private static final boolean USECONTAINS = false;
	
	private static int _lastId = 0;
	private int _id;
	private int _nPoints;
	
	public Region(){
		_id = _lastId++;
		_minx = _miny = Integer.MAX_VALUE;
		_maxx = _maxy = Integer.MIN_VALUE;
	}
	
	public int w(){
		return _maxx - _minx + 1;
	}

	public int h(){
		return _maxy - _miny + 1;
	}
	
	public int minx(){
		return _minx;
	}
	
	public int miny(){
		return _miny;
	}
	
	public Point[] points(){
		Point ret[] = new Point[_nPoints];
		System.arraycopy(_points, 0, ret, 0, _nPoints );
		return ret;
	}
	
	public Region( Point initPoint ){
		this();
		addToRegion( initPoint, false );
	}
	
	public boolean nextTo( Point p ){
		for( int i = 0 ; i < _nPoints ; i++ ){
			Point po = _points[i];
			if( po.nextTo(p) ){
				return true;
			}
		}
		return false;
	}
	
	public boolean nextTo( Region r ){
		for( int i = 0 ; i < _nPoints ; i++ ){
			Point p = _points[i];
			if( r.nextTo(p) ){
				return true;
			}
		}
		return false;
	}

	public boolean contains(Point p){
		for( int i = 0 ; i < _nPoints ; i++ ){
			if( _points[i].equals(p) ){
				return true;
			}
		}
		return false;
	}
	
	public boolean addToRegion( Point p ){
		return addToRegion( p, true );
	}
	
	@SuppressWarnings("unused")
	public boolean addToRegion( Point p, boolean ensureNextTo ){
		if( _nPoints == 0 ){
			addPoint_Internal(p);
			return true;
		}

		if( !ensureNextTo || nextTo( p ) ){
			if( !USECONTAINS || !contains(p) ){
				addPoint_Internal(p);
			}
			return true;
		}
		
		return false;
	}

	private void addPoint_Internal(Point p) {
		_points[_nPoints] = p;
		_nPoints++;
		if( p.x() < _minx ) _minx = p.x();
		if( p.x() > _maxx ) _maxx = p.x();
		if( p.y() < _miny ) _miny = p.y();
		if( p.y() > _maxy ) _maxy = p.y();
		
		if( SORTPOINTS ){
			Arrays.sort(_points);
		}
	}
	
	
	public static List<Region> merge( List<Region> regions ){
		List<Region> ret = new ArrayList<Region>(regions);
		int oldSize = 0;
		while( oldSize != ret.size() ){
			oldSize = ret.size();
			
			boolean next = false;
			for( int i = 0 ; i < ret.size()-1 && !next; i++ ){
				Region r1 = ret.get(i);
				for( int j = i+1 ; j < ret.size() && !next; j++ ){
					Region r2 = ret.get(j);
					Region m = merge( r1, r2 );
					if( m != null ){
						ret.remove(r1);
						ret.remove(r2);
						ret.add(m);
						next = true;
					}
				}
			}
		}
		
		return ret;
	}
	
	
	@SuppressWarnings("unused")
	public static Region merge( Region r1, Region r2 ){
		if( !r1.nextTo(r2) ){
			return null;
		}
		
		Region ret = new Region();
		for( int i = 0 ; i < r1._nPoints ; i++ ){
			Point p = r1._points[i];
			if( !USECONTAINS || !ret.contains(p) ){
				ret.addPoint_Internal(p);
			}
		}
		for( int i = 0 ; i < r2._nPoints ; i++ ){
			Point p = r2._points[i];
			if( !USECONTAINS || !ret.contains(p) ){
				ret.addPoint_Internal(p);
			}
		}
		return ret;
	}
	
	public int size(){
		return _nPoints;
	}
	
	@Override
	public String toString() {
		return Arrays.asList(_points).toString();
	}

	public boolean matchAny(List<Piece> pieces) {
		for( Piece p: pieces){
			if( match(p) ){
				return true;
			}
		}
		
		return false;
	}

	public boolean match(Piece piece){
		boolean[][] m = Piece.fromRegion(this);
		if( !Piece.exists(m) ){
			return false;
		}
		Piece region = Piece.create(m);
		for( Piece p: piece.rotateUnique() ){
			if( region.equals(p) ){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		Region r = (Region) obj;
		if( r._nPoints != _nPoints ){
			return false;
		}
		for( int i = 0 ; i < r._nPoints ; i++ ){
			Point p = r._points[i];
			if( !contains(p) ){
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(Region o) {
		return size() - o.size();
	}

	public static Region fromStrings(String[] s, char c) {
		List<Region> regions = new ArrayList<Region>();
		for( int y = 0 ; y < s.length ; y++ ){
			for( int x = 0 ; x < s[y].length() ;x++ ){
				if( s[y].charAt(x) == c ){
					regions.add( new Region( Point.create(x, y)) );
				}
			}
		}
		
		List<Region> list = merge(regions);
		if( list.size() != 1 ){
			throw new IllegalStateException( "" + list.size() );
		}
		return list.get(0);
	}

	public int id() {
		return _id;
	}
}
