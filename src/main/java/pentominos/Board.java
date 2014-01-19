package pentominos;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {

	class PiecePosition{
		PiecePosition( Piece pi, Point po ){
			this.pi = pi;
			this.po = po;
		}
		Piece pi;
		Point po;
		
		PiecePosition rotate( Direction d ){
			Point o = Point.create(0,0).rotate( d, pi.w(), pi.h() );
			Point p = po.rotate(d, w(), h());
			p = Point.create( p.x() - o.x(), p.y() - o.y() );
			return new PiecePosition( pi.rotate(d), p );
		}
		
		public void dump( PrintStream ps ){
			ps.println( "Position:" + po );
			pi.dump(ps);
		}
		
		@Override
		public boolean equals(Object obj) {
			PiecePosition pp = (PiecePosition) obj;
			return pp.pi.equals(pi) && pp.po.equals(po);
		}
	}

	
	private List<PiecePosition> _pieces = new ArrayList<PiecePosition>();
	
	private Piece[][] _board;

	private List<Region> _emptyRegions;
	
	public Board( int w, int h ){
		_board = new Piece[w][h];
	}
	
	public static Board fromStrings(String s[]){
		int h = s.length;
		int w = s[0].length();
		Board b = new Board(w,h);
		
		Set<Character> chars = new HashSet<Character>();
		for( int x = 0 ; x < w ; x++ ){
			for( int y = 0 ; y < h ; y++ ){
				char c = s[y].charAt(x);
				if( Character.isLetterOrDigit(c) ){
					chars.add( c );
				}
			}
		}
		
		for( char c: chars ){
			Region r = Region.fromStrings(s,c);
			Piece p = Piece.create(Piece.fromRegion(r));
			b = b.put( p, r.minx(), r.miny() );
		}
		
		return b;
		
	}
	
	public Board put( Piece pi, int x, int y ){
		
		if( overlaps( pi, x, y ) ){
			return null;
		}
		
		Board ret = copy();
		
		ret.addToBoard( pi, x, y );
		
		return ret;
	}

	private Board copy() {
		Board ret = new Board(w(), h());
		for( int x = 0 ; x < w() ; x++ ){
			for( int y = 0 ; y < h() ; y++ ){
				ret._board[x][y] = _board[x][y];
			}
		}
		
		for( PiecePosition pp: _pieces ){
			ret._pieces.add( ret.new PiecePosition(pp.pi,pp.po) );
		}
		
		return ret;
	}
	
	public void dump(){
		try{
			dump(System.out);
		}
		catch( IOException e ){
			e.printStackTrace();
		}
	}
	
	public void dump(PrintStream w) throws IOException {
		for( int y = 0 ; y < h() ; y++ ){
			for( int x = 0 ; x < w() ; x++ ){
				Piece p = _board[x][y];
				char c =  (char) (p == null ? '.' : 'A' + p.uniqueIndex());
				w.print( c );
			}
			w.println();
		}
	}
	

	private void addToBoard(Piece pi, int x0 , int y0 ) {
		
		if( pi.w() + x0 > w() ){
			throw new IllegalArgumentException();
		}
		if( pi.h() + y0 > h() ){
			throw new IllegalArgumentException();
		}
		
		_pieces.add( new PiecePosition( pi, Point.create(x0, y0) ) );
		
		for( int x = 0 ; x < pi.w() ; x++ ){
			for( int y = 0 ; y < pi.h() ; y++ ){
				if( pi.contains(x, y) ){
					_board[x+x0][y+y0] = pi;
				}
			}
		}
	}
	
	public List<Region> emptyRegions(){
		if( _emptyRegions != null ){
			return _emptyRegions;
		}
		
		List<Region> ret = regions(true);
		return _emptyRegions = ret;
	}

	
	private List<Region> regions(boolean empty ){
		Region regions[][] = new Region[w()][h()];
		ArrayList<Region> ret = new ArrayList<Region>();
		
		for( int x = 0 ; x < w() ; x++ ){
			for( int y = 0 ; y < h() ; y++ ){
				if( (_board[x][y] != null ) == empty ){
					continue;
				}
				Point p = Point.create(x,y);

				Region rUp = y > 0 ? regions[x][y-1] : null;
				Region rDown = y < h()-1 ? regions[x][y+1] : null;
				Region rLeft = x < w()-1 ? regions[x+1][y] : null;
				Region rRight = x > 0 ? regions[x-1][y] : null;
				
				
				Region region = null;
				Region regs[] = { rUp, rDown, rLeft, rRight };
				for( Region r: regs ){
					
					// SI NO HAY REGION EN ESE LADO, SE SIGUE
					if( r == null ){
						continue;
					}
					
					// SI AUN NO HAY REGION PARA EL PUNTO, PERTENECE A ESE LADO
					if( region == null ){
						r.addToRegion( p, false );
						region = r;
						continue;
					}
					
					// SI LA REGION EN ESE LADO NO ES LA DEL PUNTO, SE MEZCLAN Y SE QUITA LA DE ESE LADO
					if( r != region ){
						for( Point point: r.points() ){
							regions[point.x()][point.y()] = region;
							region.addToRegion(point,false);
						}
						ret.remove(r);
					}
				}
				
				if( region == null ){
					region = new Region(p);
					ret.add(region);
				}
				regions[x][y] = region;
				
				if( false ){
					System.out.println("===============================");
					for( int y2 = 0 ; y2 < h() ; y2++ ){
						for( int x2 = 0 ; x2 < w() ; x2++ ){
							char c = regions[x2][y2] == null ? '.' : (char)('A' + regions[x2][y2].id());
							System.out.print(c);
						}
						System.out.println();
					}
					System.out.println();
				}
			}
		}
		
		return ret;
	}
	
	public List<Region> fullRegions(){
		List<Region> ret = regions(false);
		return ret;
	}
	

	public int h() {
		return _board[0].length;
	}

	public int w() {
		return _board.length;
	}

	
	
	private boolean overlaps(Piece pi, int x0, int y0) {
		if( pi.w() + x0 > w() ){
			return true;
		}
		if( pi.h() + y0 > h() ){
			return true;
		}
		
		for( int x = 0 ; x < pi.w() ; x++ ){
			for( int y = 0 ; y < pi.h() ; y++ ){
				if( pi.contains(x, y) ){
					if( _board[x+x0][y+y0] != null ){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean full(){
		for( int x = 0 ; x < w() ; x++ ){
			for( int y = 0 ; y < h() ; y++ ){
				if( _board[x][y] == null ){
					return false;
				}
			}
		}
		return true;
	}
	
	public static Board fromEmptyRegion( Region r ){
		Board b = new Board( r.w(), r.h() );
		for( int x = 0 ; x < b.w() ; x++ ){
			for( int y = 0 ; y < b.h() ; y++ ){
				b._board[x][y] = Piece.NULLPIECE;
			}
		}
		
		for( Point p: r.points() ){
			int x = p.x() - r.minx();
			int y = p.y() - r.miny();
			b._board[x][y] = null;
		}
		
		return b;
		
	}
	
	@Override
	public boolean equals(Object obj) {
		Board b = (Board) obj;
		if( w() != b.w() ){
			return false;
		}
		if( h() != b.h() ){
			return false;
		}
		if( b._pieces.size() != _pieces.size() ){
			return false;
		}
		for( PiecePosition pp : b._pieces ){
			if( !_pieces.contains(pp) ){
				return false;
			}
		}
		return true;
	}
	
	public List<Board> rotate(){
		return Arrays.asList( new Board[]{
			rotate(Direction.up),
			rotate(Direction.down),
			rotate(Direction.flipH),
			rotate(Direction.flipV),
			rotate(Direction.left),
			rotate(Direction.right),
		});
	}
	
	public Board rotate( Direction d ){

		int w = (d == Direction.left || d == Direction.right ) ? h() : w();
		int h = (d == Direction.left || d == Direction.right ) ? w() : h();

		Board b = new Board(w,h);

		for (int x = 0; x < w(); x++) {
			for (int y = 0; y < h(); y++) {
				Point p = Point.create(x, y).rotate(d, w(), h() );
				int x2 = p.x();
				int y2 = p.y();
				b._board[x2][y2] = _board[x][y];
			}
		}
		
		for( PiecePosition pp: _pieces ){
			b._pieces.add( pp.rotate(d) );
		}
		
		return b;
	}

	public static void test(){
		PieceGenerator.generate(5);
		String s[] = new String[]{
			"IIIIIJJGGG",
			"BBBFJJEGGC",
			"BFFFJEEECC",
			"BFDDKKLEAC",
			"DDDHKLLLAC",
			"HHHHKKLAAA",
		};
		Board.fromStrings(s).dump();
	}

	public static void main(String[] args) {
		test();
	}

	public List<Piece> pieces(){
		List<Piece> ret = new ArrayList<Piece>();
		for(PiecePosition pp: _pieces){
			ret.add(pp.pi);
		}
		return ret;
	}
	
	private static void test2() {
		System.out.println( Point.create(3,3).rotate(Direction.right, 10, 6 ) );
		
		PieceGenerator.generate(4);
		
		List<Piece> pieces = Piece.cache();
		
		pieces.get(0).dumpAll();
		
		Board b = new Board(10,6);
		System.out.println( "********************");
		b.dump();
		
		b = b.put( pieces.get(0), 0, 0 );
		System.out.println( "********************");
		b.dump();

		b = b.put( pieces.get(10), 3, 3 );
		System.out.println( "********************");
		b.dump();
		
		b = b.rotate( Direction.right );
		System.out.println( "********************");
		b.dump();
	}

}
