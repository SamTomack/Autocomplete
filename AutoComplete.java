package Proj2;
/**
 * An implementation of the AutoCompleteInterface using a DLB Trie.
 */

public class AutoComplete implements AutoCompleteInterface {

	private static final char SENTINEL = '^';
	private DLBNode root;
	private StringBuilder currentPrefix;
	private DLBNode currentNode;
	private DLBNode pointerNode;
	private boolean madeRoot = false;
	private boolean locked = false;
	private int lockedCounter = 0;
	private boolean successfulAdd = false;
	
	public AutoComplete(){
		root = null;
		currentPrefix = new StringBuilder();
		currentNode = null;
	}

	public String getCurrentPrefix() {
		return currentPrefix.toString();
	}

	/**
	 * Adds a word to the dictionary in O(word.length()) time
	 * @param word the String to be added to the dictionary
	 * @return true if add is successful, false if word already exists
	 * @throws IllegalArgumentException if word is the empty string
	 */
	public boolean add(String word) {
		locked = false;
		lockedCounter = 0;
		if (word == null || word == "") throw new IllegalArgumentException("calls add() on null or empty string");
		successfulAdd = false;
		word = word + SENTINEL;
		add(root, word, 0);
		if(successfulAdd) 
			return true;
		else {
			currentNode = pointerNode; //Nope
			return false;
		}
	}
	
	/**
	 * Private recursive helper method for add
	 * @param x The node to be recursed on
	 * @param word the String being added
	 * @param pos The position in the word 
	 * @return A DLBNode at the root of the word
	 */
	private DLBNode add(DLBNode x, String word, int pos) {
		DLBNode result = x;
		if (x == null){
			result = new DLBNode(word.charAt(pos));
			successfulAdd = true;
			if(result.data != SENTINEL)
				result.size++;
			if(madeRoot == false) {
				root = result;
				madeRoot = true;
			}
			if(pos < word.length()-1){
				result.child = add(result.child, word, pos+1);
				result.child.parent = result;
			}
		} else if(x.data == word.charAt(pos)) {
			if(pos < word.length()-1){
				result.child = add(x.child, word, pos+1);
				result.child.parent = result;
				result.size++;
			}
		} else {
			result.nextSibling = add(x.nextSibling, word, pos);
			result.nextSibling.previousSibling = result;
			result.nextSibling.parent = result.parent;
		}
		currentNode = result;
		return result;
	}

	/**
	 * appends the character c to the current prefix in O(1) time. This method 
	 * doesn't modify the dictionary.
	 * @param c: the character to append
	 * @return true if the current prefix after appending c is a prefix to a word 
	 * in the dictionary and false otherwise
	 */
	public boolean advance(char c){
		currentPrefix.append(c);
		if(currentNode == null)
			currentNode = root;
		while(currentNode.previousSibling != null) {
			currentNode = currentNode.previousSibling;
		}
		while(currentNode.data != c) {
			if(currentNode.nextSibling == null) {
				locked = true;
				lockedCounter++;
				return false;
			}
			else 
				currentNode = currentNode.nextSibling;
		}
		if(currentNode.data == c) {
			if(currentNode.size > 0) {
				if(!locked) {
					currentNode = currentNode.child;
					pointerNode = currentNode;
					return true;
				}
			}
			else {
				locked = true;
				lockedCounter++;
				return false;
			}
		}
		else {
			locked = true; 
			lockedCounter++;
			return false;
		}
		return false;
	}
	
	/**
	 * removes the last character from the current prefix in O(1) time. This 
	 * method doesn't modify the dictionary.
	 * @throws IllegalStateException if the current prefix is the empty string
	 */
	public void retreat(){
		if(locked) {
			currentPrefix.setLength(currentPrefix.length()-1);
			lockedCounter--;
			if(lockedCounter == 0) {
				locked = false;
			}
		}
		else if(currentPrefix.length() > 0) {
			currentPrefix.setLength(currentPrefix.length()-1);
			currentNode = currentNode.parent;
		}
		else if (currentPrefix.length() == 0) {
			throw new IllegalStateException();
		}
	}

