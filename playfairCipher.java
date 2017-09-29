package playfairCipher;

import java.util.ArrayList;

public class playfairCipher
{
	private char[] alphabet;
	private final char[] keyword;
	private final char[] key; //represented this way for faster computation
	private String cipher;
	
	public playfairCipher(String keyword)
	{
		this.alphabet = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'}; // i = 8
		this.keyword = keyword.toLowerCase().toCharArray();
		this.key = new char[25];
		this.cipher = "";
		
		int index = 0;
		boolean keywordDone = false;
		for(int i = 0; i < 25; i++)
		{
			if(!keywordDone)
			{
				if(search(this.keyword[index]))
				{
					key[i] = this.keyword[index];
				}
				else //letter has shown up already
				{
					//cancels out increment to stay where we are in the key
					i--;
				}
				index++;

				if(index >= this.keyword.length)
				{
					keywordDone = true;
				}
			}
			else
			{
				key[i] = getNextLetter();
			}
		}
	}
	
	private boolean search(char letter)
	{
		int index = 0;
		boolean found = false;
		while(!found && index < 25)
		{
			if(letter == this.alphabet[index] || (letter == 'j' && this.alphabet[index] == 'i'))
			{
				found = true;
				this.alphabet[index] = '!';
			}
			index++;
		}
		return found;
	}
	
	private char getNextLetter()
	{
		char nextLetter = ' ';
		
		int index = 0;
		while(nextLetter == ' ')
		{
			if(this.alphabet[index] != '!')
			{
				nextLetter = this.alphabet[index];
				this.alphabet[index] = '!';
			}
			index++;
		}
		
		return nextLetter;
	}
	
	private int getIndexOf(char c)
	{
		int i = 0;
		boolean found = false;
		while(!found && i < 25)
		{
			if(key[i] == c || (c == 'j' && key[i] == 'i'))
			{
				found = true;
			}
			i++;
		}
		
		return i - 1;
	}
	
