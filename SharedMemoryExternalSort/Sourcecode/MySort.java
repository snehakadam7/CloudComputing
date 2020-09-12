import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MySort {
	public static double start_time, end_time, read_start_time, read_end_time;
	public static double write_start_time, write_end_time, merge_start_time, merge_end_time;
	static long chunk_rows, chunk_rows_io; // No of rows
	static long read_write_chunk_rows; // No of rows
	static long file_size; // File size in bytes
	static long memoryLimit; // Max memory allocated to process in bytes
	static int input_chunk_files = 0;
	static File input_file, output_file;
	static long start = 0, end = 0, start_write = 0, end_write = 0;
	static int esortIterationCount = 0;
	static boolean isChunkWrite = false;
	static int sortThreadCount = 0;
	static int ioThreadCount = 0;
	static ArrayList<String> chunk_dataList = new ArrayList<String>();
	static String chunk_file = "sorted_input_chunk";
	static int filenameCounter = 0;

	public static void main(String[] args) throws IOException {
		//Code to read Command line arguments for MySort class with optional last
		// argument only used for External sorting
		
		sortThreadCount = Integer.parseInt(args[0]); // Thread count for Sort process
		ioThreadCount = Integer.parseInt(args[1]); // Thread count for I/O Operations
		String inputFileName = args[2];
		String outputFileName = args[3];
		double inputChunkSize = 0.0;
		if (args.length == 5) {
			inputChunkSize = Double.parseDouble(args[4]); // Chunk size in GB to be read at time for External sort
		}

		List<List<String>> dividedDataList = new ArrayList<List<String>>();
		List<List<String>> sortedDataList = null;
		List<String> inputDataList = null;

		input_file = getInputFile(inputFileName);
		if (!input_file.exists()) {
			System.err.println("File not found");
			System.exit(1);
		}

		output_file = createOutputFile(outputFileName);

		// Get size of input file and JVM memory limit provided
		file_size = input_file.length();
		memoryLimit = Runtime.getRuntime().maxMemory();
		System.out.println("Setting Memory Limit : " + memoryLimit);

		/**
		 * This method prepares important arguments needed for Internal/External sort
		 * execution
		 */
		getChunkSize(sortThreadCount, inputChunkSize, ioThreadCount);

		double read_time = 0;
		double merge_time = 0;
		start_time = System.currentTimeMillis();

		if (memoryLimit > file_size) {
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
			internal_sort(sortedDataList);
			merge_end_time = System.currentTimeMillis();
			merge_time = merge_end_time - merge_start_time; // in milliseconds
			System.out.println("Time Taken to perform merge sort = " + merge_time / 1000 + " sec");

		} else {
			/**
			 * Call External sorting methods as we have more less memory available
			 */
			System.out.println("Performing External Sort");

			filenameCounter = 0; // Counter for sorted chunk files written on disk

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
					String filename = chunk_file + (filenameCounter) + ".txt";
					File sorted_Chunk_File = new File(filename);
					isChunkWrite = true;
					multiThreadedWrite(sorted_Chunk_File, sortedData, ioThreadCount);
					isChunkWrite = false;
					filenameCounter++;
					start_write = 0;
					end_write = 0;
				}

				System.out.println("Divided file count: " + filenameCounter);
				merge_end_time = System.currentTimeMillis();
				merge_time = merge_time + merge_end_time - merge_start_time; // in milliseconds
			}

			// Read the lines from sorted data chunk files
			// Merge and write final sorted data in outputfile
			external_sort();
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
	// Get input file to be sorted
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
		if (memoryLimit > file_size) {
			// Internal Sort
			chunk_rows = file_size / (sortThreadCount * 100);
			read_write_chunk_rows = file_size / (ioThreadCount * 100);

			if ((file_size % (ioThreadCount * 100)) == 0) {
				input_chunk_files = ioThreadCount;
			} else {
				input_chunk_files = ioThreadCount + 1;
			}

			System.out.println("Number of Threads for Sorting : " + sortThreadCount);
			System.out.println("Number of Threads for I/O : " + ioThreadCount);
		} else {
			// External Sort
			chunk_rows = (long) (inputChunkSize * 1e7 / sortThreadCount);
			chunk_rows_io = (long) (inputChunkSize * 1e7);
			read_write_chunk_rows = chunk_rows_io / ioThreadCount;

			if ((chunk_rows_io % ioThreadCount) == 0) {
				input_chunk_files = ioThreadCount;
			} else {
				input_chunk_files = ioThreadCount + 1;
			}

			if ((file_size % (inputChunkSize * 1e9)) == 0) {
				esortIterationCount = (int) (file_size / (inputChunkSize * 1e9));
			} else {
				esortIterationCount = (int) ((file_size / (inputChunkSize * 1e9)) + 1);
			}

			System.out.println("sortThreadCount " + sortThreadCount);
			System.out.println("Chunk rows " + chunk_rows);
			System.out.println("ioThreadCount " + ioThreadCount);
			System.out.println("chunk_rows_io " + chunk_rows_io);
			System.out.println("read_write_chunk_rows " + read_write_chunk_rows);
			System.out.println("input_chunk_files " + input_chunk_files);
			System.out.println("esortIterationCount " + esortIterationCount);
		}
	}

	/**
	 * This method reads Sorted data list chunks from memory, merge and write the
	 * final sorted data to output file.
	 */
	public static void internal_sort(List<List<String>> sortedDataList) {
		double internal_start_time = System.currentTimeMillis();
		HashMap<Integer, List<String>> sortedDataMap = new HashMap<>();
		long totalLines = file_size / 100;

		try {
			int i, j;
			i = j = 0;
			String temp;
			int minFile;

			String[] topNums = new String[sortedDataList.size()];
			List<String> sortedData = new ArrayList<String>();
			Iterator<String> it = null;

			// This loop gets the first row of each sorted array
			for (i = 0; i < sortedDataList.size(); i++) {
				sortedData = sortedDataList.get(i);
				sortedDataMap.put(i, sortedData);
				temp = sortedData.get(0);

				if (temp != null) {
					topNums[i] = (temp);
				}
			}

			FileWriter fw = new FileWriter(output_file);

			// This loop finds the minimum from first row and write it to file until all
			// rows are processed
			for (i = 0; i < totalLines; i++) {
				String min = topNums[0];
				minFile = 0;

				for (j = 0; j < sortedDataList.size(); j++) {
					if (min.compareTo(topNums[j]) > 0) {
						min = topNums[j];
						minFile = j;
					}
				}

				fw.write(min + " \n");

				it = sortedDataMap.get(minFile).iterator();

				if (it.hasNext()) {
					it.next();
					it.remove();
				}

				try {
					temp = it.next();
					topNums[minFile] = (temp);
				} catch (NoSuchElementException e) {
					topNums[minFile] = "~~~~~~~~~~";
				}
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double internal_end_time = System.currentTimeMillis();
		double internal_time = internal_end_time - internal_start_time;
		System.out.println("Time Taken to Complete Merge process = " + internal_time / 1000 + " sec");
	}

	/**
	 * 
	 * This method divides the input data into multiple chunks so that they can be
	 * sorted by different threads
	 */
	public static List<List<String>> divideFileToChunks(List<String> inputDataList, long chunk_rows) {
		double internal_start_time = System.currentTimeMillis();
		List<List<String>> dividedDataList = new ArrayList<List<String>>();

		Iterator<String> it = inputDataList.iterator();

		long count = 0;
		long line_num = 0;

		while (it.hasNext()) {
			List<String> chunkDataList = new ArrayList<String>();
			count = line_num;
			String line = "";
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
		double internal_time = internal_end_time - internal_start_time;
		System.out.println("Time Taken to divide into chunks = " + internal_time / 1000 + " sec");
		return dividedDataList;
	}

	/**
	 * 
	 * This method Spawn Threads based on ioThreadCount to Read the input data file
	 */
	public static List<String> multiThreadedRead(int ioThreadCount) {
		System.out.println("Spawning [" + ioThreadCount + "] Threads for read operation");
		//
		List<Future<List<String>>> futuresInputDataList = new ArrayList<>();
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
		List<String> inputDataList = new ArrayList<String>();
		for (Future<List<String>> future : futuresInputDataList) {
			List<String> inputData = new ArrayList<String>();
			try {
				inputData = future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (String row : inputData) {
				inputDataList.add(row);
			}
		}
		return inputDataList;
	}

	/**
	 * This method spawn threads based on sortThreadCount to Sort the individual
	 * chunks of data
	 * 
	 */
	public static List<List<String>> multiThreadedSort(List<List<String>> dividedDataList, int sortThreadCount) {
		System.out.println("Spawning [" + sortThreadCount + "] Threads for mergesort operation");
		List<Future<List<String>>> futuresList = new ArrayList<>();
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
		List<List<String>> sortedDataList = new ArrayList<List<String>>();
		for (Future<List<String>> future : futuresList) {
			List<String> sortResult = new ArrayList<String>();
			try {
				sortResult = future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			sortedDataList.add(sortResult);
		}
		System.out.println("size of sorted data list " + sortedDataList.size());

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
	 * This method reads data individual sorted chunk file, sort them while data is
	 * in memory, and write it to output file to free up the memory for next set of
	 * data
	 */
	public static void external_sort() {
		double internal_start_time = System.currentTimeMillis();
		long totalLines = file_size / 100;
		System.out.println("Total lines = " + totalLines);
		int slices = filenameCounter;
		int i = 0, j = 0;

		try {
			String[] topStrings = new String[slices];
			BufferedReader[] brs = new BufferedReader[slices];
			// This loop gets the first row of each sorted file using Buffered Reader
			for (i = 0; i < slices; i++) {
				brs[i] = new BufferedReader(new FileReader(chunk_file + i + ".txt"));
				String t = brs[i].readLine();
				if (t != null) {
					topStrings[i] = (t);
				}
			}

			FileWriter fw = new FileWriter(output_file);

			// This loop finds the minimum from first row and write it to file until all
			// rows are processed
			for (i = 0; i < totalLines; i++) {
				String min = topStrings[0];
				int minFile = 0;

				for (j = 0; j < slices; j++) {
					if (min.compareTo(topStrings[j]) > 0) {
						min = topStrings[j];
						minFile = j;
					}
				}

				fw.write(min + "\n");

				String t = brs[minFile].readLine();
				if (t != null) {
					topStrings[minFile] = (t);
				} else {
					topStrings[minFile] = "~~~~~~~~~~";
				}
			}

			for (i = 0; i < slices; i++) {
				brs[i].close();
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double internal_end_time = System.currentTimeMillis();
		double internal_time = internal_end_time - internal_start_time;
		System.out.println("Time Taken to perform External Merge= " + internal_time / 1000 + " sec");
		System.out.println("mergesort complete.");
	}
}
