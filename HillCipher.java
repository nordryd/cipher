package hillCipher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class HillCipher
{
	private final int[][] key;
	private final int matrixSize;
	private final int mod;
	private final char[] alphabet;
	private String cipher;
	private final Random padder;
	
	public HillCipher(int[][] key)
	{
		this.alphabet = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
		this.mod = alphabet.length;
		this.key = new int[key[0].length][key.length];
		this.matrixSize = this.key.length;
		this.padder = new Random();
		
		for(int i = 0; i < this.key[0].length; i++)
		{
			for(int j = 0; j < this.key.length; j++)
			{
				this.key[i][j] = key[i][j];
			}
		}
	}
	
	private int getIndexOf(char c)
	{
		int i = 0;
		boolean found = false;
		while(!found && i < (mod - 1))
		{
			if(alphabet[i] == c)
			{
				found = true;
			}
			i++;
		}
		
		return i - 1;
	}
	
	private String removeSpaces(String string)
	{
		String str = "";
		
		for(int i = 0; i < string.length(); i++)
        {
        	if(string.charAt(i) != ' ')
        	{
        		str += string.charAt(i);
        	}
        }
        
        return str;
	}
	
	private int[] matrixMult(int[] x, int[][] y)
	{
		if(y[0].length != x.length)
		{
			throw new IllegalArgumentException("x:Rows " + y[0].length + " does not match y:Columns " + x.length + ".");
		}
		//x = pair
		//y = key
		int[] matrix = new int[x.length];
		
		for(int i = 0; i < matrix.length; i++)
		{
			matrix[i] = 0;
		}
		
		int indexX = 0;
		int index = 0;
		for(int[] i : y)
		{
			for(int j : i)
			{
				matrix[index] += j * x[indexX];
				indexX++;
			}
			indexX = 0;
			index++;
		}
		
		return matrix;
	}
	
	/**
	 * C++ version retrieved from http://www.geeksforgeeks.org/multiplicative-inverse-under-modulo-m/ on 9/5/2017
	 * @param n
	 * @param modulus
	 * @return multiplicative inverse of n
	 */
	private int modInverse(int n, int m)
	{
		int result = -1;
		int a = n % m;
		
		int i = 1;
		while(result < 0 && i < m)
		{
			if((a * i) % m == 1)
			{
				result = i;
			}
			i++;
		}
		return result;
	}
	
	private int[][] matrixInvert(int[][] matrix, int m)
	{	
		int[][] inverted = new int[matrix.length][matrix.length];
		if(this.matrixSize == 2)	//2x2 key
		{
			//a=0,0 b=0,1
			//c=1,0 d=1,1
			int invDeterminant = modInverse((matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]), m);

			inverted[0][0] = (matrix[1][1] * invDeterminant) % m;
			inverted[0][1] = ((m - matrix[0][1]) * invDeterminant) % m;
			inverted[1][0] = ((m - matrix[1][0]) * invDeterminant) % m;
			inverted[1][1] = (matrix[0][0] * invDeterminant) % m;
		}
		else						//3x3 key
		{
			//a=0,0 b=0,1 c=0,2
			//d=1,0 e=1,1 f=1,2
			//g=2,0 h=2,1 i=2,2
			
			int invDeterminant = 0;
			for(int i = 0; i < 3; i++)
			{
				invDeterminant += matrix[0][i] * (matrix[1][(i + 1) % 3] * matrix[2][(i + 2) % 3] - matrix[1][(i + 2) % 3] * matrix[2][(i + 1) % 3]);
			}
			if(invDeterminant < 0)
			{
				while(invDeterminant < 0)
				{
					invDeterminant += m;
				}
			}
			invDeterminant = modInverse(invDeterminant, m);
			
			//just hard code the Matrix of Minors/CoFactors/Adjugate (since it's ONLY for 3x3 matrix)
			inverted[0][0] = ((matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) * invDeterminant) % m;
			inverted[1][0] = (-(matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0]) * invDeterminant) % m;//
			inverted[2][0] = ((matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]) * invDeterminant) % m;//
			inverted[0][1] = (-(matrix[0][1] * matrix[2][2] - matrix[0][2] * matrix[2][1])* invDeterminant) % m;//
			inverted[1][1] = ((matrix[0][0] * matrix[2][2] - matrix[0][2] * matrix[2][0]) * invDeterminant) % m;
			inverted[2][1] = (-(matrix[0][0] * matrix[2][1] - matrix[0][1] * matrix[2][0]) * invDeterminant) % m;////
			inverted[0][2] = ((matrix[0][1] * matrix[1][2] - matrix[0][2] * matrix[1][1]) * invDeterminant) % m;//
			inverted[1][2] = (-(matrix[0][0] * matrix[1][2] - matrix[1][0] * matrix[0][2]) * invDeterminant) % m;////
			inverted[2][2] = ((matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]) * invDeterminant) % m;
			
			for(int i = 0; i < inverted.length; i++)
			{
				for(int j = 0; j < inverted[0].length; j++)
				{
					if(inverted[i][j] < 0)
					{
						inverted[i][j] += m;
					}
				}
			}
		}
		
		return inverted;
	}
	
	public String encrypt(String plaintext)
	{
		String ciphertext = "";		
		String plainSansSpaces = removeSpaces(plaintext.toLowerCase());
		
		//padding
		if((plainSansSpaces.length() % matrixSize) != 0)
		{
			while((plainSansSpaces.length() % matrixSize) != 0)
			{
				plainSansSpaces += alphabet[padder.nextInt(mod)];
			}
		}
		
		int[] enumeratedPlaintext = new int[plainSansSpaces.length()];
		for(int i = 0; i < enumeratedPlaintext.length; i++)
		{
			enumeratedPlaintext[i] = getIndexOf(plainSansSpaces.charAt(i));
		}
		
		ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
		
		int iterationCap = enumeratedPlaintext.length - (matrixSize - 1);
		boolean keyIs3x3 = (matrixSize == 3);
		for(int i = 0; i < iterationCap; i += matrixSize)
		{
			ArrayList<Integer> x = new ArrayList<Integer>();
			x.add(enumeratedPlaintext[i]);
			x.add(enumeratedPlaintext[i + 1]);
			if(keyIs3x3)
			{
				x.add(enumeratedPlaintext[i + 2]);
			}
			
			groups.add(x);
		}
		
		for(ArrayList<Integer> g : groups)
		{
			int[] currGroup = new int[matrixSize];
			for(int i = 0; i < currGroup.length; i++)
			{
				currGroup[i] = g.get(i);
			}
			
			currGroup = matrixMult(currGroup, this.key);
			
			for(int i : currGroup)
			{
				ciphertext += alphabet[i % mod];
			}
		}
		
		return ciphertext;
	}
	
	public String decrypt(String ciphertext)
	{
		String plaintext = "";
		String cipherSansSpaces = removeSpaces(ciphertext.toLowerCase());
		
		int[] enumeratedCiphertext = new int[cipherSansSpaces.length()];
		for(int i = 0; i < enumeratedCiphertext.length; i++)
		{
			enumeratedCiphertext[i] = getIndexOf(cipherSansSpaces.charAt(i));
		}
		
		ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
		
		int iterationCap = enumeratedCiphertext.length - (matrixSize - 1);
		boolean keyIs3x3 = (matrixSize == 3);
		for(int i = 0; i < iterationCap; i += matrixSize)
		{
			ArrayList<Integer> x = new ArrayList<Integer>();
			x.add(enumeratedCiphertext[i]);
			x.add(enumeratedCiphertext[i + 1]);
			if(keyIs3x3)
			{
				x.add(enumeratedCiphertext[i + 2]);
			}
			
			groups.add(x);
		}
		
		int[][] keyInverse = matrixInvert(this.key, mod);
		
		for(ArrayList<Integer> g : groups)
		{
			int[] currGroup = new int[matrixSize];
			for(int i = 0; i < currGroup.length; i++)
			{
				currGroup[i] = g.get(i);
			}
			
			currGroup = matrixMult(currGroup, keyInverse);
			
			for(int i : currGroup)
			{
				plaintext += alphabet[i % mod];
			}
		}
		
		return plaintext;
	}
	
	public int test()
	{
		return modInverse(9, 29);
	}
	
	public String getCipher()
	{
		return this.cipher;
	}
	
	public String toString()
	{
		String str = "";
		
		for(int[] i : this.key)
		{
			for(int j : i)
			{
				str += j + " ";
			}
			str += "\n";
		}
		
		return str;
	}
}