package pentominos;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BoardGenerator {


	private static boolean USEGENERATEONEBOARD = false;
	private static boolean SHOWPROGRESS = false;
	private int _w;
	private int _h;
	private List<Board> _fullBoards;
	private int _size;

	public BoardGenerator( int w, int h, int size ){
		_size = size;
		_w = w;
		_h = h;
		_fullBoards = new ArrayList<Board>();
		PieceGenerator.generate(_size);
	}
	
	public void generateAllBoards(){
		_fullBoards.clear();
		Piece.clearCache();
		PieceGenerator.generate(_size);
		List<Piece> uniques = Piece.uniques();
		Board board = new Board(_w, _h);
		
		generateAllBoards( uniques, board, 0 );
	}
	
	
	private void generateAllBoards(List<Piece> uniques, Board board, int i) {

		if( board.full() ){
			addFullBoard( board );
			return;
		}
		
		if( i >= uniques.size() ){
			return;
		}
		
		List<Piece> availablePieces = uniques.subList(i, uniques.size());
		if( !canFillRegions( board, availablePieces ) ){
			return;
		}

		if( i == 3 && SHOWPROGRESS){
			System.out.println( );
			System.out.println( "generate: pieces:" + Piece.cacheSize() );
			System.out.println( "  i:" + i );
			board.dump();
		}

		
		Piece piece = uniques.get(i);
		for( int x = 0 ; x < board.w() ; x++ ){
			for( int y = 0 ; y < board.h() ; y++ ){
				for( Piece p: piece.rotateUnique() ){
					
					// PONGO LA PIEZA EN EL TABLERO, SI PUEDO
					Board b = board.put(p, x, y);
					if( b == null ){
						continue;
					}
					
					// SIGO PONIENDO LA SIGUIENTE PIEZA
					generateAllBoards( uniques, b, i+1 );
				}
			}
		}
	}

	private boolean canFillSizeRegions(Board b, int pieceSize){
		List<Region> emptyRegions = b.emptyRegions();
		for( Region r: emptyRegions ){
			
			// SI LA REGION LIBRE NO ES MULTIPLO DEL TAMAÑO DE PIEZA, NO SE PUEDE LLENAR
			if( r.size()% pieceSize != 0 ){
				return false;
			}
		}
		return true;
	}
	
	private boolean canFillRegions(Board originalBoard, List<Piece> availablePieces) {
		if( availablePieces.size() == 0 ){
			return false;
		}
		
		if( !canFillSizeRegions(originalBoard,_size) ){
			return false;
		}
		
		List<Region> emptyRegions = originalBoard.emptyRegions();
		for( Region r: emptyRegions ){
			// MIRO SI LAS REGIONES DEL TAMAÑO DE UNA PIEZA TIENEN UNA PIEZA DISPONIBLE
			if( r.size() == _size ){
				if( !r.matchAny(availablePieces) ){
					return false;
				}
			}
		}

		if( USEGENERATEONEBOARD && emptyRegions.size() > 1 ){
			// SI SOLO HAY UNA REGION, YA SE LLENA IGUAL DE DEPRISA CON EL PROCEDIMIENTO
			// NORMAL, SOLO LO HAGO SI HAY MAS DE UNA REGION VACIA
			List<Region> bigRegions = new ArrayList<Region>();
			for( Region r: emptyRegions ){
				if( r.size() > _size && r.size() <= _size*2 ){
					bigRegions.add(r);
				}
			}

			for( Region r: bigRegions ){
				// MIRO SI PUEDO LLENAR LAS REGIONES DE MAS PIEZAS
				Board b = Board.fromEmptyRegion(r);
				Board generated = generateOneBoard( availablePieces, b );
				if( generated == null ){
					System.out.print("+");
					return false;
				}
			}
		}
		
		return true;
	}
	
	public Board generateOneBoard(List<Piece> pieces, Board board ){
		
		if( board.full() ){
			return board;
		}
		
		if( pieces.isEmpty() ){
			return null;
		}

		List<Piece> restOfPieces = new ArrayList<Piece>(pieces);
		for( Piece piece: pieces ){
			restOfPieces.remove(piece);
			for( Piece p: piece.rotateUnique() ){
				for( int x = 0 ; x < board.w() ; x++ ){
					for( int y = 0 ; y < board.h() ; y++ ){
						
						// PONGO LA PIEZA EN EL TABLERO, SI PUEDO
						Board b = board.put(p, x, y);
						if( b == null ){
							continue;
						}
						
						// SIGO PONIENDO LA SIGUIENTE PIEZA
						Board ret = generateOneBoard( restOfPieces, b );
						if( ret != null ){
							return ret;
						}
					}
				}
			}
			restOfPieces.add(piece);
		}
		
		return null;
	}

	private void addFullBoard(Board board) {
		_fullBoards.add( board );
		System.out.println( "Solucion " + _fullBoards.size() );
		board.dump();
		System.out.println();
		
		if( true ){
			try {
				int index = _fullBoards.size();
				FileOutputStream fos = new FileOutputStream("Tablero" + index + ".dat");
				PrintStream w = new PrintStream(fos);
				w.println( "Solucion " + _fullBoards.size() );
				board.dump(w);
				w.println();
				w.flush();
				w.close();
				fos.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public List<Board> fullBoards(){
		return Collections.unmodifiableList(_fullBoards);
	}
	
	private static List<Piece> uniquesBut( List<Piece> pieces ){
		List<Piece> uniques = Piece.uniques();
		List<Piece> ret = new ArrayList<Piece>(uniques);
		for( Piece piece: pieces ){
			for( Piece p: piece.rotateUnique() ){
				ret.remove(p);
			}
		}
		return ret;
	}
	
	public static void test(){
		String[] s = new String[]{
				"AAA       ",
				"BA   FF   ",
				"BA    F D ",
				"BBBC EFFDD",
				" CCCCEE  D",
				"    EE   D",
		};
		BoardGenerator bg = new BoardGenerator(10, 6, 5 );
		PieceGenerator.generate(5);
		Board b = Board.fromStrings(s);
		List<Piece> pieces = uniquesBut( b.pieces() );
		bg.generateAllBoards( pieces, b, 0 );
		
		for( Board bo : bg.fullBoards() ){
			System.out.println( "Solucion:");
			bo.dump();
		}
		System.exit(0);
		
	}
	
	public static void main(String[] args) {
		
//		Board.REGIONS1 = args.length != 0;
//		System.out.println( "REGIONS1:" + Board.REGIONS1 ); 

		System.out.println( "Pentominos, por alvarogonzalezsotillo@gmail.com" );
		
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		BoardGenerator bg = new BoardGenerator(10, 6, 5);
		bg.generateAllBoards();
		
		for( int i = 0 ; i < bg.fullBoards().size() ; i++){
			Board b = bg.fullBoards().get(i);
			System.out.println( "Solucion " + i + ":" );
			b.dump();
		}
	}
}
