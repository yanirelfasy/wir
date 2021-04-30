package webdata;
import java.io.*;


public class SlowIndexWriter {

	/**
	* Given product review data, creates an on disk index
	* inputFile is the path to the file containing the review data
	* dir is the directory in which all index files will be created
	* if the directory does not exist, it should be created
	*/
	public void slowWrite(String inputFile, String dir){
		this.makeOutputDir(dir);
		Parser reviewsParser = new Parser();
		reviewsParser.parse(inputFile);
		TokensDictionary tokensDictionary = new TokensDictionary(reviewsParser.getParsedReviews(), dir);
		ProductsDictionary productsDictionary = new ProductsDictionary(reviewsParser.getParsedReviews(), dir);
		ReviewsMetaData reviewsMetaData = new ReviewsMetaData(reviewsParser.getParsedReviews());

		try{
			tokensDictionary.writeDictionaryToDisk();
			productsDictionary.writeDictionaryToDisk();
			reviewsMetaData.writeDataToDisk(dir + File.separator + "metaData");
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}

	}

	/**
	* Delete all index files by removing the given directory
	*/
	public void removeIndex(String dir) {
		File file;
		String[] subDirs = {Consts.SUB_DIRS.tokensDictionary.name(), Consts.SUB_DIRS.productsDictionary.name(), Consts.SUB_DIRS.metaData.name()};
		for(String subDir : subDirs){
			for(Consts.FILE_NAMES fileName : Consts.FILE_NAMES.values()){
				file = new File(dir + File.separator + subDir + File.separator + fileName.name());
				if(file.exists()){
					file.delete();
				}
			}
			file = new File(dir + File.separator + subDir);
			if(file.exists()){
				file.delete();
			}
		}
		file = new File(dir);
		if(file.exists()){
			file.delete();
		}
	}

	public void makeOutputDir(String dir){
		File outputDir = new File(dir);
		if(outputDir.exists()){
			this.removeIndex(dir);
		}
		outputDir.mkdir();
		String[] subDirs = {Consts.SUB_DIRS.tokensDictionary.name(), Consts.SUB_DIRS.productsDictionary.name(), Consts.SUB_DIRS.metaData.name()};
		for (String subDir : subDirs){
			outputDir = new File(dir + File.separator + subDir);
			outputDir.mkdir();
		}
	}
}