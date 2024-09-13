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
    String baseUrl = System.getProperty("baseUrl", "http://localhost:5000");
    String xApiToken = System.getProperty("apiToken", "436a428234cb49e99a026e46516f548a64b9e82e9718417f87a26d99f2aa3405");

    static String username = "johndoe";
    static String password = "10dev2";

    static Integer rampTo = Integer.parseInt(System.getProperty("rampTo", "10"));
    static Integer rampTime = Integer.parseInt(System.getProperty("rampTime", "1"));
    static Integer concurrentUsers = Integer.parseInt(System.getProperty("concurrentUsers", "20"));
    static Integer concurrentTime = Integer.parseInt(System.getProperty("concurrentTime", "3"));

    static boolean initialized = false;

    private HttpProtocolBuilder httpProtocol = http
            .baseUrl(baseUrl)
            .header("Cache-Control", "no-cache")
            .contentTypeHeader("application/json")
            .acceptHeader("application/json")
            .header("X-Api-Token", xApiToken);

    private static ChainBuilder initSession =
            doIf(session -> !initialized).then(
                    exec(session -> {
                        System.out.println( "rampTo: " + rampTo);
                        System.out.println( "rampTime: " + rampTime.toString());
                        System.out.println( "concurrentUsers: " + concurrentUsers.toString());
                        System.out.println( "concurrentTime: " + concurrentTime);
                        initialized = true;
                        return session;
                    })
            );

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
            .exec(initSession)
            //.exec(Authentication.authenticate)
            .exec(Locations.changePosition)
            //.exec(Health.checkHealth)
            ;

    {
        setUp(
                scn.injectClosed(
                        rampConcurrentUsers(1).to(rampTo).
                                during(Duration.ofMinutes(rampTime)),
                        constantConcurrentUsers(concurrentUsers).
                                during(Duration.ofMinutes(concurrentTime))))
                .protocols(httpProtocol);
    }

}
