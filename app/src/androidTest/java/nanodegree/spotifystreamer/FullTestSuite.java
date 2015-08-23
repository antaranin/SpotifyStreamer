package nanodegree.spotifystreamer;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by Arin on 17/08/15.
 */
public class FullTestSuite extends TestSuite
{
    public FullTestSuite()
    {
        super();
    }

    public static Test suite()
    {
        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }
}