	/**
	 * resets the current prefix to the empty string in O(1) time
	 */
	public void reset(){
		currentPrefix.setLength(0);
		currentNode = root;
	}

	/**
	 * @return true if the current prefix is a word in the dictionary and false
	 * otherwise
	 */
	public boolean isWord(){
		if(locked)
			return false;
		while(currentNode.previousSibling != null) {
			currentNode = currentNode.previousSibling;
		}
		while(true) {
			if(currentNode.data == SENTINEL)
				return true;
			else if(currentNode.nextSibling != null)
				currentNode = currentNode.nextSibling;
			else
				return false;
		}
	}

	/**
	 * adds the current prefix as a word to the dictionary (if not already a word)
	 * The running time is O(length of the current prefix). 
	 */
	public void add(){
		add(currentPrefix.toString());
	}

	/** 
	 * @return the number of words in the dictionary that start with the current 
	 * prefix (including the current prefix if it is a word). The running time is 
	 * O(1).
	 */
	public int getNumberOfPredictions(){
		if(locked) {
			return 0;
		}
		else if(currentNode.parent != null) {
			return currentNode.parent.size;
		}
		else 
			return 0;
	}

	/**
	 * retrieves one word prediction for the current prefix. The running time is 
	 * O(prediction.length()-current prefix.length())
	 * @return a String or null if no predictions exist for the current prefix
	 */
	public String retrievePrediction(){
		if(locked)
			return null;
		String str = currentPrefix.toString();
		pointerNode = currentNode;
		if(pointerNode.data == SENTINEL) 
			return str;	
		while(true) {
			if(currentNode.nextSibling == null) {
				if(currentNode.size > 0) {
					str = str + currentNode.data;
					currentNode = currentNode.child;
				}
				else if(currentNode.data == SENTINEL) {
					currentNode = pointerNode;
					return str;
				}
				else
					return null;
			}
			else 
				currentNode = currentNode.nextSibling;
		}
	}

	/* ==============================
	 * Helper methods for debugging.
	 * ==============================
	 */

	//print the subtrie rooted at the node at the end of the start String
	public void printTrie(String start){
		System.out.println("==================== START: DLB Trie Starting from \""+ start + "\" ====================");
		if(start.equals("")){
			printTrie(root, 0);
		} else {
			DLBNode startNode = getNode(root, start, 0);
			if(startNode != null){
				printTrie(startNode.child, 0);
			}
		}
		System.out.println("==================== END: DLB Trie Starting from \""+ start + "\" ====================");
	}

	//a helper method for printTrie
	private void printTrie(DLBNode node, int depth){
		if(node != null){
			for(int i=0; i<depth; i++){
				System.out.print(" ");
			}
			System.out.print(node.data);
			if(node.isWord){
				System.out.print(" *");
			}
			System.out.println(" (" + node.size + ")");
			printTrie(node.child, depth+1);
			printTrie(node.nextSibling, depth);
		}
	}

	//return a pointer to the node at the end of the start String.
	private DLBNode getNode(DLBNode node, String start, int index){
		if(start.length() == 0){
			return node;
		}
		DLBNode result = node;
		if(node != null){
			if((index < start.length()-1) && (node.data == start.charAt(index))) {
				result = getNode(node.child, start, index+1);
			} else if((index == start.length()-1) && (node.data == start.charAt(index))) {
				result = node;
			} else {
				result = getNode(node.nextSibling, start, index);
			}
		}
		return result;
	} 

	//The DLB node class
	private class DLBNode{
		private char data;
		private int size;
		private boolean isWord;
		private DLBNode nextSibling;
		private DLBNode previousSibling;
		private DLBNode child;
		private DLBNode parent;

		private DLBNode(char data){
			this.data = data;
			size = 0;
			isWord = false;
			nextSibling = previousSibling = child = parent = null;
		}
	}
}