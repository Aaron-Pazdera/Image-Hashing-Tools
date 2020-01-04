package utils;

public class PixelUtils {
	
	public static int checkOverflow(int a, int b) throws IllegalArgumentException {
		if (b == 0) {
			return 0;
		}

		int product = a * b;
		if (a == product / b) {
			return product;
		} else {
			throw new IllegalArgumentException("Width and height of new GreyscaleImage would overflow int.");
		}
	}
	
	public static int[] byteArrayToInt(byte[] arr) {
		int[] intArr = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = arr[i] & 0xff;
		}
		return intArr;
	}
	
	public static byte[] intArrayToByte(int[] arr) {
		byte[] intArr = new byte[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (byte) arr[i];
		}
		return intArr;
	}
	
	public static int[][] array1dToArray2d(int[] arr, int x, int y) {
		int[][] array2d = new int[y][x];
		int index = 0;
		for (int i = 0; i < array2d.length; i++) {
			for (int j = 0; j < array2d[i].length; j++) {
				array2d[i][j] = arr[index];
				index++;
			}
		}
		return array2d;
	}

	public static byte[][] array1dToArray2d(byte[] arr, int x, int y) {
		byte[][] array2d = new byte[y][x];
		int index = 0;
		for (int i = 0; i < array2d.length; i++) {
			for (int j = 0; j < array2d[i].length; j++) {
				array2d[i][j] = arr[index];
				index++;
			}
		}
		return array2d;
	}

	public static int[] array2dToArray1d(int[][] arr, int x, int y) {
		// arr[x][y]
		int[] array1d = new int[x * y];
		for (int i = 0; i < x; i++) {
			int[] currentArray = arr[i];
			if (currentArray.length != y) {
				throw new IllegalArgumentException("All subarrays of pixels must be of length equal to the width.");
			}

			for (int j = 0; j < y; j++) {
				array1d[i * y + j] = currentArray[j];
			}
		}
		return array1d;
	}

	public static byte[] array2dToArray1d(byte[][] arr, int x, int y) {
		// arr[x][y]
		byte[] array1d = new byte[x * y];
		for (int i = 0; i < x; i++) {
			byte[] currentArray = arr[i];
			if (currentArray.length != y) {
				throw new IllegalArgumentException("All subarrays of pixels must be of length equal to the width.");
			}

			for (int j = 0; j < y; j++) {
				array1d[i * y + j] = currentArray[j];
			}
		}
		return array1d;
	}

	public static int[][] transpose(int[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;

		int[][] transposedMatrix = new int[n][m];
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < m; y++) {
				transposedMatrix[x][y] = matrix[y][x];
			}
		}
		return transposedMatrix;
	}

	public static byte[][] transpose(byte[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;

		byte[][] transposedMatrix = new byte[n][m];
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < m; y++) {
				transposedMatrix[x][y] = matrix[y][x];
			}
		}
		return transposedMatrix;
	}

	public static int[] transpose1dAs2d(int[] arr, int oldWidth, int oldHeight) {
		if (arr.length != oldWidth * oldHeight) {
			throw new IllegalArgumentException();
		}

		int[] transposed = new int[arr.length];
		for (int offset = 0; offset < arr.length; offset++) {
			int x = offset % oldWidth;
			int y = offset / oldWidth;
			// Use x as y, and vice-versa
			transposed[x * oldWidth + y] = arr[offset];
		}

		return transposed;
	}

	public static byte[] transpose1dAs2d(byte[] arr, int oldWidth, int oldHeight) {
		if (arr.length != oldWidth * oldHeight) {
			throw new IllegalArgumentException();
		}

		byte[] transposed = new byte[arr.length];
		for (int offset = 0; offset < arr.length; offset++) {
			int x = offset % oldWidth;
			int y = offset / oldWidth;
			// Use x as y, and vice-versa
			transposed[x * oldWidth + y] = arr[offset];
		}

		return transposed;
	}

}
