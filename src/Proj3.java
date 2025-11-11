/****************************************************
 * @file: Proj3.java
 * @description: Implements Bubble Sort, Merge Sort, Quick Sort, Heap Sort, and Odd-Even Transposition Sort. Reads movies from CSV, sorts by rating (ascending),
 *  times performance for sorted, shuffled, and reversed data, and writes results to analysis.txt and sorted.txt.
 * @acknowledgment: Portions of this code and documentation were developed with assistance from ChatGPT by OpenAI.
 * @author: Tim Hultman
 * @date: November 13, 2025
 ****************************************************/

import java.io.*;
import java.util.*;

public class Proj3 {

    /**
     * Entry point for Project 3.
     * Accepts command-line arguments: dataset file, sorting algorithm, and number of lines (N).
     * Loads movies, prepares sorted/shuffled/reversed datasets, and runs sorting trials.
     *
     * @param args command-line arguments: {dataset-file} {algorithm} {number-of-lines}
     * @throws IOException if file reading or writing fails
     * Return void
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java Proj3 {dataset-file} {algorithm} {number-of-lines}");
            return;
        }

        String filename = args[0];
        String algorithm = args[1].toLowerCase();
        int N = Integer.parseInt(args[2]);

        ArrayList<Movie> movies = Parser.readMovies(filename, N);
        if (movies.isEmpty()) {
            System.err.println("No movies loaded!");
            return;
        }

        System.out.println("Loaded " + movies.size() + " movies.");

        // Prepare test cases
        ArrayList<Movie> sorted = new ArrayList<>(movies);
        Collections.sort(sorted);
        ArrayList<Movie> shuffled = new ArrayList<>(movies);
        Collections.shuffle(shuffled);
        ArrayList<Movie> reversed = new ArrayList<>(movies);
        Collections.sort(reversed, Collections.reverseOrder());

        // Run tests for sorted, shuffled, reversed
        runCase(algorithm, sorted, "sorted");
        runCase(algorithm, shuffled, "shuffled");
        runCase(algorithm, reversed, "reversed");
    }

    /**
     * Runs a sorting test case for one dataset order (sorted/shuffled/reversed)
     * using the specified algorithm, measuring execution time and comparisons.
     *
     * @param alg   the algorithm name ("merge", "quick", "heap", "bubble", "transposition")
     * @param data  the dataset to sort
     * @param order label describing data order for output ("sorted", "shuffled", "reversed")
     * @throws IOException if writing output files fails
     */
    private static void runCase(String alg, ArrayList<Movie> data, String order) throws IOException {
        ArrayList<Movie> copy = new ArrayList<>(data);
        long start = System.nanoTime();
        Counter counter = new Counter();

        switch (alg) {
            case "merge" -> mergeSort(copy, 0, copy.size() - 1, counter);
            case "quick" -> quickSort(copy, 0, copy.size() - 1, counter);
            case "heap" -> heapSort(copy, 0, copy.size() - 1, counter);
            case "bubble" -> bubbleSort(copy, copy.size(), counter);
            case "transposition" -> transpositionSort(copy, copy.size(), counter);
            default -> { System.err.println("Invalid algorithm."); return; }
        }

        long end = System.nanoTime();
        double timeSec = (end - start) / 1_000_000_000.0;

        System.out.printf("%-12s %-10s N=%-5d  Time: %.6fs  Comparisons: %d%n",
                alg, order, copy.size(), timeSec, counter.count);

        appendAnalysis(alg, order, copy.size(), timeSec, counter.count);
        writeSorted(copy, alg, order);
    }

    /**
     * Appends a single line of test results to analysis.txt in CSV format.
     *
     * @param alg   sorting algorithm name
     * @param order dataset order label
     * @param n     number of elements sorted
     * @param time  execution time in seconds
     * @param comps number of comparisons performed
     * @throws IOException if writing to file fails
     */
    private static void appendAnalysis(String alg, String order, int n, double time, int comps) throws IOException {
        try (FileWriter fw = new FileWriter("analysis.txt", true)) {
            fw.write(String.format(Locale.US, "%s,%s,%d,%.6f,%d%n", alg, order, n, time, comps));
        }
    }
    /**
     * Writes the sorted movie list to sorted.txt for verification.
     *
     * @param list  sorted list of movies
     * @param alg   sorting algorithm name
     * @param order dataset order label
     * @throws IOException if writing to file fails
     */
    private static void writeSorted(ArrayList<Movie> list, String alg, String order) throws IOException {
        try (FileWriter fw = new FileWriter("sorted.txt", true)) {
            fw.write(String.format("=== %s (%s) ===%n", alg.toUpperCase(), order));
            for (Movie m : list) fw.write(m + "\n");
            fw.write("\n");
        }
    }

    /**
     * Simple wrapper class for counting comparisons.
     * Used to pass mutable comparison count between recursive calls.
     */
    private static class Counter {
        int count = 0;
    }

