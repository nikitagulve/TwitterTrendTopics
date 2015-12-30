package Collection;

import java.io.FileOutputStream;
import java.io.PrintStream;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Class to read the Twitter Api public stream
 * @author yogi
 *
 */
public class SimpleStream {

	static long startTime = System.currentTimeMillis();
	static long limit = 60000;

	public static void main(String[] args) throws Exception {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		/*
		 * Yogesh's Twitter Api Keys
		 * cb.setOAuthConsumerKey("KeeH2HM1s67NNvWWRLmk1MFr8");
		 * cb.setOAuthConsumerSecret
		 * ("NUbIA4JHOmCrXShOcA6X9vwDGDJclLuKjECcc0IQjcfXBVswv8");
		 * cb.setOAuthAccessToken
		 * ("45809142-ZbYLRZhbjUUb3F6St4CfP67H0rmexJTQT6NfLfG2X");
		 * cb.setOAuthAccessTokenSecret
		 * ("GUAHc0DXqkA38qLGGEymUbXKj9T8kchvm7igK2Yi8DCeC");
		 */
		/*
		 * Nikita's Twitter Api Keys
		 */
		cb.setOAuthConsumerKey("FpVQheylVXKCGFGlGTFBIb08X");
		cb.setOAuthConsumerSecret("DxqudCgYENhHXXqWKCFEAaQ15xKKagie4Okr57l8QQm1QlO2aQ");
		cb.setOAuthAccessToken("3037417520-NAPwgfJjgxoQghAydmFZmG5iEgqsoKt5hrppOTy");
		cb.setOAuthAccessTokenSecret("RSsnvWKNBZ92XXXa1JvJHkOmLUtcapvMpvO8bEYGWaxcL");
		cb.setJSONStoreEnabled(true);

		TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
				.getInstance();

		StatusListener listener = new StatusListener() {

			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatus(Status status) {
				try {
					System.out.println(TwitterObjectFactory.getRawJSON(status));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub

			}

		};
		FilterQuery fq = new FilterQuery();

		String keywords[] = { "Boston" };

		// Used if keywords needs to be specified
		// fq.track(keywords);

		double[][] locations = { { -71.0, 42.0 }, { -70.0, 43.0 } };
		fq.locations(locations);

		// Filter Tweets based on English Language
		fq.language("en");

		twitterStream.addListener(listener);

		// Set the output file
		System.setOut(new PrintStream(new FileOutputStream("EachHour/1.txt")));

		// Start a thread to write the data to different files at each interval
		FileClass f = new FileClass(startTime, limit);
		f.start();
		twitterStream.filter(fq);

	}
}

/**
 * Class which listens to Public stream and runs a thread to write to new file
 * after the limit time, i.e. at each 15 minute, 1 hour etc
 * 
 * @author yogi
 *
 */
class FileClass implements Runnable {

	long startTime;
	long limit;

	FileClass(long startTime, long limit) {
		this.startTime = startTime;
		this.limit = limit;
	}

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {

		try {
			while (true) {
				long current = System.currentTimeMillis();
				if (Long.compare((current - startTime) % limit, 0) >= 0
						&& Long.compare((current - startTime) % limit, 100) <= 0) {
					System.setOut(new PrintStream(new FileOutputStream(
							"EachHour/" + (int) (current / startTime) + ".txt")));
				}
				Thread.sleep((current - this.startTime) % (limit));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}