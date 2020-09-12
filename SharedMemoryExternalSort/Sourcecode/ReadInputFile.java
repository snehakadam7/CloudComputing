import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ReadInputFile implements Callable<List<String>> {
	File file;
	long start;
	long end;
	List<String> chunkDataList;
	
	public ReadInputFile(File file1, long start1, long end1) {
      file = file1;
      start = start1;
      end = end1;
   }
	
	@Override
	public List<String> call() throws Exception {
		try {
			chunkDataList = readLargeFile(file, start, end);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return chunkDataList;
	}
	
	/**
	 * This method reads the Large file by splitting it into smaller chunks in multi-threaded way  
	 */
	public ArrayList<String> readLargeFile(File file, long start, long end) {
		System.out.println("Started Read Thread [" + Thread.currentThread().getId() +"]");
		ArrayList<String> chunk_dataList = new ArrayList<String>();
		try
		{
			String line ="";
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			
			raf.seek(start);

	        while (raf.getFilePointer() < end) {
	            line = raf.readLine();
	            if (line == null) 
	                break; //continue;
	            chunk_dataList.add(line);
	        }
			raf.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return chunk_dataList;
	}		
}
