package edu.hkust.jmatch.NLP;

import android.content.Context;

import java.io.*;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.hkust.jmatch.MainActivity;
import edu.hkust.jmatch.R;

public class TextMatching {


    /*
     *  @Param      two strings
     *  @Output     similarity scores
     * */
    public static double computeDistance(String coverLetter, String jd) throws IOException {
        coverLetter = preprocessing(coverLetter);
        jd = preprocessing(jd);
        return cosine_distance_countVectors_method(coverLetter, jd);
    }

    /*
     *  @Param      one string
     *  @Output     the keywords
     * */
    public static List<Map.Entry<String, Double>> extractKeywords(Context context, String jd) throws IOException {
        jd = preprocessing(jd);
        return keywordExtraction(context, jd);
    }

    /*
     *  @Param      one string
     *  @Output     the cleaned string
     * */
    public static String cleanString(String dirty) throws IOException {
        return preprocessing(dirty);
    }

    /*
     *  @Param      one string
     *  @Output     the processed string
     * */
    private static String preprocessing(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("\\w+\\s\\d+\\s\\d+\\s", "");
        text = text.replaceAll("[^a-zA-Z\\s]+", "");
        text = text.replaceAll("\n", " ");
        text = text.replaceAll("\t", " ");
        return text;
    }


    /*
     *  @Param      two strings
     *  @Output     a percentage value representing the similarity of the two strings
     * */
    private static double cosine_distance_countVectors_method(String s1, String s2) {
        // sentences to list
        List<String> allSentences = Arrays.asList(s1, s2);

        // text to vector
        Map<String, Integer> vocabulary = new HashMap<>();
        for (String sentence : allSentences) {
            String[] words = sentence.split(" ");
            for (String word : words) {
                if (!vocabulary.containsKey(word)) {
                    vocabulary.put(word, vocabulary.size());
                }
            }
        }

        int[] text_to_vector_v1 = new int[vocabulary.size()];
        int[] text_to_vector_v2 = new int[vocabulary.size()];

        String[] words_s1 = s1.split(" ");
        for (String word : words_s1) {
            text_to_vector_v1[vocabulary.get(word)] += 1;
        }

        String[] words_s2 = s2.split(" ");
        for (String word : words_s2) {
            text_to_vector_v2[vocabulary.get(word)] += 1;
        }

        // distance of similarity
        double dot_product = 0;
        double magnitude_v1 = 0;
        double magnitude_v2 = 0;
        for (int i = 0; i < text_to_vector_v1.length; i++) {
            dot_product += text_to_vector_v1[i] * text_to_vector_v2[i];
            magnitude_v1 += Math.pow(text_to_vector_v1[i], 2);
            magnitude_v2 += Math.pow(text_to_vector_v2[i], 2);
        }

        double cosine = dot_product / (Math.sqrt(magnitude_v1) * Math.sqrt(magnitude_v2));
        double similarity = (1 - cosine) * 100;
//        System.out.println("Similarity of two sentences are equal to " + String.format("%.2f", (1 - cosine) * 100) + "%");
        return similarity;
    }


    /*
    *  @Param       one string
    *  @Output      a list of key-value pairs where the key is the keyword and value is its score
    * */
    private static List<Map.Entry<String, Double>> keywordExtraction(Context context, String text) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.gist_stopwords);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> stopWords = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            stopWords.add(line);
        }
        reader.close();
        System.out.println(stopWords);

        String[] words = text.split("\\s+");
        List<String> uniqueWords = new ArrayList<>(Arrays.asList(words));
        uniqueWords = new ArrayList<>(new HashSet<>(uniqueWords));

        Map<String, Double> wordScores = new HashMap<>();
        for (String word : uniqueWords) {
            int termFreq = 0;
            for (String w : words) {
                if (w.equalsIgnoreCase(word)) {
                    termFreq++;
                }
            }
            double docFreq = 0;
            for (String w : words) {
                if (w.equalsIgnoreCase(word)) {
                    docFreq++;
                }
            }
            docFreq = 1 + Math.log(words.length / docFreq);
            double score = termFreq * docFreq;
            if (!stopWords.contains(word))
                wordScores.put(word, score);
        }

        List<Map.Entry<String, Double>> sortedWordScores = new ArrayList<>(wordScores.entrySet());
        sortedWordScores.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        // Only keep 10 keywords with highest score
        sortedWordScores = sortedWordScores.subList(0, Math.min(sortedWordScores.size(), 10));

        // Display the result
//        for (Map.Entry<String, Double> entry : sortedWordScores) {
//            System.out.println(entry.getKey() + " : " + entry.getValue());
//        }

        return sortedWordScores;
    }


}
