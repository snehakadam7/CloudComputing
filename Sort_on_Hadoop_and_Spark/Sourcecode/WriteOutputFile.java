import java.io.File;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class WriteOutputFile implements Callable<String> {
	File file;
	long start;
	long end;
	String sortType;
	List<String> mergeSortedData;
	String output;

	public WriteOutputFile(File file1, List<String> mergeSortedData1, long start1, long end1) {
		file = file1;
		mergeSortedData = mergeSortedData1;
		start = start1;
		end = end1;
	}

	@Override
	public String call() throws Exception {
		try {
			output = writeFile(file,mergeSortedData, start, end);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Completed";
	}
	
	/**
	 * This method writes small chunks of data to the file in multi-threaded way
	 */
	public String writeFile(File file, List<String> mergeSortedData, long start, long end) {
		System.out.println("Started Write Thread [" + Thread.currentThread().getId() +"]");
		try {
			RandomAccessFile raf = new RandomAccessFile(file,"rw");
			raf.seek(start);
			
			Iterator<String> it = mergeSortedData.listIterator();
			while (raf.getFilePointer() < end) {
				while(it.hasNext()) {
					raf.writeBytes(it.next());
				}
			}
			
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Completed Write Thread [" + Thread.currentThread().getId() +"]");
		return "Completed";
	}

}