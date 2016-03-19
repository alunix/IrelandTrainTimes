package ie.markomeara.irelandtraintimes.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ie.markomeara.irelandtraintimes.utils.SecretKeys;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Mark on 21/10/2014.
 */
public class TwitterTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = TwitterTask.class.getSimpleName();

    private Context currentContext;
    private static final long IRISH_RAIL_TWITTER_ID = 15115986;

    public TwitterTask(Context c){
        this.currentContext = c;
    }

    @Override
    protected Void doInBackground(Void... params) {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setApplicationOnlyAuthEnabled(true);
        cb.setOAuthConsumerKey(SecretKeys.TWITTER4J_API_KEY);
        cb.setOAuthConsumerSecret(SecretKeys.TWITTER4J_SECRET_KEY);
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        try {

            twitter.getOAuth2Token();
            List<twitter4j.Status> statuses;

            // Get last 200 statuses from Irish Rail
            // 200 is the count before all replies and retweets are removed
            Paging page = new Paging(1, 200);

            boolean includeReplies = false;
            boolean includeRTs = false;
            statuses = twitter.getUserTimeline(IRISH_RAIL_TWITTER_ID, page, includeReplies, includeRTs);

            Map<String, RateLimitStatus> limits = twitter.getRateLimitStatus();

            Set<String> keys = limits.keySet();
            Iterator<String> iter = keys.iterator();

            TweetsDataSource tds = new TweetsDataSource(currentContext);

            tds.createRelevantTweets(statuses);

        }
        catch(TwitterException ex){
            Log.w(TAG, ex);
        }


        // TODO Get tweets from Search if we've reached timeline limit

        // TODO Scrape Twitter feed if we've reached the request limit for both timeline and search

   /*     try {

            Connection.Response res = Jsoup.connect("https://mobile.twitter.com/Mark_O_Meara").execute();

            String resString = res.body();

            Document doc = Jsoup.connect("https://mobile.twitter.com/Mark_O_Meara").userAgent("\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"").get();
            String docString = doc.toString();
            Elements elems = doc.select("#launchdata");
            if(elems.size() > 0) {
                String s = elems.toString();
                System.out.println(s);
            }
            else{
                String s = doc.toString();
                System.out.println(s);

          //      Log.w(TAG, "----null----");

            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }*/

        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        Log.i(TAG, "Tweets have been updated");
        Toast t = Toast.makeText(currentContext, "Tweets updated", Toast.LENGTH_SHORT);
        t.show();

    }

}