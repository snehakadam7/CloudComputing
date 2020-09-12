import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MySort {
	public static double start_time, end_time, read_start_time, read_end_time;
	public static double write_start_time, write_end_time, merge_start_time, merge_end_time;
	static long chunk_rows; // No of rows
	static long read_write_chunk_rows; // No of rows
	static int input_chunk_files = 0, sort_chunk_files = 0;
	static File input_file, output_file;
	static long start = 0, end = 0;
	static long	start_write = 0, end_write = 0;
	static int esortIterationCount = 0;
	static boolean isChunkWrite = false;

	public static void main(String[] args) throws IOException {
		int sortThreadCount = Integer.parseInt(args[0]);
		int ioThreadCount = Integer.parseInt(args[1]);
		String inputFileName = args[2];
		String outputFileName = args[3];
		double inputChunkSize = 0.0;
		if (args.length == 5) {
			inputChunkSize = Double.parseDouble(args[4]);
		}

		input_file = getInputFile(inputFileName);
		if (!input_file.exists()) {
			System.err.println("File not found");
			System.exit(1);
		}

		output_file = createOutputFile(outputFileName);

		System.out.println("Setting Memory Limit : " + Runtime.getRuntime().maxMemory());

		/**
		 * This method prepares important arguments needed for Internal/External sort
		 * execution
		 */
		getChunkSize(sortThreadCount, inputChunkSize, ioThreadCount);
		
		double read_time = 0;
		double merge_time = 0;
		start_time = System.currentTimeMillis();

		List<List<String>> dividedDataList = new ArrayList<List<String>>(sort_chunk_files);
		List<List<String>> sortedDataList = new ArrayList<List<String>>(sort_chunk_files);
		List<String> inputDataList = new ArrayList<String>((int) (input_file.length()/100));
		
		if (Runtime.getRuntime().maxMemory() > input_file.length()) {
			/**	
			 * Call In-Memory sorting methods as we have more memory available	
			 */
			System.out.println("Performing In-Memory Sort");
			read_start_time = System.currentTimeMillis();
			// Call Multi-threaded Read operation
			inputDataList = multiThreadedRead(ioThreadCount);
			read_end_time = System.currentTimeMillis();
			read_time = read_end_time - read_start_time; // in milliseconds
			System.out.println("Time Taken to read file = " + read_time / 1000 + " sec");
			
			merge_start_time = System.currentTimeMillis();
			// Divide the input data to multiple chunks based on number of rows in a chunk
			dividedDataList = divideFileToChunks(inputDataList, chunk_rows);
			inputDataList.clear();

			// Call Multi-threaded Sort operation
			sortedDataList = multiThreadedSort(dividedDataList, sortThreadCount);
			dividedDataList.clear();

			System.gc();
			
			// Merge the individual sorted data chunks from memory and write final sorted
			// data in file
			internalMerge(sortedDataList);
			merge_end_time = System.currentTimeMillis();
			merge_time = merge_end_time - merge_start_time; // in milliseconds
			System.out.println("Time Taken to perform merge sort = " + merge_time / 1000 + " sec");

		} else {
			/**
			 * Call External sorting methods as we have more less memory available
			 */
			System.out.println("Performing External Sort");
			
			int filenameCounter = 0; // Counter for sorted chunk files written on disk
			
			for (int i = 0; i < esortIterationCount; i++) { 
				read_start_time = System.currentTimeMillis();
				
				// Call Multi-threaded Read operation
				inputDataList = multiThreadedRead(ioThreadCount);
				
				read_end_time = System.currentTimeMillis();
				read_time = read_time + read_end_time - read_start_time; // in milliseconds
				
				merge_start_time = System.currentTimeMillis();
				// Divide the input data to multiple chunks based on number of rows in a chunk
				dividedDataList = divideFileToChunks(inputDataList, chunk_rows);

				// Call Multi-threaded Sort operation
				sortedDataList = multiThreadedSort(dividedDataList, sortThreadCount);

				// Write sorted data chunks in different files using multi-threading
				for (List<String> sortedData : sortedDataList) {
					String filename = "sorted_input_chunk" + (filenameCounter) + ".txt";
					File sorted_Chunk_File = new File(filename);
					isChunkWrite = true;
					multiThreadedWrite(sorted_Chunk_File, sortedData, ioThreadCount);
					isChunkWrite = false;
					filenameCounter++;
					start_write = 0;
					end_write = 0;
				}
				
				System.out.println("Divided file count: "+filenameCounter);
				merge_end_time = System.currentTimeMillis();
				merge_time = merge_time + merge_end_time - merge_start_time; // in milliseconds
			}

			// Read the lines from sorted data chunk files
			// Merge and write final sorted data in outputfile
			externalMerge(filenameCounter);
		}
		
		end_time = System.currentTimeMillis();
		
		System.out.println("Time Taken to read file = " + read_time / 1000 + " sec");
		System.out.println("Time Taken to perform merge sort = " + merge_time / 1000 + " sec");
		// calculate the total time taken
		double time = end_time - start_time; // in milliseconds
		System.out.println("Total Time = " + time / 1000 + " sec");

	}

	/**
	 * This method gets the input file from the path provided in arguments
	 */
	public static File getInputFile(String inputFileName) {
		File file = new File(inputFileName);
		return file;
	}

	/**
	 * This method creates the output file in the path provided
	 */
	public static File createOutputFile(String outputFileName) {
		File file = new File(outputFileName);
		return file;
	}

	/**
	 * This methods calculates important values needed to proceed with Internal or
	 * External sort
	 * 
	 */
	public static void getChunkSize(int sortThreadCount, double inputChunkSize, int ioThreadCount) {
		if (Runtime.getRuntime().maxMemory() > input_file.length()) {
			// Internal
			chunk_rows = input_file.length() / (sortThreadCount * 100);
			read_write_chunk_rows = input_file.length() / (ioThreadCount * 100);

			if ((input_file.length() % (ioThreadCount * 100)) == 0) {
				input_chunk_files = ioThreadCount;
			} else {
				input_chunk_files = ioThreadCount + 1;
			}
			
			if ((input_file.length() % (sortThreadCount * 100)) == 0) {
				sort_chunk_files = sortThreadCount;
			} else {
				sort_chunk_files = sortThreadCount + 1;
			}

			System.out.println("Number of Threads for Sorting : " + sortThreadCount);
			System.out.println("Number of Threads for I/O : " + ioThreadCount);
		} else {
			// External
			chunk_rows = (long) (inputChunkSize * 1e7 / sortThreadCount);
			long chunk_rows_io = (long) (inputChunkSize * 1e7);
			read_write_chunk_rows = chunk_rows_io / ioThreadCount;

			if ((chunk_rows_io % ioThreadCount) == 0) {
				input_chunk_files = ioThreadCount;
			} else {
				input_chunk_files = ioThreadCount + 1;
			}

			if ((input_file.length() % (inputChunkSize * 1e9)) == 0) {
				esortIterationCount = (int) (input_file.length() / (inputChunkSize * 1e9));
			} else {
				esortIterationCount = (int) ((input_file.length() / (inputChunkSize * 1e9)) + 1);
			}

			System.out.println("Number of Threads for Sorting :  " + sortThreadCount);
			System.out.println("Number of Threads for I/O : " + ioThreadCount);
			System.out.println("External Sort IterationCount : " + esortIterationCount);
		}
	}
	
	/**
	 * 
	 * This method divides the input data into multiple chunks so that they can be
	 * sorted by different threads
	 */
	public static List<List<String>> divideFileToChunks(List<String> inputDataList, long chunk_rows)
			throws IOException {
		double internal_start_time = System.currentTimeMillis();
		List<List<String>> dividedDataList = new ArrayList<List<String>>(sort_chunk_files);

		Iterator<String> it = inputDataList.iterator();

		long count = 0;
		long line_num = 0;
		String line = "";
		while (it.hasNext()) {
			List<String> chunkDataList = new ArrayList<String>((int) chunk_rows);
			count = line_num;
			line = "";
			line_num = count;

			long total_lines = count + chunk_rows;
			while (line_num < total_lines) {
				if (!it.hasNext())
					break;
				line = it.next();
				chunkDataList.add(line);
				line_num++;
			}

			if (line == null) {
				if (chunkDataList.isEmpty())
					break;
				else
					line_num = line_num + chunk_rows;
			}
			dividedDataList.add(chunkDataList);
		}
		
		double internal_end_time = System.currentTimeMillis();
		double internal_time = internal_end_time-internal_start_time;
		System.out.println("Time Taken to divide into chunks = " + internal_time / 1000 + " sec");
		return dividedDataList;
	}

	/**
	 * 
	 * This method Spawn Threads based on ioThreadCount to Read the input data file
	 */
	public static List<String> multiThreadedRead(int ioThreadCount) {
		System.out.println("Spawning [" + ioThreadCount + "] Threads for read operation");

		List<Future<List<String>>> futuresInputDataList = new ArrayList<>(input_chunk_files);
		ExecutorService read_executor = Executors.newFixedThreadPool(ioThreadCount);

		for (int i = 0; i < input_chunk_files; i++) {

			end = (start + (read_write_chunk_rows * 100));

			Callable<List<String>> worker = new ReadInputFile(input_file, start, end);
			Future<List<String>> result = read_executor.submit(worker);
			futuresInputDataList.add(result);
			start = end;
		}

		read_executor.shutdown();
		try {
			read_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Completed all Read threads");

		// Gather output from individual threads and save it to
		// List<String> inputDataList for further use
		List<String> inputDataList = new ArrayList<String>(input_chunk_files);
		List<String> inputData = new ArrayList<String>(); 
		for (Future<List<String>> future : futuresInputDataList) {
			try {
				inputData = future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (String row : inputData) {
				inputDataList.add(row);
			}
			inputData.clear();
		}

		futuresInputDataList.clear();
		return inputDataList;
	}

	/**
	 * This method spawn threads based on sortThreadCount to Sort the individual
	 * chunks of data
	 * 
	 */
	public static List<List<String>> multiThreadedSort(List<List<String>> dividedDataList, int sortThreadCount) {
		System.out.println("Spawning [" + sortThreadCount + "] Threads for mergesort operation");
		
		List<Future<List<String>>> futuresList = new ArrayList<>(dividedDataList.size());
		ExecutorService executor = Executors.newFixedThreadPool(sortThreadCount);

		for (int i = 0; i < dividedDataList.size(); i++) {
			ArrayList<String> chunkDataList = (ArrayList<String>) dividedDataList.get(i);
			Callable<List<String>> worker = new MergeSort(chunkDataList);
			Future<List<String>> result = executor.submit(worker);
			futuresList.add(result);
		}

		executor.shutdown();

		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Completed all Merge Sort threads");

		// Gather output of individual threads and Store it in SortedDataList to merge
		// later
		List<List<String>> sortedDataList = new ArrayList<List<String>>(sort_chunk_files);
		
		for (Future<List<String>> future : futuresList) {
			List<String> sortResult = new ArrayList<String>();
			try {
				sortResult = future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			sortedDataList.add(sortResult);
		}

		return sortedDataList;
	}

	/**
	 * This method spawn threads based on ioThreadCount to Write the data to file
	 */
	public static void multiThreadedWrite(File file, List<String> mergeSortedData, int ioThreadCount) {
		System.out.println("Spawning [" + ioThreadCount + "] Threads for write operation");
		
		ExecutorService write_executor = Executors.newFixedThreadPool(ioThreadCount);

		for (int i = 0; i < input_chunk_files; i++) {
			if (isChunkWrite) {
				long read_write_io = chunk_rows / ioThreadCount;
				end_write = (start_write + (read_write_io * 100));
			} else {
				end_write = (start_write + (read_write_chunk_rows * 100));
			}

			System.out.println("start_write "+ start_write);
			System.out.println("end_write " + end_write);

			if (mergeSortedData.size() < (end_write / 100)) {
				end_write = mergeSortedData.size() * 100;
			}

			List<String> outputData = mergeSortedData.subList((int) start_write / 100, (int) end_write / 100);
			Callable<String> worker = new WriteOutputFile(file, outputData, start_write, end_write);
			Future<String> result = write_executor.submit(worker);
			start_write = end_write;
		}

		write_executor.shutdown();

		try {
			write_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Completed all Write threads");
	}

	/**
	 * This method reads Sorted data list chunks from memory, merge and write the
	 * final sorted data to output file using minHeap data structure.
	 */
	public static void internalMerge(List<List<String>> sortedDataList) {
		double internal_start_time = System.currentTimeMillis();
		MySort m = new MySort();
		int initialHeapSize = sortedDataList.size();
		String[][] sortedDataArray = new String[sortedDataList.size()][];
		
		for (int i = 0; i < sortedDataList.size(); i++) {
		    List<String> row = sortedDataList.get(i);
		    sortedDataArray[i] = row.toArray(new String[row.size()]);
		}
		
		HeapNode[] heapArr = new HeapNode[initialHeapSize]; 
		int resultSize = 0; 
		for(int i = 0; i < sortedDataArray.length; i++) { 
			HeapNode node = m.new HeapNode(sortedDataArray[i][0],i,1); 
			heapArr[i] = node; 
			resultSize += sortedDataArray[i].length; 
		} 

		// Create a initial Min heap with total nodes equal to [initialHeapSize]. 
		//Every heap node has first element of an sorted data
		MinimumHeap mh = m.new MinimumHeap(heapArr, initialHeapSize); 

		try {
			FileWriter fw = new FileWriter(output_file);
			BufferedWriter bw = new BufferedWriter(fw);
	
			// This loop finds the minimum from Min-Heap and write it to and
			// replace it with next element from the Array
			for(int i = 0; i < resultSize; i++) {
				
				HeapNode root = mh.getMinNode(); 
	
				bw.write(root.element);
				
				// Find the new root element from same sorted data array 
				if(root.j < sortedDataArray[root.i].length) {
					root.element = sortedDataArray[root.i][root.j++];
				} else {
					root.element = "~~~~~~~~~~"; 
				}
				
				// Replace root with next element of array 
				mh.replaceNode(root); 
			} 
			
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double internal_end_time = System.currentTimeMillis();
		double internal_time = internal_end_time - internal_start_time;
		System.out.println("Time Taken to perform Internal Merge = " + internal_time / 1000 + " sec");
	}

	/**
	 * This method reads data from individual sorted chunk files, sort them while data is
	 * in memory, and write it to output file to free up the memory for next set of
	 * data using minHeap data structure.
	 */	
	public static void externalMerge(int filenameCounter) {
		double external_start_time = System.currentTimeMillis();
		MySort m = new MySort();
		int initialHeapSize = filenameCounter;
		long totalLines = input_file.length() / 100;
		System.out.println("Total lines = " + totalLines);
		
		int i = 0;
		String temp;
		
		try {
			BufferedReader[] brs = new BufferedReader[filenameCounter];
			// This loop gets the first row of each sorted file using Buffered Reader
			HeapNode[] heapArr = new HeapNode[initialHeapSize]; 
			for(i = 0; i < filenameCounter; i++) { 
				brs[i] = new BufferedReader(new FileReader("sorted_input_chunk" + i + ".txt"));
				temp = brs[i].readLine();
				if (temp != null) {
					HeapNode node = m.new HeapNode(temp,i,1);
					heapArr[i] = node; 
				}
			} 
	
			// Create a initial Min heap with total nodes equal to [initialHeapSize]. 
			// Every heap node has first element of an sorted data
			MinimumHeap mh = m.new MinimumHeap(heapArr, initialHeapSize); 

		
			FileWriter fw = new FileWriter(output_file);
			BufferedWriter bw = new BufferedWriter(fw);
	
			// This loop finds the minimum from Min-Heap and write it to output file and
			// replace it with next element from the Array
			for(i = 0; i < totalLines; i++) {
				
				HeapNode root = mh.getMinNode(); 
	
				bw.write(root.element + " \n");
				
				//Find the new root element from same sorted data file 
				temp = brs[root.i].readLine();
				
				if (temp != null) {
					root.element = temp;
				} else {
					root.element = "~~~~~~~~~~";
				}
				
				// Replace root with next element of array 
				mh.replaceNode(root); 
			} 
			
			for (i = 0; i < filenameCounter; i++) {
				brs[i].close();
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double external_end_time = System.currentTimeMillis();
		double external_time = external_end_time - external_start_time;
		System.out.println("Time Taken to perform External Merge = " + external_time / 1000 + " sec");
	}
	
	/**
	 * This class holds the Heap node data structure which is part of Minimum Heap.
	 */
	class HeapNode 
	{ 
		String element; // String row to be stored 
		 
		// which the element is taken 
		int i;  // Current index of the element 
		int j; 	// index of the next element  

		public HeapNode(String element, int i, int j) 
		{ 
			this.element = element; 
			this.i = i; 
			this.j = j; 
		} 
	}; 

	/**
	 * This class holds the Minimum Heap data structure to support Merge operation.
	 */
	class MinimumHeap { 
		HeapNode[] harr; 
		int heap_size;  

		/**
		 * This constructor creates Minimum heap from array of nodes and total size 
		 */
		public MinimumHeap(HeapNode a[], int size) { 
			heap_size = size; 
			harr = a; 
			int i = (heap_size - 1)/2; 
			while (i >= 0) { 
				createMinimumHeap(i); 
				i--; 
			} 
		} 

		/**
		 * This method creates Min-Heap data structure recursively 
		 * to store the data to be merge sorted 
		 */
		void createMinimumHeap(int i) { 
			int left = left(i); 
			int right = right(i); 
			int smallest = i; 
			if (left < heap_size && harr[left].element.compareTo(harr[i].element) < 0) 
				smallest = left; 
			if (right < heap_size && harr[right].element.compareTo(harr[smallest].element) < 0)
				smallest = right; 
			if (smallest != i) 
			{ 
				swapNode(harr, i, smallest); 
				createMinimumHeap(smallest); 
			} 
		} 

		/**
		 * This method returns the index of left child of node [i]
		 */
		int left(int i) { 
			return (2*i + 1); 
		} 

		/**
		 * This method returns the index of right child of node [i]
		 */
		int right(int i) {
			return (2*i + 2);
		} 

		/**
		 * This method returns the root node which is minimun in this Heap
		 */
		HeapNode getMinNode() { 
			if(heap_size <= 0) {
				//No more nodes present inside this part of heap
				return null; 
			}
			
			return harr[0]; 
		} 

		/**
		 * This method replaces the root element with new element provided
		 */
		void replaceNode(HeapNode root) { 
			harr[0] = root; 
			createMinimumHeap(0); 
		} 

		/**
		 * This method is used to swap two nodes which is needed during restructuring of Heap
		 */
		void swapNode(HeapNode[] arr, int i, int j) { 
			HeapNode temp = arr[i]; 
			arr[i] = arr[j]; 
			arr[j] = temp; 
		}  
	};	
}