	private boolean sameRow(char i, char j)
	{
		int indexi = getIndexOf(i);
		int indexj = getIndexOf(j);
		
		while(indexi > 5 && indexj > 5)
		{
			indexi -= 5;
			indexj -= 5;
		}
		
		return (indexi >= 0 && indexi <= 4) && (indexj >= 0 && indexj <= 4);
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
	
	private boolean sameCol(char i, char j)
	{
		return (getIndexOf(i) - getIndexOf(j)) % 5 == 0;
	}
	
	/**
	 * Encrypts plaintext with respect to the key.
	 * @param plaintext
	 * @return ciphertext
	 */
	public String encrypt(String plaintext)
	{
		String ciphertext = "";
		System.out.println("\n**ENCRYPTION**");
        ArrayList<Pair<Character>> pairs = new ArrayList<Pair<Character>>();
        String plainSansSpaces = removeSpaces(plaintext);
        
        for(int i = 0; i < plainSansSpaces.length() - 1; i += 2)
        {
        	pairs.add(new Pair<Character>(plainSansSpaces.charAt(i), plainSansSpaces.charAt(i + 1)));
        }
        
        if(plainSansSpaces.length() % 2 != 0)
        {
        	pairs.add(new Pair<Character>(plainSansSpaces.charAt(plainSansSpaces.length() - 1), ' '));
        }
        
        //comparing pairs' individual characters
        for(Pair<Character> pair : pairs)
        {
        	if(pair.l == pair.r || pair.r == ' ')	//end of string (odd length) or same characters in pair
        	{
        		pair.r = 'x';
        	}
        	else if(sameRow(pair.l, pair.r))		//characters are in same ROW
        	{
        		int indexl = getIndexOf(pair.l);
        		int indexr = getIndexOf(pair.r);
        		pair.l = (indexl + 1) % 5 == 0 ? key[indexl - 4] : key[indexl + 1];
        		pair.r = (indexr + 1) % 5 == 0 ? key[indexr - 4] : key[indexr + 1];
        	}
        	else if(sameCol(pair.l, pair.r))		//characters are in same COLUMN
        	{
        		int indexl = getIndexOf(pair.l);
        		int indexr = getIndexOf(pair.r);
        		pair.l = (indexl >= 20 && indexl <= 24) ? key[indexl - 20] : key[indexl + 5];
        		pair.r = (indexr >= 20 && indexr <= 24) ? key[indexr - 20] : key[indexr + 5];
        	}
        	else									//characters in DIFFERENT ROW & COLUMN
        	{
        		int indexl = getIndexOf(pair.l);
        		int indexr = getIndexOf(pair.r);
        		int coll = indexl % 5;
        		int colr = indexr % 5;
        		
        		int colDistance = Math.abs(coll - colr);
        		
        		pair.l = (coll > colr) ? key[indexl - colDistance] : key[indexl + colDistance];
        		pair.r = (coll > colr) ? key[indexr + colDistance] : key[indexr - colDistance];
        	}
        	
        	ciphertext += pair + " ";
        }
        
        this.cipher = ciphertext;
        return ciphertext;
	}
	
	/**
	 * Decrypts ciphertext with respect to the key.
	 * @param ciphertext
	 * @return plaintext
	 */
	public String decrypt(String ciphertext)
	{
		System.out.println("\n**DECRYPTION**");
		String plaintext = "";
		ArrayList<Pair<Character>> pairs = new ArrayList<Pair<Character>>();
		
        String cipherSansSpaces = removeSpaces(ciphertext);
        
		for(int i = 0; i < cipherSansSpaces.length() - 1; i += 2)
		{
			pairs.add(new Pair<Character>(cipherSansSpaces.charAt(i), cipherSansSpaces.charAt(i + 1)));
		}
        
        for(Pair<Character> pair : pairs)
        {
        	if(pair.r == 'x')						//plaintext pair was duplicates or end of string
        	{
        		if(pairs.indexOf(pair) == (pairs.size() - 1)) //checks for end of ArrayList => end of string
        		{
        			pair.r = ' ';
        		}
        		else
        		{
        			pair.r = pair.l;
        		}
        	}
        	else if(sameRow(pair.l, pair.r))		//plaintext characters are in same ROW
        	{
        		int indexl = getIndexOf(pair.l);
        		int indexr = getIndexOf(pair.r);
        		pair.l = indexl % 5 == 0 ? key[indexl + 4] : key[indexl - 1];
        		pair.r = indexr % 5 == 0 ? key[indexr + 4] : key[indexr - 1];
        	}
        	else if(sameCol(pair.l, pair.r))		//plaintext characters are in same COLUMN
        	{
        		int indexl = getIndexOf(pair.l);
        		int indexr = getIndexOf(pair.r);
        		pair.l = (indexl >= 0 && indexl <= 4) ? key[indexl + 20] : key[indexl - 5];
        		pair.r = (indexr >= 0 && indexr <= 4) ? key[indexr + 20] : key[indexr - 5];
        	}
        	else									//plaintext characters in DIFFERENT ROW & COLUMN
        	{
        		int indexl = getIndexOf(pair.l);
        		int indexr = getIndexOf(pair.r);
        		int coll = indexl % 5;
        		int colr = indexr % 5;
        		
        		int colDistance = Math.abs(coll - colr);
        		
        		pair.l = (coll > colr) ? key[indexl - colDistance] : key[indexl + colDistance];
        		pair.r = (coll > colr) ? key[indexr + colDistance] : key[indexr - colDistance];
        	}
        	
        	plaintext += pair + " ";
        }
		
		return removeSpaces(plaintext);
	}
	
	/**
	 * Gets ciphertext. If encrypt has not been executed on this cipher, an empty string will be returned.
	 * @return cipher
	 */
	public String getCipher()
	{
		return cipher;
	}
	
//	public String toString()
//	{
//		String cipher = "";
//		
//		for(int i = 0; i < 25; i++)
//		{
//			cipher += key[i] + " ";
//			if(((i + 1) % 5) == 0)
//			{
//				cipher += "\n";
//			}
//		}
//		
//		return cipher;
//	}
}
