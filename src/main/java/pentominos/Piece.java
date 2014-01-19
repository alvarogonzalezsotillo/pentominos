package pentominos;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Piece{
	
	public static Piece NULLPIECE = new Piece( new boolean[][]{{}});


	private static final Comparator<Piece> EQUALSCOMPARATOR = new Comparator<Piece>() {

		@Override
		public int compare(Piece o1, Piece o2) {
			if( o1.equals(o2) ){
				return 0;
			}
			return o1._index - o2._index;
		}
		
	};
	
	private static final Comparator<Piece> UNIQUECOMPARATOR = new Comparator<Piece>() {
		@Override
		public int compare(Piece o1, Piece o2){
			
			o2.rotate();
			
			if( !Collections.disjoint(o1.rotate(), o2.rotate() ) ){
				return 0;
			}
			
			if( !Collections.disjoint(o1.flipV().rotate(), o2.rotate() ) ){
				return 0;
			}

			if( !Collections.disjoint(o1.rotate(), o2.flipV().rotate() ) ){
				return 0;
			}
			
			return o1._index - o2._index;
		}
	};

	private static final boolean DUMPNEWPIECE = false;

	private boolean[][] _m;
	
	private static Map<Piece,Piece> _cache;
	private static List<Piece> _computedUnique;
	private static List<Piece> _computedCache;

	private int _index;
	
	public int uniqueIndex(){
		if( this == NULLPIECE ){
			return -1;
		}
		List<Piece> uniques = uniques();
		return uniques.indexOf( unique() );
	}
	
	public static boolean exists( boolean m[][] ){
		m = trim(m);
		
		if( _cache == null ){
			return false;
		}
		
		for( Piece p: _cache.keySet() ){
			if( compare( m, p._m ) ){
				return true;
			}
		}
		return false;
	}
	
	private static Piece fromCache( boolean m[][] ){
		if( _cache == null ){
			_cache = new HashMap<Piece,Piece>();
		}
		Piece p = new Piece(m);
		Piece ret = _cache.get(p);
		if( ret != null ){
			return ret;
		}
		
		p._index = _cache.size()+1;
		_cache.put(p, p);
		invalidateComputed();
		
		if( DUMPNEWPIECE ){
			System.out.println( "Nueva pieza");
			p.dump();
		}
		
		return p;
	}
	
	private static void invalidateComputed() {
		_computedUnique = null;
		_computedCache = null;
	}
	
	public static void clearCache(){
		_cache = null;
		invalidateComputed();
	}

	public static int cacheSize(){
		return _cache.size();
	}
	
	private Piece _down, _left, _right, _flipV, _flipH;
	private List<Piece> _rotate;


	private List<Piece> _rotateUnique;

	private int _size;
	
	public static List<Piece> cache(){
		if( _cache == null ){
			return null;
		}
		
		if( _computedCache != null ){
			return _computedCache;
		}
		
		return _computedCache = Collections.unmodifiableList( new ArrayList<Piece>(_cache.values()));
	}
	
	public Piece unique(){
		for( Piece p: uniques() ){
			if( UNIQUECOMPARATOR.compare(this, p) == 0 ){
				return p;
			}
		}
		throw new IllegalStateException();
	}
	
	public static List<Piece> uniques(){
		
		if( _computedUnique != null ){
			return _computedUnique;
		}
		

		List<Piece> pieces = cache();

		List<Piece> ret = uniqueList(pieces, UNIQUECOMPARATOR);
		
		return _computedUnique = Collections.unmodifiableList(new ArrayList<Piece>( ret ));
	}

	private static List<Piece> uniqueList(List<Piece> pieces, Comparator<Piece> comparator) {
		List<Piece> ret = new ArrayList<Piece>();
		for( Piece p: pieces ){
			boolean found = false;
			
			
			for( Iterator<Piece> it = ret.iterator() ; it.hasNext() && !found ; ){ 
				Piece pp = it.next();
				if( comparator.compare(p, pp) == 0 ){
					found = true;
				}
			}
			
			if( !found ){
				ret.add(p);
			}
		}
		return ret;
	}
	
	public static Piece create( String[] s, char c){
		boolean[][] m = fromStrings(s, c);
		return create(m);
	}

	public static Piece create( boolean[][] m ){
		return fromCache(m);
	}
	
	
	private Piece( boolean[][] m ){
		_m = trim(m);
		_size = 0;
		for (int x = 0; x < _m.length; x++) {
			for (int y = 0; y < _m[0].length; y++) {
				boolean b = _m[x][y];
				_size += b ? 1 : 0;
			}
		}
	}
	
	public int size(){
		return _size;
	}
	
	public static boolean[][] trim( boolean[][] m ){
		int minx = m.length;
		int miny = m[0].length;
		int maxx = 0;
		int maxy = 0;

		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				if( !m[x][y] ){
					continue;
				}
				if( x < minx ) minx = x;
				if( y < miny ) miny = y;
				if( x > maxx ) maxx = x;
				if( y > maxy ) maxy = y;
			}
		}
		
		boolean[][] ret = init( maxx-minx+1, maxy-miny+1 );
		for (int x = 0; x < ret.length; x++) {
			for (int y = 0; y < ret[0].length; y++) {
				ret[x][y] = m[minx+x][miny+y];
			}
		}
		return ret;
	}
	
	public static boolean[][] init(int w, int h) {
		boolean m[][] = new boolean[w][h];
		return m;
	}

	public int h(){
		return _m[0].length;
	}
	
	public int w(){
		return _m.length;
	}
	
	public int maxWH(){
		return w()>h()?w():h();
	}
	
	public boolean contains(int x, int y){
		return _m[x][y];
	}
	

	public List<Piece> rotate(){
		if( _rotate != null ){
			return _rotate;
		}
		Set<Piece> ret = new HashSet<Piece>();
		for( Direction d: Direction.values() ){
			ret.add( rotate(d) );
			ret.add( rotate(Direction.flipH).rotate(d) );
			
		}
		return _rotate = Collections.unmodifiableList( new ArrayList<Piece>(ret) );
	}
	
	public List<Piece> rotateUnique(){
		if( _rotateUnique != null ){
			return _rotateUnique;
		}
		return _rotateUnique = uniqueList(rotate(), EQUALSCOMPARATOR);
	}
	
	public Piece rotate( Direction upToThisDirection ){
		switch( upToThisDirection ){
			case down:
				return down();
			case flipH:
				return flipH();
			case flipV:
				return flipV();
			case left:
				return left();
			case right:
				return right();
			case up:
				return up();
			default:
				throw new UnsupportedOperationException();
		}
	}

	private Piece right() {
		if( _right != null ){
			return _right;
		}
		
		boolean[][] m = init(  h(), w() );
		
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				int x2 = y;
				int y2 = h()-x-1;
				m[x][y] = _m[x2][y2];
			}
		}
		
		return _right = create( m );
	}

	private Piece left() {
		if( _left != null ){
			return _left;
		}
		
		boolean[][] m = init(  h(), w() );
		
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				int x2 = w()-y-1;
				int y2 = x;
				m[x][y] = _m[x2][y2];
			}
		}
		
		return _left = create( m );
	}

	private Piece flipH() {
		if( _flipH != null ){
			return _flipH;
		}
		
		boolean[][] m = init( w(), h() );
		
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				int x2 = x;
				int y2 = h()-y-1;
				m[x][y] = _m[x2][y2];
			}
		}
		
		return _flipH = create( m );
	}

	private Piece flipV() {
		if( _flipV != null ){
			return _flipV;
		}
		
		boolean[][] m = init( w(), h() );
		
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				int x2 = w()-x-1;
				int y2 = y;
				m[x][y] = _m[x2][y2];
			}
		}
		
		return _flipV = create( m );
	}
	
	private Piece down() {
		if( _down != null ){
			return _down;
		}
		
		boolean[][] m = init( w(), h() );
		
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				int x2 = w()-x-1;
				int y2 = h()-y-1;
				m[x][y] = _m[x2][y2];
			}
		}
		
		return _down = create( m );
	}
	
	public void dump(){
		dump( System.out);
	}
	
	public void dump( PrintStream ps ){
		ps.println( _index + ":" + hashCode() );
		dump(ps,_m);
	}

	public static void dump(PrintStream ps, boolean[][] m) {
		for (int y = 0; y < m[0].length; y++) {
			for (int x = 0; x < m.length; x++) {
				ps.print( m[x][y] ? "#" : "." );
			}
			ps.println();
		}
	}

	public void dumpAll(){
		for( Piece p: rotateUnique() ){
			p.dump();
			System.out.println();
		}
	}
	
	private Piece up() {
		return this;
	}

	public static boolean[][] fromRegion( Region r ){
		boolean m[][] = init( r.w(), r.h() );
		for( Point p : r.points() ){
			m[p.x()-r.minx()][p.y() - r.miny()] = true;
		}
		return trim(m);
	}
	
	
	private static boolean[][] fromStrings( String[] s, char c ){
		boolean m[][] = init( s[0].length(), s.length );
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				m[x][y] = s[y].charAt(x) == c;
			}
		}
		return trim(m);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if( this == obj ){
			return true;
		}
		
		if( !(obj instanceof Piece) ){
			return false;
		}
		
		Piece p = (Piece) obj;

		return compare( _m, p._m );
	}
	
	private static boolean compare( boolean[][] a, boolean[][] b ){
		if( a.length != b.length ){
			return false;
		}
		if( a[0].length != b[0].length ){
			return false;
		}
		
		for (int x = 0; x < a.length; x++) {
			for (int y = 0; y < a[0].length; y++) {
				if( a[x][y] != b[x][y] ){
					return false;
				}
			}
		}
		return true;
	}
	
	public int spin(){
		return rotateUnique().size();
	}
	
	
	@Override
	public int hashCode() {
		int ret = 0;
		for (int x = 0; x < _m.length; x++) {
			ret <<= 1;
			for (int y = 0; y < _m[0].length; y++) {
				ret <<= 1;
				ret += _m[x][y] ? 1 : 0;
			}
		}
		return ret;
	}
	
	public static void main(String[] args) {
		String[][] ss = {
				{
					"####",
					"#   ",
				},
				{
					"###",
					"#  ",
					"#  ",
				},
				{
					"#####",
				},
				{
					"###",
					" # ",
					" # ",
				},
		};

		for( String[] s: ss){
			Piece p = create(s, '#');
			System.out.println("**************************");
			p.dumpAll();
		}
	}
}
