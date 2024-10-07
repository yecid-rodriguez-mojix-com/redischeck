package towbook;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class SwoopSimulation extends Simulation {

    // Define target Url
    String baseUrl = System.getProperty("baseUrl", "http://localhost:5000");
    //String xApiToken = System.getProperty("apiToken", "b644a7f8f1894435b468cee4fd1cf060b2d3f2def7bd4803a782392cb52c9865");
    String xApiToken = System.getProperty("apiToken", "4f5e59d96237484ba68cf1cb23cea6e191fa6cd6d40446fd9498492d5b362905");

    static Integer rampTo = Integer.parseInt(System.getProperty("rampTo", "2"));
    static Integer rampTime = Integer.parseInt(System.getProperty("rampTime", "5"));

    static boolean initialized = false;

    private HttpProtocolBuilder httpProtocol = http
            .baseUrl(baseUrl)
            .header("Cache-Control", "no-cache")
            .contentTypeHeader("application/json")
            .acceptHeader("application/json")
            .header("X-Api-Token", xApiToken)
            .header("GraphQL-Subscription-Webhook-Secret", "ufkSSu2vMdlQcNAucX3UaDLRDZv2UBFJGGqPjwTmuhj");

    private static ChainBuilder initSession =
            doIf(session -> !initialized).then(
                    exec(session -> {
                        System.out.println( "rampTime: " + rampTime);
                        initialized = true;
                        return session;
                    })
            );

    private static class MotorClub {

        private static FeederBuilder.Batchable<String> positonsFeeder =
                csv("data/positions.csv").random();

        private static ChainBuilder notify =
                feed(positonsFeeder)
                        .exec(session -> {
                            String guid = UUID.randomUUID().toString().replace("-","").substring(0,12);

                            Random rnd = new Random();
                            Integer pon = 10000000 + rnd.nextInt(90000000);

                            Duration diff = Duration.between(
                                    LocalTime.MIN,
                                    LocalTime.parse("00:05:00")
                            );
                            Instant now = Instant.now();
                            Instant res = now.plus(diff);

                            LocalDateTime ldt = LocalDateTime.ofInstant(res, TimeZone.getTimeZone("UTC").toZoneId());

//                            var formattedDate = year + '-' + month + '-' + day + 'T' + hours + ':' + minutes + ':' + seconds + 'Z';
                            DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            String formattedString = ldt.format(CUSTOM_FORMATTER);
                            System.out.println("res = " + formattedString);

                            return session
                                    .set("responseId", guid)
                                    .set("purchaseOrderNumber", pon.toString())
                                    .set("timestamp", formattedString)
                                    ;
                        })
                        .exec(http("Notify")
                                .post("/api/receivers/swoop/notify")
                                .body(StringBody(
                                        """
                                            {
                                              "result": {
                                                "data": {
                                                  "activeJobsFeed": {
                                                    "id": "#{responseId}",
                                                    "swcid": #{purchaseOrderNumber},
                                                    "status": "Assigned",
                                                    "expiresAt": "#{timestamp}",
                                                    "acceptable": true,
                                                    "eta": {
                                                        "current": "#{timestamp}"
                                                    },
                                                    "notes": {
                                                    },
                                                    "service": {
                                                      "name": "Accident Tow (P)",
                                                      "symptom": "Accident",
                                                      "scheduledFor": null,
                                                      "answers": {
                                                        "edges": [
                                                          {
                                                            "node": {
                                                              "question": "Keys present?",
                                                              "answer": "Yes",
                                                              "extra": null
                                                            }
                                                          },
                                                          {
                                                            "node": {
                                                              "question": "Customer with vehicle?",
                                                              "answer": "Yes",
                                                              "extra": null
                                                            }
                                                          },
                                                          {
                                                            "node": {
                                                              "question": "Vehicle can be put in neutral?",
                                                              "answer": "Unknown",
                                                              "extra": null
                                                            }
                                                          },
                                                          {
                                                            "node": {
                                                              "question": "Vehicle 4 wheel drive?",
                                                              "answer": "No",
                                                              "extra": null
                                                            }
                                                          },
                                                          {
                                                            "node": {
                                                              "question": "Low clearance? (Below 7ft)",
                                                              "answer": "No",
                                                              "extra": null
                                                            }
                                                          }
                                                        ]
                                                      }
                                                    },
                                                    "vehicle": {
                                                      "make": "Volkswagen",
                                                      "model": "Tiguan",
                                                      "year": 2022,
                                                      "color": "Grey",
                                                      "license": "",
                                                      "odometer": null,
                                                      "vin": null
                                                    },
                                                    "partner": {
                                                      "site": null,
                                                      "rateAgreement": {
                                                        "id": "4C6avhPHWsRYk9i3vHdCZd",
                                                        "swcid": 2196,
                                                        "name": "Guardian Towing LLC - ASM 2",
                                                        "status": "Active",
                                                        "vendorId": "128014",
                                                        "facilityNumber": "4",
                                                        "anchorLocation": {
                                                          "address": "1400 S. Lane St., Seattle, WA 98144",
                                                          "city": "Seattle",
                                                          "country": "US",
                                                          "lat": 47.5968124,
                                                          "lng": -122.3138513,
                                                          "state": "WA",
                                                          "street": "1400 S Lane St",
                                                          "streetName": "S Lane St",
                                                          "postalCode": "98144"
                                                        }
                                                      }
                                                    },
                                                    "location": {
                                                      "serviceLocation": {
                                                        "address": "13000 Lake City Way Northeast, Seattle, WA 98125, USA",
                                                        "lat": 47.7240816,
                                                        "lng": -122.2919624,
                                                        "locationType": "Parking Lot"
                                                      },
                                                      "dropoffLocation": {
                                                        "address": "16275 Northeast 85th Street, Redmond, WA 98052, USA",
                                                        "lat": 47.6781386,
                                                        "lng": -122.1231528,
                                                        "locationType": "Residence",
                                                        "googlePlaceId": "ChIJyeNcH7NykFQRLfAI6TKMZow"
                                                      },
                                                      "pickupContact": null,
                                                      "dropoffContact": null
                                                    }
                                                  }
                                                }
                                              },
                                              "more": true
                                            }
                                        """
                                )).asJson()
                                .check(
                                        status().in(200, 201, 204),
                                        bodyString().saveAs("BODY")
                                )
                        )
                        .exec(session -> {
                            System.out.println( "Body response: " + session.getString("BODY"));
                            return session;
                        });

    }

    private ScenarioBuilder scn = scenario("Create Swoop CallRequest")
            .exec(initSession)
            .exec(MotorClub.notify)
            //.exec(Health.checkHealth)
            ;

    {
        setUp(
                scn.injectOpen(
                        atOnceUsers(3)
                )
//                scn.injectClosed(
//                        rampConcurrentUsers(1).to(rampTo).
//                                during(Duration.ofSeconds(rampTime))
//                )

        )
                .protocols(httpProtocol);
    }

}
