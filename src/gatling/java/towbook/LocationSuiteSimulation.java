package towbook;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class LocationSuiteSimulation extends Simulation {

    // Define target Url
    String baseUrl = System.getProperty("baseUrl", "http://localhost");
    String xApiToken = "436a428234cb49e99a026e46516f548a64b9e82e9718417f87a26d99f2aa3405"; //local
    static String username = "johndoe";
    static String password = "10dev2";

    private HttpProtocolBuilder httpProtocol = http
            .baseUrl(baseUrl)
            .header("Cache-Control", "no-cache")
            .contentTypeHeader("application/json")
            .acceptHeader("application/json")
            .header("X-Api-Token", xApiToken);


    private static class Authentication {
        private static ChainBuilder authenticate =
                doIf(session -> !session.getBoolean("authenticated")).then(
                        exec(http("Authentication")
                                .post("/api/authentication")
                                .body(StringBody("{\"username\": \"" + username + "\",\"password\": \"" + password + "\"}"))
                                .check(status().is(200))
                                .check(bodyString().saveAs("xApiToken"))
                        )
                        .exec(session -> session.set("authenticated", true)));
    }

    private static class Locations {

        private static FeederBuilder.Batchable<String> positonsFeeder =
                csv("data/positions.csv").random();

        private static ChainBuilder changePosition =
                feed(positonsFeeder)
//                        .exec(Authentication.authenticate)
                        .exec(http("Change Location")
                                .post("/api/location")
                                .body(StringBody(
                                        """
                                                {
                                                  "latitude":#{latitude},
                                                  "longitude":#{longitude},
                                                  "street":"190 Kime Hatchery Road",
                                                  "city":"Gardners",
                                                  "state":"PA",
                                                  "zipCode":"12323",
                                                  "country":"USA"
                                                }
                                        """
                                )).asJson()
                                .check(
                                        status().in(201, 202),
                                        bodyString().saveAs("BODY")
                                )
                        )
                        .exec(session -> {
                            System.out.println( "Body response: " + session.getString("BODY"));
                            return session;
                        });

    }

    private static class Health {

        private static final int min = 1;
        private static final int max = 9;

        private static ChainBuilder checkHealth =
                doIf(session -> {
                    int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
                    System.out.println( "Random: " + randomNum);
                    return randomNum == 1;
                }).
                    then(
                        exec(http("Check Health")
                                .get("/api/health")
                                .check(status().is(200))
                                .check(
                                    bodyString().saveAs("HEALTH")
                                        //,
                                    //jsonPath("$.serviceHealth").saveAs("redisStats")
                                )
                        )
                        .exec(session -> {
                            System.out.println( "Redis stats: " + session.getString("redisStats"));
                            return session;
                        })
                    );
    }

    private ScenarioBuilder scn = scenario("Change location Simulation")
            //.exec(Authentication.authenticate)
            .exec(Locations.changePosition)
            //.exec(Health.checkHealth)
            ;

    {
        setUp(
                scn.injectClosed(
                        rampConcurrentUsers(1).to(70).
                                during(Duration.ofSeconds(180)),
                        constantConcurrentUsers(75).
                                during(Duration.ofSeconds(120))))
                .protocols(httpProtocol);
    }

}
