package splunk.movie.test;


import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;
import java.awt.Image;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

public class Movies extends TestCase{

    private static SplunkMovie [] movies;
    private static boolean setUpIsCompete;
    /*SetUp for all Test Cases*/
    @Before
    public void setUp(){
        if(!setUpIsCompete) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                String url = "https://splunk.mocklab.io/movies?q=batman";
                HttpGet httpGet = new HttpGet(url);
                httpGet.addHeader("Accept", "application/json");
                ResponseHandler<String> responseHandler=new BasicResponseHandler();
                String responseBody = httpclient.execute(httpGet, responseHandler);

                JSONObject response = new JSONObject(responseBody);

                JSONArray res = response.getJSONArray("results");
                int len = res.length();
                movies = new SplunkMovie[len];

                for(int i = 0; i < len; i++) {
                    JSONObject obj = res.getJSONObject(i);
                    int id = obj.getInt("id");
                    String title = obj.getString("title");
                    String imageURL = obj.optString("poster_path");
                    Image image = null;
                    if(isValid(imageURL)) {
                        URL u = new URL(imageURL);
                        image = ImageIO.read(u);
                    }

                    JSONArray gIds = obj.getJSONArray("genre_ids");
                    int gLen =  gIds.length();
                    int [] genreIDs = new int [gLen];
                    int genreSum = 0;
                    for(int j = 0; j < gLen; ++j) {
                        genreIDs[j] = gIds.getInt(j);
                        genreSum += genreIDs[j];
                    }

                    boolean hasPalindrome = false;

                    String [] words = title.split(" ");
                    for(String word : words) {
                        if(isPalindrome(word)) {
                            hasPalindrome = true;
                            break;
                        }
                    }
                    SplunkMovie movie = new SplunkMovie(id, title, imageURL, image, genreIDs, genreSum, hasPalindrome);
                    movies[i] = movie;
                }

                setUpIsCompete = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public boolean isPalindrome(String s) {
        String x = s.toLowerCase();
        char [] c = x.toCharArray();
        for(int i = 0; i < c.length/2; i++) {
            if(c[i] != c[c.length-1-i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isValid(String url){
        try {
            new URL(url).toURI();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /* SPL 001: TestCase 001: No two movies should have the same image */


    @Test
    public void testForDuplicateImageURLs() {
        Set<String> set = new HashSet<String>();
        for(SplunkMovie mov : movies) {
            String imageURL = mov.getImageURL();
            if(set.contains(imageURL)){
                fail("Two movies have the same Images - " + imageURL);
            }else {
                set.add(imageURL);
            }
        }
    }
    /*SPL 002: TestCase 002: All poster_path links must be valid or null is also acceptable */

    @Test
    public void testForValidPosterPaths() {
        for(SplunkMovie mov : movies) {
            String imageURL = mov.getImageURL();
            if(imageURL != null && !isValid(imageURL)) {
                fail("Invalid Poster Path "+ imageURL);
            }
        }
    }

    /*SPL 003: TestCase 003: Part 1: Movies with genre_ids == null should be first in response.
     * Part 2, if multiple movies have genre_ids == null, then sort by id (ascending).
     * For movies that have non-null genre_ids, results should be sorted by id (ascending)
     */

    @Test
    public void testForSortingMovies() {
        boolean flag = false;
        for(SplunkMovie mov : movies) {
            if(mov.getGenreSum() == 0 && flag) {
                fail("Movies not sorted with null genre ids ");
            }
            if(mov.getGenreSum() != 0) {
                flag = true;
            }
        }
    }

    @Test
    public void testForSortingOfNullGenres() {
        int prev = Integer.MIN_VALUE;
        for(SplunkMovie mov : movies) {
            if(mov.getGenreSum() == 0) {
                int id = mov.getId();
                if(id < prev) {
                    fail("Movies with NULL genre ids are not sorted by id");
                }
                prev = id;
            }
        }
    }

    @Test
    public void testForSortingOfNonNullGenres() {
        int prev = Integer.MIN_VALUE;
        for(SplunkMovie mov : movies) {
            if(mov.getGenreSum() != 0) {
                int id = mov.getId();
                if(id < prev) {
                    fail("Movies with NOT NULL genre ids are not sorted by id");
                }
                prev = id;
            }
        }

    }
    /*SPL 004: TestCase 004: The number of movies whose sum of "genre_ids" > 400 should be no more than 7  */

    @Test
    public void testForGenreSum400LessThan7() {
        int count = 0;
        for(SplunkMovie mov : movies) {
            if(mov.getGenreSum() > 400) {
                count++;
            }
        }
        if(count > 7) {
            fail("There are more than 7 movies with a Genre Sum greater than 400");
        }
    }

    /*SPL 005: TestCase 005: There is at least one movie in the database whose title has a palindrome in it.*/

    @Test
    public void testForAtLeastOneMovieWithPalindrome() {
        int count = 0;
        for(SplunkMovie mov : movies) {
            if(mov.doesHavePalindrome()) {
                count++;
            }
        }
        if(count < 1) {
            fail("There is no movie in the database with a palindrome in it's title");
        }
    }

    /*SPL 006: TestCase 006:There are at least two movies in the database whose title contain the title of another movie.*/

    @Test
    public void testForSubstringCountIsAtLeastTwo() {
        int count = 0;
        for(int i = 0; i < movies.length; ++i) {
            for(int j = i+1; j < movies.length; ++j) {
                String t1 = movies[i].getTitle();
                String t2 = movies[j].getTitle();
                if(t1.contains(t2) || t2.contains(t1)) {
                    count++;
                }
            }
        }
        if(count < 2) {
            fail("There are less than two set of movies in the database whose title contain the title of another movie.");
        }
    }



}
