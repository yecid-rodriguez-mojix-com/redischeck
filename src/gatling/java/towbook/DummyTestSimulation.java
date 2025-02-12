package towbook;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class DummyTestSimulation extends Simulation {

    // Define target Url
//    String baseUrl = System.getProperty("baseUrl", "http://localhost:5000");
//    String xApiToken = System.getProperty("apiToken", "2dc42bc5bc804cb8a9f52ae4595d16c74d1d700a72854599955561acaa11fe09");
    String baseUrl = System.getProperty("baseUrl", "http://20.94.116.226");
    String xApiToken = System.getProperty("apiToken", "4abe74198f464a238f000a5b39713f6711b19aa80ea24996ae67e881f4bfecec");


    static Integer rampTo = Integer.parseInt(System.getProperty("rampTo", "25"));
    static Integer rampTime = Integer.parseInt(System.getProperty("rampTime", "1"));
    static Integer concurrentUsers = Integer.parseInt(System.getProperty("concurrentUsers", "25"));
    static Integer concurrentTime = Integer.parseInt(System.getProperty("concurrentTime", "4"));

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
    private static class DummyTest {

        private static FeederBuilder.Batchable<String> positonsFeeder =
                csv("data/positions.csv").random();

        private static ChainBuilder usersByCompany1 =
                feed(positonsFeeder)
//                        .exec(Authentication.authenticate)
                        .exec(http("Dummy test 1")
                                .get("/api/tests/1")
                                .check(
                                        status().is(200),
                                        bodyString().saveAs("BODY")
                                )
                        )
                        .exec(session -> {
                            System.out.println( "Body response: " + session.getString("BODY"));
                            return session;
                        });
        private static ChainBuilder usersByCompany2 =
                feed(positonsFeeder)
//                        .exec(Authentication.authenticate)
                        .exec(http("Dummy test 2")
                                .get("/api/tests/2")
                                .check(
                                        status().is(200),
                                        bodyString().saveAs("BODY")
                                )
                        )
                        .exec(session -> {
                            System.out.println( "Body response: " + session.getString("BODY"));
                            return session;
                        });
        private static ChainBuilder usersByCompany =
                feed(positonsFeeder)
//                        .exec(Authentication.authenticate)
                        .exec(http("Dummy test 24776")
                                .get("/api/tests/24776")
                                .check(
                                        status().is(200),
                                        bodyString().saveAs("BODY")
                                )
                        )
                        .exec(session -> {
                            System.out.println( "Body response: " + session.getString("BODY"));
                            return session;
                        });

    }

    private ScenarioBuilder scn = scenario("Dummy Test usersByCompany")
            .exec(initSession)
            .exec(DummyTest.usersByCompany1)
            .exec(DummyTest.usersByCompany2)
            .exec(Locations.changePosition)
            .exec(DummyTest.usersByCompany)
            ;

    static Integer minutes = 2;

    static Integer times = minutes * 6;
    {
        setUp(
                scn.injectClosed(
                        rampConcurrentUsers(1).to(rampTo).
                                during(Duration.ofMinutes(rampTime))
                        ,
                        constantConcurrentUsers(concurrentUsers).
                                during(Duration.ofMinutes(concurrentTime))
                )

        )
                .protocols(httpProtocol);
    }

}
