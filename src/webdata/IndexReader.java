package webdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class IndexReader {

	TokensDictionary tokensDictionary;
	ProductsDictionary productsDictionary;
	ReviewsMetaData metaData;
	Consts.DATA_TYPES activeType;
	String dataDir;

	private void setActiveDictionary(Consts.DATA_TYPES dataType){
		switch(dataType){
			case tokens:
				if(activeType != Consts.DATA_TYPES.tokens){
					this.tokensDictionary = new TokensDictionary(this.dataDir);
					productsDictionary = null;
					metaData = null;
					activeType = Consts.DATA_TYPES.tokens;
				}
				break;
			case products:
				if(activeType != Consts.DATA_TYPES.products){
					this.productsDictionary = new ProductsDictionary(this.dataDir);
					tokensDictionary = null;
					metaData = null;
					activeType = Consts.DATA_TYPES.products;
				}
				break;
			case metaData:
				if(activeType != Consts.DATA_TYPES.metaData){
					this.metaData = new ReviewsMetaData(this.dataDir);
					tokensDictionary = null;
					productsDictionary = null;
					activeType = Consts.DATA_TYPES.metaData;
				}
				break;
		}
	}
	/**
	* Creates an IndexReader which will read from the given directory
	*/
	public IndexReader(String dir) {
		this.tokensDictionary = new TokensDictionary(dir);
		this.activeType = Consts.DATA_TYPES.tokens;
		this.dataDir = dir;
	}
	
	/**
	* Returns the product identifier for the given review
	* Returns null if there is no review with the given identifier
	*/
	public String getProductId(int reviewId) {
		this.setActiveDictionary(Consts.DATA_TYPES.metaData);
		if((reviewId >= 1) && reviewId <= metaData.getNumOfReviews()){
			return metaData.getProductIDByReviewID(reviewId);
		}
		return null;
	}

	/**
	* Returns the score for a given review
	* Returns -1 if there is no review with the given identifier
	*/
	public int getReviewScore(int reviewId) {
		this.setActiveDictionary(Consts.DATA_TYPES.metaData);
		if((reviewId >= 1) && reviewId <= metaData.getNumOfReviews()){
			return metaData.getScore(reviewId);
		}
		return -1;
	}

	/**
	* Returns the numerator for the helpfulness of a given review
	* Returns -1 if there is no review with the given identifier
	*/
	public int getReviewHelpfulnessNumerator(int reviewId) {
		this.setActiveDictionary(Consts.DATA_TYPES.metaData);
		if((reviewId >= 1) && reviewId <= metaData.getNumOfReviews()){
			return metaData.getFirstHelpfulness(reviewId);
		}
		return -1;
	}

	/**
	* Returns the denominator for the helpfulness of a given review
	* Returns -1 if there is no review with the given identifier
	*/
	public int getReviewHelpfulnessDenominator(int reviewId) {
		this.setActiveDictionary(Consts.DATA_TYPES.metaData);
		if((reviewId >= 1) && reviewId <= metaData.getNumOfReviews()){
			return metaData.getSecondHelpfulness(reviewId);
		}
		return -1;
	}

	/**
	* Returns the number of tokens in a given review
	* Returns -1 if there is no review with the given identifier
	*/
	public int getReviewLength(int reviewId) {
		this.setActiveDictionary(Consts.DATA_TYPES.metaData);
		if((reviewId >= 1) && reviewId <= metaData.getNumOfReviews()){
			return metaData.getNumOfTokens(reviewId);
		}
		return -1;
	}

	/**
	* Return the number of reviews containing a given token (i.e., word)
	* Returns 0 if there are no reviews containing this token
	*/
	public int getTokenFrequency(String token) {
		this.setActiveDictionary(Consts.DATA_TYPES.tokens);
		token = token.toLowerCase();
		int tokenIndex = this.tokensDictionary.search(token);
		if(tokenIndex == -1){
			return 0;
		}
		long postinglistPtr = this.tokensDictionary.getPostingListPointer(tokenIndex);
		long nextPostinglistPtr = this.tokensDictionary.getPostingListPointer(tokenIndex + 1);
		return this.tokensDictionary.getPostinglistLength(postinglistPtr, nextPostinglistPtr);
	}

	/**
	* Return the number of times that a given token (i.e., word) appears in
	* the reviews indexed
	* Returns 0 if there are no reviews containing this token
	*/
	public int getTokenCollectionFrequency(String token) {
		this.setActiveDictionary(Consts.DATA_TYPES.tokens);
		int tokenPosition = this.tokensDictionary.search(token.toLowerCase());
		if (tokenPosition >= 0){
			return this.tokensDictionary.getFrequency(tokenPosition);
		}
		return 0;
	}

	/**
	* Return a series of integers of the form id-1, freq-1, id-2, freq-2, ... such
	* that id-n is the n-th review containing the given token and freq-n is the
	* number of times that the token appears in review id-n
	* Only return ids of reviews that include the token
	* Note that the integers should be sorted by id
	*
	* Returns an empty Enumeration if there are no reviews containing this token
	* */
	public Enumeration<Integer> getReviewsWithToken(String token) {
		this.setActiveDictionary(Consts.DATA_TYPES.tokens);
		ArrayList<Integer> postingList = new ArrayList<>();
		ArrayList<Integer> freqList = new ArrayList<>();
		token = token.toLowerCase();
		int tokenIndex = this.tokensDictionary.search(token);
		if(tokenIndex >= 0){
			long postinglistPtr = this.tokensDictionary.getPostingListPointer(tokenIndex);
			long nextPostinglistPtr = this.tokensDictionary.getPostingListPointer(tokenIndex + 1);
			long freqListPtr = this.tokensDictionary.getFreqListPointer(tokenIndex);
			long nextFreqListPtr = this.tokensDictionary.getFreqListPointer(tokenIndex + 1);
			postingList = this.tokensDictionary.getDecodedPostinglist(postinglistPtr, nextPostinglistPtr, true);
			freqList = this.tokensDictionary.getDecodedFreqList(freqListPtr, nextFreqListPtr);
		}
		return Collections.enumeration(Utils.alternateLists(postingList, freqList));
	}

	/**
	* Return the number of product reviews available in the system
	*/
	public int getNumberOfReviews() {
		this.setActiveDictionary(Consts.DATA_TYPES.metaData);
		return this.metaData.getNumOfReviews();
	}

	/**
	* Return the number of number of tokens in the system
	* (Tokens should be counted as many times as they appear)
	*/
	public int getTokenSizeOfReviews() {
		this.setActiveDictionary(Consts.DATA_TYPES.metaData);
		return this.metaData.totalNumOfTokens();
	}

	/**
	* Return the ids of the reviews for a given product identifier
	* Note that the integers returned should be sorted by id
	*
	* Returns an empty Enumeration if there are no reviews for this product
	*/
	public Enumeration<Integer> getProductReviews(String productId) {
		this.setActiveDictionary(Consts.DATA_TYPES.products);
		ArrayList<Integer> result = new ArrayList<>();
		int productIDIndex = this.productsDictionary.search(productId);
		if(productIDIndex >= 0){
			long postinglistPtr = this.productsDictionary.getPostingListPointer(productIDIndex);
			long nextPostinglistPtr = this.productsDictionary.getPostingListPointer(productIDIndex + 1);
			result = this.productsDictionary.getDecodedPostinglist(postinglistPtr, nextPostinglistPtr, true);
		}
		return Collections.enumeration(result);
	}

	public String getToken(int index){
		return this.tokensDictionary.getTokenFromIndex(index);
	}
}