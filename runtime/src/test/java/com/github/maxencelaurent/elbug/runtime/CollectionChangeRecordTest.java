package com.github.maxencelaurent.elbug.runtime;

import com.github.maxencelaurent.elbug.Item;
import com.github.maxencelaurent.elbug.MyRESTClient;
import com.github.maxencelaurent.elbug.Container;
import java.io.File;
import java.io.IOException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author maxence
 */
@RunWith(Arquillian.class)
public class CollectionChangeRecordTest {

    @Rule
    public TestName name = new TestName();

    private static final Logger logger = LoggerFactory.getLogger(CollectionChangeRecordTest.class);


    private static MyRESTClient client;
    private static MyRESTClient client2;


    @Deployment(name = "elBug1")
    @TargetsContainer("payara1")
    public static WebArchive deployFirst() {
        return createDeployment();
    }

    @Deployment(name = "elBug2")
    @TargetsContainer("payara2")
    public static WebArchive deploySecond() {
        return createDeployment();
    }

    public static WebArchive createDeployment() {
        String warPath;
        warPath = "../app/target/ElBug";

        WebArchive war = ShrinkWrap.create(ExplodedImporter.class)
            .importDirectory(new File(warPath))
            .as(WebArchive.class);

        return war;
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        client = new MyRESTClient("http://localhost:28080/ElBug");
        client2 = new MyRESTClient("http://localhost:28081/ElBug");
        logger.info("SETUP COMPLETED");
    }


    @Test
    public void testCollectionChangeRecordIssue() throws IOException, Exception {
        String containerUrl = "/rest/R/Container";
        String moveUrl =  "/rest/R/MoveItem";

        // create a container on first instance
        Container container1st = client.post(containerUrl, new Container(), Container.class);

        // load it from second instance -> container is in both caches
        Container container2nd = client2.get(containerUrl + "/" + container1st.getId(), Container.class);

        Assert.assertArrayEquals(container1st.getItems().toArray(), container2nd.getItems().toArray());

        // add two items to container
        client.post(containerUrl + "/" + container1st.getId() + "/Item", null);
        Thread.sleep(500l);
        client.post(containerUrl + "/" + container1st.getId() + "/Item", null);

        Thread.sleep(500l);
        // assert both instances have the same list
        container1st = client.get(containerUrl + "/" + container1st.getId(), Container.class);
        container2nd = client2.get(containerUrl + "/" + container1st.getId(), Container.class);

        logger.info("Items from 1st instance: {}", container1st.getItems());
        logger.info("Items from 2nd instance: {}", container2nd.getItems());

        Assert.assertArrayEquals(container1st.getItems().toArray(), container2nd.getItems().toArray());

        // move item
        Item a = container1st.getItems().get(1);
        client.put(moveUrl + "/" + a.getId() + "/0");

        // assert both instances have the same list
        container1st = client.get(containerUrl + "/" + container1st.getId(), Container.class);
        container2nd = client2.get(containerUrl + "/" + container2nd.getId(), Container.class);

        logger.info("Items from 1st instance: {}", container1st.getItems());
        logger.info("Items from 2nd instance: {}", container2nd.getItems());

        Assert.assertArrayEquals(container1st.getItems().toArray(), container2nd.getItems().toArray());
    }
}
