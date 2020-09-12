import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MergeSort implements Callable<List<String>> {
	ArrayList<String> chunk_dataList;
	List<String> sorted_datalist;

	public MergeSort(ArrayList<String> chunkDataList) {
		chunk_dataList = chunkDataList;
	}

	@Override
	public List<String> call() throws Exception {
		try {
			sorted_datalist = merge_Sort(chunk_dataList, 0, chunk_dataList.size() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sorted_datalist;
	}

	/**
	 * This method recursively performs merge sort by dividing Arraylist into smaller parts
	 */
	public static ArrayList<String> merge_Sort(ArrayList<String> chunk_dataList, long start, long end)
			throws Exception {
		ArrayList<String> left = null;
		ArrayList<String> right = null;

		long total_len = start + end;
		long mid = total_len / 2;
		if (end < start) {
			ArrayList<String> newList = new ArrayList<String>();
			return newList;
		}
		if (start == end) {
			ArrayList<String> one_element;
			one_element = new ArrayList<String>();
			String element = chunk_dataList.get((int) start);
			one_element.add(element);
			return one_element;
		}

		left = merge_Sort(chunk_dataList, start, mid);
		right = merge_Sort(chunk_dataList, mid + 1, end);
		return merge_chunk_list(left, right);
	}

	public static ArrayList<String> merge_chunk_list(ArrayList<String> left_list, ArrayList<String> right_list)
			throws Exception {
		ArrayList<String> sorted_chunk_list = new ArrayList<String>();
		int left_position = 0, right_position = 0;

		long position = 0;
		long totallen = left_list.size() + right_list.size();

		while (position < totallen) {
			if (right_position >= right_list.size()) {
				String ele = left_list.get(left_position);
				sorted_chunk_list.add(ele);
				left_position++;
			} else if (left_position >= left_list.size()) {
				String ele = right_list.get(right_position);
				sorted_chunk_list.add(ele);
				right_position++;
			} else if (compareTwoListVal(left_list.get(left_position), right_list.get(right_position))) {
				String ele = left_list.get(left_position);
				sorted_chunk_list.add(ele);
				left_position++;
			} else {
				String ele = right_list.get(right_position);
				sorted_chunk_list.add(ele);
				right_position++;
			}
			position++;
		}
		return sorted_chunk_list;
	}

	public static boolean compareTwoListVal(String l, String r) {
		if (l.compareTo(r) <= 0)
			return true;
		else
			return false;
	}
}