    /**
     * Performs Merge Sort on a sublist of the given ArrayList.
     *
     * @param a     the list to sort
     * @param left  starting index
     * @param right ending index
     * @param c     counter for comparison tracking
     */
    public static <T extends Comparable> void mergeSort(ArrayList<T> a, int left, int right, Counter c) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(a, left, mid, c);
            mergeSort(a, mid + 1, right, c);
            merge(a, left, mid, right, c);
        }
    }
    /**
     * Merges two sorted halves of an ArrayList into one sorted list.
     *
     * @param a     the list being merged
     * @param left  leftmost index
     * @param mid   midpoint index
     * @param right rightmost index
     * @param c     counter for comparison tracking
     */
    public static <T extends Comparable> void merge(ArrayList<T> a, int left, int mid, int right, Counter c) {
        ArrayList<T> temp = new ArrayList<>();
        int i = left, j = mid + 1;
        while (i <= mid && j <= right) {
            c.count++;
            if (a.get(i).compareTo(a.get(j)) <= 0)
                temp.add(a.get(i++));
            else
                temp.add(a.get(j++));
        }
        while (i <= mid) temp.add(a.get(i++));
        while (j <= right) temp.add(a.get(j++));
        for (int k = 0; k < temp.size(); k++)
            a.set(left + k, temp.get(k));
    }

    /**
     * Performs Quick Sort on a sublist of the given ArrayList.
     *
     * @param a     the list to sort
     * @param left  starting index
     * @param right ending index
     * @param c     counter for comparison tracking
     */
    public static <T extends Comparable> void quickSort(ArrayList<T> a, int left, int right, Counter c) {
        if (left < right) {
            int p = partition(a, left, right, c);
            quickSort(a, left, p - 1, c);
            quickSort(a, p + 1, right, c);
        }
    }
    /**
     * Partitions the ArrayList for Quick Sort, returning the pivot index.
     *
     * @param a     list being partitioned
     * @param left  starting index
     * @param right ending index
     * @param c     counter for comparison tracking
     * @return index of pivot after partition
     */
    public static <T extends Comparable> int partition(ArrayList<T> a, int left, int right, Counter c) {
        T pivot = a.get(right);
        int i = left - 1;
        for (int j = left; j < right; j++) {
            c.count++;
            if (a.get(j).compareTo(pivot) <= 0) {
                i++;
                swap(a, i, j);
            }
        }
        swap(a, i + 1, right);
        return i + 1;
    }

    /**
     * Performs Heap Sort on the given ArrayList.
     *
     * @param a     the list to sort
     * @param left  leftmost index
     * @param right rightmost index
     * @param c     counter for comparison tracking
     */
    public static <T extends Comparable> void heapSort(ArrayList<T> a, int left, int right, Counter c) {
        int n = right - left + 1;
        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(a, n, i, c);
        for (int i = n - 1; i > 0; i--) {
            swap(a, 0, i);
            heapify(a, i, 0, c);
        }
    }
    /**
     * Maintains the heap property for a subtree rooted at index i.
     *
     * @param a the list representing the heap
     * @param n number of elements in the heap
     * @param i current index being heapified
     * @param c counter for comparison tracking
     */
    public static <T extends Comparable> void heapify(ArrayList<T> a, int n, int i, Counter c) {
        int largest = i;
        int l = 2 * i + 1, r = 2 * i + 2;
        if (l < n) { c.count++; if (a.get(l).compareTo(a.get(largest)) > 0) largest = l; }
        if (r < n) { c.count++; if (a.get(r).compareTo(a.get(largest)) > 0) largest = r; }
        if (largest != i) {
            swap(a, i, largest);
            heapify(a, n, largest, c);
        }
    }

    /**
     * Performs Bubble Sort on the given ArrayList.
     *
     * @param a    the list to sort
     * @param size number of elements in the list
     * @param c    counter for comparison tracking
     */
    public static <T extends Comparable> void bubbleSort(ArrayList<T> a, int size, Counter c) {
        boolean swapped;
        for (int i = 0; i < size - 1; i++) {
            swapped = false;
            for (int j = 0; j < size - i - 1; j++) {
                c.count++;
                if (a.get(j).compareTo(a.get(j + 1)) > 0) {
                    swap(a, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    /**
     * Performs Odd-Even Transposition Sort (parallelizable bubble variant).
     *
     * @param a    the list to sort
     * @param size number of elements in the list
     * @param c    counter for comparison tracking
     */
    public static <T extends Comparable> void transpositionSort(ArrayList<T> a, int size, Counter c) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            // Odd phase
            for (int i = 1; i < size - 1; i += 2) {
                c.count++;
                if (a.get(i).compareTo(a.get(i + 1)) > 0) {
                    swap(a, i, i + 1);
                    sorted = false;
                }
            }
            // Even phase
            for (int i = 0; i < size - 1; i += 2) {
                c.count++;
                if (a.get(i).compareTo(a.get(i + 1)) > 0) {
                    swap(a, i, i + 1);
                    sorted = false;
                }
            }
        }
    }


    /**
     * Swaps two elements in an ArrayList.
     *
     * @param a the list in which elements will be swapped
     * @param i index of the first element
     * @param j index of the second element
     * @param <T> the type of list elements
     */
    static <T> void swap(ArrayList<T> a, int i, int j) {
        T temp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, temp);
    }
}
