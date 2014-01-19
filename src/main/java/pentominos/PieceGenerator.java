package pentominos;

import java.util.ArrayList;
import java.util.List;

/*
	Cuadros:1  Piezas:1
	Cuadros:2  Piezas:1
	Cuadros:3  Piezas:2
	Cuadros:4  Piezas:5
	Cuadros:5  Piezas:12
	Cuadros:6  Piezas:35
	Cuadros:7  Piezas:108
*/
public class PieceGenerator {

	public static void generate(int size){
		Piece.clearCache();
		boolean[][] m = Piece.init(size, size);
		generate( m, -1, -1, size );
		
		if( true ){
			System.out.println( "Piezas unicas:" + Piece.uniques().size() );
			for( Piece p: Piece.uniques() ){
				p.dump();
			}
		}
	}
	
	private static void generate( boolean[][] m, int x0, int y0, int i ){
		
		int size = m.length;
		
		if( i == 0 ){
			checkPiece(m);
			return;
		}
		
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				
				if( y<y0 || (y == y0 && x <= x0 ) ){
					// ESTO YA LO HAN CONTROLADO ANTES
					continue;
				}
				m[x][y] = true;
				generate( m, x, y, i-1 );
				m[x][y] = false;
			}
		}
	}

	private static void log(String string) {
		//System.out.println( string );
		
	}

	private static boolean checkPiece(boolean[][] m) {
		
		// TODOS LOS CUADRADOS TIENEN UN LADO EN COMUN CON OTRO CUADRADO
		int size = m.length;

		List<Point> ps = new ArrayList<Point>();

		boolean found = false;
		for (int x0 = 0; x0 < size && !found; x0++) {
			for (int y0 = 0; y0 < size && !found; y0++) {
				found = m[x0][y0];
				if( found ){
					ps.add( Point.create(x0,y0) );
				}
			}
		}
		if( !found ){
			Piece.dump(System.out, m);
			throw new IllegalStateException();
		}
		
		// VEO SI LLEGO A TODOS DESDE x0, y0
		// PASO HASTA QUE NO CREZCA MAS
		int oldSize = -1;
		List<Point> copy = new ArrayList<Point>();
		while( oldSize != ps.size() ){
			copy.clear();
			copy.addAll(ps);
			for( Point p: copy ){
				oldSize = ps.size();
				int x = p.x();
				int y = p.y();
				
				
				// ARRIBA
				if( y > 0 && m[x][y-1] ){
					Point newp = Point.create(x,y-1);
					if( !ps.contains(newp) ) ps.add( newp );
				}
				// ABAJO
				if( y < size-1 && m[x][y+1] ){
					Point newp = Point.create(x,y+1);
					if( !ps.contains(newp) ) ps.add( newp );
				}
				// IZQUIERDA
				if( x > 0 && m[x-1][y] ){
					Point newp = Point.create(x-1,y);
					if( !ps.contains(newp) ) ps.add( newp );
				}
				// DERECHA
				if( x < size-1 && m[x+1][y] ){
					Point newp = Point.create(x+1,y);
					if( !ps.contains(newp) ) ps.add( newp );
				}
			}
		}
	
		if( ps.size() != size ){
			return false;
		}
	
		Piece.create(m).rotate();
		return true;
	}
	
	public static void main(String[] args) {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		for( int i = 5 ; i <= 5 ; i++ ){
			long ini = System.currentTimeMillis();
			generate(i);
			long end = System.currentTimeMillis();
			
			List<Piece> cache = Piece.uniques();
			System.out.println( "Cuadros:" + i + "  Piezas:" + cache.size() + " (" + (end-ini) + " ms)" );
			for( Piece p: cache ){
				System.out.println( "****************************" );
				p.dumpAll();
			}
		}
	}
}
