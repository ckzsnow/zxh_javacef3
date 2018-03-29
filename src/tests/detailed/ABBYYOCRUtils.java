package tests.detailed;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.abbyy.FREngine.BaseLanguageLetterSetEnum;
import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.IBaseLanguage;
import com.abbyy.FREngine.IBaseLanguages;
import com.abbyy.FREngine.IDocumentProcessingParams;
import com.abbyy.FREngine.IEngine;
import com.abbyy.FREngine.IFRDocument;
import com.abbyy.FREngine.ILanguageDatabase;
import com.abbyy.FREngine.ITextLanguage;

public class ABBYYOCRUtils {
	
	public static final String FRENGINE_DEV_LICENSE = "/mobile/ocr/SWAO-1121-0004-0204-0161-1782.ABBYY.LocalLicense";
	
	public static final String FRENGINE_DEV_KEY = "SWAD-1101-0004-0181-0031-6782";
	
	public static final String FRENGINE_DEV_SECRET = "aby#283196Win#TestPlant";
	
	public static final String FRENGINE_PATH = "/mobile/ocr/Bin64";
	
	private static IEngine engine = null;
	
	private static IFRDocument document = null;
	
	private static IDocumentProcessingParams documentProcessingParams = null;
	
	static {
		try {
			engine = Engine.GetEngineObjectEx(
					FRENGINE_PATH,
					FRENGINE_DEV_KEY,
					"", 
					"", 
					true, 
					FRENGINE_DEV_LICENSE, 
					FRENGINE_DEV_SECRET);
			engine.LoadPredefinedProfile("TextExtraction_Accuracy");
	        document = engine.CreateFRDocument();
	        documentProcessingParams = engine.CreateDocumentProcessingParams();
			//documentProcessingParams.getPageProcessingParams().getRecognizerParams().SetPredefinedTextLanguage("English");
			documentProcessingParams.getPageProcessingParams().getRecognizerParams().setTextLanguage(getTextLanguageForNUMANDZM());
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	private static ITextLanguage getTextLanguageForNUMANDZM() {
		ILanguageDatabase languageDatabase = engine.CreateLanguageDatabase();
		ITextLanguage textLanguage = languageDatabase.CreateTextLanguage();
		IBaseLanguages baseLanguages = textLanguage.getBaseLanguages();
		IBaseLanguage baseLanguage = baseLanguages.AddNew();
		baseLanguage.setLetterSet(BaseLanguageLetterSetEnum.BLLS_Alphabet, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		baseLanguage.setLetterSet(BaseLanguageLetterSetEnum.BLLS_IgnorableLetters, "");
		baseLanguage.setLetterSet(BaseLanguageLetterSetEnum.BLLS_Prefixes, "");
		baseLanguage.setLetterSet(BaseLanguageLetterSetEnum.BLLS_SubscriptAlphabet, "");
		baseLanguage.setLetterSet(BaseLanguageLetterSetEnum.BLLS_Suffixes, "");
		baseLanguage.setLetterSet(BaseLanguageLetterSetEnum.BLLS_SuperscriptAlphabet, "");
		textLanguage.setInternalName("num_zm");
		return textLanguage;
	}
	
	private static String filterImage(String inputFile){
		String imgPath = "out_img_code.jpg";
		File file = new File(inputFile);
	    BufferedImage img;
	    int white = new Color(255, 255, 255).getRGB();
	    int black = new Color(0, 0, 0).getRGB();
	    int[] rgb = new int[3];
		try {
			img = ImageIO.read(file);
			int width = img.getWidth();
		    int height = img.getHeight();
		    for (int i = 0; i < width; i++) {
		        for (int j = 0; j < height; j++) {
		            int pixel = img.getRGB(i, j);
		            rgb[0] = (pixel & 0xff0000) >> 16;
	                rgb[1] = (pixel & 0xff00) >> 8;
	                rgb[2] = (pixel & 0xff);
	                int gray = (rgb[0]*150+rgb[1]*59+rgb[2]*11+150)/150;
		            if(gray > 100)
		            	img.setRGB(i, j, white);
		            else
		            	img.setRGB(i, j, black);
		        }
		    }
		    ImageIO.write(img,"jpeg", new File(imgPath));
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return imgPath;
	}
	
	public static String ocrImage(String path){
		String newPath = filterImage(path);
		document.AddImageFile(newPath, null, null);
		document.Process(documentProcessingParams);
		String ret = document.getPlainText().getText();
		System.out.println(ret);
		return ret;
	}
	
	public static void main(String[] args){
		ABBYYOCRUtils.ocrImage("C:\\Users\\Administrator\\Desktop\\tt.png");
	}
}
