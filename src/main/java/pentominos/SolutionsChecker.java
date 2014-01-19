package pentominos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SolutionsChecker {

	
	public static void main(String[] args) throws IOException {
		String file = "soluciones.5.dat";
		if( args.length > 0){
			file = args[0];
		}
		
		List<Board> boards = readFullFile( new File(file),6,true );
		checkSolutions(boards);
	}
		
	public static void main_dir(String[] args) throws IOException{
		String dir = "soluciones";
		if( args.length > 0){
			dir = args[0];
		}
		
		List<Board> boards = readDir( dir,6,true );
		
		for(Board b: boards){
			System.out.println( "" );
			b.dump();
		}
		
		checkSolutions( boards );
	}
	
	public static void checkSolutions(List<Board> boards ){
		System.out.println( "Soluciones: " + boards.size() );
		
		List<ArrayList<Board>> groups = groupEqual( boards );
		for( ArrayList<Board> group : groups ){
			System.out.println( "********************************");
			for( Board b: group ){
				b.dump();
				System.out.println();
			}
		}
		System.out.println( "Grupos: " + groups.size() );
	}

	private static List<Board> readDir(String dir,int lines, boolean headerPresent) throws IOException {
		List<Board> ret = new ArrayList<Board>();
		
		File f = new File(dir);
		File files[] = f.listFiles( new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".dat");
			}
		});
		
		for( File file: files ){
			Board b = readSingleFile(file,lines,headerPresent);
			ret.add(b);
		}
		
		return ret;
	}

	private static Board readSingleFile(File file,int lines, boolean headerPresent) throws IOException {
		InputStream is = file.toURI().toURL().openStream();
		BufferedReader br = new BufferedReader( new InputStreamReader(is) );
		Board ret = readOneSolution(br,lines, headerPresent);
		br.close();
		return ret;
	}

	private static List<Board> readFullFile(File file,int lines, boolean headerPresent) throws IOException{
		InputStream is = file.toURI().toURL().openStream();
		ArrayList<Board> ret = new ArrayList<Board>();
		BufferedReader br = new BufferedReader( new InputStreamReader(is) );
		Board b = readOneSolution(br,lines,headerPresent);
		while( b != null ){
			b.dump();
			ret.add(b);
			b = readOneSolution(br,lines,headerPresent);
		}
		br.close();
		return ret;
		
	}
	
	private static Board readOneSolution(BufferedReader br, int lines, boolean headerPresent) throws IOException {
		if( headerPresent ){
			String header = br.readLine();
			if( header == null ) return null;
		}
		String line = br.readLine();
		if( line == null ) return null;
		List<String> strings = new ArrayList<String>();
		int i = 0;
		while( line != null && i < lines ){
			i++;
			line = line.trim();
			if( line.length() > 0 ){
				strings.add(line);
			}
			if( i < lines ){
				line = br.readLine();
			}
		}
		System.out.println( strings );
		return Board.fromStrings(strings.toArray(new String[0]));
	}
	
	private static List<ArrayList<Board>> groupEqual( List<Board> boards ){
		List<ArrayList<Board>> ret = new ArrayList<ArrayList<Board>>();
		for( Board board: boards ){
			if( !addToGroup( ret, board ) ){
				ArrayList<Board> newGroup = new ArrayList<Board>();
				newGroup.add(board);
				ret.add( newGroup );
			}
		}
		
		return ret;
	}
	
	private static boolean addToGroup( List<ArrayList<Board>> groups, Board board ){
		for(Board b : board.rotate() ){
			for( ArrayList<Board> group: groups ){
				for( Board groupElement: group ){
					if( b.equals(groupElement) ){
						group.add(board);
						return true;
					}
				}
			}
		}
		return false;
	}
}
